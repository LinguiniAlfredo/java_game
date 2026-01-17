package org.delfino.cameras;

import org.delfino.scenes.Scene;
import org.joml.Vector3f;

public class FollowCamera extends Camera {

    public float follow_distance = 10.f;

    public FollowCamera(Scene scene, Vector3f position, Vector3f front) {
        super(scene, position, front);
    }

    @Override
    public void updateCameraVectors() {
        float yaw_rad   = (float) Math.toRadians(this.yaw);
        float pitch_rad = (float) Math.toRadians(this.pitch);
        float cos_pitch = (float) Math.cos(pitch_rad);

//        if (yaw_rad != 0 || pitch_rad != 0) {
//            this.front.set(
//                    (float) Math.cos(yaw_rad) * cos_pitch,
//                    (float) Math.sin(pitch_rad),
//                    (float) Math.sin(yaw_rad) * cos_pitch
//            ).normalize();
//        }

        this.front.cross(this.worldUp, this.right).normalize();
        this.right.cross(this.front, this.up).normalize();
    }
}
