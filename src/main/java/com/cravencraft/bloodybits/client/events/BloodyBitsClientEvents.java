package com.cravencraft.bloodybits.client.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particle.BloodEmitterParticle;
import com.cravencraft.bloodybits.particle.BloodSpatterParticle;
import com.cravencraft.bloodybits.particle.BloodSprayParticle;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = BloodyBitsMod.MODID, value = Dist.CLIENT)
public class BloodyBitsClientEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        List<BloodEmitterParticle.VariantFactory> variants = new ArrayList<>();
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPATTER_PARTICLE.get(), BloodSpatterParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPRAY_PARTICLE.get(), sprites -> {
            var provider = new BloodSprayParticle.Provider(sprites);
            variants.add(provider);
            return provider;
        });
        event.registerSpriteSet(ParticleRegistry.BLOOD_EMITTER.get(), sprites -> new BloodEmitterParticle.Provider(variants));

    }

}
