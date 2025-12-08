package org.delfino.utils;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex {
    public static int bytes = Float.BYTES * 8;
    public Vector3f position;
    public Vector3f normal;
    public Vector2f tex_coords;
}
