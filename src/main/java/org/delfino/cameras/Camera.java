package org.delfino.cameras;

import org.delfino.Context;

import org.delfino.entities.Entity;
import org.delfino.entities.EntityType;
import org.delfino.scenes.Scene;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.delfino.Context.window;

public class Camera extends Entity {

    final float YAW         =   0.0f;
    final float PITCH       =   0.0f;
    final float SPEED       =  10.0f;
    final float SENSITIVITY =   6.5f;
    final float ZOOM        =  45.0f;

    static class Frustrum {
        float fov    = (float) Math.toRadians(45.f);
        float aspect = (float) Context.screenWidth / Context.screenHeight;
        float near   = 0.1f;
        float far    = 1000.f;
    }

    public Scene    scene;
    public Vector3f front;
    public Vector3f up;
    public Vector3f right;
    public Vector3f worldUp;
    public Vector3f trajectory;
    public Vector3f inputVector;

    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projMatrix = new Matrix4f();

    public float yaw;
    public float pitch;
    public float movementSpeed;
    public float mouseSensitivity;
    public float zoom;

    public Frustrum frustrum;

    public Camera(Scene scene, Vector3f position) {
        this.scene            = scene;
        this.position         = position;
        this.front            = new Vector3f(0.f, 0.f, -1.f);
        this.right            = new Vector3f();
        this.up               = new Vector3f();
        this.worldUp          = new Vector3f(0.f, 1.f, 0.f);
        this.trajectory       = new Vector3f();
        this.inputVector      = new Vector3f();
        this.yaw              = YAW;
        this.pitch            = PITCH;
        this.movementSpeed    = SPEED;
        this.mouseSensitivity = SENSITIVITY;
        this.zoom             = ZOOM;
        this.frustrum         = new Frustrum();

        updateCameraVectors();
    }

    public Camera(Scene scene, Vector3f position, Vector3f front) {
        super(scene, EntityType.CAMERA, "", position, new Quaternionf(0.f, 0.f, 1.f, 0.f), new Vector3f(1.f), "");
        this.scene            = scene;
        this.front            = front;
        this.right            = new Vector3f();
        this.up               = new Vector3f();
        this.worldUp          = new Vector3f(0.f, 1.f, 0.f);
        this.trajectory       = new Vector3f();
        this.inputVector      = new Vector3f();
        this.yaw              = YAW;
        this.pitch            = PITCH;
        this.movementSpeed    = SPEED;
        this.mouseSensitivity = SENSITIVITY;
        this.zoom             = ZOOM;
        this.frustrum         = new Frustrum();

        updateCameraVectors();
    }

    public Camera(Camera other) {
        this.scene            = other.scene;
        this.position         = other.position;
        this.front            = other.front;
        this.up               = other.up;
        this.right            = other.right;
        this.worldUp          = new Vector3f(0.f, 1.f, 0.f);
        this.trajectory       = other.trajectory;
        this.inputVector      = other.inputVector;
        this.yaw              = other.yaw;
        this.pitch            = other.pitch;
        this.movementSpeed    = other.movementSpeed;
        this.mouseSensitivity = other.mouseSensitivity;
        this.zoom             = other.zoom;
        this.frustrum         = other.frustrum;
    }

    public void update(double deltaTime) {
        updateCameraVectors();
        float scale = this.movementSpeed * (float) deltaTime;
        this.position.fma(scale, this.trajectory);
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix
                .identity()
                .lookAlong(this.front, this.up)
                .translate(-this.position.x, -this.position.y, -this.position.z);
    }

    public Matrix4f getPerspectiveMatrix() {
        return projMatrix
                .identity()
                .perspective(this.frustrum.fov, this.frustrum.aspect, this.frustrum.near, this.frustrum.far);
    }

    public void process_keyboard() {
        this.inputVector.zero();
        this.movementSpeed = SPEED;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            this.inputVector.z = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            this.inputVector.x = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            this.inputVector.z = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            this.inputVector.x = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS) {
            this.inputVector.y = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS) {
            this.inputVector.y = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            this.movementSpeed = SPEED * 4;
        }

        if (this.inputVector.x != 0 || this.inputVector.y != 0 || this.inputVector.z != 0) {
            this.inputVector.normalize();
        }
    }

    public void process_mouse_movement(double offset_x, double offset_y, double delta_time) {
        offset_x *= this.mouseSensitivity * delta_time;
        offset_y *= this.mouseSensitivity * delta_time;

        this.yaw   += (float) offset_x;
        this.pitch += (float) offset_y;

        if (this.pitch > 89.f) {
            this.pitch = 89.f;
        }
        if (this.pitch < -89.f) {
            this.pitch = -89.f;
        }
    }

    public void updateCameraVectors() {
        float yaw_rad   = (float) Math.toRadians(this.yaw);
        float pitch_rad = (float) Math.toRadians(this.pitch);
        float cos_pitch = (float) Math.cos(pitch_rad);

        if (yaw_rad != 0 || pitch_rad != 0) {
            this.front.set(
                    (float) Math.cos(yaw_rad) * cos_pitch,
                    (float) Math.sin(pitch_rad),
                    (float) Math.sin(yaw_rad) * cos_pitch
            ).normalize();
        }

        this.front.cross(this.worldUp, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();

        this.trajectory.zero();
        this.trajectory.fma(inputVector.x, right)
                .fma(inputVector.y, up)
                .fma(inputVector.z, front);
    }

    public void lookAt(Vector3f targetPosition) {
        Vector3f camPosition = new Vector3f(this.position);
        Vector3f direction = targetPosition.sub(camPosition).normalize();
        this.front.set(direction);
        updateCameraVectors();
    }

}
