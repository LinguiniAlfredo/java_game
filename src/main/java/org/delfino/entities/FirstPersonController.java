package org.delfino.entities;

import org.delfino.Context;
import org.delfino.cameras.FPSCamera;
import org.delfino.scenes.Scene;
import org.delfino.utils.Collision;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.delfino.entities.EntityType.FIRST_PERSON_CONTROLLER;

enum PlayerState {
    GROUNDED,
    AIRBORNE,
}

public class FirstPersonController extends Entity {

    public FPSCamera camera;
    private PlayerState state;
    public float width  = 2.f;
    public float height = 4.f;
    public float depth  = 2.f;
    public Vector3f gravity = new Vector3f(0.f, -9.8f, 0.f);
    public Vector3f velocity = new Vector3f();
    private ArrayList<Vector3f> collisionVectors = new ArrayList<>();
    private ArrayList<Vector3f> collisionNormals = new ArrayList<>();
    private Vector3f tmp = new Vector3f();
    public float movement_speed = 10.0f;
    public Vector3f trajectory = new Vector3f();

    public FirstPersonController(Scene scene, Vector3f position) {
        super(
            scene,
            FIRST_PERSON_CONTROLLER,
            "",
            position,
            new Quaternionf(0.f, 1.f, 0.f, 0.f),
            new Vector3f(1.f, 1.f, 1.f),
            ""
        );
        this.camera = new FPSCamera(Context.currentScene, this, position, new Vector3f(0.f, 0.f, 1.f));
        Context.activeCamera = this.camera;

        this.collision = new Collision(position, width, height, depth);
    }

    public FirstPersonController(Scene scene, Vector3f position, Quaternionf rotation, Vector3f scale) {
        super(scene, FIRST_PERSON_CONTROLLER, "", position, rotation, scale, "");
        this.camera = new FPSCamera(scene, this, position, new Vector3f(0.f, 0.f, 1.f));
        Context.activeCamera = this.camera;

        this.collision = new Collision(position, width, height, depth);
    }

    @Override
    public void update(double delta_time) {
        this.camera.update(delta_time);
        float scale = this.movement_speed * (float) delta_time;
        this.position.fma(scale, this.trajectory);

        this.velocity.zero();
        this.collisionVectors.clear();
        boolean colliding = false;

        tmp.set(this.gravity);
        this.velocity.set(this.tmp.mul((float)delta_time));

        if (this.state == PlayerState.AIRBORNE) {
            this.collision.position.add(this.velocity);
        }
        colliding = checkForGround();

        if (colliding) {
            this.collision.position.set(this.position);
        } else {
            this.position.set(this.collision.position);
        }
    }

    @Override
    public void renderCollider() {
        this.collision.render();
    }

    private boolean checkForGround() {
        this.collisionNormals.clear();
        for (Entity entity : Context.currentScene.entities) {
            if (entity.position.y < this.position.y && this.collision.intersects(entity.collision)){
                this.state = PlayerState.GROUNDED;
                this.collisionNormals.add(entity.collision.normal);
            }
        }
        if (collisionNormals.isEmpty()) {
            this.state = PlayerState.AIRBORNE;
        }
        return !this.collisionNormals.isEmpty();
    }
}
