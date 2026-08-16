package com.cravencraft.bloodybits.client.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.drip.BloodDripParticle;
import com.cravencraft.bloodybits.client.particle.mist.BloodMistParticle;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticle;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticle;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BloodyBitsClientEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPRAY_PARTICLE.get(), BloodSprayParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPATTER_PARTICLE.get(), BloodSpatterParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_MIST_PARTICLE.get(), BloodMistParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_DRIP_PARTICLE.get(), BloodDripParticle.Provider::new);
    }
}
