package org.delfino.entities;

import org.delfino.Context;
import org.delfino.cameras.FPSCamera;
import org.delfino.scenes.Scene;
import org.delfino.utils.Collision;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.delfino.entities.EntityType.PLAYER;

enum PlayerState {
    GROUNDED,
    AIRBORNE,
}

public class Player extends Entity {

    public FPSCamera camera;
    private PlayerState state;
    public float width  = 2.f;
    public float height = 4.f;
    public float depth  = 2.f;
    public Vector3f gravity = new Vector3f(0.f, -9.8f, 0.f);
    public Vector3f velocity = new Vector3f();
    private ArrayList<Vector3f> collision_vectors = new ArrayList<>();
    private ArrayList<Vector3f> collision_normals = new ArrayList<>();
    private Vector3f tmp = new Vector3f();
    private float movement_speed = 10.0f;
    public Vector3f trajectory = new Vector3f();

    public Player(Scene scene, Vector3f position) {
        super(
            scene,
            PLAYER,
            "",
            position,
            new Quaternionf(0.f, 1.f, 0.f, 0.f),
            new Vector3f(1.f, 1.f, 1.f),
            ""
        );
        this.camera = new FPSCamera(this, position, new Vector3f(0.f, 0.f, 1.f));
        Context.camera = this.camera;

        this.collision = new Collision(position, width, height, depth);
    }

    public Player(Scene scene, Vector3f position, Quaternionf rotation, Vector3f scale) {
        super(scene, PLAYER, "", position, rotation, scale, "");
        this.camera = new FPSCamera(this, position, new Vector3f(0.f, 0.f, 1.f));
        Context.camera = this.camera;

        this.collision = new Collision(position, width, height, depth);
    }

    @Override
    public void update(double delta_time) {
        this.camera.update(delta_time);

        this.velocity.zero();
        this.collision_vectors.clear();
        boolean colliding = false;

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

        float scale = this.movement_speed * (float) delta_time;
        this.position.fma(scale, this.trajectory);

    }

    @Override
    public void render_collider() {
        this.collision.render();
    }

    private boolean check_for_ground() {
        this.collision_normals.clear();
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
