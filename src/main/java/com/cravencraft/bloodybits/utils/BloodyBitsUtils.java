package com.cravencraft.bloodybits.utils;

import com.cravencraft.bloodybits.entity.custom.BloodChunkEntity;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;

import java.util.ArrayList;

public class BloodyBitsUtils {
    public static ArrayList<BloodSprayEntity> BLOOD_SPRAY_ENTITIES = new ArrayList<>();
    public static ArrayList<BloodChunkEntity> BLOOD_CHUNK_ENTITIES = new ArrayList<>();

    public static double getRandomAngle(double range) {

        return (Math.random() > 0.5) ? Math.random() * range : -(Math.random() * range);
    }
}