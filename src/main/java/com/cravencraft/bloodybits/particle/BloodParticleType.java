package com.cravencraft.bloodybits.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BloodParticleType extends ParticleType<BloodParticleOptions> {

    public BloodParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NotNull MapCodec<BloodParticleOptions> codec() {
        return BloodParticleOptions.CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, BloodParticleOptions> streamCodec() {
        return BloodParticleOptions.STREAM_CODEC;
    }
}
