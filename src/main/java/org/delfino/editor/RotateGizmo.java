package org.delfino.editor;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class RotateGizmo extends Gizmo {

    public RotateGizmo(Editor editor, Vector3f position) {
        super(editor, position);
    }

    @Override
    public void move_object(Entity object, double mouse_x, double mouse_y, double delta_time) {
        if (selected_axis != null) {
            mouse_x *= this.editor.camera.mouse_sensitivity * delta_time;
            mouse_y *= this.editor.camera.mouse_sensitivity * delta_time;
            Vector3f mouse_vector_view = new Vector3f().fma((float) mouse_x, this.editor.camera.right)
                    .fma((float) mouse_y, this.editor.camera.up)
                    .fma(0.f, this.editor.camera.front);

            switch (this.selected_axis) {
                case X:
                    object.position.add(new Vector3f(mouse_vector_view.x, 0.f, 0.f));
                    break;
                case Y:
                    object.position.add(new Vector3f(0.f, mouse_vector_view.y, 0.f));
                    break;
                case Z:
                    object.position.add(new Vector3f(0.f, 0.f, mouse_vector_view.z));
                    break;
            }
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

        glBindVertexArray(this.VAO);
        glDrawArrays(GL_LINE_LOOP, 0,   this.circle_resolution);
        glDrawArrays(GL_LINE_LOOP, 100, this.circle_resolution);
        glDrawArrays(GL_LINE_LOOP, 200, this.circle_resolution);
        glBindVertexArray(0);
    }

    @Override
    public void create_vertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();

        add_circle_vertices(vertices, Axis.X);
        add_circle_vertices(vertices, Axis.Y);
        add_circle_vertices(vertices, Axis.Z);

        this.num_vertices = vertices.size();
        this.vertex_buffer = Utils.vertices_3f_to_fb(vertices);
    }

    private void add_circle_vertices(ArrayList<Vector3f>vertices, Axis axis) {
        for (int i = 0; i < this.circle_resolution; i++) {
            Vector3f vertex = new Vector3f(0.f);
            Vector3f red    = new Vector3f(1.f, 0.f, 0.f);
            Vector3f green  = new Vector3f(0.f, 1.f, 0.f);
            Vector3f blue   = new Vector3f(0.f, 0.f, 1.f);

            float angle = (float)(2 * Math.PI * i / this.circle_resolution);
            switch (axis) {
                case X:
                    vertex.y = (float) (this.circle_radius * Math.cos(angle));
                    vertex.z = (float) (this.circle_radius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(red);
                    break;
                case Y:
                    vertex.x = (float) (this.circle_radius * Math.cos(angle));
                    vertex.z = (float) (this.circle_radius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(green);
                    break;
                case Z:
                    vertex.x = (float) (this.circle_radius * Math.cos(angle));
                    vertex.y = (float) (this.circle_radius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(blue);
                    break;
            }
        }
    }
}
