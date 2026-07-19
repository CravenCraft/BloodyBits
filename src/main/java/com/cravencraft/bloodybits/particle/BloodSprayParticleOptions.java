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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public record BloodSprayParticleOptions(int color, Vec3 direction, float scale) implements ParticleOptions {

    public BloodSprayParticleOptions(int color, Vec3 direction) {
        this(color, direction, 1.0f);
    }

    public static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
    ).apply(instance, Vec3::new));

    public static final MapCodec<BloodSprayParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(BloodSprayParticleOptions::color),
                    Vec3.CODEC.fieldOf("direction").forGetter(BloodSprayParticleOptions::direction),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodSprayParticleOptions::scale)
            ).apply(instance, BloodSprayParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BloodSprayParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
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

    public BloodSprayParticleOptions(Integer color, Double x, Double y, Double z, Float scale) {
        this(color, new Vec3(x, y, z), scale);
    }

    public static float red(int color) {
        return ((color >> 16) & 0xFF) / 255f;
    }

    public static float green(int color) {
        return ((color >> 8) & 0xFF) / 255f;
    }

    public static float blue(int color) {
        return (color & 0xFF) / 255f;
    }

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_EMITTER.get();
    }
}
