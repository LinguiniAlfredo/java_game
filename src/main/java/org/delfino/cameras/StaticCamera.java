package org.delfino.cameras;

import org.delfino.Context;
import org.delfino.scenes.Scene;
import org.delfino.utils.Collision;
import org.joml.Vector3f;

public class StaticCamera extends Camera {
    public String name;
    public Scene scene;

    public StaticCamera(Scene scene, Vector3f position, Vector3f front) {
        super(scene, position, front);
        this.scene = scene;
        this.collision = new Collision(this.position, 50.f, 50.f, 50.f);
    }

    @Override
    public void update(double deltaTime) {
        if (Context.current_scene.player.collision.intersects(this.collision)) {
            Context.active_camera = this;
        }
    }

    @Override
    public void process_keyboard() {
        // no key processing implementation for static cameras
    }

    @Override
    public void process_mouse_movement(double offset_x, double offset_y, double delta_time) {
        // no mouse movement implementation for static cameras
    }

    @Override
    public void update_camera_vectors() {
        this.front.cross(this.world_up, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();
    }
}
