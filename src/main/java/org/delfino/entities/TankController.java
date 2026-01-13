package org.delfino.entities;

import org.delfino.scenes.Scene;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.delfino.Context.window;
import static org.delfino.entities.EntityType.TANK_CONTROLLER;

// TODO - maybe this needs to be a component to be placed onto an Entity rather than an Entity itself
public class TankController extends Entity {

    public Vector3f inputVector = new Vector3f();
    public float movementSpeed = 10.f;
    public float rotateSpeed = 1.f;
    public Vector3f trajectory = new Vector3f();
    public Vector3f up = new Vector3f(0.f, 1.f, 0.f);

    public TankController(Scene scene, Vector3f position) {
        super (
                scene,
                TANK_CONTROLLER,
                "models/player.fbx",
                position,
                new Quaternionf(0.f, 1.f, 0.f, 0.f),
                new Vector3f(1.f, 1.f, 1.f),
                ""
        );
    }

    public TankController(Scene scene, Vector3f position, Quaternionf orientation, Vector3f scale) {
        super (scene, TANK_CONTROLLER, "models/player.fbx", position, orientation, scale, "");
    }

    @Override
    public void update(double deltaTime) {
        float movementVel = this.movementSpeed * (float) deltaTime;
        float rotateVel = this.rotateSpeed * (float) deltaTime;
        rotateVel *= this.inputVector.x;

        this.orientation.rotateAxis(rotateVel, this.up);
        this.trajectory.set(this.inputVector.rotate(this.orientation));
        this.position.fma(movementVel, this.trajectory);
    }

    public void process_keyboard() {
        this.inputVector.zero();
        this.movementSpeed = 10.f;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            this.inputVector.z = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            this.inputVector.x = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            this.inputVector.z = -1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            this.inputVector.x = 1;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            this.movementSpeed = this.movementSpeed * 2;
        }

        if (this.inputVector.x != 0 || this.inputVector.y != 0 || this.inputVector.z != 0) {
            this.inputVector.normalize();
        }

    }

    public void process_mouse_movement(double xOffset, double yOffset, double deltaTime) {

    }


}
