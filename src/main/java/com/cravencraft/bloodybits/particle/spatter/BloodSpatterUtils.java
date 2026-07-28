package com.cravencraft.bloodybits.particle.spatter;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class BloodSpatterUtils {

    // Create the 2D box that will be drawn on the given block.
    public static Vec2[] createCorners(float minWidth, float maxWidth, float minHeight, float maxHeight) {
        return new Vec2[] {
                new Vec2(minWidth, minHeight),
                new Vec2(minWidth, maxHeight),
                new Vec2(maxWidth, maxHeight),
                new Vec2(maxWidth, minHeight),
        };
    }

    /**
     * Gets the
     *
     * @param distanceFromCamera
     * @param hitDirection
     * @return
     */
    public static Vec3 getSpatterDistanceFromCamera(Vec3 distanceFromCamera, Direction hitDirection) {
        float xDistance = 0.0f;
        float yDistance = 0.0f;
        float zDistance = 0.0f;



        return new Vec3(xDistance, yDistance, zDistance);
    }
}
