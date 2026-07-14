package com.cravencraft.bloodybits.client.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particle.BloodSprayParticle;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

//@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = BloodyBitsMod.MODID, value = Dist.CLIENT)
public class BloodyBitsClientEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPRAY_PARTICLE.get(), BloodSprayParticle.Provider::new);
    }
}
