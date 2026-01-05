package org.delfino.entities;

import org.delfino.scenes.Scene;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.delfino.entities.EntityType.SPHERE;

public class Sphere extends Entity {

    public Sphere(Scene scene, Vector3f position) {
        super (
                scene,
                SPHERE,
                "models/sphere.obj",
                position,
                new Quaternionf(0.f, 1.f, 0.f, 0.f),
                new Vector3f(1.f, 1.f, 1.f),
                ""
        );
    }

    public Sphere(Scene scene, Vector3f position, Quaternionf rotation, Vector3f scale) {
        super(scene, SPHERE, "models/sphere.obj", position, rotation, scale, "");
    }
}
