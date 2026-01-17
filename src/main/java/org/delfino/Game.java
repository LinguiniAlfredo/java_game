package org.delfino;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import org.apache.commons.lang3.SystemUtils;
import org.delfino.editor.*;
import org.delfino.scenes.Scene;
import org.delfino.ui.UI;

import static org.delfino.Gamemode.*;

import org.delfino.utils.Timer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.delfino.editor.CameraMode.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    double[] mouseX = new double[1];
    double[] mouseY = new double[1];
    boolean firstFrame = true;
    public double prevMouseX = 0;
    public double prevMouseY = 0;

    public void run() {
        initContext();
        initGlfw();

        Context.ui = new UI();
        Context.currentScene = new Scene("./example_level.json");

        gameLoop();
        closeApp();
    }

    private void initContext() {
        Context.gamemode        = GAME;
        Context.screenWidth = 1920;
        Context.screenHeight = 1080;
        Context.ticksPerFrame = 1000.f / 144.0f;
        Context.wireframe       = false;
        Context.showShadowMap = false;
        Context.showCollisions = false;
    }
//
    private void closeApp() {
        Context.ui.delete();
        Context.currentScene.delete();
        if (Context.editor != null) {
            Context.editor.delete();
        }

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(Context.window);
        glfwDestroyWindow(Context.window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void toggle_wireframe() {
        Context.wireframe = !Context.wireframe;
        if (Context.wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    private void toggle_shadow_map() {
        Context.showShadowMap = !Context.showShadowMap;
    }

    private void toggle_collision_render() {
        Context.showCollisions = !Context.showCollisions;
    }

    private void toggle_paused() {
        if (Context.gamemode != EDIT) {
            if (Context.gamemode == PAUSED) {
                Context.gamemode = GAME;
            } else {
                Context.gamemode = PAUSED;
            }
        }
    }

    private void toggle_editor() {
        if (Context.gamemode != PAUSED) {
            if (Context.gamemode != EDIT) {
                Context.gamemode = EDIT;
                if (Context.editor != null) {
                    Context.editor.delete();
                }
                Context.editor = new Editor();
            } else {
                Context.gamemode = GAME;
                if (Context.currentScene != null) {
                    Context.currentScene.delete();
                }
                Context.currentScene = new Scene("./example_level.json");
            }
        }
    }

    private void handle_events(double delta_time) {
        glfwPollEvents();
        if (!ImGui.getIO().getWantCaptureMouse()) {
            double x_offset, y_offset;
            glfwGetCursorPos(Context.window, mouseX, mouseY);
            if (firstFrame) {
                prevMouseX = mouseX[0];
                prevMouseY = mouseY[0];
                firstFrame = false;
            }
            x_offset = mouseX[0] - prevMouseX;
            y_offset = -(mouseY[0] - prevMouseY);
            prevMouseX = mouseX[0];
            prevMouseY = mouseY[0];

            if (Context.gamemode != PAUSED) {
//                Context.current_scene.player.process_mouse_movement(x_offset, y_offset, delta_time);
                Context.activeCamera.process_mouse_movement(x_offset, y_offset, delta_time);
            }
        }
        if (!ImGui.getIO().getWantCaptureKeyboard()) {
//            Context.current_scene.player.process_keyboard();
            Context.activeCamera.process_keyboard();
        }
    }

    private void update(double delta_time) {
        Context.currentScene.update(delta_time);
    }

    private void render() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Context.currentScene.render();
        Context.ui.render();

        if (Context.gamemode == EDIT) {
            Context.editor.render();
        }

        glfwSwapBuffers(Context.window);
    }

    private void initGlfw() {
        // Wayland is not fully supported in GLFW
        // this will force using X11 on wayland (XWayland)
        if (SystemUtils.IS_OS_LINUX) {
            glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
        }

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        Context.window = glfwCreateWindow(Context.screenWidth, Context.screenHeight, "", NULL, NULL);
        assert Context.window != NULL;

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(Context.window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    Context.window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
        } // the stack frame is popped automatically

        // Make the OpenGL Context current
        glfwMakeContextCurrent(Context.window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(Context.window);

        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, Context.screenWidth, Context.screenHeight);

        init_imgui();

        set_key_callbacks();
        set_mouse_callbacks();
    }

    private void init_imgui() {
        ImGui.createContext();
        Context.guiGlfw = new ImGuiImplGlfw();
        Context.guiGl3 = new ImGuiImplGl3();
        Context.guiGlfw.init(Context.window, true);
        Context.guiGl3.init("#version 330");
    }

    private void set_key_callbacks() {
        GLFW.glfwSetKeyCallback(Context.window, (window, key, scancode, action, mods) -> {
            if (!ImGui.getIO().getWantCaptureKeyboard()) {
                if (action == GLFW.GLFW_PRESS) {
                    if (Context.gamemode == GAME || Context.gamemode == EDIT) {
                        switch (key) {
                            case GLFW.GLFW_KEY_TAB -> toggle_paused();
                            case GLFW.GLFW_KEY_F1 -> toggle_wireframe();
                            case GLFW.GLFW_KEY_F2 -> toggle_shadow_map();
                            case GLFW.GLFW_KEY_F3 -> toggle_collision_render();
                            case GLFW.GLFW_KEY_F5 -> toggle_editor();
                            case GLFW.GLFW_KEY_ESCAPE -> Context.gamemode = QUIT;
                        }
                    }
                    if (Context.gamemode == EDIT) {
                        switch (key) {
                            case GLFW_KEY_1 -> {
                                if (Context.editor.gizmo != null) {
                                    Context.editor.gizmo.delete();
                                    Context.editor.gizmo = new TranslateGizmo(Context.editor, Context.editor.selectedObject.position);
                                }
                            }
                            case GLFW_KEY_2 -> {
                                if (Context.editor.gizmo != null) {
                                    Context.editor.gizmo.delete();
                                    Context.editor.gizmo = new RotateGizmo(Context.editor, Context.editor.selectedObject.position);
                                }
                            }
                            case GLFW_KEY_3 -> {
                                if (Context.editor.gizmo != null) {
                                    Context.editor.gizmo.delete();
                                    Context.editor.gizmo = new ScaleGizmo(Context.editor, Context.editor.selectedObject.position);
                                }
                            }
                            case GLFW_KEY_F -> Context.editor.findObject();
                            case GLFW_KEY_F12-> Context.currentScene.saveSceneToFile();
                            case GLFW_KEY_D -> {
                                if ((mods & GLFW_MOD_CONTROL) != 0) {
                                    Context.editor.duplicateObject();
                                }
                            }
                            case GLFW_KEY_DELETE -> Context.editor.deleteObject();
                        }
                    }
                }
            }
        });
    }

    private void set_mouse_callbacks() {
        GLFW.glfwSetMouseButtonCallback(Context.window, (window, button, action, mods) -> {
            ImGui.getIO().setMouseDown(button, action == GLFW_PRESS);
            if (!ImGui.getIO().getWantCaptureMouse()) {
                if (Context.gamemode == EDIT) {
                    if (action == GLFW_PRESS) {
                        switch (button) {
                            case GLFW_MOUSE_BUTTON_1 -> Context.editor.select();
                            case GLFW_MOUSE_BUTTON_2 -> {
                                if (mods == GLFW_MOD_ALT) { // TODO - this is actual bitwise 0x0004
                                    Context.editor.camera.set_mode(ORBIT);
                                } else {
                                    Context.editor.camera.set_mode(FLY);
                                }
                            }
                        }
                    }
                    if (action == GLFW_RELEASE) {
                        switch (button) {
                            case GLFW_MOUSE_BUTTON_1 -> Context.editor.releaseGizmo();
                            case GLFW_MOUSE_BUTTON_2 -> {
                                Context.editor.camera.set_mode(SELECT);
                                Context.editor.camera.inputVector.set(0);
                            }
                        }
                    }
                }
            }
        });

        glfwSetCursorPosCallback(Context.window, (w, x, y) -> {
            ImGui.getIO().setMousePos((float)x, (float)y);
            if (!ImGui.getIO().getWantCaptureMouse()) {
            }
        });

        glfwSetScrollCallback(Context.window, (w, xoff, yoff) -> {
            ImGui.getIO().setMouseWheel((float)yoff);
            if (!ImGui.getIO().getWantCaptureMouse()) {
            }
        });
    }

    private void gameLoop() {
        Timer total_timer = new Timer();
        Timer fps_cap_timer = new Timer();
        double current_frame = 0;
        double fps = 0;
        double delta_time = 0.f;

        total_timer.start();
        fps_cap_timer.start();

        while(Context.gamemode != QUIT) {
            current_frame++;

            handle_events(delta_time);

            switch (Context.gamemode) {
                case GAME -> {
                    update(delta_time);
                    render();
                }
                case PAUSED -> render();
                case EDIT -> {
                    Context.editor.update(delta_time);
                    render();
                }
            }

            double ticks = fps_cap_timer.get_ticks();
            if (ticks < Context.ticksPerFrame) {
                try {
                    Thread.sleep((long) (Context.ticksPerFrame - ticks));
                } catch (InterruptedException e) {
                    System.out.println("sleep interrupted...");
                }
            }

            delta_time = (float) fps_cap_timer.get_ticks();
            fps = current_frame / total_timer.get_ticks();
            fps_cap_timer.start();
        }
    }

    public static void main(String[] args) {
        new Game().run();
    }
}
