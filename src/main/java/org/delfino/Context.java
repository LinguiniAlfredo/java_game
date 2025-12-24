package org.delfino;

import org.delfino.scenes.Scene;
import org.delfino.ui.*;

public record Context() {
    public static long              window;
    public static UI                ui;
    public static Gamemode          gamemode;
    public static Scene             current_scene;
    public static int               screen_width;
    public static int               screen_height;
    public static float             ticks_per_frame;
    public static boolean           wireframe;
    public static boolean           show_shadow_map;
    public static boolean           show_collisions;
}
