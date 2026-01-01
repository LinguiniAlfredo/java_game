package org.delfino.editor.menus;

import imgui.ImGui;
import org.delfino.editor.Editor;
import org.delfino.entities.Entity;

import java.util.ArrayList;

public class ObjectListMenu extends Menu {
    public ArrayList<Entity> entities;
    public Editor editor;
    public boolean selected;

    public ObjectListMenu(Editor editor, ArrayList<Entity> entities) {
        this.editor   = editor;
        this.entities = entities;
    }

    public void render() {
        ImGui.begin("Objects");

        for (Entity entity : entities) {
            if (ImGui.selectable(entity.name, selected)) {
                editor.set_selected_object(entity);
            }
        }

        ImGui.end();
    }
}
