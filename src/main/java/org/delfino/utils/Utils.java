package org.delfino.utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

public class Utils {

    // Caller must allocate buffer with MemoryStack
    public static void vertices_to_fb(ArrayList<Vertex> vertices, FloatBuffer fb) {
        for (Vertex v : vertices) {
            fb.put(v.position.x).put(v.position.y).put(v.position.z);
            fb.put(v.normal.x).put(v.normal.y).put(v.normal.z);
            fb.put(v.tex_coords.x).put(v.tex_coords.y);
        }
        fb.flip();
    }

    public static void vertices_3f_to_fb(ArrayList<Vector3f> vertices, FloatBuffer fb) {
        for (Vector3f v : vertices) {
            fb.put(v.x).put(v.y).put(v.z);
        }
        fb.flip();
    }

    public static void float_arr_to_fb(float[] vertices, FloatBuffer fb) {
        for (float v : vertices) {
            fb.put(v);
        }
        fb.flip();
    }

    public static void indices_to_ib(ArrayList<Integer> indices, IntBuffer ib) {
        for (Integer i : indices) {
            ib.put(i);
        }
        ib.flip();
    }

    public static float clamp_to_zero(float value) {
        final double epsilon = 1e-10;

        if (Math.abs(value) < epsilon) {
            return 0;
        } else {
            return value;
        }
    }
}
