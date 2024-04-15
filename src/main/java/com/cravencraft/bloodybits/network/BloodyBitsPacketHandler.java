package com.cravencraft.bloodybits.network;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class BloodyBitsPacketHandler {
    public static SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(BloodyBitsMod.MODID, "main"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    private static int packetId = 0;

    private static int getId() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.registerMessage(0, EntityMessage.class, EntityMessage::encode, EntityMessage::decode, EntityMessage::handle);
    }
}