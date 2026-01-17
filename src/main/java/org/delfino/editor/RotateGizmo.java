package org.delfino.editor;

import org.delfino.entities.Entity;
import org.delfino.utils.Collision;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.opengl.GL30.*;

public class RotateGizmo extends Gizmo {

    public RotateGizmo(Editor editor, Vector3f position) {
        super(editor, position);
    }

    @Override
    public void transform_object(Entity object, double offset_x, double offset_y, double delta_time) {
        if (selectedAxis != null) {
            offset_x *= this.editor.camera.mouseSensitivity * delta_time;
            offset_y *= this.editor.camera.mouseSensitivity * delta_time;
            Vector3f mouse_vector_view = new Vector3f().fma((float) offset_x, this.editor.camera.right)
                    .fma((float) offset_y, this.editor.camera.up)
                    .fma(0.f, this.editor.camera.front);

            Vector3f axis = new Vector3f();
            switch (this.selectedAxis) {
                case X -> axis.set(1.f, 0.f, 0.f);
                case Y -> axis.set(0.f, 1.f, 0.f);
                case Z -> axis.set(0.f, 0.f, 1.f);
            }
            float angle_view = (float) Math.acos(mouse_vector_view.dot(axis));
            object.orientation.rotateAxis(angle_view, axis);
        }
    }

    @Override
    public void render() {
        Matrix4f mat_model = new Matrix4f().translate(this.position);
        Matrix4f mat_view  = this.editor.camera.getViewMatrix();
        Matrix4f mat_proj  = this.editor.camera.getPerspectiveMatrix();

        this.shader.use();
        this.shader.setMat4("model", mat_model);
        this.shader.setMat4("view", mat_view);
        this.shader.setMat4("projection", mat_proj);

        glClear(GL_DEPTH_BUFFER_BIT);
        glLineWidth(this.lineWidth);

        glBindVertexArray(this.VAO);
        this.shader.setInt("hovered", this.hoveredAxis == Axis.X || this.selectedAxis == Axis.X ? 1 : 0);
        glDrawArrays(GL_LINE_LOOP, 0,   this.circleResolution);
        this.shader.setInt("hovered", this.hoveredAxis == Axis.Y || this.selectedAxis == Axis.Y ? 1 : 0);
        glDrawArrays(GL_LINE_LOOP, 100, this.circleResolution);
        this.shader.setInt("hovered", this.hoveredAxis == Axis.Z || this.selectedAxis == Axis.Z ? 1 : 0);
        glDrawArrays(GL_LINE_LOOP, 200, this.circleResolution);
        glBindVertexArray(0);

        glLineWidth(1.f);
    }

    @Override
    public void createVertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();

        add_circle_vertices(vertices, Axis.X);
        add_circle_vertices(vertices, Axis.Y);
        add_circle_vertices(vertices, Axis.Z);

        this.numVertices = vertices.size();
        this.vertexBuffer = Utils.vertices3FToFb(vertices);
    }

    @Override
    public void create_collisions() {
        float circle_diameter = this.circleRadius * 2;
        this.xAxisVolume = new Collision(
                new Vector3f(this.position),
                0.2f, circle_diameter, circle_diameter);
        this.yAxisVolume = new Collision(
                new Vector3f(this.position),
                circle_diameter, 0.2f, circle_diameter);
        this.zAxisVolume = new Collision(
                new Vector3f(this.position),
                circle_diameter, circle_diameter, 0.2f);
    }

    @Override
    public void check_hovered(Vector3f ray) {
        Vector3f x_intersection = this.xAxisVolume.getIntersection(ray);
        Vector3f y_intersection = this.yAxisVolume.getIntersection(ray);
        Vector3f z_intersection = this.zAxisVolume.getIntersection(ray);

        if (x_intersection.distance(this.editor.selectedObject.position) >= this.circleRadius - 0.2 &&
            x_intersection.distance(this.editor.selectedObject.position) <= this.circleRadius + 0.2) {
            this.hoveredAxis = Axis.X;
        } else if (y_intersection.distance(this.editor.selectedObject.position) >= this.circleRadius - 0.2 &&
            y_intersection.distance(this.editor.selectedObject.position) <= this.circleRadius + 0.2) {
            this.hoveredAxis = Axis.Y;
        } else if (z_intersection.distance(this.editor.selectedObject.position) >= this.circleRadius - 0.2 &&
            z_intersection.distance(this.editor.selectedObject.position) <= this.circleRadius + 0.2) {
            this.hoveredAxis = Axis.Z;
        } else {
            this.hoveredAxis = null;
        }
    }

    private void add_circle_vertices(ArrayList<Vector3f>vertices, Axis axis) {
        for (int i = 0; i < this.circleResolution; i++) {
            Vector3f vertex = new Vector3f(0.f);
            Vector3f red    = new Vector3f(1.f, 0.f, 0.f);
            Vector3f green  = new Vector3f(0.f, 1.f, 0.f);
            Vector3f blue   = new Vector3f(0.f, 0.f, 1.f);

            float angle = (float)(2 * Math.PI * i / this.circleResolution);
            switch (axis) {
                case X:
                    vertex.y = (float) (this.circleRadius * Math.cos(angle));
                    vertex.z = (float) (this.circleRadius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(red);
                    break;
                case Y:
                    vertex.x = (float) (this.circleRadius * Math.cos(angle));
                    vertex.z = (float) (this.circleRadius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(green);
                    break;
                case Z:
                    vertex.x = (float) (this.circleRadius * Math.cos(angle));
                    vertex.y = (float) (this.circleRadius * Math.sin(angle));
                    vertices.add(vertex);
                    vertices.add(blue);
                    break;
            }
        }
    }
}
