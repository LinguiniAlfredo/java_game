package org.delfino.scenes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.delfino.Context;
import org.delfino.entities.Cube;
import org.delfino.entities.Entity;
import org.delfino.entities.LightCube;
import org.delfino.renderer.Shadowmap;
import org.delfino.renderer.Skybox;
import org.delfino.utils.Camera;
import org.delfino.utils.PlayerControllerFPS;
import org.joml.Vector3f;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class Scene {
    public Camera            camera;
    public Skybox            skybox;
    public LightCube         light_cube;
    public ArrayList<Entity> world_blocks = new ArrayList<>();
    public ArrayList<Entity> entities = new ArrayList<>();
    public Shadowmap         shadow_map;

    public Scene(String filename) {
        this.camera     = new PlayerControllerFPS(new Vector3f(0.f, 10.f, 20.f));
        this.skybox     = new Skybox();
        this.light_cube = new LightCube(new Vector3f(-25.f, 25.f, -25.f));
        this.shadow_map = new Shadowmap();

        init_entities();
//        load_scene_from_file(filename);
    }

    public void reset_colliders() {
        for (Entity world_block : this.world_blocks) {
            world_block.collision.reset();
        }
        for (Entity entity : this.entities) {
            entity.collision.reset();
        }
    }

    public void update(double delta_time) {
        if (Context.show_collisions) {
            reset_colliders();
        }
        this.camera.update(delta_time);
        for (Entity entity : this.entities) {
            entity.update(delta_time);
        }
    }

    public void render() {
        this.shadow_map.do_pass();
        this.light_cube.render();

        for (Entity world_block : this.world_blocks) {
            world_block.render();
            if (Context.show_collisions) {
                world_block.render_collider();
            }
        }

        for (Entity entity : this.entities) {
            entity.render();
            if (Context.show_collisions) {
                entity.render_collider();
            }
        }

        if (Context.show_shadow_map) {
            this.shadow_map.render_depth_quad();
        }

        this.skybox.render();
    }

    public void delete() {
        this.light_cube.delete();
        this.skybox.delete();
        this.shadow_map.delete();
        for (Entity world_block : this.world_blocks) {
            world_block.delete();
        }
        for (Entity entity : this.entities) {
            entity.delete();
        }
    }

    private void init_entities() {
        for (int i = 1; i < 20; i+=2) {
            for (int j = 1; j < 20; j+=2) {
                float x = (float)i;
                float z = (float)j;
                this.world_blocks.add(new Cube(new Vector3f(x, 0.f, z)));
                this.world_blocks.add(new Cube(new Vector3f(-x, 0.f, -z)));
                this.world_blocks.add(new Cube(new Vector3f(x, 0.f, -z)));
                this.world_blocks.add(new Cube(new Vector3f(-x, 0.f, z)));

                if (x == 9) {
                    this.world_blocks.add(new Cube(new Vector3f(x, 2.f, z)));
                    this.world_blocks.add(new Cube(new Vector3f(-x, 2.f, -z)));
                    this.world_blocks.add(new Cube(new Vector3f(-x, 4.f, -z)));
                    this.world_blocks.add(new Cube(new Vector3f(-x, 6.f, -z)));
                }
                if (z == 9) {
                    this.world_blocks.add(new Cube(new Vector3f(x, 2.f, z)));
                    this.world_blocks.add(new Cube(new Vector3f(-x, 2.f, -z)));
                }
            }
        }
        this.world_blocks.add(new Cube(new Vector3f(0.f, 4.f, 0.f)));
        this.world_blocks.add(new Cube(new Vector3f(-5.f, 2.f, 5.f)));

        this.world_blocks.add(new Cube(new Vector3f(-17.f, 2.f, 19.f)));
        this.world_blocks.add(new Cube(new Vector3f(-19.f, 2.f, 17.f)));
    }

    private void load_scene_from_file(String filename) {
        String path = String.valueOf(Scene.class.getResource("/" + filename));
        try (Reader reader = new FileReader(path)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonArray()) {
                JsonArray json_array = root.getAsJsonArray();
//                JsonObject obj = root.getAsJsonObject();
//                String type = obj.get("type").getAsString();
//                switch (type) {
//                    case "cube":
//                        this.world_blocks.add(new Cube(position, rotation, scale));
//                }
            }

        } catch(IOException e) {
            System.out.println("failed to read scene file...");
        }
    }
}
