package org.delfino.editor;

import org.delfino.utils.Utils;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

enum Axis {
    X,
    Y,
    Z
}

public class RotateGizmo {
    public int         VAO, VBO;
    public Vector3f    position;
    public FloatBuffer vertex_buffer;
    public int         num_vertices;
    public int         resolution = 100;
    public float       radius     = 1.f;

    public RotateGizmo(Vector3f position) {
        this.position = position;
        create_vertices();
        init();
    }

    public void delete() {
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
    }

    private void create_vertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();

        add_circle_vertices(vertices, Axis.X);
        add_circle_vertices(vertices, Axis.Y);
        add_circle_vertices(vertices, Axis.Z);

        this.num_vertices = vertices.size();
        this.vertex_buffer = Utils.vertices_3f_to_fb(vertices);
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

    private void add_circle_vertices(ArrayList<Vector3f>vertices, Axis axis) {
        for (int i = 0; i < this.resolution; i++) {
            Vector3f vertex = new Vector3f(0.f);
            Vector3f red    = new Vector3f(1.f, 0.f, 0.f);
            Vector3f green  = new Vector3f(0.f, 1.f, 0.f);
            Vector3f blue   = new Vector3f(0.f, 0.f, 1.f);

            float angle = (float)(2 * Math.PI * i / this.resolution);
            switch (axis) {
                case X:
                    vertex.y = (float) (this.radius * Math.cos(angle));
                    vertex.z = (float) (this.radius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(red);
                    break;
                case Y:
                    vertex.x = (float) (this.radius * Math.cos(angle));
                    vertex.z = (float) (this.radius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(green);
                    break;
                case Z:
                    vertex.x = (float) (this.radius * Math.cos(angle));
                    vertex.y = (float) (this.radius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(blue);
                    break;
            }
        }
    }
}
