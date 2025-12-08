package org.delfino.utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

public class Utils {

    public static FloatBuffer vec3_to_fb(Vector3f v) {
       FloatBuffer fb = BufferUtils.createFloatBuffer(3);
       fb.put(v.x).put(v.y).put(v.z);
       fb.flip();
       return fb;
    }

    public static FloatBuffer vec4_to_fb(Vector4f v) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);
        fb.put(v.x).put(v.y).put(v.z).put(v.w);
        fb.flip();
        return fb;
    }

    public static FloatBuffer mat4_to_fb(Matrix4f m) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);

        fb.put(m.m00()).put(m.m10()).put(m.m20()).put(m.m30());
        fb.put(m.m01()).put(m.m11()).put(m.m21()).put(m.m31());
        fb.put(m.m02()).put(m.m12()).put(m.m22()).put(m.m32());
        fb.put(m.m03()).put(m.m13()).put(m.m23()).put(m.m33());

        fb.rewind();
        return fb;
    }

    public static FloatBuffer vertices_to_fb(ArrayList<Vertex> vertices) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.size() * Vertex.bytes);
        for (Vertex v : vertices) {
            fb.put(v.position.x).put(v.position.y).put(v.position.z);
            fb.put(v.normal.x).put(v.normal.y).put(v.normal.z);
            fb.put(v.tex_coords.x).put(v.tex_coords.y);
        }
        return fb;
    }

    public static FloatBuffer vertices_3f_to_fb(ArrayList<Vector3f> vertices) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.size() * Vertex.bytes);
        for (Vector3f v : vertices) {
            fb.put(v.x).put(v.y).put(v.z);
        }
        return fb;
    }

    public static FloatBuffer float_arr_to_fb(float[] vertices) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length * Float.BYTES);
        for (float v : vertices) {
            fb.put(v);
        }
        return fb;
    }


    public static IntBuffer indices_to_ib(ArrayList<Integer> indices) {
        IntBuffer ib = BufferUtils.createIntBuffer(indices.size() * 24);
        for (Integer i : indices) {
            ib.put(i);
        }
        return ib;
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
