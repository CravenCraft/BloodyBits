package com.cravencraft.bloodybits.client.particle.mist;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class BloodMistParticleType extends ParticleType<BloodMistParticleOptions> {

    public BloodMistParticleType(boolean overrideLimiter) {
        super(overrideLimiter, BloodMistParticleOptions.DESERIALIZER);
    }

    @Override
    public @NotNull Codec<BloodMistParticleOptions> codec() {
        return BloodMistParticleOptions.CODEC;
    }
}
