package com.cravencraft.bloodybits.client.particle.drip;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class BloodDripParticleType extends ParticleType<BloodDripParticleOptions> {

    public BloodDripParticleType(boolean overrideLimiter) {
        super(overrideLimiter, BloodDripParticleOptions.DESERIALIZER);
    }

    @Override
    public @NotNull Codec<BloodDripParticleOptions> codec() {
        return BloodDripParticleOptions.CODEC;
    }
}
