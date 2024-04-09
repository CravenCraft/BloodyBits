package com.cravencraft.bloodybits.network;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.network.messages.BloodySprayEntityMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

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
        INSTANCE.registerMessage(0, BloodySprayEntityMessage.class, BloodySprayEntityMessage::encode, BloodySprayEntityMessage::decode, BloodySprayEntityMessage::handle);
    }
}