package com.cravencraft.bloodybits.client.particle.spray;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class BloodSprayParticleType extends ParticleType<BloodSprayParticleOptions> {

    public BloodSprayParticleType(boolean overrideLimiter) {
        super(overrideLimiter, BloodSprayParticleOptions.DESERIALIZER);
    }

    @Override
    public @NotNull Codec<BloodSprayParticleOptions> codec() {
        return BloodSprayParticleOptions.CODEC;
    }
}
