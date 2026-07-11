package com.cravencraft.bloodybits.network;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.network.messages.EntityDamageMessage;
import com.cravencraft.bloodybits.network.messages.EntityHealMessage;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class BloodyBitsPacketHandler {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(BloodyBitsMod.MODID)
                .versioned("1.0");

        registrar.playToClient(
                EntityMessage.TYPE,
                EntityMessage.STREAM_CODEC,
                EntityMessage::handle
        );
        registrar.playToClient(
                EntityDamageMessage.TYPE,
                EntityDamageMessage.STREAM_CODEC,
                EntityDamageMessage::handle
        );
        registrar.playToClient(
                EntityHealMessage.TYPE,
                EntityHealMessage.STREAM_CODEC,
                EntityHealMessage::handle
        );
    }
}