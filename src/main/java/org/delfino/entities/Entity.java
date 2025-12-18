package org.delfino.entities;

import org.delfino.utils.Collision;
import org.delfino.utils.Shader;
import org.delfino.utils.Vertex;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Entity {
    public Model       model;
    public Shader      shader;
    public Collision   collision;
    public Vector3f    position;
    public Vector3f    target_position;
    public Quaternionf orientation;
    public Quaternionf target_orientation;
    public float       scale;

    public Entity(String filename, Vector3f position, Quaternionf orientation, float scale, String texture) {
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
        this.shader.delete();
        this.model.delete();
        this.collision.delete();
    }

    public void update(double delta_time) {

    }

    public void render() {
        this.model.render(this.shader, this.position, this.orientation, this.scale);
    }

    public void render_collider() {
        this.collision.render();
    }

    public void render_shadow_map(Shader shadow_map_shader) {
        this.model.render_shadow_map(shadow_map_shader, this.position, this.orientation, this.scale);
    }

    private Vector3f get_dimensions() {
        float min_x = 0.f, max_x = 0.f;
        float min_y = 0.f, max_y = 0.f;
        float min_z = 0.f, max_z = 0.f;

        for (int i = 0; i < this.model.meshes.size(); i++) {
            Mesh mesh = this.model.meshes.get(i);
            for (int j = 0; j < mesh.vertices.size(); j++) {
                Vertex vertex = mesh.vertices.get(i);
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
}
