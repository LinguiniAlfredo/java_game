package org.delfino.editor;

import org.delfino.entities.Entity;
import org.delfino.utils.Collision;
import org.delfino.utils.Shader;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

enum Axis {
    X,
    Y,
    Z
}

public class Gizmo {

    public int         VAO, VBO;
    public FloatBuffer vertexBuffer;
    public Vector3f    position;
    public int numVertices;
    public Axis selectedAxis;
    public Axis hoveredAxis;
    public Collision xAxisVolume;
    public Collision yAxisVolume;
    public Collision zAxisVolume;
    public Shader      shader;
    public Editor      editor;
    public float lineLength = 2.f;
    public float lineWidth = 3.f;
    public int circleResolution = 100;
    public float circleRadius = 2.f;


    public Gizmo(Editor editor, Vector3f position) {
        this.editor   = editor;
        this.position = position;
        this.shader   = new Shader("shaders/gizmo.vert", "shaders/gizmo.frag");

        create_collisions();
        createVertices();
        init();
    }

    public void delete() {
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
        this.shader.delete();

        if (this.xAxisVolume != null) this.xAxisVolume.delete();
        if (this.yAxisVolume != null) this.yAxisVolume.delete();
        if (this.zAxisVolume != null) this.zAxisVolume.delete();
    }

    public void render() {
        // To be overridden in implementations
    }

    public void transform_object(Entity object, double offset_x, double offset_y, double delta_time) {
        // To be overridden in implementations
    }

    public void render_collisions() {
        this.xAxisVolume.render();
        this.yAxisVolume.render();
        this.zAxisVolume.render();
    }

    public void check_hovered(Vector3f ray) {
        if (this.selectedAxis == null) {
            if (this.xAxisVolume.intersects(ray)) {
                this.hoveredAxis = Axis.X;
            } else if (this.yAxisVolume.intersects(ray)) {
                this.hoveredAxis = Axis.Y;
            } else if (this.zAxisVolume.intersects(ray)) {
                this.hoveredAxis = Axis.Z;
            } else {
                this.hoveredAxis = null;
            }
        }
    }

    public void translate_collision(Vector3f position) {
        this.xAxisVolume.position.add(position);
        this.yAxisVolume.position.add(position);
        this.zAxisVolume.position.add(position);
    }

    public void createVertices() {
        // To be overridden in implementations
    }

    public void create_collisions() {
        // To be overridden in implementations
    }


    private void init() {
        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();

        glBindVertexArray(this.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 6, 0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 6, Float.BYTES * 3);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
}
