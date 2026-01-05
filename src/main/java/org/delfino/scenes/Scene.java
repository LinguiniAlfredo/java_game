package org.delfino.scenes;

import com.google.gson.*;
import org.delfino.Context;
import org.delfino.Gamemode;
import org.delfino.entities.*;
import org.delfino.renderer.Shadowmap;
import org.delfino.renderer.Skybox;
import org.delfino.scenes.dtos.EntityDTO;
import org.joml.Vector3f;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Scene {
    public Player            player;
    public Skybox            skybox;
    public LightCube         light_cube;
    public ArrayList<Entity> entities = new ArrayList<>();
    public Shadowmap         shadow_map;
    public String            filename;

    public Scene(String filename) {
        this.filename   = filename;
        this.skybox     = new Skybox();
        this.light_cube = new LightCube(new Vector3f(-25.f, 25.f, -25.f));
        this.shadow_map = new Shadowmap();

//        init_entities();
        glfwSetInputMode(Context.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        load_scene_from_file();
    }

    public void reset_colliders() {
        for (Entity entity : this.entities) {
            entity.collision.reset();
        }
    }

    public void update(double delta_time) {
        if (Context.show_collisions) {
            reset_colliders();
        }
        for (Entity entity : this.entities) {
            entity.update(delta_time);
        }
    }

    public void render() {
        this.shadow_map.do_pass();
        this.light_cube.render();

        if (Context.gamemode == Gamemode.EDIT) {
            this.player.render_collider();
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

        for (Entity entity : this.entities) {
            entity.delete();
        }
        this.entities.clear();
    }

    public void add_entity(EntityType type) {
        switch (type) {
            case CUBE:
                this.entities.add(new Cube(this, new Vector3f()));
                break;
            case SPHERE:
                this.entities.add(new Sphere(this, new Vector3f()));
                break;
            case PLAYER:
                if (this.player == null) {
                    this.entities.add(new Player(this, new Vector3f()));
                }
                break;
        }
    }

    private void init_entities() {
        this.entities.add(this.player);
        for (int i = 1; i < 20; i+=2) {
            for (int j = 1; j < 20; j+=2) {
                float x = (float)i;
                float z = (float)j;
                this.entities.add(new Cube(this, new Vector3f(x, 0.f, z)));
                this.entities.add(new Cube(this, new Vector3f(-x, 0.f, -z)));
                this.entities.add(new Cube(this, new Vector3f(x, 0.f, -z)));
                this.entities.add(new Cube(this, new Vector3f(-x, 0.f, z)));

                if (x == 9) {
                    this.entities.add(new Cube(this, new Vector3f(x, 2.f, z)));
                    this.entities.add(new Cube(this, new Vector3f(-x, 2.f, -z)));
                    this.entities.add(new Cube(this, new Vector3f(-x, 4.f, -z)));
                    this.entities.add(new Cube(this, new Vector3f(-x, 6.f, -z)));
                }
                if (z == 9) {
                    this.entities.add(new Cube(this, new Vector3f(x, 2.f, z)));
                    this.entities.add(new Cube(this, new Vector3f(-x, 2.f, -z)));
                }
            }
        }
        this.entities.add(new Cube(this, new Vector3f(0.f, 4.f, 0.f)));
        this.entities.add(new Cube(this, new Vector3f(-5.f, 2.f, 5.f)));

        this.entities.add(new Cube(this, new Vector3f(-17.f, 2.f, 19.f)));
        this.entities.add(new Cube(this, new Vector3f(-19.f, 2.f, 17.f)));
    }

    private void load_scene_from_file() {
        Gson gson = new Gson();
        try(FileReader reader = new FileReader(this.filename)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonArray()) {
                JsonArray json_array = root.getAsJsonArray();
                for (JsonElement object : json_array) {
                    EntityDTO entity = gson.fromJson(object, EntityDTO.class);
                    switch (entity.type) {
                        case "cube":
                            this.entities.add(new Cube(this, entity.position, entity.orientation, entity.scale));
                            break;
                        case "sphere":
                            this.entities.add(new Sphere(this, entity.position, entity.orientation, entity.scale));
                            break;
                        case "player":
                            if (this.player == null) {
                                this.player = new Player(this, entity.position, entity.orientation, entity.scale);
                                this.entities.add(this.player);
                            }
                            break;
                    }
                }
            }

        } catch(IOException e) {
            System.out.println("failed to read scene file...");
        }
    }

    public void save_scene_to_file() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        JsonArray json_array = new JsonArray();
        String saved_file = this.filename;
        for (int i = 0; i < this.entities.size(); i++) {
            EntityDTO dto = new EntityDTO(this.entities.get(i), i);
            json_array.add(gson.toJsonTree(dto));
        }

        try {
            Path path = Paths.get(saved_file);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Writer writer = Files.newBufferedWriter(path);
            gson.toJson(json_array, writer);
            writer.close();

        } catch (IOException e) {
            System.out.println("failed to save level file...");
        }
    }

}
