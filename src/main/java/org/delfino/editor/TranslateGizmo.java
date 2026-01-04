package org.delfino.editor;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.utils.Collision;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class TranslateGizmo extends Gizmo {

    public TranslateGizmo(Editor editor, Vector3f position) {
        super(editor, position);
    }

    @Override
    public void transform_object(Entity object, double offset_x, double offset_y, double delta_time) {
        if (this.selected_axis != null) {
            offset_x *= this.editor.camera.mouse_sensitivity * delta_time;
            offset_y *= this.editor.camera.mouse_sensitivity * delta_time;
            Vector3f mouse_vector_view = new Vector3f().fma((float) offset_x, this.editor.camera.right)
                    .fma((float) offset_y, this.editor.camera.up)
                    .fma(0.f, this.editor.camera.front);
            Vector3f translation_vector = new Vector3f();

            switch(this.selected_axis) {
                case X:
                    translation_vector.x = mouse_vector_view.x / 2;
                    object.position.add(translation_vector);
                    break;
                case Y:
                    translation_vector.y = mouse_vector_view.y / 2;
                    object.position.add(translation_vector);
                    break;
                case Z:
                    translation_vector.z = mouse_vector_view.z / 2;
                    object.position.add(translation_vector);
                    break;
            }
            this.translate_collision(translation_vector);
        }
    }

    @Override
    public void render() {
        Matrix4f mat_model = new Matrix4f().translate(this.position);
        Matrix4f mat_view  = Context.camera.get_view_matrix();
        Matrix4f mat_proj  = Context.camera.get_perspective_matrix();

        this.shader.use();
        this.shader.set_mat4("model", mat_model);
        this.shader.set_mat4("view", mat_view);
        this.shader.set_mat4("projection", mat_proj);

        glClear(GL_DEPTH_BUFFER_BIT);

        glLineWidth(this.line_width);
        glBindVertexArray(this.VAO);
        this.shader.set_int("hovered", this.hovered_axis == Axis.X || this.selected_axis == Axis.X ? 1 : 0);
        glDrawArrays(GL_LINES, 0, 2);
        this.shader.set_int("hovered", this.hovered_axis == Axis.Y || this.selected_axis == Axis.Y ? 1 : 0);
        glDrawArrays(GL_LINES, 2, 2);
        this.shader.set_int("hovered", this.hovered_axis == Axis.Z || this.selected_axis == Axis.Z ? 1 : 0);
        glDrawArrays(GL_LINES, 4, 2);
        glBindVertexArray(0);

        glLineWidth(1.f);
    }

    @Override
    public void create_collisions() {
        this.x_axis_volume = new Collision(
                new Vector3f(position).add(new Vector3f(line_length / 2, 0.f, 0.f)),
                line_length, 0.2f,
                0.2f);
        this.y_axis_volume = new Collision(
                new Vector3f(position).add(new Vector3f(0.f, line_length / 2, 0.f)),
                0.2f, line_length,
                0.2f);
        this.z_axis_volume = new Collision(
                new Vector3f(position).add(new Vector3f(0.f, 0.f, line_length / 2)),
                0.2f, 0.2f,
                line_length);

    }

    @Override
    public void create_vertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        add_line_vertices(vertices, Axis.X);
        add_line_vertices(vertices, Axis.Y);
        add_line_vertices(vertices, Axis.Z);

        this.num_vertices = vertices.size();
        this.vertex_buffer = Utils.vertices_3f_to_fb(vertices);
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
