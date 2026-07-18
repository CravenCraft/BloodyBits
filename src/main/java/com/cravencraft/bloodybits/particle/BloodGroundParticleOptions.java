package com.cravencraft.bloodybits.particle;

import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BloodGroundParticleOptions(int color, float scale) implements ParticleOptions {

    public BloodGroundParticleOptions(int color) {
        this(color, 1f);
    }

    public static final MapCodec<BloodGroundParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
       instance.group(
               Codec.INT.optionalFieldOf("color", ParticleRegistry.DEFAULT_BLOOD_COLOR).forGetter(BloodGroundParticleOptions::color),
               Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodGroundParticleOptions::scale)
       ).apply(instance, BloodGroundParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BloodGroundParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            BloodGroundParticleOptions::color,
            ByteBufCodecs.FLOAT,
            BloodGroundParticleOptions::scale,
            BloodGroundParticleOptions::new
    );

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_SPATTER_PARTICLE.get();
    }
}
