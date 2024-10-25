package com.cravencraft.bloodybits.utils;

import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.*;

public class BloodyBitsUtils {
    public static ArrayList<BloodSprayEntity> BLOOD_SPRAY_ENTITIES = new ArrayList<>();
    public static final HashMap<Integer, EntityInjuries> INJURED_ENTITIES = new HashMap<>();
    public static final List<String> INJURY_LAYER_ENTITIES = new ArrayList<>();

    /**
     * Convenient helper method to simplify vertex drawing.
     */
    public static void vertex(Matrix4f pMatrix, Matrix3f pNormal, VertexConsumer pConsumer, float pX, float pY, float pZ, float pU, float pV, int pNormalX, int pNormalZ, int pNormalY, int packedLight, int red, int green, int blue, int alpha) {
        pConsumer
                .vertex(pMatrix, pX, pY, pZ)
                .color(red, green, blue, alpha)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pNormal, (float)pNormalX, (float)pNormalY, (float)pNormalZ)
                .endVertex();
    }

    // TODO: One of these sounds isn't being properly found. Find out which one and remove it.
    public static SoundEvent getRandomSound(int randomNumber) {
        return switch (randomNumber) {
            case 1 -> SoundEvents.MUD_HIT;
            case 2 -> SoundEvents.WET_GRASS_HIT;
            default -> SoundEvents.MUD_STEP;
        };
    }

    public static String[] decompose(String pLocation, char pSeparator) {
        String[] astring = new String[]{"minecraft", pLocation};
        int i = pLocation.indexOf(pSeparator);
        if (i >= 0) {
            astring[1] = pLocation.substring(i + 1);
            if (i >= 1) {
                astring[0] = pLocation.substring(0, i);
            }
        }

        return astring;
    }

    public static String getEntityDamageHexColor(String entityName) {
        String damageHexColor = "#c80000";

        // TODO: Figure out how you're actually going to show damage on solid entities.
        if (CommonConfig.solidEntities().contains(entityName)) {
            damageHexColor = "#ffffff";
        }
        else {
            for (Map.Entry<String, List<String>> mapElement : ClientConfig.entityBloodColors().entrySet()) {
                if (mapElement.getValue().contains(Objects.requireNonNull(entityName))) {
                    damageHexColor = mapElement.getKey();
                    break;
                }
            }
        }
        return damageHexColor;
    }
}