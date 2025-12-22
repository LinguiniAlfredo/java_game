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

    private final Matrix4f view_matrix = new Matrix4f();
    private final Matrix4f proj_matrix = new Matrix4f();

    public float yaw;
    public float pitch;
    public float movement_speed;
    public float mouse_sensitivity;
    public float zoom;

    public double prev_mouse_x = 0;
    public double prev_mouse_y = 0;

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
        float scale = this.movement_speed * (float) delta_time;
        this.position.fma(scale, this.trajectory);
    }

    public Matrix4f get_view_matrix() {
        return view_matrix
                .identity()
                .lookAlong(this.front, this.up)
                .translate(-this.position.x, -this.position.y, -this.position.z);
    }

    public Matrix4f get_perspective_matrix() {
        return proj_matrix
                .identity()
                .perspective(this.frustrum.fov, this.frustrum.aspect, this.frustrum.near, this.frustrum.far);
    }

    public void process_keyboard() {
        this.input_vector.zero();
        this.movement_speed = SPEED;

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

        if (this.input_vector.x != 0 || this.input_vector.y != 0 || this.input_vector.z != 0) {
            this.input_vector.normalize();
        }
    }

    public void process_mouse(double mouse_x, double mouse_y, double delta_time) {
        double x_offset, y_offset;
        x_offset = mouse_x - prev_mouse_x;
        y_offset = -(mouse_y - prev_mouse_y);
        prev_mouse_x = mouse_x;
        prev_mouse_y = mouse_y;

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
        float yaw_rad   = (float) Math.toRadians(this.yaw);
        float pitch_rad = (float) Math.toRadians(this.pitch);
        float cos_pitch = (float) Math.cos(pitch_rad);

        this.front.set(
                (float) Math.cos(yaw_rad) * cos_pitch,
                (float) Math.sin(pitch_rad),
                (float) Math.sin(yaw_rad) * cos_pitch
        ).normalize();

        this.front.cross(this.world_up, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();

        this.trajectory.zero();
        this.trajectory.fma(input_vector.x, right)
                .fma(input_vector.y, up)
                .fma(input_vector.z, front);
    }

}
