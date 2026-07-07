package com.cravencraft.bloodybits.client;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.renderer.BloodSprayRenderer;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BloodyBitsMod.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityRegistry.BLOOD_SPRAY.get(), BloodSprayRenderer::new);
    }
}