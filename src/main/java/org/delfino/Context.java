package org.delfino;

import java.util.ArrayList;

import org.delfino.entities.*;
import org.delfino.renderer.*;
import org.delfino.ui.*;
import org.delfino.utils.*;

enum Gamemode {
    MENU,
    GAME,
    PAUSED,
    GAMEOVER,
    EDIT,
    QUIT
}

public class Context {
    public static long              window;
    public static Shadowmap         shadow_map;
    public static Skybox            skybox;
    public static LightCube         light_cube;
    public static Camera            camera;
    public static UI                ui;
    public static ArrayList<Entity> world_blocks = new ArrayList<>();
    public static ArrayList<Entity> entities = new ArrayList<>();
    public static Gamemode          gamemode;
    public static int               screen_width;
    public static int               screen_height;
    public static float             ticks_per_frame;
    public static boolean           wireframe;
    public static boolean           show_shadow_map;
    public static boolean           show_collisions;
}
