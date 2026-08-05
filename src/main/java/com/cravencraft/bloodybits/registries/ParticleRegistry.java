package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particle.drip.BloodDripParticleType;
import com.cravencraft.bloodybits.particle.spatter.BloodSpatterParticleType;
import com.cravencraft.bloodybits.particle.spray.BloodSprayParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ParticleRegistry {

    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, BloodyBitsMod.MODID);

    public static void registerParticles(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }

    public static final String DEFAULT_BLOOD_COLOR = "#96000F";
    public static final Supplier<BloodSprayParticleType> BLOOD_EMITTER;
    public static final Supplier<SimpleParticleType> BLOOD_SPRAY_PARTICLE;
    public static final Supplier<BloodSpatterParticleType> BLOOD_SPATTER_PARTICLE;
    public static final Supplier<BloodDripParticleType> BLOOD_DRIP_PARTICLE;

    static {
        BLOOD_EMITTER = PARTICLE_TYPES.register("blood_emitter", () -> new BloodSprayParticleType(false));
        BLOOD_SPRAY_PARTICLE = PARTICLE_TYPES.register("blood_spray_particles", () -> new SimpleParticleType(false));
        BLOOD_SPATTER_PARTICLE = PARTICLE_TYPES.register("blood_spatter_particles", () -> new BloodSpatterParticleType(false));
        BLOOD_DRIP_PARTICLE = PARTICLE_TYPES.register("blood_drip_particles", () -> new BloodDripParticleType(false));
    }
}
