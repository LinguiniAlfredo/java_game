package org.delfino.scenes.dtos;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public record EntityDTO() {
    public static int         id;
    public static String      type;
    public static Vector3f    position;
    public static Quaternionf rotation;
    public static float       scale;
}
