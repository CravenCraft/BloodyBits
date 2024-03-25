package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particles.BloodyBitsParticles;
import com.cravencraft.bloodybits.particles.custom.BloodParticles;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// TODO: Can probably delete if I don't want to use particles now.
//       Will keep until I know for a fact I won't use any.
@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BloodyBitsBusEvents {

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BloodyBitsParticles.BLOOD_PARTICLES.get(), BloodParticles.Provider::new);
    }
}
