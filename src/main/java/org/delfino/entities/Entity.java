package org.delfino.entities;

import org.apache.commons.io.FilenameUtils;
import org.delfino.Context;
import org.delfino.Gamemode;
import org.delfino.cameras.Camera;
import org.delfino.cameras.StaticCamera;
import org.delfino.scenes.Scene;
import org.delfino.utils.Collision;
import org.delfino.utils.Shader;
import org.delfino.utils.Vertex;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Entity {
    public Model       model;
    public Shader      shader;
    public Collision   collision;
    public Vector3f    position;
    public Vector3f    target_position;
    public Quaternionf orientation;
    public Quaternionf target_orientation;
    public Vector3f    scale;
    public boolean     selected = false;
    public String      name;
    public Scene       scene;
    public EntityType  type;

    public Entity() {

    }

    // This function is for the camera class's constructor
    // It simply creates a dummy entity of type CAMERA for integration into editor
    // All functionality happens in the parent camera class
    public Entity(Scene scene, EntityType type, Vector3f position) {
        this.scene = scene;
        this.type  = type;
        this.name  = get_entity_name();
        this.position = position;
    }

    public Entity(Scene scene, EntityType type, String filename, Vector3f position, Quaternionf orientation, Vector3f scale, String texture) {
        this.scene              = scene;
        this.type               = type;
        this.name               = get_entity_name();
        this.position           = position;
        this.target_position    = position;
        this.orientation        = orientation;
        this.target_orientation = orientation;
        this.scale              = scale;
        this.model              = new Model(filename, texture);
        this.shader             = new Shader("shaders/lighting.vert", "shaders/lighting.frag");

        Vector3f dimensions = get_dimensions();
        this.collision      = new Collision(position, dimensions.x, dimensions.y, dimensions.z);
    }

    public void delete() {
        if (this.shader != null) this.shader.delete();
        if (this.model != null) this.model.delete();
        if (this.collision != null) this.collision.delete();
    }

    public void update(double delta_time) {

    }

    public void render() {
        if (this.model != null) {
            this.model.render(Context.active_camera, this.shader, this.position, this.orientation, this.scale, this.selected);
        }
    }
    public void render(Camera cam) {
        if (this.model != null) {
            this.model.render(cam, this.shader, this.position, this.orientation, this.scale, this.selected);
        }
    }

    public void render_collider() {
        if (this.collision != null) {
            this.collision.render();
        }
    }

    public void render_shadow_map(Shader shadow_map_shader) {
        if (this.model != null) {
            this.model.render_shadow_map(shadow_map_shader, this.position, this.orientation, this.scale);
        }
    }


    private Vector3f get_dimensions() {
        float min_x = 0.f, max_x = 0.f;
        float min_y = 0.f, max_y = 0.f;
        float min_z = 0.f, max_z = 0.f;

        for (int i = 0; i < this.model.meshes.size(); i++) {
            Mesh mesh = this.model.meshes.get(i);
            for (int j = 0; j < mesh.vertices.size(); j++) {
                Vertex vertex = mesh.vertices.get(j);
                if (vertex.position.x < min_x) {
                    min_x = vertex.position.x;
                }
                if (vertex.position.x < min_y) {
                    min_y = vertex.position.y;
                }
                if (vertex.position.z < min_z) {
                    min_z = vertex.position.z;
                }
                if (vertex.position.x > max_x) {
                    max_x = vertex.position.x;
                }
                if (vertex.position.y > max_y) {
                    max_y = vertex.position.y;
                }
                if (vertex.position.z > max_z) {
                    max_z = vertex.position.z;
                }
            }
        }
        return new Vector3f(max_x - min_x, max_y - min_y, max_z - min_z);
    }

    private String get_entity_name() {
        String     name = this.type.toString();
        EntityType type = EntityType.valueOf(name.toUpperCase());

        int num_of_type = scene.entities.stream().filter(e -> e.type == type).toList().size();
        return name + "_" + num_of_type;
    }

}
