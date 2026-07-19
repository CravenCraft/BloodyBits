package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particle.BloodSpatterParticleType;
import com.cravencraft.bloodybits.particle.BloodSprayParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ParticleRegistry {

    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, BloodyBitsMod.MODID);

    public static void registerParticles(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }

    public static final Vector3f BLOOD_COLOR = new Vector3f(0.5f, 0.0f, 0.05f);
    public static final int DEFAULT_BLOOD_COLOR = 0xFF000000
            | (Math.round(BLOOD_COLOR.x * 255) << 16)
            | (Math.round(BLOOD_COLOR.y * 255) << 8)
            | Math.round(BLOOD_COLOR.z * 255);

    public static final Supplier<BloodSpatterParticleType> BLOOD_SPATTER_PARTICLE;
    public static final Supplier<SimpleParticleType> BLOOD_SPRAY_PARTICLE;
    public static final Supplier<BloodSprayParticleType> BLOOD_EMITTER;

    static {
        BLOOD_SPRAY_PARTICLE = PARTICLE_TYPES.register("blood_spray_particles", () -> new SimpleParticleType(false));
        BLOOD_SPATTER_PARTICLE = PARTICLE_TYPES.register("blood_spatter_particles", () -> new BloodSpatterParticleType(false));
        BLOOD_EMITTER = PARTICLE_TYPES.register("blood_emitter", () -> new BloodSprayParticleType(false));
    }
}
