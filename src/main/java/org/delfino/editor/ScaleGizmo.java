package org.delfino.editor;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.utils.Collision;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class ScaleGizmo extends Gizmo {

    public ScaleGizmo(Editor editor, Vector3f position) {
        super(editor, position);
    }

    @Override
    public void delete() {
        super.delete();
    }

    @Override
    public void transform_object(Entity object, double offset_x, double offset_y, double delta_time) {
        if (this.selectedAxis != null) {
            offset_x *= this.editor.camera.mouseSensitivity * delta_time;
            offset_y *= this.editor.camera.mouseSensitivity * delta_time;
            Vector3f mouse_vector_view = new Vector3f().fma((float) offset_x, this.editor.camera.right)
                    .fma((float) offset_y, this.editor.camera.up)
                    .fma(0.f, this.editor.camera.front);
            Vector3f transformation_vector = new Vector3f();

            switch(this.selectedAxis) {
                case X:
                    transformation_vector.x = mouse_vector_view.x / 2;
                    object.scale.x = transformation_vector.x;
                    break;
                case Y:
                    transformation_vector.y = mouse_vector_view.y / 2;
                    object.scale.y = transformation_vector.y;
                    break;
                case Z:
                    transformation_vector.z = mouse_vector_view.z / 2;
                    object.scale.z = transformation_vector.z;
                    break;
            }
            this.translate_collision(transformation_vector);
        }
    }

    @Override
    public void render() {
        Matrix4f mat_model = new Matrix4f().translate(this.position);
        Matrix4f mat_view  = Context.activeCamera.getViewMatrix();
        Matrix4f mat_proj  = Context.activeCamera.getPerspectiveMatrix();

        this.shader.use();
        this.shader.setMat4("model", mat_model);
        this.shader.setMat4("view", mat_view);
        this.shader.setMat4("projection", mat_proj);

        glClear(GL_DEPTH_BUFFER_BIT);

        glLineWidth(this.lineWidth);
        glBindVertexArray(this.VAO);
        this.shader.setInt("hovered", this.hoveredAxis == Axis.X || this.selectedAxis == Axis.X ? 1 : 0);
        glDrawArrays(GL_LINES, 0, 2);
        this.shader.setInt("hovered", this.hoveredAxis == Axis.Y || this.selectedAxis == Axis.Y ? 1 : 0);
        glDrawArrays(GL_LINES, 2, 2);
        this.shader.setInt("hovered", this.hoveredAxis == Axis.Z || this.selectedAxis == Axis.Z ? 1 : 0);
        glDrawArrays(GL_LINES, 4, 2);
        glBindVertexArray(0);

        glLineWidth(1.f);
    }

    @Override
    public void create_collisions() {
        this.xAxisVolume = new Collision(
                new Vector3f(position).add(new Vector3f(lineLength / 2, 0.f, 0.f)),
                lineLength, 0.2f,
                0.2f);
        this.yAxisVolume = new Collision(
                new Vector3f(position).add(new Vector3f(0.f, lineLength / 2, 0.f)),
                0.2f, lineLength,
                0.2f);
        this.zAxisVolume = new Collision(
                new Vector3f(position).add(new Vector3f(0.f, 0.f, lineLength / 2)),
                0.2f, 0.2f,
                lineLength);

    }

    @Override
    public void createVertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        add_line_vertices(vertices, Axis.X);
        add_line_vertices(vertices, Axis.Y);
        add_line_vertices(vertices, Axis.Z);

        this.numVertices = vertices.size();
        this.vertexBuffer = Utils.vertices3FToFb(vertices);
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
                vertex.x = lineLength;
                vertices.add(vertex);
                vertices.add(red);
                break;
            case Y:
                vertices.add(green);
                vertex.y = lineLength;
                vertices.add(vertex);
                vertices.add(green);
                break;
            case Z:
                vertices.add(blue);
                vertex.z = lineLength;
                vertices.add(vertex);
                vertices.add(blue);
                break;
        }
    }
}
