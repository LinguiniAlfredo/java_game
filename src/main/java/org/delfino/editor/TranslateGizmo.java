package org.delfino.editor;

import org.delfino.utils.Utils;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class TranslateGizmo {
    public int         VAO, VBO;
    public FloatBuffer vertex_buffer;
    public Vector3f    position;
    public float       line_length = 2.f;
    public int         num_vertices;

    public TranslateGizmo(Vector3f position) {
        this.position = position;
        create_vertices();
        init();
    }

    public void delete() {
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
    }

    private void create_vertices() {
        float[] vertices = {
                0.f, 0.f, 0.f,         1.f, 0.f, 0.f,
                line_length, 0.f, 0.f, 1.f, 0.f, 0.f,
                0.f, 0.f, 0.f,         0.f, 1.f, 0.f,
                0.f, line_length, 0.f, 0.f, 1.f, 0.f,
                0.f, 0.f, 0.f,         0.f, 0.f, 1.f,
                0.f, 0.f, line_length, 0.f, 0.f, 1.f
        };
        this.num_vertices = vertices.length;
        this.vertex_buffer = Utils.float_arr_to_fb(vertices);
    }

    private void init() {

        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();

        glBindVertexArray(this.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.vertex_buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 6, 0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 6, Float.BYTES * 3);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
}
