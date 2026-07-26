package com.cravencraft.bloodybits.particle.spatter;

import net.minecraft.world.phys.Vec2;

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

    
}
