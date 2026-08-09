package com.cravencraft.bloodybits.client.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.emitter.BloodEmitterParticle;
import com.cravencraft.bloodybits.client.particle.drip.BloodDripParticle;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticle;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticle;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID, value = Dist.CLIENT)
public class BloodyBitsClientEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        List<BloodEmitterParticle.VariantFactory> variants = new ArrayList<>();
        event.registerSpriteSet(ParticleRegistry.BLOOD_EMITTER.get(), sprites -> new BloodEmitterParticle.Provider(variants));
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPATTER_PARTICLE.get(), BloodSpatterParticle.Provider::new);

        // TODO: Do I need to set this up this way, or can I register it like the others?
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPRAY_PARTICLE.get(), sprites -> {
            var provider = new BloodSprayParticle.Provider(sprites);
            variants.add(provider);
            return provider;
        });
        event.registerSpriteSet(ParticleRegistry.BLOOD_DRIP_PARTICLE.get(), BloodDripParticle.Provider::new);
    }
}
