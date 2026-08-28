package com.cravencraft.bloodybits.client.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.drip.BloodDripParticle;
import com.cravencraft.bloodybits.client.particle.mist.BloodMistParticle;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticle;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticle;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = BloodyBitsMod.MODID, value = Dist.CLIENT)
public class BloodyBitsClientEvents {
    private static ShaderInstance smoothFadeShader;

    public static ShaderInstance getSmoothFadeShader() {
        return smoothFadeShader;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, "smooth_fade_particle"),
                        DefaultVertexFormat.PARTICLE
                ),
                shaderInstance -> smoothFadeShader = shaderInstance
        );
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPRAY_PARTICLE.get(), BloodSprayParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPATTER_PARTICLE.get(), BloodSpatterParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_MIST_PARTICLE.get(), BloodMistParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_DRIP_PARTICLE.get(), BloodDripParticle.Provider::new);
    }
}
