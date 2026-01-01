package org.delfino.entities;


public enum EntityType {
    CUBE,
    SPHERE;

    public static String[] to_string_array() {
        EntityType[] values = EntityType.values();
        String[] names = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }
}
