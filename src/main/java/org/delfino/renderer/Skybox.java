package org.delfino.renderer;

import org.delfino.Context;
import org.delfino.entities.Model;
import org.delfino.utils.Shader;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class Skybox {
    public int                 texture_id;
    public Shader              shader;
    public ArrayList<Vector3f> vertices;
    public FloatBuffer         vertex_buffer;
    public int                 VAO, VBO;

    public Skybox() {
        ArrayList<String> faces = new ArrayList<>();
        faces.add("textures/skyboxes/right.jpg");
        faces.add("textures/skyboxes/left.jpg");
        faces.add("textures/skyboxes/top.jpg");
        faces.add("textures/skyboxes/bottom.jpg");
        faces.add("textures/skyboxes/front.jpg");
        faces.add("textures/skyboxes/back.jpg");

        load_texture(faces);
        this.shader        = new Shader("shaders/skybox.vert", "shaders/skybox.frag");
        this.vertices      = get_vertices();
        this.vertex_buffer = Utils.vertices_3f_to_fb(this.vertices);
        init();
    }

    public void delete() {
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
        glDeleteTextures(this.texture_id);
        this.shader.delete();
    }

    public void render() {
        glDepthFunc(GL_LEQUAL);
        this.shader.use();

        Matrix4f mat_view = Context.active_camera.get_view_matrix();
        mat_view.setTranslation(0.f, 0.f, 0.f);
        Matrix4f mat_proj = Context.active_camera.get_perspective_matrix();

        shader.set_mat4("view", mat_view);
        shader.set_mat4("projection", mat_proj);

        glBindVertexArray(this.VAO);
        glBindTexture(GL_TEXTURE_CUBE_MAP, this.texture_id);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glDepthFunc(GL_LESS);
    }

    private void load_texture(ArrayList<String> faces) {
        this.texture_id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, this.texture_id);

        for (int i = 0; i < faces.size(); i++) {
            try (InputStream input_stream = Model.class.getClassLoader().getResourceAsStream(faces.get(i))) {
                Path temp_file = Files.createTempFile("texture-", ".jpg");
                temp_file.toFile().deleteOnExit();

                try (BufferedOutputStream output_stream = new BufferedOutputStream(Files.newOutputStream(temp_file))) {
                    byte[] buffer = new byte[1024];
                    int bytes_read;
                    while ((bytes_read = input_stream.read(buffer)) != -1) {
                        output_stream.write(buffer, 0, bytes_read);
                    }
                }

                IntBuffer width_buffer = BufferUtils.createIntBuffer(1);
                IntBuffer height_buffer = BufferUtils.createIntBuffer(1);
                IntBuffer component_buffer = BufferUtils.createIntBuffer(1);

                ByteBuffer data = STBImage.stbi_load(temp_file.toFile().getAbsolutePath(), width_buffer, height_buffer, component_buffer, 0);

                if (data != null) {
                    int width = width_buffer.get(0);
                    int height = height_buffer.get(0);
                    int components = component_buffer.get(0);

                    int format;
                    if (components == 1) {
                        format = GL_RED;
                    } else if (components == 3) {
                        format = GL_RGB;
                    } else if (components == 4) {
                        format = GL_RGBA;
                    } else {
                        throw new RuntimeException("unsupported number of image components...");
                    }

                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, data);
                    STBImage.stbi_image_free(data);

                    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
                }

            } catch (IOException e) {
                System.out.println("failed to load skybox...");
            }
        }
    }

    private ArrayList<Vector3f> get_vertices() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(-1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f,  1.0f));
        vertices.add(new Vector3f(-1.0f,  1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f, -1.0f));
        vertices.add(new Vector3f(-1.0f, -1.0f,  1.0f));
        vertices.add(new Vector3f( 1.0f, -1.0f,  1.0f));
        return vertices;
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
