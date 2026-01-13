package org.delfino.editor.menus;

import imgui.ImGui;
import org.delfino.cameras.StaticCamera;
import org.delfino.entities.Entity;

public class PropertiesMenu extends Menu {
    public float[] v = new float[1];
    public Entity selected_object;
    public StaticCamera selected_camera;
    public float[] object_rotation;
    public float[] object_scale;

    public PropertiesMenu(Entity selected_object) {
        this.selected_object = selected_object;
        this.object_rotation = new float[] {
        };
        this.object_scale = new float[] {
        };
    }
    public PropertiesMenu(StaticCamera selected_camera) {
        this.selected_camera = selected_camera;
        this.object_rotation = new float[] {};
        this.object_scale    = new float[] {};
    }
    public void render() {
        ImGui.begin("Properties");

        if (this.selected_object != null) {
            ImGui.text(this.selected_object.name);
            ImGui.dragFloat3("Position", new float[] {
                    this.selected_object.position.x,
                    this.selected_object.position.y,
                    this.selected_object.position.z
            });
            ImGui.dragFloat3("Orientation", object_rotation);
            ImGui.dragFloat3("Scale"   , object_scale);

        } else if (this.selected_camera != null) {
            ImGui.text(this.selected_camera.name);
            ImGui.dragFloat3("Position", new float[] {
                    this.selected_object.position.x,
                    this.selected_object.position.y,
                    this.selected_object.position.z
            });
            ImGui.dragFloat3("Orientation", object_rotation);
        }
        ImGui.end();
    }
}
