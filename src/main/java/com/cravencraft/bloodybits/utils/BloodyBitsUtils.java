package com.cravencraft.bloodybits.utils;

import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;

import java.util.ArrayList;

import static org.joml.Math.clamp;

public class BloodyBitsUtils {
    public static ArrayList<BloodSprayEntity> BLOOD_SPRAY_ENTITIES = new ArrayList<>();

    public static int rgbToInt(int alpha, int red, int green, int blue) {
        alpha = clamp(alpha, 0, 255);
        red = clamp(red, 0, 255);
        green = clamp(green, 0, 255);
        blue = clamp(blue, 0, 255);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }


}
