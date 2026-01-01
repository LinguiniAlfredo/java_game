package org.delfino.entities;

import org.delfino.scenes.Scene;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Cube extends Entity {
    public Cube(Scene scene, Vector3f position) {
        super(scene, "models/cube.obj", position, new Quaternionf(0.f, 1.f, 0.f, 0.f), new Vector3f(1.f, 1.f, 1.f), "textures/cube.png");
    }
}
