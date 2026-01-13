package org.delfino.editor;

import org.delfino.Context;
import org.delfino.utils.Shader;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL30.*;

public class Gridlines {
    public int         VAO, VBO;
    public FloatBuffer vertex_buffer;
    public float       grid_size = 1000;
    public Shader      shader;

    public Gridlines() {
        float[] quad_vertices = {
                -grid_size, 0, -grid_size,
                 grid_size, 0, -grid_size,
                 grid_size, 0,  grid_size,
                -grid_size, 0,  grid_size
        };
        this.vertex_buffer = Utils.float_arr_to_fb(quad_vertices);
        this.shader        = new Shader("shaders/gridlines.vert", "shaders/gridlines.frag");
        init();
    }

    public void delete() {
        this.shader.delete();
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
    }

    public void render() {
        Matrix4f mat_view = Context.active_camera.get_view_matrix();
        Matrix4f mat_proj = Context.active_camera.get_perspective_matrix();

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
