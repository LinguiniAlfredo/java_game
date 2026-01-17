package org.delfino;

import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.delfino.editor.Editor;
import org.delfino.scenes.Scene;
import org.delfino.ui.*;
import org.delfino.cameras.Camera;

public record Context() {
    public static long          window;
    public static UI            ui;
    public static Gamemode      gamemode;
    public static Scene         currentScene;
    public static Editor        editor;
    public static Camera        activeCamera;
    public static int           screenWidth;
    public static int           screenHeight;
    public static float         ticksPerFrame;
    public static boolean       wireframe;
    public static boolean       showShadowMap;
    public static boolean       showCollisions;
    public static ImGuiImplGlfw guiGlfw;
    public static ImGuiImplGl3  guiGl3;
}
