package org.delfino.editor;

import org.delfino.Context;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class Compass extends Gizmo {

    public Compass(Editor editor) {
        super(editor, new Vector3f());
    }

    @Override
    public void render() {
        glViewport(50, 50, 100, 100);
        Matrix4f mat_model = new Matrix4f();
        Matrix4f mat_view  = new Matrix4f()
                .identity()
                .lookAt(Context.camera.front, new Vector3f(0.f, 0.f, 0.f), Context.camera.up);
        Matrix4f mat_proj  = new Matrix4f().ortho(-1.f, 1.f, -1.f, 1.f, 1.f, 1000.f);

        this.shader.use();
        this.shader.set_mat4("model", mat_model);
        this.shader.set_mat4("view", mat_view);
        this.shader.set_mat4("projection", mat_proj);

        this.shader.set_int("hovered", 1);
        glBindVertexArray(this.VAO);
        glDrawArrays(GL_LINES, 0, 6);
        glBindVertexArray(0);

        glViewport(0, 0, Context.screen_width, Context.screen_height);
    }

    @Override
    public void create_vertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        add_line_vertices(vertices, Axis.X);
        add_line_vertices(vertices, Axis.Y);
        add_line_vertices(vertices, Axis.Z);

        this.vertex_buffer = Utils.vertices_3f_to_fb(vertices);
        add_cube_vertices();
    }

    private void add_line_vertices(ArrayList<Vector3f> vertices, Axis axis) {
        Vector3f zero = new Vector3f(0.f);
        Vector3f vertex = new Vector3f(0.f);
        Vector3f red    = new Vector3f(1.f, 0.f, 0.f);
        Vector3f green  = new Vector3f(0.f, 1.f, 0.f);
        Vector3f blue   = new Vector3f(0.f, 0.f, 1.f);

        vertices.add(zero);
        switch (axis) {
            case X:
                vertices.add(red);
                vertex.x = line_length;
                vertices.add(vertex);
                vertices.add(red);
                break;
            case Y:
                vertices.add(green);
                vertex.y = line_length;
                vertices.add(vertex);
                vertices.add(green);
                break;
            case Z:
                vertices.add(blue);
                vertex.z = line_length;
                vertices.add(vertex);
                vertices.add(blue);
                break;
        }
    }

    private void add_cube_vertices() {
        float[] vertices = new float[]{
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                 0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                 0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                -0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                 0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                 0.5f,  0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                -0.5f,  0.5f, 0.5f, 0.0f, 0.0f, 1.0f,

                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,

                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
                 0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
                 0.5f, -0.5f,  0.5f, 0.0f, -1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f, 0.0f, -1.0f, 0.0f,

                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                 0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                 0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f
        };
        FloatBuffer cube_buffer = Utils.float_arr_to_fb(vertices);
        this.vertex_buffer = Utils.append_float_buffers(this.vertex_buffer, cube_buffer);
    }
}
