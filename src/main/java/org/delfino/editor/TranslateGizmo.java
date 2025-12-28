package org.delfino.editor;

import org.delfino.Context;
import org.delfino.utils.Collision;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class TranslateGizmo {
    public int         VAO, VBO;
    public FloatBuffer vertex_buffer;
    public Vector3f    position;
    public float       line_length = 2.f;
    public int         num_vertices;
    public Gizmo       gizmo;
    public Axis        selected_axis;
    public Axis        hovered_axis;
    public Collision   x_axis_volume;
    public Collision   y_axis_volume;
    public Collision   z_axis_volume;

    public TranslateGizmo(Gizmo gizmo, Vector3f position) {
        this.gizmo    = gizmo;
        this.position = position;

        x_axis_volume = new Collision(
                new Vector3f(position).add(new Vector3f(line_length / 2, 0.f, 0.f)),
                line_length, 0.2f,
                0.2f);
        y_axis_volume = new Collision(
                new Vector3f(position).add(new Vector3f(0.f, line_length / 2, 0.f)),
                0.2f, line_length,
                0.2f);
        z_axis_volume = new Collision(
                new Vector3f(position).add(new Vector3f(0.f, 0.f, line_length / 2)),
                0.2f, 0.2f,
                line_length);

        create_vertices();
        init();
    }

    public void delete() {
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
    }

    public void render() {
        Matrix4f mat_model = new Matrix4f().translate(this.position);
        Matrix4f mat_view  = Context.camera.get_view_matrix();
        Matrix4f mat_proj  = Context.camera.get_perspective_matrix();

        gizmo.shader.use();
        gizmo.shader.set_mat4("model", mat_model);
        gizmo.shader.set_mat4("view", mat_view);
        gizmo.shader.set_mat4("projection", mat_proj);

        glClear(GL_DEPTH_BUFFER_BIT);

        glLineWidth(gizmo.line_width);
        glBindVertexArray(this.VAO);
        gizmo.shader.set_int("hovered", hovered_axis == Axis.X || selected_axis == Axis.X ? 0 : 1);
        glDrawArrays(GL_LINES, 0, 2);
        gizmo.shader.set_int("hovered", hovered_axis == Axis.Y || selected_axis == Axis.Y ? 0 : 1);
        glDrawArrays(GL_LINES, 2, 2);
        gizmo.shader.set_int("hovered", hovered_axis == Axis.Z || selected_axis == Axis.Z ? 0 : 1);
        glDrawArrays(GL_LINES, 4, 2);
        glBindVertexArray(0);
    }

    public void render_collisions() {
        this.x_axis_volume.render();
        this.y_axis_volume.render();
        this.z_axis_volume.render();
    }

    public void check_hovered(Vector3f ray) {
        if (this.x_axis_volume.intersects(ray)) {
            this.hovered_axis = Axis.X;
        } else if (this.y_axis_volume.intersects(ray)) {
            this.hovered_axis = Axis.Y;
        } else if (this.z_axis_volume.intersects(ray)) {
            this.gizmo.translate_gizmo.hovered_axis = Axis.Z;
        } else {
            this.gizmo.translate_gizmo.hovered_axis = null;
        }
    }

    private void create_vertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        add_line_vertices(vertices, Axis.X);
        add_line_vertices(vertices, Axis.Y);
        add_line_vertices(vertices, Axis.Z);

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
}
