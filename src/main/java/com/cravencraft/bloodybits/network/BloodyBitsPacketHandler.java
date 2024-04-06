package com.cravencraft.bloodybits.network;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.network.messages.BloodySprayEntityMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class BloodyBitsPacketHandler {

    public static final String NET_VERSION = "1.0";
//    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(BloodyBitsMod.MODID, "master"), () -> NET_VERSION, NET_VERSION::equals, NET_VERSION::equals);
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
        //TODO: The Optional might not be necessary here.
        INSTANCE.registerMessage(0, BloodySprayEntityMessage.class, BloodySprayEntityMessage::encode, BloodySprayEntityMessage::decode, BloodySprayEntityMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}