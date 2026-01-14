package org.delfino.scenes.dtos;

import org.delfino.cameras.Camera;
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
    public Vector3f    front;

    public EntityDTO(Entity entity, int id) {
        this.id = id;
        this.type = entity.type.toString().toLowerCase();
        this.position = entity.position;
        this.orientation = entity.orientation;
        this.scale = entity.scale;

        if (entity.type == EntityType.CAMERA) {
            Camera cam = (Camera) entity;
            this.front = cam.front;
        }
    }
}
