package com.cravencraft.bloodybits.utils;

import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.custom.BloodChunkEntity;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;

import java.util.ArrayList;

public class BloodyBitsUtils {
    public static ArrayList<BloodSprayEntity> BLOOD_SPRAY_ENTITIES = new ArrayList<>();
    public static ArrayList<BloodChunkEntity> BLOOD_CHUNK_ENTITIES = new ArrayList<>();

    public static double getRandomAngle(double range) {
        return (Math.random() > 0.5) ? Math.random() * range : -(Math.random() * range);
    }

    public static void shrinkBloodChunk(BloodChunkEntity bloodChunkEntity) {
        float lifetimePercentage = (float) bloodChunkEntity.currentLifeTime / (float) CommonConfig.despawnTime();
        bloodChunkEntity.xMin = bloodChunkEntity.initialMinX - (bloodChunkEntity.initialMinX * lifetimePercentage);
        bloodChunkEntity.xMax = bloodChunkEntity.initialMaxX - (bloodChunkEntity.initialMaxX * lifetimePercentage);
        bloodChunkEntity.yMin = bloodChunkEntity.initialMinY - (bloodChunkEntity.initialMinY * lifetimePercentage);
        bloodChunkEntity.yMax = bloodChunkEntity.initialMaxY - (bloodChunkEntity.initialMaxY * lifetimePercentage);
        bloodChunkEntity.zMin = bloodChunkEntity.initialMinZ - (bloodChunkEntity.initialMinZ * lifetimePercentage);
        bloodChunkEntity.zMax = bloodChunkEntity.initialMaxZ - (bloodChunkEntity.initialMaxZ * lifetimePercentage);
    }
}