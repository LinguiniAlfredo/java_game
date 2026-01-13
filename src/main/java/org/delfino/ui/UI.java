package org.delfino.ui;

import org.delfino.Context;
import org.delfino.Gamemode;
import org.delfino.cameras.FPSCamera;
import org.delfino.utils.Shader;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class UI {

    public int                 crosshair_VAO, crosshair_VBO;
    public Shader              shader;
    public ArrayList<Vector2f> crosshair_vertices;
    public FloatBuffer         crosshair_vertex_buffer;
    public Matrix4f            model = new Matrix4f(), view = new Matrix4f(), proj = new Matrix4f();

    public UI() {
        this.shader                  = new Shader("shaders/simple_ui.vert", "shaders/simple_ui.frag");
        this.crosshair_vertices      = generate_crosshair_vertices();
        this.crosshair_vertex_buffer = Utils.vertices_2f_to_fb(this.crosshair_vertices);
        init();
    }

    public void delete() {
        this.shader.delete();
        glDeleteVertexArrays(this.crosshair_VAO);
        glDeleteBuffers(this.crosshair_VBO);
    }

    public void render() {
        if (Context.gamemode == Gamemode.PAUSED) {
            render_pause_menu();
        } else if (Context.gamemode == Gamemode.GAME){
            if (Context.active_camera instanceof FPSCamera) {
                render_crosshair();
            }
        }
    }

    private void render_pause_menu() {

    }

    private void render_crosshair() {
        model.identity();
        view.identity();
        proj.identity();
        proj.ortho(0.f, (float)Context.screen_width, 0.f, (float)Context.screen_height, -1.f, 1.f);

        this.shader.use();
        this.shader.set_mat4("model", model);
        this.shader.set_mat4("view", view);
        this.shader.set_mat4("projection", proj);

        glBindVertexArray(this.crosshair_VAO);
        glDrawArrays(GL_LINES, 0, this.crosshair_vertices.size());
        glBindVertexArray(0);
    }

    private ArrayList<Vector2f> generate_crosshair_vertices() {
        ArrayList<Vector2f> vertices = new ArrayList<>();
        float center_x    = Context.screen_width * 0.5f;
        float center_y    = Context.screen_height * 0.5f;
        float line_size   = 6.f;
        float center_size = 4.f;

        vertices.add(new Vector2f(center_x, center_y + center_size));
        vertices.add(new Vector2f(center_x, center_y + center_size + line_size));
        vertices.add(new Vector2f(center_x, center_y - center_size));
        vertices.add(new Vector2f(center_x, center_y - center_size - line_size));
        vertices.add(new Vector2f(center_x + center_size, center_y));
        vertices.add(new Vector2f(center_x + center_size + line_size, center_y));
        vertices.add(new Vector2f(center_x - center_size, center_y));
        vertices.add(new Vector2f(center_x - center_size - line_size, center_y));

        return vertices;
    }

    private void init() {
        this.crosshair_VAO = glGenVertexArrays();
        this.crosshair_VBO = glGenBuffers();

        glBindVertexArray(this.crosshair_VAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.crosshair_VBO);
        glBufferData(GL_ARRAY_BUFFER, this.crosshair_vertex_buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, Float.BYTES * 2, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

}
