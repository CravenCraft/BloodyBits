package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerSkinToServerMessage {

    public int hashedPlayerName;
    public byte[] customSkin;

    public PlayerSkinToServerMessage(byte[] customSkin, int hashedPlayerName) {
        this.hashedPlayerName = hashedPlayerName;
        this.customSkin = customSkin;
    }

    public static void encode(PlayerSkinToServerMessage message, FriendlyByteBuf buffer) {
        buffer.writeByteArray(message.customSkin).writeInt(message.hashedPlayerName);
    }

    public static PlayerSkinToServerMessage decode(FriendlyByteBuf buffer) {
        return new PlayerSkinToServerMessage(buffer.readByteArray(), buffer.readInt());
    }

    public static void handleClientToServer(PlayerSkinToServerMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                BloodyBitsUtils.sendPlayerSkinDataToAllClients(message.customSkin, message.hashedPlayerName);
            }
        });
        context.setPacketHandled(true);
    }
}