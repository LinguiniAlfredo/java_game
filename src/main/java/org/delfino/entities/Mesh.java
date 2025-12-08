package org.delfino.entities;

import org.delfino.Context;
import org.delfino.utils.Shader;
import org.delfino.utils.Texture;
import org.delfino.utils.Utils;
import org.delfino.utils.Vertex;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;

public class Mesh {
    int                VAO, VBO, EBO;
    ArrayList<Vertex>  vertices;
    ArrayList<Integer> indices;
    ArrayList<Texture> textures;

    public Mesh(ArrayList<Vertex> vertices, ArrayList<Integer> indices, ArrayList<Texture> textures) {
        this.vertices = vertices;
        this.indices  = indices;
        this.textures = textures;

        init();
    }

    public void render(Shader shader, Vector3f position, Quaternionf orientation, float scale) {
        shader.use();
        shader.set_int("shadow_map", 0);
        shader.set_int("texture1", 1);
        shader.set_vec3("camera_pos", Context.camera.position);
        shader.set_vec3("light_pos", new Vector3f(-20.f, 20.f, 0.f));
        shader.set_mat4("light_space_matrix", Context.shadow_map.light_space_matrix);
        shader.set_vec3("light_color", new Vector3f(1.f, 1.f, 1.f));
        shader.set_vec3("mesh_color",  new Vector3f(1.f, 0.5f, 0.31f));

        Matrix4f mat_model = new Matrix4f();
        mat_model.scale(scale, scale, scale)
                 .setTranslation(position)
                 .rotate(orientation);

        Matrix4f mat_view = Context.camera.get_view_matrix();
        Matrix4f mat_proj = Context.camera.get_perspective_matrix();

        shader.set_mat4("model", mat_model);
        shader.set_mat4("view", mat_view);
        shader.set_mat4("projection", mat_proj);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, Context.shadow_map.depth_map);

        if (!this.textures.isEmpty()) {
            shader.set_int("has_texture", GL_TRUE);
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, this.textures.get(0).id);
        } else {
            shader.set_int("has_texture", GL_FALSE);
        }

        glBindVertexArray(this.VAO);
        glDrawElements(GL_TRIANGLES, this.indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void render_shadow_map(Shader shadow_map_shader, Vector3f position, Quaternionf orientation, float scale) {
        Matrix4f mat_model = new Matrix4f();
        mat_model.scale(scale, scale, scale);
        mat_model.setTranslation(position);
        mat_model.rotate(orientation);

        shadow_map_shader.set_mat4("model", mat_model);

        glBindVertexArray(this.VAO);
        glDrawElements(GL_TRIANGLES, this.indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    private void init() {
        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();
        this.EBO = glGenBuffers();

        glBindVertexArray(this.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, Utils.vertices_to_fb(this.vertices), GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Utils.indices_to_ib(this.indices), GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.bytes, Utils.vertices_to_fb(this.vertices));
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Vertex.bytes, Utils.vertices_to_fb(this.vertices).position(3));
        glVertexAttribPointer(2, 3, GL_FLOAT, false, Vertex.bytes, Utils.vertices_to_fb(this.vertices).position(6));
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

}

