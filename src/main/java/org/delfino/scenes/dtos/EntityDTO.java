package org.delfino.scenes.dtos;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.delfino.entities.Entity;
import org.delfino.entities.EntityType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityDTO {
    public int         id;
    public String      type;
    public Vector3f    position;
    public Quaternionf orientation;
    public Vector3f    scale;

    public EntityDTO() {

    }

    public EntityDTO(JsonObject json_object) {
        Gson gson = new Gson();
        this.id = json_object.get("id").getAsInt();
        this.type = json_object.get("type").getAsString().toLowerCase();

        JsonArray pos_string   = json_object.get("position").getAsJsonArray();
        JsonArray rot_string   = json_object.get("rotation").getAsJsonArray();
        JsonArray scale_string = json_object.get("scale").getAsJsonArray();

        float[] pos_array   = gson.fromJson(pos_string, float[].class);
        float[] rot_array   = gson.fromJson(rot_string, float[].class);
        float[] scale_array = gson.fromJson(scale_string, float[].class);

        this.position = new Vector3f(pos_array[0], pos_array[1], pos_array[2]);
        this.orientation = new Quaternionf(rot_array[0], rot_array[1], rot_array[2], rot_array[3]);
        this.scale    = new Vector3f(scale_array[0], scale_array[1], scale_array[2]);
    }

    public EntityDTO(Entity entity, int id) {
        this.id = id;
        this.type = entity.type.toString().toLowerCase();
        this.position = entity.position;
        this.orientation = entity.orientation;
        this.scale = entity.scale;
    }
}
