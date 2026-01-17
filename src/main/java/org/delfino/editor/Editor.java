package org.delfino.editor;

import imgui.ImGui;
import org.delfino.Context;
import org.delfino.cameras.StaticCamera;
import org.delfino.editor.menus.ObjectListMenu;
import org.delfino.editor.menus.PropertiesMenu;
import org.delfino.entities.Entity;
import org.delfino.entities.EntityType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Editor {
    public EditorCamera   camera;
    public Gizmo          gizmo;
    public Gridlines      gridlines;
    public Entity         selectedObject;
    public PropertiesMenu propertiesMenu;
    public ObjectListMenu objectListMenu;
    public Compass        compass;

    public Editor() {
        Vector3f p            = new Vector3f(40.f, 20.f, 0.f);
        Vector3f f            = new Vector3f(0.f, 0.f, 0.f).sub(p).normalize();
        this.camera           = new EditorCamera(Context.currentScene, this, p, f);
        this.gridlines        = new Gridlines();
        this.objectListMenu   = new ObjectListMenu(this, Context.currentScene.entities);
        this.compass          = new Compass(this);

        Context.activeCamera = this.camera;
    }

    public void delete() {
        this.gridlines.delete();
        if (this.gizmo != null) {
            this.gizmo.delete();
        }
        if (this.compass != null) {
            this.compass.delete();
        }
    }

    public void update(double delta_time) {
        this.camera.update(delta_time);
        if (this.gizmo != null) {
            this.gizmo.check_hovered(this.camera.ray);
        }
    }

    public void render() {
        Context.guiGlfw.newFrame();
        Context.guiGl3.newFrame();
        ImGui.newFrame();

        this.objectListMenu.render();
        if (this.selectedObject != null) {
            this.propertiesMenu.render();
            if (this.selectedObject.type == EntityType.CAMERA) {
                StaticCamera cam = (StaticCamera) this.selectedObject;
                cam.render_viewport();
            }
        }
        this.compass.render();

        renderGizmo();
//        this.gridlines.render();

        ImGui.render();
        Context.guiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void renderGizmo() {
        if (this.selectedObject != null) {
            if (this.gizmo == null) {
                this.gizmo = new TranslateGizmo(this, selectedObject.position);
            }
            this.gizmo.render();
            if (Context.showCollisions) {
                this.gizmo.render_collisions();
            }
        }
    }

    public void setSelectedObject(Entity object) {
        deselectObject();

        object.selected = true;
        this.selectedObject = object;
        if (this.gizmo != null) {
            this.gizmo.delete();
            this.gizmo = null;
        }
        this.propertiesMenu = new PropertiesMenu(selectedObject);
    }

    private void deselectObject() {
        if (this.selectedObject != null) {
            this.selectedObject.selected = false;
            this.selectedObject = null;
        }
        if (this.gizmo != null) {
            this.gizmo.delete();
            this.gizmo = null;
        }
        this.propertiesMenu = null;
    }

    public void select() {
        // check gizmo intersection first, and return early if true
        if (this.gizmo != null) {
            if (this.gizmo.hoveredAxis != null) {
                this.gizmo.selectedAxis = this.gizmo.hoveredAxis;
                return;
            }
        }

        ArrayList<Entity> intersecting_objects = new ArrayList<>();
        for (Entity object : Context.currentScene.entities) {
            object.selected = false;
            if (object.collision.intersects(this.camera.ray)) {
                intersecting_objects.add(object);
            }
        }
        if (intersecting_objects.isEmpty()) {
            deselectObject();
        }

        float min_dist = Float.MAX_VALUE;
        for (Entity object : intersecting_objects) {
            float dist = object.position.distance(Context.activeCamera.position);
            if (dist < min_dist) {
                min_dist = dist;
            }
        }
        for (Entity object : intersecting_objects) {
            float dist = object.position.distance(Context.activeCamera.position);
            if (dist == min_dist) {
                setSelectedObject(object);
            }
        }
    }

    public void releaseGizmo() {
        if (this.gizmo != null) {
            if (this.gizmo.selectedAxis != null) {
                this.gizmo.selectedAxis = null;
            }
        }
    }

    public void processMouseMovement(double offset_x, double offset_y, double delta_time) {
        if (gizmo != null) {
            this.gizmo.transform_object(selectedObject, offset_x, offset_y, delta_time);
        }
    }

    public void findObject() {
        if (this.selectedObject != null && this.camera.mode == CameraMode.SELECT) {
            Vector3f objectPosition = new Vector3f(this.selectedObject.position);
            this.camera.position = objectPosition.sub(new Vector3f(10.f, 10.f, -10.f));
            this.camera.lookAt(objectPosition);
        }
    }

    public void deleteObject() {
        if (this.selectedObject != null) {
            Context.currentScene.entities.remove(this.selectedObject);
            deselectObject();
        }
    }

    public void duplicateObject() {
        if (this.selectedObject != null) {
            Context.currentScene.add_entity(
                    this.selectedObject.type,
                    new Vector3f(this.selectedObject.position),
                    new Quaternionf(this.selectedObject.orientation),
                    new Vector3f(this.selectedObject.scale)
            );
        }
    }
}
