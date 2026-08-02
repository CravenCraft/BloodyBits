package com.cravencraft.bloodybits.particle.drip;

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

public record BloodDripParticleOptions(int color, int direction, float scale) implements ParticleOptions {

    public static final MapCodec<BloodDripParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
       instance.group(
               Codec.INT.optionalFieldOf("color", ParticleRegistry.DEFAULT_BLOOD_COLOR).forGetter(BloodDripParticleOptions::color),
               Codec.INT.optionalFieldOf("location", Direction.DOWN.get3DDataValue()).forGetter(BloodDripParticleOptions::direction),
               Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodDripParticleOptions::scale)
       ).apply(instance, BloodDripParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BloodDripParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            BloodDripParticleOptions::color,
            ByteBufCodecs.VAR_INT,
            BloodDripParticleOptions::direction,
            ByteBufCodecs.FLOAT,
            BloodDripParticleOptions::scale,
            BloodDripParticleOptions::new
    );

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_DRIP_PARTICLE.get();
    }
}
