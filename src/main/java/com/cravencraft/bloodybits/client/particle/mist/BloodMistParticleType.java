package com.cravencraft.bloodybits.client.particle.mist;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BloodMistParticleType extends ParticleType<BloodMistParticleOptions> {

    public BloodMistParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NotNull MapCodec<BloodMistParticleOptions> codec() {
        return BloodMistParticleOptions.CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, BloodMistParticleOptions> streamCodec() {
        return BloodMistParticleOptions.STREAM_CODEC;
    }
}