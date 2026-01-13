package org.delfino.renderer;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.entities.EntityType;
import org.delfino.utils.Shader;
import org.delfino.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Shadowmap {
    public final int DEPTH_MAP_WIDTH  = 2048;
    public final int DEPTH_MAP_HEIGHT = 2048;

    public int         depth_map;
    public int         FBO;
    public float       near_plane, far_plane;
    public Matrix4f    light_space_matrix = new Matrix4f();
    public Shader      shader, depth_quad_shader;
    public Matrix4f    mat_proj = new Matrix4f();
    public Matrix4f    mat_view = new Matrix4f();
    public FloatBuffer quad_vertex_buffer;

    public Shadowmap() {
        this.near_plane   = 0.5f;
        this.far_plane    = 100.f;
        shader            = new Shader("shaders/depth.vert", "shaders/depth.frag");
        depth_quad_shader = new Shader("shaders/depth_quad.vert", "shaders/depth_quad.frag");

        float[] quad_vertices = {
                // positions        // texture Coords
                -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
        };
        this.quad_vertex_buffer = Utils.float_arr_to_fb(quad_vertices);

        init();
    }

    public void delete() {
        this.shader.delete();
        this.depth_quad_shader.delete();
        glDeleteBuffers(this.FBO);
        glDeleteTextures(this.depth_map);
    }

    public void do_pass() {
        mat_proj.identity()
                .ortho(-20.f, 20.f, -20.f, 20.f, this.near_plane, this.far_plane);

        // TODO - Remove heap allocation if gc causes stuttering, currently the only heap alloc in the loop
//        mat_view.identity()
//                .lookAlong(0.f, 0.f, 0.f, 0.f, 0.1f, 0.f)
//                .translate(-Context.current_scene.light_cube.position.x, -Context.current_scene.light_cube.position.y, -Context.current_scene.light_cube.position.z);
        mat_view = new Matrix4f().lookAt(Context.current_scene.light_cube.position, new Vector3f(0.f), new Vector3f(0.f, 1.f, 0.f));

        this.light_space_matrix.set(mat_proj.mul(mat_view));

        this.shader.use();
        this.shader.set_mat4("light_space_matrix", this.light_space_matrix);

        glViewport(0, 0, DEPTH_MAP_WIDTH, DEPTH_MAP_HEIGHT);
        glBindFramebuffer(GL_FRAMEBUFFER, this.FBO);
        glClear(GL_DEPTH_BUFFER_BIT);

        render_shadow_map();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Context.screen_width, Context.screen_height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void init() {
        this.FBO = glGenFramebuffers();
        this.depth_map = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.depth_map);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, DEPTH_MAP_WIDTH, DEPTH_MAP_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        float[] border_color = {1.0f, 1.0f, 1.0f, 1.0f};
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, border_color);

        glBindFramebuffer(GL_FRAMEBUFFER, this.FBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depth_map, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void render_depth_quad() {
        glViewport(0, 0, Context.screen_width/4, Context.screen_height/4);
        this.depth_quad_shader.use();
        this.depth_quad_shader.set_float("near_plane", this.near_plane);
        this.depth_quad_shader.set_float("far_plane", this.far_plane);
        this.depth_quad_shader.set_int("depth_map", 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.depth_map);

        int quad_vao;
        int quad_vbo;


        quad_vao = glGenVertexArrays();
        quad_vbo = glGenBuffers();
        glBindVertexArray(quad_vao);
        glBindBuffer(GL_ARRAY_BUFFER, quad_vbo);
        glBufferData(GL_ARRAY_BUFFER, this.quad_vertex_buffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, Float.BYTES * 3);

        glBindVertexArray(quad_vao);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
        glViewport(0, 0, Context.screen_width, Context.screen_height);
    }

    public void render_shadow_map() {
        for (Entity entity : Context.current_scene.entities) {
            entity.render_shadow_map(this.shader);
        }
    }
}
