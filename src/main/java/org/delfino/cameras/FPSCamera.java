package org.delfino.cameras;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.utils.Collision;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static org.delfino.Context.window;

public class FPSCamera extends Camera {
    public Collision collision;

    public FPSCamera(Vector3f position) {
        super(position);
    }

    public FPSCamera(Vector3f position, Vector3f front) {
        super(position, front);
    }

    public FPSCamera(Camera other) {
        super(other);
    }

    @Override
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
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            this.movement_speed = SPEED * 2;
        }

        if (this.input_vector.x != 0 || this.input_vector.y != 0 || this.input_vector.z != 0) {
            this.input_vector.normalize();
        }
    }

    @Override
    public void update_camera_vectors() {
        // we update the vectors once without pitch so movement trajectory is bound to the plane
        // then update vectors again with pitch for looking around
        float yaw_rad   = (float) Math.toRadians(this.yaw);
        float pitch_rad = (float) Math.toRadians(this.pitch);
        float cos_pitch = (float) Math.cos(pitch_rad);

        if (yaw_rad != 0 || pitch_rad != 0) {
            this.front.set(
                    (float) Math.cos(yaw_rad) * cos_pitch,
                    0.f,
                    (float) Math.sin(yaw_rad) * cos_pitch
            ).normalize();
        }

        this.front.cross(this.world_up, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();

        this.trajectory.zero();
        this.trajectory.fma(input_vector.x, right)
                .fma(input_vector.y, up)
                .fma(input_vector.z, front);

        if (yaw_rad != 0 || pitch_rad != 0) {
            this.front.set(
                    (float) Math.cos(yaw_rad) * cos_pitch,
                    (float) Math.sin(pitch_rad),
                    (float) Math.sin(yaw_rad) * cos_pitch
            ).normalize();
        }

        this.front.cross(this.world_up, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();
    }
}
