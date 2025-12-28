package org.delfino.editor;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Editor {
    public EditorCamera camera;
    public Gizmo        gizmo;
    public Gridlines    gridlines;
    public Entity       selected_object;

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
        if (this.gizmo != null) {
            this.gizmo.translate_gizmo.check_hovered(this.camera.ray);
        }
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

    public void select() {
        // check gizmo intersection first, if not, then check objects
        if (this.gizmo != null) {
            if (this.gizmo.translate_gizmo.hovered_axis != null) {
                this.gizmo.translate_gizmo.selected_axis = this.gizmo.translate_gizmo.hovered_axis;
            }
            return;
        }

        ArrayList<Entity> intersecting_objects = new ArrayList<>();
        for (Entity object : Context.current_scene.world_blocks) {
            object.selected = false;
            if (object.collision.intersects(this.camera.ray)) {
                intersecting_objects.add(object);
            }
        }
        float min_dist = Float.MAX_VALUE;
        for (Entity object : intersecting_objects) {
            float dist = object.position.distance(Context.camera.position);
            if (dist < min_dist) {
                min_dist = dist;
            }
        }
        for (Entity object : intersecting_objects) {
            float dist = object.position.distance(Context.camera.position);
            if (dist == min_dist) {
                set_selected_object(object);
            }
        }
    }

    public void release_gizmo() {
        if (this.gizmo.translate_gizmo.selected_axis != null) {
            this.gizmo.translate_gizmo.selected_axis = null;
        }
    }
}
