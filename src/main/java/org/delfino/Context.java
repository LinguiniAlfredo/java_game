package org.delfino;

import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.delfino.cameras.StaticCamera;
import org.delfino.editor.Editor;
import org.delfino.entities.Entity;
import org.delfino.entities.TankController;
import org.delfino.scenes.Scene;
import org.delfino.ui.*;
import org.delfino.cameras.Camera;

public record Context() {
    public static long           window;
    public static UI             ui;
    public static Gamemode       gamemode;
    public static Scene          current_scene;
    public static Editor         editor;
    public static Camera         active_camera;
    public static int            screen_width;
    public static int            screen_height;
    public static float          ticks_per_frame;
    public static boolean        wireframe;
    public static boolean        show_shadow_map;
    public static boolean        show_collisions;
    public static ImGuiImplGlfw  gui_glfw;
    public static ImGuiImplGl3   gui_gl3;
}
