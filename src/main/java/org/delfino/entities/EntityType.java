package org.delfino.entities;


public enum EntityType {
    CUBE,
    SPHERE,
    FIRST_PERSON_CONTROLLER,
    TANK_CONTROLLER,
    CAMERA;

    public static String[] toStringArray() {
        EntityType[] values = EntityType.values();
        String[] names = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }
}
