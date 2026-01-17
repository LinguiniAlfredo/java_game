package org.delfino.utils;

import org.delfino.Context;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Intersectionf;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class Collision {

    public int                 VAO, VBO;
    public Vector3f            position;
    public Vector3f            halfDimensions;
    public Vector3f            normal;
    public ArrayList<Vector3f> vertices;
    public FloatBuffer         vertexBuffer;
    public Shader              shader;
    public boolean             isColliding;
    public Matrix4f            model = new Matrix4f();
    public final Vector3f      red   = new Vector3f(1.f, 0.f, 0.f);
    public final Vector3f      green = new Vector3f(0.f, 1.f, 0.f);

    public Collision(Vector3f position, float width, float height, float depth) {
        this.position = position;
        this.halfDimensions = new Vector3f(width * 0.5f, height * 0.5f, depth * 0.5f);
        this.shader = new Shader("shaders/simple.vert", "shaders/simple.frag");
        this.isColliding = false;

        this.vertices = getVertices();
        this.vertexBuffer = Utils.vertices3FToFb(this.vertices);
        initVAO();
    }

    public void delete() {
        this.shader.delete();
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
    }

    public boolean intersects(Collision other) {
        boolean colliding = false;

        colliding = (Math.abs(this.position.x - other.position.x) <= (this.halfDimensions.x + other.halfDimensions.x)) &&
                    (Math.abs(this.position.y - other.position.y) <= (this.halfDimensions.y + other.halfDimensions.y)) &&
                    (Math.abs(this.position.z - other.position.z) <= (this.halfDimensions.z + other.halfDimensions.z));

        if (colliding) {
            this.isColliding = colliding;
            other.isColliding = colliding;
//            calc_collision_normal(other);
        }

        return colliding;
    }

    public boolean intersects(Vector3f ray) {
        // need min and max in world space, so add position to the dimensions
        Vector3f min = new Vector3f(this.halfDimensions).negate().add(this.position);
        Vector3f max = new Vector3f(this.halfDimensions).add(this.position);
        Vector2f result = new Vector2f();

        return Intersectionf.intersectRayAab(
                Context.activeCamera.position,
                ray,
                min,
                max,
                result
        );
    }

    public Vector3f getIntersection(Vector3f ray) {
        // need min and max in world space, so add position to the dimensions
        Vector3f min = new Vector3f(this.halfDimensions).negate().add(this.position);
        Vector3f max = new Vector3f(this.halfDimensions).add(this.position);
        Vector2f result = new Vector2f();
        Vector3f hit_point = new Vector3f();

        boolean hit = Intersectionf.intersectRayAab(
                Context.activeCamera.position,
                ray,
                min,
                max,
                result
        );

        if (hit && result.y >= 0.f) {
            float t_hit = result.x >= 0.f ? result.x : result.y;
            hit_point = new Vector3f(ray)
                    .mul(t_hit)
                    .add(Context.activeCamera.position);
        }

        return hit_point;

    }

    private void calcCollisionNormal(Collision other) {
        Vector3f delta = new Vector3f(this.position);
        delta.sub(other.position);

        Vector3f dim_delta = new Vector3f(this.halfDimensions);
        dim_delta.add(other.halfDimensions);

        Vector3f norm = new Vector3f(
                delta.x / dim_delta.x,
                delta.y / dim_delta.y,
                delta.z / dim_delta.z
        );

        Vector3f abs_norm = new Vector3f(
                Math.abs(norm.x),
                Math.abs(norm.y),
                Math.abs(norm.z)
        );

        Vector3f result = new Vector3f();
        float ax = abs_norm.x;
        float ay = abs_norm.y;
        float az = abs_norm.z;

        if (ax >= ay && ax >= az) {
            result.set(norm.x, 0f, 0f);
        } else if (ay >= ax && ay >= az) {
            result.set(0f, norm.y, 0f);
        } else {
            result.set(0f, 0f, norm.z);
        }

        result.normalize();
        other.normal.set(result);
    }

    public void render() {
        this.model.identity().translate(this.position);

        Matrix4f view = Context.activeCamera.getViewMatrix();
        Matrix4f proj = Context.activeCamera.getPerspectiveMatrix();
        Vector3f color = this.isColliding ? this.red : this.green;

        this.shader.use();
        this.shader.setMat4("model", model);
        this.shader.setMat4("view", view);
        this.shader.setMat4("projection", proj);
        this.shader.setVec3("in_color", color);

        glBindVertexArray(this.VAO);
        glDrawArrays(GL_LINES, 0, this.vertices.size());
        glBindVertexArray(0);
    }

    public void reset() {
        this.isColliding = false;
    }

    private void initVAO() {
        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();
        glBindVertexArray(this.VAO);
        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 3, 0);
        glBindVertexArray(0);
    }

    private ArrayList<Vector3f> getVertices() {
        Vector3f min = new Vector3f(this.halfDimensions).negate();
        Vector3f max = new Vector3f(this.halfDimensions);

        ArrayList<Vector3f> vertices = new ArrayList<>();

        vertices.add(new Vector3f(min.x, min.y, min.z));
        vertices.add(new Vector3f(max.x, min.y, min.z));
        vertices.add(new Vector3f(max.x, min.y, min.z));
        vertices.add(new Vector3f(max.x, max.y, min.z));
        vertices.add(new Vector3f(max.x, max.y, min.z));
        vertices.add(new Vector3f(min.x, max.y, min.z));
        vertices.add(new Vector3f(min.x, max.y, min.z));
        vertices.add(new Vector3f(min.x, min.y, min.z));
        vertices.add(new Vector3f(min.x, min.y, max.z));
        vertices.add(new Vector3f(max.x, min.y, max.z));
        vertices.add(new Vector3f(max.x, min.y, max.z));
        vertices.add(new Vector3f(max.x, max.y, max.z));
        vertices.add(new Vector3f(max.x, max.y, max.z));
        vertices.add(new Vector3f(min.x, max.y, max.z));
        vertices.add(new Vector3f(min.x, max.y, max.z));
        vertices.add(new Vector3f(min.x, min.y, max.z));
        vertices.add(new Vector3f(min.x, min.y, min.z));
        vertices.add(new Vector3f(min.x, min.y, max.z));
        vertices.add(new Vector3f(max.x, min.y, min.z));
        vertices.add(new Vector3f(max.x, min.y, max.z));
        vertices.add(new Vector3f(min.x, max.y, min.z));
        vertices.add(new Vector3f(min.x, max.y, max.z));
        vertices.add(new Vector3f(max.x, max.y, min.z));
        vertices.add(new Vector3f(max.x, max.y, max.z));

        return vertices;
    }
}
