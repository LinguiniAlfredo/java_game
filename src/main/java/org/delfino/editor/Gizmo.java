package org.delfino.editor;

import org.delfino.Context;
import org.delfino.utils.Shader;
import org.joml.Vector3f;

enum Axis {
    X,
    Y,
    Z
}

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

        this.translate_gizmo = new TranslateGizmo(this, position);
//        this.rotate_gizmo    = new RotateGizmo(this, position);
//        this.scale_gizmo     = new ScaleGizmo(position);
    }

    public void delete() {
        this.translate_gizmo.delete();
        this.shader.delete();
    }

    public void render() {
        this.translate_gizmo.render();
//        this.rotate_gizmo.render();

        if (Context.show_collisions) {
            this.translate_gizmo.render_collisions();
//            this.rotate_gizmo.render_collisions();
        }
    }
}
