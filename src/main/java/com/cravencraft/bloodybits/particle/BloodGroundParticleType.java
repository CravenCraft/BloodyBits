package com.cravencraft.bloodybits.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BloodGroundParticleType extends ParticleType<BloodGroundParticleOptions> {

    public BloodGroundParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NotNull MapCodec<BloodGroundParticleOptions> codec() {
        return BloodGroundParticleOptions.CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, BloodGroundParticleOptions> streamCodec() {
        return BloodGroundParticleOptions.STREAM_CODEC;
    }


}
