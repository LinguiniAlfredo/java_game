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
    public EntityType        add_entity_type;
    public ImInt             add_entity_index = new ImInt(0);

    public ObjectListMenu(Editor editor, ArrayList<Entity> entities) {
        this.editor          = editor;
        this.entities        = entities;
        this.add_entity_type = EntityType.values()[add_entity_index.intValue()];
    }

    public void render() {
        ImGui.begin("Entities");

        if (ImGui.combo("##", add_entity_index, EntityType.to_string_array())) {
            this.add_entity_type = EntityType.values()[add_entity_index.intValue()];
        }

        if (ImGui.button("Add Entity")) {
            Context.current_scene.add_entity(this.add_entity_type);
        }

        ImGui.newLine();
        for (Entity entity : entities) {
            if (ImGui.selectable(entity.name, selected)) {
                editor.set_selected_object(entity);
            }
        }

        ImGui.end();
    }
}
