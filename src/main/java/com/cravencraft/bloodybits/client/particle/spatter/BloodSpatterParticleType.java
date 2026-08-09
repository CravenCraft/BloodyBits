package com.cravencraft.bloodybits.client.particle.spatter;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class BloodSpatterParticleType extends ParticleType<BloodSpatterParticleOptions> {

    public BloodSpatterParticleType(boolean overrideLimiter) {
        super(overrideLimiter, BloodSpatterParticleOptions.DESERIALIZER);
    }

    @Override
    public @NotNull Codec<BloodSpatterParticleOptions> codec() {
        return BloodSpatterParticleOptions.CODEC;
    }
}
