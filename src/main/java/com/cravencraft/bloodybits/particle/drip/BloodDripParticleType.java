package com.cravencraft.bloodybits.particle.drip;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BloodDripParticleType extends ParticleType<BloodDripParticleOptions> {

    public BloodDripParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NotNull MapCodec<BloodDripParticleOptions> codec() {
        return BloodDripParticleOptions.CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, BloodDripParticleOptions> streamCodec() {
        return BloodDripParticleOptions.STREAM_CODEC;
    }
}
