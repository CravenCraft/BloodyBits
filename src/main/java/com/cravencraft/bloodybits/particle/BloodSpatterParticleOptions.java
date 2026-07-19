package com.cravencraft.bloodybits.particle;

import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BloodSpatterParticleOptions(int color, int direction, float scale) implements ParticleOptions {

    public BloodSpatterParticleOptions(int color, int direction) {
        this(color, direction, 1f);
    }

    public static final MapCodec<BloodSpatterParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
       instance.group(
               Codec.INT.optionalFieldOf("color", ParticleRegistry.DEFAULT_BLOOD_COLOR).forGetter(BloodSpatterParticleOptions::color),
               Codec.INT.optionalFieldOf("location", Direction.DOWN.get3DDataValue()).forGetter(BloodSpatterParticleOptions::direction),
               Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodSpatterParticleOptions::scale)
       ).apply(instance, BloodSpatterParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BloodSpatterParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            BloodSpatterParticleOptions::color,
            ByteBufCodecs.VAR_INT,
            BloodSpatterParticleOptions::direction,
            ByteBufCodecs.FLOAT,
            BloodSpatterParticleOptions::scale,
            BloodSpatterParticleOptions::new
    );

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_SPATTER_PARTICLE.get();
    }
}
