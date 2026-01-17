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
        updateCameraVectors();
        if (Context.currentScene.player.collision.intersects(this.collision)) {
            Context.activeCamera = this;
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
    public void updateCameraVectors() {
        this.front.rotate(this.orientation);
        this.front.cross(this.worldUp, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();
    }

    public void render_viewport() {
        int width = Context.screenWidth / 4;
        int height = Context.screenHeight / 4;
        int pos_x = Context.screenWidth - width;

        glClear(GL_DEPTH_BUFFER_BIT);
        glViewport(pos_x, 0, width, height);

        for (Entity entity : Context.currentScene.entities) {
            entity.render(this);
        }
        Context.currentScene.skybox.render(this);

        glViewport(0, 0, Context.screenWidth, Context.screenHeight);
    }
}
