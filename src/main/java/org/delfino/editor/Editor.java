package org.delfino.editor;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.utils.Camera;
import org.joml.Vector3f;



public class Editor {
    public Camera      camera;
    public Gizmo       gizmo;
    public Gridlines   gridlines;
    public Entity      selected_object;

    public Editor() {
        Vector3f p     = new Vector3f(40.f, 20.f, 0.f);
        Vector3f f     = new Vector3f(0.f, 0.f, 0.f).sub(p).normalize();
        this.camera    = new EditorCamera(this, p, f);
        Context.camera = this.camera;

        this.gridlines = new Gridlines();
    }

    public void delete() {
        this.gridlines.delete();
        this.gizmo.delete();
    }

    public void update(double delta_time) {
        this.camera.update(delta_time);
    }

    public void render() {
//        this.gridlines.render();
        render_gizmo();
    }

    private void render_gizmo() {
        if (this.selected_object != null) {
            if (this.gizmo == null) {
                this.gizmo = new Gizmo(selected_object.position);
            }
            this.gizmo.render();
        }
    }

    public void set_selected_object(Entity object) {
        object.selected = true;
        this.selected_object = object;
        if (this.gizmo != null) {
            this.gizmo.delete();
            this.gizmo = null;
        }
    }
}
