package org.delfino;

import org.apache.commons.lang3.SystemUtils;
import org.delfino.editor.Editor;
import org.delfino.scenes.Scene;
import org.delfino.ui.UI;

import static org.delfino.Gamemode.*;

import org.delfino.utils.Timer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    double[] mouse_x = new double[1];
    double[] mouse_y = new double[1];
    public double prev_mouse_x = 0;
    public double prev_mouse_y = 0;

    public void run() {
        init_context();
        init_glfw();

        Context.ui            = new UI();
        Context.current_scene = new Scene("scenes/example_level.json");

        game_loop();
        close_app();
    }

    private void init_context() {
        Context.gamemode        = GAME;
        Context.screen_width    = 1920;
        Context.screen_height   = 1080;
        Context.ticks_per_frame = 1000.f / 144.0f;
        Context.wireframe       = false;
        Context.show_shadow_map = false;
        Context.show_collisions = false;
    }
//
    private void close_app() {
        Context.ui.delete();
        Context.current_scene.delete();
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
        Context.show_shadow_map = !Context.show_shadow_map;
    }

    private void toggle_collision_render() {
        Context.show_collisions = !Context.show_collisions;
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
                if (Context.editor == null) {
                    Context.editor = new Editor();
                }
            } else {
                Context.gamemode = GAME;
                Context.current_scene.reload();
            }
        }
    }

    private void handle_events(double delta_time) {
        double x_offset, y_offset;
        glfwGetCursorPos(Context.window, mouse_x, mouse_y);
        x_offset = mouse_x[0] - prev_mouse_x;
        y_offset = -(mouse_y[0] - prev_mouse_y);
        prev_mouse_x = mouse_x[0];
        prev_mouse_y = mouse_y[0];

        glfwPollEvents();

        Context.camera.process_keyboard();
        if (Context.gamemode != PAUSED) {
            Context.camera.process_mouse_movement(x_offset, y_offset, delta_time);
        }
    }

    private void update(double delta_time) {
        Context.current_scene.update(delta_time);
    }

    private void render() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Context.current_scene.render();
        Context.ui.render();

        if (Context.gamemode == EDIT) {
            Context.editor.render();
        }

        glfwSwapBuffers(Context.window);
    }

    private void init_glfw() {
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

        Context.window = glfwCreateWindow(Context.screen_width, Context.screen_height, "", NULL, NULL);
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
        glViewport(0, 0, Context.screen_width, Context.screen_height);

        GLFW.glfwSetKeyCallback(Context.window, (window, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                switch (key) {
                    case GLFW.GLFW_KEY_TAB -> toggle_paused();
                    case GLFW.GLFW_KEY_F1 -> toggle_wireframe();
                    case GLFW.GLFW_KEY_F2 -> toggle_shadow_map();
                    case GLFW.GLFW_KEY_F3 -> toggle_collision_render();
                    case GLFW.GLFW_KEY_F5 -> toggle_editor();
                    case GLFW.GLFW_KEY_ESCAPE -> Context.gamemode = QUIT;
                }
            }
        });
    }

    private void game_loop() {
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
            if (ticks < Context.ticks_per_frame) {
                try {
                    Thread.sleep((long) (Context.ticks_per_frame - ticks));
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
