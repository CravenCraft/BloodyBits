package com.cravencraft.bloodybits.utils;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
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


    public static int getMobDamageColor(String entityName) {
        int redDamage = 200;
        int greenDamage = 1;
        int blueDamage = 1;
        int alphaDamage = 255;

        if (CommonConfig.solidEntities().contains(entityName)) {
            redDamage = 0;
            greenDamage = 0;
            blueDamage = 0;
            alphaDamage = 0;
        }
        else {
            for (Map.Entry<String, List<String>> mapElement : ClientConfig.entityBloodColors().entrySet()) {
                if (mapElement.getValue().contains(Objects.requireNonNull(entityName))) {
                    String bloodColorHexVal = mapElement.getKey();
                    redDamage = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
                    greenDamage = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
                    blueDamage = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
                    break;
                }
            }
        }
        return FastColor.ABGR32.color(alphaDamage, blueDamage, greenDamage, redDamage);
    }

    public static int getBurnDamageColor() {
        String bloodColorHexVal = ClientConfig.getBurnDamageColor();
        int redDamage = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
        int greenDamage = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
        int blueDamage = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
        return FastColor.ABGR32.color(255, blueDamage, greenDamage, redDamage);
    }

    public static void paintDamageToNativeImage(NativeImage unpaintedDamageLayerTexture, int damageColorRGBA) {
        for (int x = 0; x < unpaintedDamageLayerTexture.getWidth(); x++) {
            for (int y = 0; y < unpaintedDamageLayerTexture.getHeight(); y++) {
                if (unpaintedDamageLayerTexture.getPixelRGBA(x, y) != 0) {
                    int median = 125;

                    int damageLayerPixelRGBA = unpaintedDamageLayerTexture.getPixelRGBA(x, y);
                    int currentDamageLayerAlpha = FastColor.ABGR32.alpha(damageLayerPixelRGBA);
                    int currentDamageLayerRed = FastColor.ABGR32.red(damageLayerPixelRGBA);
                    int currentDamageLayerGreen = FastColor.ABGR32.green(damageLayerPixelRGBA);
                    int currentDamageLayerBlue = FastColor.ABGR32.blue(damageLayerPixelRGBA);

//                    BloodyBitsMod.LOGGER.info("current red {} green {} blue {}", currentDamageLayerRed, currentDamageLayerGreen, currentDamageLayerBlue);

                    int newDamageColorRed = FastColor.ABGR32.red(damageColorRGBA);
                    int newDamageColorGreen = FastColor.ABGR32.green(damageColorRGBA);
                    int newDamageColorBlue = FastColor.ABGR32.blue(damageColorRGBA);


//                    BloodyBitsMod.LOGGER.info("new red {} green {} blue {}", newDamageColorRed, newDamageColorGreen, newDamageColorBlue);

                    newDamageColorRed = (int) Math.min(newDamageColorRed * ((float) currentDamageLayerRed / median), 255);
                    newDamageColorGreen = (int) Math.min(newDamageColorGreen * ((float) currentDamageLayerGreen / median), 255);
                    newDamageColorBlue = (int) Math.min(newDamageColorBlue * ((float) currentDamageLayerBlue / median), 255);

                    BloodyBitsMod.LOGGER.info("modified new red {} green {} blue {}", newDamageColorRed, newDamageColorGreen, newDamageColorBlue);

                    int newDamageLayerRGBA = FastColor.ABGR32.color(currentDamageLayerAlpha, newDamageColorBlue, newDamageColorGreen, newDamageColorRed);

                    unpaintedDamageLayerTexture.setPixelRGBA(x, y, newDamageLayerRGBA);
                }
            }
        }
    }
}