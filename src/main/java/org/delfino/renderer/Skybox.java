package org.delfino.renderer;

import org.delfino.cameras.Camera;
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
    public int textureId;
    public Shader shader;
    public ArrayList<Vector3f> vertices;
    public FloatBuffer vertexBuffer;
    public int VAO, VBO;

    public Skybox() {
        ArrayList<String> faces = new ArrayList<>();
        faces.add("textures/skyboxes/right.jpg");
        faces.add("textures/skyboxes/left.jpg");
        faces.add("textures/skyboxes/top.jpg");
        faces.add("textures/skyboxes/bottom.jpg");
        faces.add("textures/skyboxes/front.jpg");
        faces.add("textures/skyboxes/back.jpg");

        loadTexture(faces);
        this.shader = new Shader("shaders/skybox.vert", "shaders/skybox.frag");
        this.vertices = getVertices();
        this.vertexBuffer = Utils.vertices3FToFb(this.vertices);
        init();
    }

    public void delete() {
        glDeleteVertexArrays(this.VAO);
        glDeleteBuffers(this.VBO);
        glDeleteTextures(this.textureId);
        this.shader.delete();
    }

    public void render(Camera cam) {
        glDepthFunc(GL_LEQUAL);
        this.shader.use();

        Matrix4f matView = cam.getViewMatrix();
        matView.setTranslation(0.f, 0.f, 0.f);
        Matrix4f matProj = cam.getPerspectiveMatrix();

        shader.setMat4("view", matView);
        shader.setMat4("projection", matProj);

        glBindVertexArray(this.VAO);
        glBindTexture(GL_TEXTURE_CUBE_MAP, this.textureId);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glDepthFunc(GL_LESS);
    }

    private void loadTexture(ArrayList<String> faces) {
        this.textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, this.textureId);

        for (int i = 0; i < faces.size(); i++) {
            try (InputStream input_stream = Model.class.getClassLoader().getResourceAsStream(faces.get(i))) {
                Path tempFile = Files.createTempFile("texture-", ".jpg");
                tempFile.toFile().deleteOnExit();

                try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(tempFile))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = input_stream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
                IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
                IntBuffer componentBuffer = BufferUtils.createIntBuffer(1);

                ByteBuffer data = STBImage.stbi_load(tempFile.toFile().getAbsolutePath(), widthBuffer, heightBuffer, componentBuffer, 0);

                if (data != null) {
                    int width = widthBuffer.get(0);
                    int height = heightBuffer.get(0);
                    int components = componentBuffer.get(0);

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

    private ArrayList<Vector3f> getVertices() {
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
        glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 3, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

}
