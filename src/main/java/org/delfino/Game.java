package org.delfino;

import org.apache.commons.lang3.SystemUtils;
import org.delfino.entities.Cube;
import org.delfino.entities.Entity;
import org.delfino.entities.LightCube;
import org.delfino.renderer.Shadowmap;
import org.delfino.renderer.Skybox;
import org.delfino.ui.UI;
import org.delfino.utils.Camera;

import static org.delfino.Context.window;
import static org.delfino.Gamemode.*;

import org.delfino.utils.Timer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import org.joml.Vector3f;
import java.nio.*;
import java.util.Vector;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    public void run() {
        init_context();
        init_glfw();

        Context.light_cube = new LightCube(new Vector3f(0.f, 5.f, 0.f));
        Context.camera = new Camera(new Vector3f(0.0f, 0.0f, 20.0f));
        Context.shadow_map = new Shadowmap();
        Context.world_blocks.add(new Cube(new Vector3f(0.f)));

//        init_level();
        game_loop();
        close_app();
    }

    private void init_context() {
        Context.gamemode        = GAME;
        Context.screen_width    = 1920/2;
        Context.screen_height   = 1080/2;
        Context.ticks_per_frame = 1000.f / 144.0f;
        Context.wireframe       = false;
        Context.show_shadow_map = false;
        Context.show_collisions = false;
    }
//
    private void close_app() {
        Context.light_cube.delete();
        for (Entity world_block : Context.world_blocks) {
            world_block.delete();
        }
        Context.shadow_map.delete();

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
        if (Context.gamemode == PAUSED) {
            glfwSetInputMode(Context.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            // relative mouse mode true ?
            glfwSetCursorPos(Context.window, Context.screen_width * 0.5, Context.screen_height * 0.5);
            Context.gamemode = GAME;
        } else {
            glfwSetInputMode(Context.window, GLFW_CURSOR, GLFW_CURSOR_CAPTURED);
            // relative mouse mode false ?
            glfwSetCursorPos(Context.window, Context.screen_width * 0.5, Context.screen_height * 0.5);
            Context.gamemode = PAUSED;
        }
    }

    private void toggle_flycam() {
        // TODO
    }

    private void destroy_level() {
        for (Entity world_block : Context.world_blocks) {
            world_block.delete();
        }
        Context.light_cube.delete();
    }

    private void init_level() {

    }

    private void handle_events(double delta_time) {
        double[] mouse_x = new double[1];
        double[] mouse_y = new double[1];
        glfwGetCursorPos(Context.window, mouse_x, mouse_y);

        glfwPollEvents();

        if (GLFW.glfwGetKey(Context.window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            Context.gamemode = QUIT;
        }

        if (GLFW.glfwGetKey(Context.window, GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS) {
//            toggle_paused();
        }

        if (GLFW.glfwGetKey(Context.window, GLFW.GLFW_KEY_F1) == GLFW.GLFW_PRESS) {
            toggle_wireframe();
        }

        if (GLFW.glfwGetKey(Context.window, GLFW.GLFW_KEY_F2) == GLFW.GLFW_PRESS) {
            toggle_shadow_map();
        }

        if (GLFW.glfwGetKey(Context.window, GLFW.GLFW_KEY_F3) == GLFW.GLFW_PRESS) {
            toggle_collision_render();
        }

        if (GLFW.glfwGetKey(Context.window, GLFW.GLFW_KEY_F5) == GLFW.GLFW_PRESS) {
            toggle_flycam();
        }

        if (GLFW.glfwGetKey(Context.window, GLFW.GLFW_KEY_1) == GLFW.GLFW_PRESS) {
            destroy_level();
            init_level();
        }
        Context.camera.process_keyboard();
        if (Context.gamemode != PAUSED) {
            Context.camera.process_mouse(mouse_x[0], mouse_y[0], delta_time);
        }

    }

    private void reset_colliders() {
    }

    private void update(double delta_time) {
        Context.camera.update(delta_time);
    }

    private void render() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Context.shadow_map.do_pass();

        for (Entity world_block : Context.world_blocks) {
            world_block.render();
            if (Context.show_collisions) {
                world_block.render_collider();
            }
        }

        Context.light_cube.render();
        for (Entity entity : Context.entities) {
            entity.render();
            if (Context.show_collisions) {
                entity.render_collider();
            }
        }

        if (Context.show_shadow_map) {
            Context.shadow_map.render_depth_quad();
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
        if (Context.window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

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
        glfwSetInputMode(Context.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, Context.screen_width, Context.screen_height);
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
