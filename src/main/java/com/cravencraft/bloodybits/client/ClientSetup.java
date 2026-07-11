package com.cravencraft.bloodybits.client;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.renderer.BloodSprayRenderer;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.BLOOD_SPRAY.get(), BloodSprayRenderer::new);
    }
}