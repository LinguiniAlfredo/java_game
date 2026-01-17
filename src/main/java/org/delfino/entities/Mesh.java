package org.delfino.entities;

import org.delfino.Context;
import org.delfino.cameras.Camera;
import org.delfino.utils.Shader;
import org.delfino.utils.Texture;
import org.delfino.utils.Utils;
import org.delfino.utils.Vertex;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class Mesh {
    int VAO, VBO, EBO;
    ArrayList<Vertex> vertices;
    FloatBuffer vertexBuffer;
    IntBuffer indices;
    ArrayList<Texture> textures;
    Matrix4f matModel = new Matrix4f();

    public Mesh(ArrayList<Vertex> vertices, ArrayList<Integer> indices, ArrayList<Texture> textures) {
        this.vertices = vertices;
        this.vertexBuffer = Utils.vertices_to_fb(this.vertices);
        this.indices = Utils.indices_to_ib(indices);
        this.textures = textures;

        init();
    }

    public void render(Camera camera, Shader shader, Vector3f position, Quaternionf orientation, Vector3f scale, boolean selected) {
        shader.use();
        shader.setInt("shadow_map", 0);
        shader.setInt("texture1", 1);
        shader.setVec3("camera_pos", camera.position);
        shader.setVec3("light_pos", Context.currentScene.lightCube.position);
        shader.setMat4("light_space_matrix", Context.currentScene.shadowMap.lightSpaceMatrix);
        shader.setInt("selected", selected ? 1 : 0);

        matModel.identity().scale(scale).translate(position).rotate(orientation);
        Matrix4f matView = camera.getViewMatrix();
        Matrix4f matProj = camera.getPerspectiveMatrix();

        shader.setMat4("model", matModel);
        shader.setMat4("view", matView);
        shader.setMat4("projection", matProj);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, Context.currentScene.shadowMap.depthMap);

        if (!this.textures.isEmpty()) {
            shader.setInt("has_texture", GL_TRUE);
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, this.textures.get(0).id);
        } else {
            shader.setInt("has_texture", GL_FALSE);
        }

        glBindVertexArray(this.VAO);
        glDrawElements(GL_TRIANGLES, this.indices.remaining(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void renderShadowMap(Shader shadowMapShader, Vector3f position, Quaternionf orientation, Vector3f scale) {
        matModel.identity().scale(scale).translate(position).rotate(orientation);

        shadowMapShader.setMat4("model", matModel);

        glBindVertexArray(this.VAO);
        glDrawElements(GL_TRIANGLES, this.indices.remaining(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    private void init() {
        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();
        this.EBO = glGenBuffers();

        glBindVertexArray(this.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 8, 0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 8, Float.BYTES * 3);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES * 8, Float.BYTES * 6);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }
}

