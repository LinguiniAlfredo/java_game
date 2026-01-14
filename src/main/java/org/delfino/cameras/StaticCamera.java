package org.delfino.cameras;

import org.delfino.Context;
import org.delfino.Gamemode;
import org.delfino.entities.Entity;
import org.delfino.scenes.Scene;
import org.delfino.utils.Collision;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL30.*;

public class StaticCamera extends Camera {
    public String name;
    public Scene scene;

    public StaticCamera(Scene scene, Vector3f position, Vector3f front) {
        super(scene, position, front);
        this.scene = scene;
        this.collision = new Collision(this.position.add(new Vector3f(0.f, 0.f, 10.f)), 50.f, 50.f, 50.f);
    }

    @Override
    public void render() {
        if (Context.gamemode == Gamemode.EDIT) {
//            this.model.render(this.shader, this.position, this.orientation, this.scale, this.selected);
        }
    }

    @Override
    public void update(double deltaTime) {
        update_camera_vectors();
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
        this.front.rotate(this.orientation);
        this.front.cross(this.world_up, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();
    }

    public void render_viewport() {
        int width = Context.screen_width / 4;
        int height = Context.screen_height / 4;
        int pos_x = Context.screen_width - width;

        glClear(GL_DEPTH_BUFFER_BIT);
        glViewport(pos_x, 0, width, height);

        for (Entity entity : Context.current_scene.entities) {
            entity.render(this);
        }
        Context.current_scene.skybox.render(this);

        glViewport(0, 0, Context.screen_width, Context.screen_height);
    }
}
