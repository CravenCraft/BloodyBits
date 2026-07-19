package com.cravencraft.bloodybits.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BloodSpatterParticleType extends ParticleType<BloodSpatterParticleOptions> {

    public BloodSpatterParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NotNull MapCodec<BloodSpatterParticleOptions> codec() {
        return BloodSpatterParticleOptions.CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, BloodSpatterParticleOptions> streamCodec() {
        return BloodSpatterParticleOptions.STREAM_CODEC;
    }


}
