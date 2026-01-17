package org.delfino.editor.menus;

import imgui.ImGui;
import imgui.type.ImInt;
import org.delfino.Context;
import org.delfino.editor.Editor;
import org.delfino.entities.Entity;
import org.delfino.entities.EntityType;

import java.util.ArrayList;

public class ObjectListMenu extends Menu {
    public ArrayList<Entity> entities;
    public Editor            editor;
    public boolean           selected;
    public EntityType        addEntityType;
    public ImInt             addEntityIndex = new ImInt(0);

    public ObjectListMenu(Editor editor, ArrayList<Entity> entities) {
        this.editor          = editor;
        this.entities        = entities;
        this.addEntityType   = EntityType.values()[this.addEntityIndex.intValue()];
    }

    public void render() {
        ImGui.begin("Objects");

        if (ImGui.combo("##", this.addEntityIndex, EntityType.toStringArray())) {
            this.addEntityType = EntityType.values()[this.addEntityIndex.intValue()];
        }

        if (ImGui.button("Add Entity")) {
            Context.currentScene.add_entity(this.addEntityType);
        }

        ImGui.newLine();
        for (Entity entity : this.entities) {
            if (ImGui.selectable(entity.name, this.selected)) {
                this.editor.setSelectedObject(entity);
            }
        }

        ImGui.end();
    }
}
