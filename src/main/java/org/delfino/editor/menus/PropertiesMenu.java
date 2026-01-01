package org.delfino.editor.menus;

import imgui.ImGui;
import org.delfino.entities.Entity;

public class PropertiesMenu extends Menu {
    public float[] v = new float[1];
    public Entity selected_object;
    public float[] object_rotation;
    public float[] object_scale;

    public PropertiesMenu(Entity selected_object) {
        this.selected_object = selected_object;
        this.object_rotation = new float[] {
        };
        this.object_scale = new float[] {
        };
    }

    public void render() {
        ImGui.begin("Properties");

        ImGui.text(this.selected_object.name);
        ImGui.dragFloat3("Position", new float[] {
                this.selected_object.position.x,
                this.selected_object.position.y,
                this.selected_object.position.z
        });
        ImGui.dragFloat3("Rotation", object_rotation);
        ImGui.dragFloat3("Scale"   , object_scale);
        ImGui.end();
    }
}
