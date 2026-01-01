package org.delfino.utils;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static org.delfino.Context.window;

enum PlayerState {
    GROUNDED,
    AIRBORNE,
}

public class PlayerControllerFPS extends Camera {
    public Collision            collision;
    public Vector3f             gravity;
    private PlayerState         state;
    private Vector3f            velocity = new Vector3f();
    private ArrayList<Vector3f> collision_vectors = new ArrayList<>();
    private ArrayList<Vector3f> collision_normals = new ArrayList<>();
    private Vector3f            tmp = new Vector3f();

    public PlayerControllerFPS(Vector3f position) {
        super(position);
        this.gravity   = new Vector3f(0.f, -9.8f, 0.f);
        this.collision = new Collision(position, 2.f, 10.f, 2.f);
        this.state     = PlayerState.AIRBORNE;
    }

    public PlayerControllerFPS(Camera other) {
        super(other);
        this.gravity   = new Vector3f(0.f, -9.8f, 0.f);
        this.collision = new Collision(position, 2.f, 10.f, 2.f);
        this.state     = PlayerState.AIRBORNE;
    }

    public void delete() {
        this.collision.delete();
    }

    @Override
    public void update(double delta_time) {
        super.update(delta_time);
        this.velocity.zero();
        this.collision_vectors.clear();
        boolean colliding = false;
        update_camera_vectors();

        tmp.set(this.gravity);
        this.velocity.set(this.tmp.mul((float)delta_time));

        if (this.state == PlayerState.AIRBORNE) {
            this.collision.position.add(this.velocity);
        }
        colliding = check_for_ground();

        if (colliding) {
            this.collision.position.set(this.position);
        } else {
            this.position.set(this.collision.position);
        }
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

        this.front.set(
                (float) Math.cos(yaw_rad) * cos_pitch,
                0.f,
                (float) Math.sin(yaw_rad) * cos_pitch
        ).normalize();

        this.front.cross(this.world_up, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();

        this.trajectory.zero();
        this.trajectory.fma(input_vector.x, right)
                .fma(input_vector.y, up)
                .fma(input_vector.z, front);

        this.front.set(
                (float) Math.cos(yaw_rad) * cos_pitch,
                (float) Math.sin(pitch_rad),
                (float) Math.sin(yaw_rad) * cos_pitch
        ).normalize();

        this.front.cross(this.world_up, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();
    }

    private void resolve_collisions() {

    }

    private boolean check_for_ground() {
        for (Entity entity : Context.current_scene.entities) {
            if (entity.position.y < this.position.y && this.collision.intersects(entity.collision)){
                this.state = PlayerState.GROUNDED;
                this.collision_normals.add(entity.collision.normal);
            }
        }
        if (collision_normals.isEmpty()) {
            this.state = PlayerState.AIRBORNE;
        }
        return !this.collision_normals.isEmpty();
    }


}
