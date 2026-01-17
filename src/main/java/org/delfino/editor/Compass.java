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
        Matrix4f matModel = new Matrix4f();
        Matrix4f matView  = new Matrix4f()
                .identity()
                .lookAt(Context.activeCamera.front, new Vector3f(0.f, 0.f, 0.f), Context.activeCamera.up);
        Matrix4f matProj  = new Matrix4f().ortho(-1.f, 1.f, -1.f, 1.f, 1.f, 1000.f);

        this.shader.use();
        this.shader.setMat4("model", matModel);
        this.shader.setMat4("view", matView);
        this.shader.setMat4("projection", matProj);

        this.shader.setInt("hovered", 1);
        glBindVertexArray(this.VAO);
        glDrawArrays(GL_LINES, 0, 6);
        glBindVertexArray(0);

        glViewport(0, 0, Context.screenWidth, Context.screenHeight);
    }

    @Override
    public void createVertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        addLineVertices(vertices, Axis.X);
        addLineVertices(vertices, Axis.Y);
        addLineVertices(vertices, Axis.Z);

        this.vertexBuffer = Utils.vertices3FToFb(vertices);
        addCubeVertices();
    }

    private void addLineVertices(ArrayList<Vector3f> vertices, Axis axis) {
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

    private void addCubeVertices() {
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
        FloatBuffer cubeBuffer = Utils.floatArrToFb(vertices);
        this.vertexBuffer = Utils.appendFloatBuffer(this.vertexBuffer, cubeBuffer);
    }
}
