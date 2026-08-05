package com.cravencraft.bloodybits.particle.spray;

import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record BloodSprayParticleOptions(String color, Vec3 direction, float scale) implements ParticleOptions {

    public static final MapCodec<BloodSprayParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("color").forGetter(BloodSprayParticleOptions::color),
                    Vec3.CODEC.fieldOf("direction").forGetter(BloodSprayParticleOptions::direction),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodSprayParticleOptions::scale)
            ).apply(instance, BloodSprayParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BloodSprayParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            BloodSprayParticleOptions::color,
            ByteBufCodecs.DOUBLE,
            bloodSprayParticleOptions -> bloodSprayParticleOptions.direction.x,
            ByteBufCodecs.DOUBLE,
            bloodSprayParticleOptions -> bloodSprayParticleOptions.direction.y,
            ByteBufCodecs.DOUBLE,
            bloodSprayParticleOptions -> bloodSprayParticleOptions.direction.z,
            ByteBufCodecs.FLOAT,
            BloodSprayParticleOptions::scale,
            BloodSprayParticleOptions::new
    );

    public BloodSprayParticleOptions(String color, Double x, Double y, Double z, Float scale) {
        this(color, new Vec3(x, y, z), scale);
    }

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_EMITTER.get();
    }
}
