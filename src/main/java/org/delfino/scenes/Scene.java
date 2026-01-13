package org.delfino.scenes;

import com.google.gson.*;
import org.delfino.Context;
import org.delfino.cameras.StaticCamera;
import org.delfino.entities.*;
import org.delfino.renderer.Shadowmap;
import org.delfino.renderer.Skybox;
import org.delfino.scenes.dtos.EntityDTO;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Scene {
    public TankController          player;
    public Skybox                  skybox;
    public LightCube               light_cube;
    public Shadowmap               shadow_map;
    public String                  filename;
    public ArrayList<Entity>       entities = new ArrayList<>();

    public Scene(String filename) {
        this.filename   = filename;
        this.skybox     = new Skybox();
        this.light_cube = new LightCube(new Vector3f(-25.f, 25.f, -25.f));
        this.shadow_map = new Shadowmap();

        Vector3f p = new Vector3f(0.f, 20.f, -20.f);
        Vector3f f = new Vector3f(0.f, 0.f, 0.f).sub(p).normalize();
        StaticCamera staticCamera = new StaticCamera(this, p, f);
        this.entities.add(staticCamera);
        Context.active_camera = staticCamera;
        this.player = new TankController(this, new Vector3f(0.f, 1.f, 10.f));
        this.entities.add(this.player);

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

    public void add_entity(EntityType type, Vector3f position, Quaternionf orientation, Vector3f scale) {
        switch (type) {
            case CUBE:
                this.entities.add(new Cube(this, position, orientation, scale));
                break;
            case SPHERE:
                this.entities.add(new Sphere(this, position, orientation, scale));
                break;
            case FIRST_PERSON_CONTROLLER:
                if (this.player == null) {
                    this.entities.add(new FirstPersonController(this, position, orientation, scale));
                }
                break;
            case TANK_CONTROLLER:
                if (this.player == null) {
                    this.entities.add(new TankController(this, position, orientation, scale));
                }
                break;
            case CAMERA:
                this.entities.add(new StaticCamera(this, position, new Vector3f(0.f, 0.f, 1.f)));
                break;
        }
    }

    public void add_entity(EntityType type) {
        this.add_entity(type, new Vector3f(), new Quaternionf(), new Vector3f(1.f));
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
                                this.player = new TankController(this, entity.position, entity.orientation, entity.scale);
                                this.entities.add(this.player);
                            }
                            break;
                        case "camera":
                            this.entities.add(new StaticCamera(this, entity.position, entity.front));
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
