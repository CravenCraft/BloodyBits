package com.cravencraft.bloodybits.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BloodSprayParticleType extends ParticleType<BloodSprayParticleOptions> {

    public BloodSprayParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NotNull MapCodec<BloodSprayParticleOptions> codec() {
        return BloodSprayParticleOptions.CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, BloodSprayParticleOptions> streamCodec() {
        return BloodSprayParticleOptions.STREAM_CODEC;
    }
}
