package org.delfino.scenes;

import com.google.gson.*;
import org.delfino.Context;
import org.delfino.Gamemode;
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
    public FirstPersonController player;
    public Skybox skybox;
    public LightCube lightCube;
    public Shadowmap shadowMap;
    public String filename;
    public ArrayList<Entity> entities = new ArrayList<>();

    public Scene(String filename) {
        this.filename = filename;
        this.skybox = new Skybox();
        this.lightCube = new LightCube(new Vector3f(-25.f, 25.f, -25.f));
        this.shadowMap = new Shadowmap();

        glfwSetInputMode(Context.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        loadSceneFromFile();
    }

    public void resetColliders() {
        for (Entity entity : this.entities) {
            entity.collision.reset();
        }
    }

    public void update(double delta_time) {
        if (Context.showCollisions) {
            resetColliders();
        }
        for (Entity entity : this.entities) {
            entity.update(delta_time);
        }
    }

    public void render() {
        this.shadowMap.doPass();
        this.lightCube.render();

        for (Entity entity : this.entities) {
            entity.render();
            if (Context.showCollisions || (Context.gamemode == Gamemode.EDIT && entity.type == EntityType.FIRST_PERSON_CONTROLLER)) {
                entity.renderCollider();
            }
        }

        if (Context.showShadowMap) {
            this.shadowMap.renderDepthQuad();
        }

        this.skybox.render(Context.activeCamera);
    }

    public void delete() {
        this.lightCube.delete();
        this.skybox.delete();
        this.shadowMap.delete();

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

    private void loadSceneFromFile() {
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
                        case "first_person_controller":
                            if (this.player == null) {
                                this.player = new FirstPersonController(this, entity.position, entity.orientation, entity.scale);
                                this.entities.add(this.player);
                            }
                            break;
                        case "tank_controller":
//                            if (this.player == null) {
//                                this.player = new TankController(this, entity.position, entity.orientation, entity.scale);
//                                this.entities.add(this.player);
//                            }
//                            break;
                        case "camera":
                            StaticCamera cam = new StaticCamera(this, entity.position, entity.front);
                            this.entities.add(cam);
                            Context.activeCamera = cam;
                            break;
                    }
                }
            }

        } catch(IOException e) {
            System.out.println("failed to read scene file...");
        }
    }

    public void saveSceneToFile() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        JsonArray jsonArray = new JsonArray();
        String savedFile = this.filename;
        for (int i = 0; i < this.entities.size(); i++) {
            EntityDTO dto = new EntityDTO(this.entities.get(i), i);
            jsonArray.add(gson.toJsonTree(dto));
        }

        try {
            Path path = Paths.get(savedFile);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Writer writer = Files.newBufferedWriter(path);
            gson.toJson(jsonArray, writer);
            writer.close();

        } catch (IOException e) {
            System.out.println("failed to save level file...");
        }
    }

}
