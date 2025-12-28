package org.delfino.editor;

import org.delfino.Context;
import org.delfino.entities.Entity;
import org.delfino.utils.Camera;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

enum CameraMode {
    SELECT,
    FLY
}

public class EditorCamera extends Camera {

    private CameraMode mode;
    private CameraMode prev_mode;
    private Editor     editor;
    public  Vector3f   ray;

    public EditorCamera(Editor editor, Vector3f position) {
        super(position);
        this.editor = editor;
        set_mode(CameraMode.SELECT);
        set_mouse_callbacks();
    }
    public EditorCamera(Editor editor, Vector3f position, Vector3f front) {
        super(position, front);
        this.editor = editor;
        set_mode(CameraMode.SELECT);
        set_mouse_callbacks();
    }

    @Override
    public void update(double delta_time) {
        super.update(delta_time);
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
    public void process_mouse_movement(double mouse_x, double mouse_y, double delta_time) {
        if (this.mode == CameraMode.FLY) {
            super.process_mouse_movement(mouse_x, mouse_y, delta_time);
        }
    }

    private void cast_ray() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer mouse_x = stack.mallocDouble(1);
            DoubleBuffer mouse_y = stack.mallocDouble(1);
            glfwGetCursorPos(Context.window, mouse_x, mouse_y);

            float ndc_mouse_x = (float) (2.f * mouse_x.get(0) / Context.screen_width - 1.f);
            float ndc_mouse_y = (float) (1.f - 2.f * mouse_y.get(0) / Context.screen_height);
            this.ray = get_ray_from_mouse(ndc_mouse_x, ndc_mouse_y);
        }
    }


    private Vector3f get_ray_from_mouse(float ndc_mouse_x, float ndc_mouse_y) {
        Vector4f ray_clip = new Vector4f(ndc_mouse_x, ndc_mouse_y, -1, 0);
        Vector4f ray_eye  = ray_clip.mul(Context.camera.get_perspective_matrix().invert());
        ray_eye.z = -1.f;
        ray_eye.w =  0.f;
        Vector4f tmp = ray_eye.mul(Context.camera.get_view_matrix().invert());
        return new Vector3f(tmp.x, tmp.y, tmp.z).normalize();
    }

    private void set_mode(CameraMode mode) {
        this.prev_mode = this.mode;
        this.mode = mode;
    }

    private void set_mouse_callbacks() {
        GLFW.glfwSetMouseButtonCallback(Context.window, (window, key, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_MOUSE_BUTTON_1 -> editor.select();
                    case GLFW_MOUSE_BUTTON_2 -> set_mode(CameraMode.FLY);
                }
            }
            if (action == GLFW_RELEASE) {
                switch (key) {
                    case GLFW_MOUSE_BUTTON_2 -> {
                        set_mode(CameraMode.SELECT);
                        this.input_vector.set(0);
                    }
                }
            }
        });
    }

}
