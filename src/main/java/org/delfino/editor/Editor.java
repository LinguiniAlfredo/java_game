package org.delfino.editor;

import org.delfino.Context;
import org.delfino.utils.Camera;
import org.delfino.utils.Shader;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Editor {
    public Camera      camera;
    public Shader      shader;
    public int         VAO, VBO;
    public FloatBuffer vertex_buffer;
    public float       grid_size = 1000;

    public Editor() {
        Vector3f p = new Vector3f(40.f, 20.f, 0.f);
        Vector3f f = new Vector3f(0.f, 0.f, 0.f).sub(p).normalize();
        this.camera = new EditorCamera(p, f);
        Context.camera = this.camera;

        this.shader = new Shader("shaders/editor.vert", "shaders/editor.frag");
        float[] quad_vertices = {
            -grid_size, 0, -grid_size,
             grid_size, 0, -grid_size,
             grid_size, 0,  grid_size,
            -grid_size, 0,  grid_size
        };
        this.vertex_buffer = Utils.float_arr_to_fb(quad_vertices);
        init();
    }

    public void delete() {
        this.shader.delete();
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
    }

    public void update(double delta_time) {
        this.camera.update(delta_time);
    }

    public void render() {
        render_gridlines();
        // if mesh selected
            // render_gizmo()
        // render_menus()
    }

    private void render_gridlines() {
        Matrix4f mat_view = Context.camera.get_view_matrix();
        Matrix4f mat_proj = Context.camera.get_perspective_matrix();

        this.shader.use();
        this.shader.set_float("grid_scale", 1.f);
        this.shader.set_mat4("view", mat_view);
        this.shader.set_mat4("projection", mat_proj);

        glBindVertexArray(this.VAO);
        glDrawArrays(GL_TRIANGLES, 0, 4);
        glBindVertexArray(0);
    }

    private void init() {
        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();

        glBindVertexArray(this.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.vertex_buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 3, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }
}
