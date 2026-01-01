package org.delfino.editor;

import imgui.ImGui;
import imgui.type.ImString;
import org.delfino.Context;
import org.delfino.entities.Entity;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;

public class Editor {
    public EditorCamera camera;
    public Gizmo        gizmo;
    public Gridlines    gridlines;
    public Entity       selected_object;
    public ImString     text_input = new ImString();
    float[] v = new float[100];

    public Editor() {
        Vector3f p     = new Vector3f(40.f, 20.f, 0.f);
        Vector3f f     = new Vector3f(0.f, 0.f, 0.f).sub(p).normalize();
        this.camera    = new EditorCamera(this, p, f);
        this.gridlines = new Gridlines();
        Context.camera = this.camera;

    }

    public void delete() {
        this.gridlines.delete();
        if (this.gizmo != null) {
            this.gizmo.delete();
        }
    }

    public void update(double delta_time) {
        this.camera.update(delta_time);
        if (this.gizmo != null) {
            this.gizmo.check_hovered(this.camera.ray);
        }
    }

    public void render() {
        render_menu();
        render_gizmo();
//        this.gridlines.render();
    }

    private void render_menu() {
        Context.gui_glfw.newFrame();
        Context.gui_gl3.newFrame();
        ImGui.newFrame();
        ImGui.begin("windwo");

        ImGui.text("my text");
        if (ImGui.button("Save")) {
            System.out.println("saving");
        }
        ImGui.inputText("label", this.text_input);
        ImGui.sliderFloat("float", this.v, 0.f, 100.f);

        ImGui.end();
        ImGui.render();
        Context.gui_gl3.renderDrawData(ImGui.getDrawData());
    }

    private void render_gizmo() {
        if (this.selected_object != null) {
            if (this.gizmo == null) {
                this.gizmo = new TranslateGizmo(this, selected_object.position);
            }
            this.gizmo.render();
            if (Context.show_collisions) {
                this.gizmo.render_collisions();
            }
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
            if (this.gizmo.hovered_axis != null) {
                this.gizmo.selected_axis = this.gizmo.hovered_axis;
                return;
            }
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
        if (this.gizmo != null) {
            if (this.gizmo.selected_axis != null) {
                this.gizmo.selected_axis = null;
            }
        }
    }

    public void process_mouse_movement(double mouse_x, double mouse_y, double delta_time) {
        if (gizmo != null) {
            this.gizmo.transform_object(selected_object, mouse_x, mouse_y, delta_time);
        }
    }

    public void find_object() {
        if (this.selected_object != null && this.camera.mode == CameraMode.SELECT) {
            Vector3f object_position = new Vector3f(this.selected_object.position);
            this.camera.position = object_position.sub(new Vector3f(10.f, 10.f, -10.f));
            this.camera.look_at(object_position);
        }
    }
}
