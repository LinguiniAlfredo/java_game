package org.delfino.editor;

import org.delfino.Context;
import org.delfino.cameras.Camera;
import org.delfino.scenes.Scene;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class EditorCamera extends Camera {

    public CameraMode mode;
    public CameraMode prevMode;
    private Editor    editor;
    public  Vector3f  ray;

    public EditorCamera(Scene scene, Editor editor, Vector3f position) {
        super(scene, position);
        this.editor = editor;
        set_mode(CameraMode.SELECT);
    }
    public EditorCamera(Scene scene, Editor editor, Vector3f position, Vector3f front) {
        super(scene, position, front);
        this.editor = editor;
        set_mode(CameraMode.SELECT);
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        if (this.mode == CameraMode.FLY) {
            glfwSetInputMode(Context.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        } else {
            glfwSetInputMode(Context.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            cast_ray();
        }
    }

    @Override
    public void process_keyboard() {
        if (this.mode == CameraMode.FLY) {
            super.process_keyboard();
        }
    }

    @Override
    public void process_mouse_movement(double offset_x, double offset_y, double delta_time) {
        switch (this.mode) {
            case FLY:
                super.process_mouse_movement(offset_x, offset_y, delta_time);
                break;
            case SELECT:
                editor.processMouseMovement(offset_x, offset_y, delta_time);
                break;
            case ORBIT:
        }
    }

    private void cast_ray() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer mouseX = stack.mallocDouble(1);
            DoubleBuffer mouseY = stack.mallocDouble(1);
            glfwGetCursorPos(Context.window, mouseX, mouseY);

            float ndcMouseX = (float) (2.f * mouseX.get(0) / Context.screenWidth - 1.f);
            float ndcMouseY = (float) (1.f - 2.f * mouseY.get(0) / Context.screenHeight);
            this.ray = getRayFromMouse(ndcMouseX, ndcMouseY);
        }
    }


    private Vector3f getRayFromMouse(float ndcMouseX, float ndcMouseY) {
        Vector4f rayClip = new Vector4f(ndcMouseX, ndcMouseY, -1, 0);
        Vector4f rayEye  = rayClip.mul(Context.activeCamera.getPerspectiveMatrix().invert());
        rayEye.z = -1.f;
        rayEye.w =  0.f;
        Vector4f tmp = rayEye.mul(Context.activeCamera.getViewMatrix().invert());
        return new Vector3f(tmp.x, tmp.y, tmp.z).normalize();
    }

    public void set_mode(CameraMode mode) {
        this.prevMode = this.mode;
        this.mode = mode;
    }
}
