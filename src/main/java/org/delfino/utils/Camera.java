package org.delfino.utils;

import org.delfino.Context;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.delfino.Context.window;

public class Camera {

    final float YAW         = -90.0f;
    final float PITCH       =   0.0f;
    final float SPEED       =  10.0f;
    final float SENSITIVITY =   6.5f;
    final float ZOOM        =  45.0f;

    static class Frustrum {
        float fov    = (float) Math.toRadians(45.f);
        float aspect = (float) Context.screen_width / Context.screen_height;
        float near   = 0.1f;
        float far    = 1000.f;
    }

    public Vector3f position;
    public Vector3f front;
    public Vector3f up;
    public Vector3f right;
    public Vector3f world_up;
    public Vector3f trajectory;
    public Vector3f input_vector;

    public float yaw;
    public float pitch;
    public float movement_speed;
    public float mouse_sensitivity;
    public float zoom;

    public Frustrum frustrum;

    public Camera(Vector3f position) {
        this.position          = position;
        this.front             = new Vector3f(0.f, 0.f, -1.f);
        this.right             = new Vector3f();
        this.up                = new Vector3f();
        this.world_up          = new Vector3f(0.f, 1.f, 0.f);
        this.trajectory        = new Vector3f();
        this.input_vector      = new Vector3f();
        this.yaw               = YAW;
        this.pitch             = PITCH;
        this.movement_speed    = SPEED;
        this.mouse_sensitivity = SENSITIVITY;
        this.zoom              = ZOOM;
        this.frustrum          = new Frustrum();

        update_camera_vectors();
    }

    public Camera(Camera other) {
        this.position          = other.position;
        this.front             = other.front;
        this.up                = other.up;
        this.right             = other.right;
        this.world_up          = new Vector3f(0.f, 1.f, 0.f);
        this.trajectory        = other.trajectory;
        this.input_vector      = other.input_vector;
        this.yaw               = other.yaw;
        this.pitch             = other.pitch;
        this.movement_speed    = other.movement_speed;
        this.mouse_sensitivity = other.mouse_sensitivity;
        this.zoom              = other.zoom;
        this.frustrum          = other.frustrum;
    }

    public void update(double delta_time) {
        update_camera_vectors();
        this.position.add(new Vector3f(this.trajectory).mul(this.movement_speed).mul((float)delta_time));
    }

    public Matrix4f get_view_matrix() {
        return new Matrix4f().setLookAt(this.position, new Vector3f(this.position).add(this.front), this.up);
    }

    public Matrix4f get_perspective_matrix() {
        return new Matrix4f().setPerspective(frustrum.fov, frustrum.aspect, frustrum.near, frustrum.far);
    }

    public void process_keyboard() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            this.input_vector.z = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            this.input_vector.x = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            this.input_vector.z = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            this.input_vector.x = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS) {
            this.input_vector.y = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS) {
            this.input_vector.y = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            this.movement_speed = SPEED * 4;
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_RELEASE) {
            this.input_vector.z = 0;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_RELEASE) {
            this.input_vector.x = 0;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_RELEASE) {
            this.input_vector.z = 0;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_RELEASE) {
            this.input_vector.x = 0;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Q) == GLFW.GLFW_RELEASE) {
            this.input_vector.y = 0;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_E) == GLFW.GLFW_RELEASE) {
            this.input_vector.y = 0;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            this.movement_speed = SPEED;
        }

        if (this.input_vector.x != 0 || this.input_vector.y != 0 || this.input_vector.z != 0) {
            this.input_vector.normalize();
        }
    }

    public void process_mouse(double x_offset, double y_offset, double delta_time) {
        x_offset *= this.mouse_sensitivity * delta_time;
        y_offset *= this.mouse_sensitivity * delta_time;

        this.yaw   += x_offset;
        this.pitch += y_offset;

        if (this.pitch > 89.f) {
            this.pitch = 89.f;
        }
        if (this.pitch < -89.f) {
            this.pitch = -89.f;
        }
    }

    private void update_camera_vectors() {
        Vector3f f = new Vector3f();
        f.x = (float) (Math.cos(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch)));
        f.y = (float) (Math.sin(Math.toRadians(this.pitch)));
        f.z = (float) (Math.sin(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch)));
        this.front = new Vector3f(f);
        this.front.normalize();
        this.right = new Vector3f(this.front).cross(this.world_up);
        this.right.normalize();
        this.up    = new Vector3f(this.right).cross(this.front);
        this.up.normalize();

        this.trajectory.add(new Vector3f(right).mul(input_vector.x, new Vector3f()));
        this.trajectory.add(new Vector3f(up).mul(input_vector.y, new Vector3f()));
        this.trajectory.add(new Vector3f(front).mul(input_vector.z, new Vector3f()));
    }

}
