package com.cravencraft.bloodybits.utils;

import com.cravencraft.bloodybits.model.BloodType;
import com.cravencraft.bloodybits.registries.BloodTypeRegistry;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BloodyBitsUtils {
    private static final String doesNotBleed = "does_not_bleed";

    public static String getEntityBloodColor(LivingEntity entity) {
        String bloodColor = ParticleRegistry.DEFAULT_BLOOD_COLOR;
        for (BloodType bloodType : BloodTypeRegistry.getBloodTypes()) {
            if (entity.getType().is(bloodType.entityTag())) {

                if (bloodType.entityTag().location().getPath().equals(doesNotBleed)) {
                    return null;
                }
                else {
                    bloodColor = bloodType.color();
                    break;
                }
            }
        }

        return bloodColor;
    }

    // TODO: One of these sounds isn't being properly found. Find out which one and remove it.
    public static SoundEvent getRandomSound(int randomNumber) {
        return switch (randomNumber) {
            case 1 -> SoundEvents.MUD_HIT;
            case 2 -> SoundEvents.WET_GRASS_HIT;
            default -> SoundEvents.MUD_STEP;
        };
    }

    public static Vec3 getRandomSignVectorVariance(double min, double max) {
        return new Vec3(
                applyRandomSign((float) getRandomVariance(min, max)),
                applyRandomSign((float) getRandomVariance(min, max)),
                applyRandomSign((float) getRandomVariance(min, max))
        );
    }

    public static Vec3 getRandomVectorVariance(double min, double max) {
        return new Vec3(
                BloodyBitsUtils.getRandomVariance(min, max),
                BloodyBitsUtils.getRandomVariance(min, max),
                BloodyBitsUtils.getRandomVariance(min, max)
        );
    }

    public static double getRandomVariance(double min, double max) {
        return min + (Math.random() * (max - min));
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