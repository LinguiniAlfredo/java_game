package org.delfino.renderer;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.entities.EntityType;
import org.delfino.utils.Shader;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Shadowmap {
    public final int DEPTH_MAP_WIDTH  = 2048;
    public final int DEPTH_MAP_HEIGHT = 2048;

    public int depthMap;
    public int FBO;
    public float nearPlane, farPlane;
    public Matrix4f lightSpaceMatrix = new Matrix4f();
    public Shader shader, depthQuadShader;
    public Matrix4f matProj = new Matrix4f();
    public Matrix4f matView = new Matrix4f();
    public FloatBuffer quadVertexBuffer;

    public Shadowmap() {
        this.nearPlane = 0.5f;
        this.farPlane = 100.f;
        shader = new Shader("shaders/depth.vert", "shaders/depth.frag");
        depthQuadShader = new Shader("shaders/depth_quad.vert", "shaders/depth_quad.frag");

        float[] quadVertices = {
                // positions        // texture Coords
                -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
        };
        this.quadVertexBuffer = Utils.floatArrToFb(quadVertices);

        init();
    }

    public void delete() {
        this.shader.delete();
        this.depthQuadShader.delete();
        glDeleteBuffers(this.FBO);
        glDeleteTextures(this.depthMap);
    }

    public void doPass() {
        matProj.identity()
                .ortho(-20.f, 20.f, -20.f, 20.f, this.nearPlane, this.farPlane);

        // TODO - Remove heap allocation if gc causes stuttering, currently the only heap alloc in the loop
//        mat_view.identity()
//                .lookAlong(0.f, 0.f, 0.f, 0.f, 0.1f, 0.f)
//                .translate(-Context.current_scene.light_cube.position.x, -Context.current_scene.light_cube.position.y, -Context.current_scene.light_cube.position.z);
        matView = new Matrix4f().lookAt(Context.currentScene.lightCube.position, new Vector3f(0.f), new Vector3f(0.f, 1.f, 0.f));

        this.lightSpaceMatrix.set(matProj.mul(matView));

        this.shader.use();
        this.shader.setMat4("light_space_matrix", this.lightSpaceMatrix);

        glViewport(0, 0, DEPTH_MAP_WIDTH, DEPTH_MAP_HEIGHT);
        glBindFramebuffer(GL_FRAMEBUFFER, this.FBO);
        glClear(GL_DEPTH_BUFFER_BIT);

        renderShadowMap();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Context.screenWidth, Context.screenHeight);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void init() {
        this.FBO = glGenFramebuffers();
        this.depthMap = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.depthMap);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, DEPTH_MAP_WIDTH, DEPTH_MAP_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        float[] border_color = {1.0f, 1.0f, 1.0f, 1.0f};
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, border_color);

        glBindFramebuffer(GL_FRAMEBUFFER, this.FBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthMap, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void renderDepthQuad() {
        glViewport(0, 0, Context.screenWidth /4, Context.screenHeight /4);
        this.depthQuadShader.use();
        this.depthQuadShader.set_float("near_plane", this.nearPlane);
        this.depthQuadShader.set_float("far_plane", this.farPlane);
        this.depthQuadShader.setInt("depth_map", 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.depthMap);

        int quad_vao;
        int quad_vbo;

        quad_vao = glGenVertexArrays();
        quad_vbo = glGenBuffers();
        glBindVertexArray(quad_vao);
        glBindBuffer(GL_ARRAY_BUFFER, quad_vbo);
        glBufferData(GL_ARRAY_BUFFER, this.quadVertexBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, Float.BYTES * 3);

        glBindVertexArray(quad_vao);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
        glViewport(0, 0, Context.screenWidth, Context.screenHeight);
    }

    public void renderShadowMap() {
        for (Entity entity : Context.currentScene.entities) {
            if (entity.type != EntityType.CAMERA) {
                entity.renderShadowMap(this.shader);
            }
        }
    }
}
