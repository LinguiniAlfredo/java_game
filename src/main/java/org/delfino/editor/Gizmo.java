package org.delfino.editor;

import org.delfino.Context;
import org.delfino.utils.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL30.*;


public class Gizmo {
    public float          line_width = 3.f;
    public Shader         shader;
    public Vector3f       position;
    public TranslateGizmo translate_gizmo;
    public RotateGizmo    rotate_gizmo;
    public ScaleGizmo     scale_gizmo;

    public Gizmo(Vector3f position) {
        this.position = position;
        this.shader   = new Shader("shaders/gizmo.vert", "shaders/gizmo.frag");

        this.translate_gizmo = new TranslateGizmo(position);
        this.rotate_gizmo    = new RotateGizmo(position);
        this.scale_gizmo     = new ScaleGizmo(position);
    }

    public void delete() {
        this.translate_gizmo.delete();
        this.shader.delete();
    }

    public void render() {
        Matrix4f mat_model = new Matrix4f().translate(this.position);
        Matrix4f mat_view  = Context.camera.get_view_matrix();
        Matrix4f mat_proj  = Context.camera.get_perspective_matrix();

        this.shader.use();
        this.shader.set_mat4("model", mat_model);
        this.shader.set_mat4("view", mat_view);
        this.shader.set_mat4("projection", mat_proj);

        glClear(GL_DEPTH_BUFFER_BIT);

        glLineWidth(line_width);
        glBindVertexArray(this.translate_gizmo.VAO);
        glDrawArrays(GL_LINES, 0, this.translate_gizmo.num_vertices);
        glBindVertexArray(0);

        glBindVertexArray(this.rotate_gizmo.VAO);
        glDrawArrays(GL_LINE_LOOP, 0, this.rotate_gizmo.resolution);
        glDrawArrays(GL_LINE_LOOP, 100, this.rotate_gizmo.resolution);
        glDrawArrays(GL_LINE_LOOP, 200, this.rotate_gizmo.resolution);
        glBindVertexArray(0);
    }
}
