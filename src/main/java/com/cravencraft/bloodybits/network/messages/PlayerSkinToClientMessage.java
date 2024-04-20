package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

public class PlayerSkinToClientMessage {

    public int hashedPlayerName;
    public byte[] customSkin;

    public PlayerSkinToClientMessage(byte[] customSkin, int hashedPlayerName) {
        this.hashedPlayerName = hashedPlayerName;
        this.customSkin = customSkin;
    }

    public static void encode(PlayerSkinToClientMessage message, FriendlyByteBuf buffer) {
        buffer.writeByteArray(message.customSkin).writeInt(message.hashedPlayerName);
    }

    public static PlayerSkinToClientMessage decode(FriendlyByteBuf buffer) {
        return new PlayerSkinToClientMessage(buffer.readByteArray(), buffer.readInt());
    }

    public static void handleServerToClient(PlayerSkinToClientMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                try {
                    BloodyBitsUtils.setNativeImage(message.customSkin, message.hashedPlayerName);
                } catch (IOException e) {
                    BloodyBitsMod.LOGGER.error("Error mapping custom player skin: ", e);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
