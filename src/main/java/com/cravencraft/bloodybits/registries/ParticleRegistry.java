package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.drip.BloodDripParticleType;
import com.cravencraft.bloodybits.client.particle.mist.BloodMistParticleType;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticleType;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ParticleRegistry {

    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, BloodyBitsMod.MODID);

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }

    public static final String DEFAULT_BLOOD_COLOR = "#96000F";

    public static final Supplier<BloodSprayParticleType> BLOOD_SPRAY_PARTICLE;
    public static final Supplier<BloodMistParticleType> BLOOD_MIST_PARTICLE;
    public static final Supplier<BloodSpatterParticleType> BLOOD_SPATTER_PARTICLE;
    public static final Supplier<BloodDripParticleType> BLOOD_DRIP_PARTICLE;

    static {
        BLOOD_SPRAY_PARTICLE = PARTICLE_TYPES.register("blood_spray_particles", () ->
                new BloodSprayParticleType(false));
        BLOOD_MIST_PARTICLE = PARTICLE_TYPES.register("blood_mist_particles", () ->
                new BloodMistParticleType(false));
        BLOOD_SPATTER_PARTICLE = PARTICLE_TYPES.register("blood_spatter_particles", () ->
                new BloodSpatterParticleType(false));
        BLOOD_DRIP_PARTICLE = PARTICLE_TYPES.register("blood_drip_particles", () ->
                new BloodDripParticleType(false));
    }
}
