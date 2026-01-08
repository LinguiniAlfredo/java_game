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
    public FloatBuffer vertex_buffer;
    public Vector3f    position;
    public int         num_vertices;
    public Axis        selected_axis;
    public Axis        hovered_axis;
    public Collision   x_axis_volume;
    public Collision   y_axis_volume;
    public Collision   z_axis_volume;
    public Shader      shader;
    public Editor      editor;
    public float       line_length = 2.f;
    public float       line_width  = 3.f;
    public int         circle_resolution = 100;
    public float       circle_radius   = 2.f;


    public Gizmo(Editor editor, Vector3f position) {
        this.editor   = editor;
        this.position = position;
        this.shader   = new Shader("shaders/gizmo.vert", "shaders/gizmo.frag");

        create_collisions();
        create_vertices();
        init();
    }

    public void delete() {
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
        this.shader.delete();

        if (this.x_axis_volume != null) this.x_axis_volume.delete();
        if (this.y_axis_volume != null) this.y_axis_volume.delete();
        if (this.z_axis_volume != null) this.z_axis_volume.delete();
    }

    public void render() {
        // To be overridden in implementations
    }

    public void transform_object(Entity object, double offset_x, double offset_y, double delta_time) {
        // To be overridden in implementations
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
            this.hovered_axis = Axis.Z;
        } else {
            this.hovered_axis = null;
        }
    }

    public void translate_collision(Vector3f position) {
        this.x_axis_volume.position.add(position);
        this.y_axis_volume.position.add(position);
        this.z_axis_volume.position.add(position);
    }

    public void create_vertices() {
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
        glBufferData(GL_ARRAY_BUFFER, this.vertex_buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 6, 0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 6, Float.BYTES * 3);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
}
