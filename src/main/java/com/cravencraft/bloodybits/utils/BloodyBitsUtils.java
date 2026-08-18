package com.cravencraft.bloodybits.utils;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.*;

public class BloodyBitsUtils {

    // TODO: One of these sounds isn't being properly found. Find out which one and remove it.
    public static SoundEvent getRandomSound(int randomNumber) {
        return switch (randomNumber) {
            case 1 -> SoundEvents.MUD_HIT;
            case 2 -> SoundEvents.WET_GRASS_HIT;
            default -> SoundEvents.MUD_STEP;
        };
    }

    public static double getRandomVariance(double min, double max) {
        return min + (Math.random() * (max - min));
    }

    public static double getRandomVarianceTest(double originalValue) {
        var randomVariance = new Random().nextInt(-1, 1);
        var test = Mth.clamp(originalValue - (Math.random() * randomVariance), 0.0F, 1.0F);

        return test;
    }

    public static float getRandomPitch() {
        return 1.8F / (new Random().nextFloat() * 0.2F + 0.9F);
    }

    public static float normalizeColorValue(int colorValue) {
        return colorValue / 255f;
    }

    public static float applyRandomSign(float number) {
        return (new Random().nextBoolean() ? 1 : -1) * number;
    }

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