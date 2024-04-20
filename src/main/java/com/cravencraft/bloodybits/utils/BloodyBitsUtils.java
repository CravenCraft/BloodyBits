package com.cravencraft.bloodybits.utils;

import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.custom.BloodChunkEntity;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.PlayerSkinToClientMessage;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BloodyBitsUtils {
    public static ArrayList<BloodSprayEntity> BLOOD_SPRAY_ENTITIES = new ArrayList<>();
    public static ArrayList<BloodChunkEntity> BLOOD_CHUNK_ENTITIES = new ArrayList<>();
    public static HashMap<Integer, NativeImage> PLAYER_SKINS;


    public static void sendPlayerSkinDataToAllClients(byte[] nativeImageByteArray, int hashedPlayerName) {
        BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new PlayerSkinToClientMessage(nativeImageByteArray,hashedPlayerName));
    }

    public static void setNativeImage(byte[] nativeImageByteArray, int hashedPlayerName) throws IOException {
        PLAYER_SKINS.put(hashedPlayerName, NativeImage.read(nativeImageByteArray));
    }

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
}