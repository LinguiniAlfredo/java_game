package org.delfino.editor.menus;

import imgui.ImGui;
import org.delfino.cameras.Camera;
import org.delfino.entities.Entity;
import org.delfino.entities.EntityType;

public class PropertiesMenu extends Menu {
    public float[] v = new float[1];
    public Entity selectedObject;
    public float[] objectRotation;
    public float[] objectScale;

    public PropertiesMenu(Entity selectedObject) {
        this.selectedObject = selectedObject;
        this.objectRotation = new float[] {
        };
        this.objectScale = new float[] {
        };
    }

    public void render() {
        ImGui.begin("Properties");

        if (this.selectedObject != null) {
            ImGui.text(this.selectedObject.name);
            ImGui.dragFloat3("Position", new float[]{
                    this.selectedObject.position.x,
                    this.selectedObject.position.y,
                    this.selectedObject.position.z
            });
            ImGui.dragFloat3("Orientation", objectRotation);
            ImGui.dragFloat3("Scale", objectScale);

            if (this.selectedObject.type == EntityType.CAMERA) {
                Camera cam = (Camera) this.selectedObject;
                ImGui.dragFloat3("Front", new float[] {
                        cam.front.x,
                        cam.front.y,
                        cam.front.z
                });
            }
        }
        ImGui.end();
    }
}
