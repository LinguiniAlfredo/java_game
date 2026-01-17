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

    public int crosshairVAO, crosshairVBO;
    public Shader shader;
    public ArrayList<Vector2f> crosshairVertices;
    public FloatBuffer crosshairVertexbuffer;
    public Matrix4f model = new Matrix4f(), view = new Matrix4f(), proj = new Matrix4f();

    public UI() {
        this.shader = new Shader("shaders/simple_ui.vert", "shaders/simple_ui.frag");
        this.crosshairVertices = generateCrosshairVertices();
        this.crosshairVertexbuffer = Utils.vertices_2f_to_fb(this.crosshairVertices);
        init();
    }

    public void delete() {
        this.shader.delete();
        glDeleteVertexArrays(this.crosshairVAO);
        glDeleteBuffers(this.crosshairVBO);
    }

    public void render() {
        if (Context.gamemode == Gamemode.PAUSED) {
            renderPauseMenu();
        } else if (Context.gamemode == Gamemode.GAME){
            if (Context.activeCamera instanceof FPSCamera) {
                renderCrosshair();
            }
        }
    }

    private void renderPauseMenu() {

    }

    private void renderCrosshair() {
        model.identity();
        view.identity();
        proj.identity();
        proj.ortho(0.f, (float)Context.screenWidth, 0.f, (float)Context.screenHeight, -1.f, 1.f);

        this.shader.use();
        this.shader.setMat4("model", model);
        this.shader.setMat4("view", view);
        this.shader.setMat4("projection", proj);

        glBindVertexArray(this.crosshairVAO);
        glDrawArrays(GL_LINES, 0, this.crosshairVertices.size());
        glBindVertexArray(0);
    }

    private ArrayList<Vector2f> generateCrosshairVertices() {
        ArrayList<Vector2f> vertices = new ArrayList<>();
        float center_x    = Context.screenWidth * 0.5f;
        float center_y    = Context.screenHeight * 0.5f;
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
        this.crosshairVAO = glGenVertexArrays();
        this.crosshairVBO = glGenBuffers();

        glBindVertexArray(this.crosshairVAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.crosshairVBO);
        glBufferData(GL_ARRAY_BUFFER, this.crosshairVertexbuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, Float.BYTES * 2, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

}
