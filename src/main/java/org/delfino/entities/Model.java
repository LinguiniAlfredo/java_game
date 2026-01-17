package org.delfino.entities;

import org.apache.commons.io.FilenameUtils;
import org.delfino.cameras.Camera;
import org.delfino.utils.Shader;
import org.delfino.utils.Texture;
import org.delfino.utils.Vertex;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.stb.STBImage;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import static org.lwjgl.opengl.GL30.*;

public class Model {
    ArrayList<Mesh>    meshes = new ArrayList<>();
    ArrayList<Texture> texturesLoaded = new ArrayList<>();
    String modelPath;
    String texturePath;

    public Model(String modelPath, String texturePath) {
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        if (modelPath != "") {
            loadModel();
        }
    }

    public void delete() {
        for (Mesh mesh : this.meshes) {
            glDeleteVertexArrays(mesh.VAO);
            glDeleteBuffers(mesh.VBO);
            glDeleteBuffers(mesh.EBO);
            for (Texture texture : mesh.textures) {
                glDeleteTextures(texture.id);
            }
        }
    }

    public void render(Camera cam, Shader shader, Vector3f position, Quaternionf orientation, Vector3f scale, boolean selected) {
        for (Mesh mesh : this.meshes) {
            mesh.render(cam, shader, position, orientation, scale, selected);
        }
    }

    public void renderShadowMap(Shader shadow_map_shader, Vector3f position, Quaternionf orientation, Vector3f scale) {
        for (Mesh mesh : this.meshes) {
            mesh.renderShadowMap(shadow_map_shader, position, orientation, scale);
        }
    }

    private void loadModel() {
        try (InputStream input_stream = Model.class.getClassLoader().getResourceAsStream(this.modelPath)) {
            Path temp_file = Files.createTempFile("model-", FilenameUtils.getExtension(this.modelPath));
            temp_file.toFile().deleteOnExit();

            try (BufferedOutputStream output_stream = new BufferedOutputStream(Files.newOutputStream(temp_file))) {
                byte[] buffer = new byte[1024];
                int bytes_read;
                while((bytes_read = input_stream.read(buffer)) != -1) {
                    output_stream.write(buffer, 0, bytes_read);
                }
            }

            try (AIScene scene = Assimp.aiImportFile(temp_file.toFile().getAbsolutePath(), Assimp.aiProcess_Triangulate | Assimp.aiProcess_GenSmoothNormals | Assimp.aiProcess_FlipUVs |Assimp.aiProcess_CalcTangentSpace)) {
                if (scene == null || (scene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || scene.mRootNode() == null) {
                    throw new Exception();
                }
                processNode(scene.mRootNode(), scene);
            } catch (Exception e) {
                System.out.println("failed to load model file...");
            }
        } catch (IOException e) {
            System.out.println("failed to find model file...");
        }
    }

    private void processNode(AINode node, AIScene scene) {
        if (node == null) {
            System.out.println("found null node...skipping");
            return;
        }
        for (int i = 0; i < node.mNumMeshes(); i++) {
            if (Objects.requireNonNull(node.mMeshes()).get(i) < scene.mNumMeshes()) {
                try (AIMesh mesh = AIMesh.create(Objects.requireNonNull(scene.mMeshes()).get(Objects.requireNonNull(node.mMeshes()).get(i)))) {
                    this.meshes.add(process_mesh(mesh, scene));
                } catch (Exception e) {
                    System.out.println("found null mesh...skipping");
                    return;
                }
            } else {
                System.out.println("invalid mesh id...");
            }
        }
        if (node.mNumChildren() == 0) {
            return;
        }
        for (int i = 0; i < node.mNumChildren(); i++) {
            processNode(AINode.create(Objects.requireNonNull(node.mChildren()).get(i)), scene);
        }
    }

    private Mesh process_mesh(AIMesh mesh, AIScene scene) {
        ArrayList<Vertex>  vertices = new ArrayList<>();
        ArrayList<Integer> indices  = new ArrayList<>();
        ArrayList<Texture> textures = new ArrayList<>();

        for (int i = 0; i < mesh.mNumVertices(); i++) {
            Vertex   vertex = new Vertex();
            Vector3f vector = new Vector3f();

            vector.x = mesh.mVertices().get(i).x();
            vector.y = mesh.mVertices().get(i).y();
            vector.z = mesh.mVertices().get(i).z();
            vertex.position = new Vector3f(vector);

            if (Objects.requireNonNull(mesh.mNormals()).hasRemaining()) {
                vector.x = Objects.requireNonNull(mesh.mNormals()).get(i).x();
                vector.y = Objects.requireNonNull(mesh.mNormals()).get(i).y();
                vector.z = Objects.requireNonNull(mesh.mNormals()).get(i).z();
                vertex.normal = new Vector3f(vector);
            }

            if (mesh.mTextureCoords(0) != null) {
                AIVector3D.Buffer tc = mesh.mTextureCoords(0);
                if (tc != null) {
                    vertex.texCoords = new Vector2f(tc.get(i).x(), tc.get(i).y());
                }
            } else {
                vertex.texCoords = new Vector2f();
            }

            vertices.add(vertex);
        }

        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace face = mesh.mFaces().get(i);
            for (int j = 0; j < face.mNumIndices(); j++) {
                indices.add(face.mIndices().get(j));
            }
        }

        if (!texturePath.isEmpty()) {
            Texture texture = new Texture();
            texture.id   = textureFromFile(this.texturePath);
            texture.type = "";
            texture.path = this.texturePath;
            textures.add(texture);
        }

        return new Mesh(vertices, indices, textures);
    }

    private int textureFromFile(String texturePath) {
        try (InputStream input_stream = Model.class.getClassLoader().getResourceAsStream(this.texturePath)) {
            Path tempFile = Files.createTempFile("texture-", ".png");
            tempFile.toFile().deleteOnExit();

            try (BufferedOutputStream output_stream = new BufferedOutputStream(Files.newOutputStream(tempFile))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input_stream.read(buffer)) != -1) {
                    output_stream.write(buffer, 0, bytesRead);
                }
            }
            int textureId = glGenTextures();

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

                glBindTexture(GL_TEXTURE_2D, textureId);
                glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, data);
                glGenerateMipmap(GL_TEXTURE_2D);

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

                STBImage.stbi_image_free(data);
            } else {
                System.out.println("error loading texture...");
            }

            return textureId;

        } catch (IOException e) {
            throw new RuntimeException("failed to find texture file...");
        }
    }
}
