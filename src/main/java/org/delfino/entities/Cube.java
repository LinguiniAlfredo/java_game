package org.delfino.entities;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Cube extends Entity {
    public Cube(Vector3f position) {
        super("models/cube.obj", position, new Quaternionf(0.f, 1.f, 0.f, 0.f), 1.f, "textures/cube.png");
    }
}
