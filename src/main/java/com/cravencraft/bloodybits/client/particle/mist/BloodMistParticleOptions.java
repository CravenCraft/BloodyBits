package com.cravencraft.bloodybits.client.particle.mist;

import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticleOptions;
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
import org.jetbrains.annotations.NotNull;

public record BloodMistParticleOptions(String color, Vec3 direction, float scale) implements ParticleOptions {

    public static final MapCodec<BloodMistParticleOptions> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("color").forGetter(BloodMistParticleOptions::color),
                    Vec3.CODEC.fieldOf("direction").forGetter(BloodMistParticleOptions::direction),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodMistParticleOptions::scale)
            ).apply(instance, BloodMistParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BloodMistParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            BloodMistParticleOptions::color,
            ByteBufCodecs.DOUBLE,
            bloodMistParticleOptions -> bloodMistParticleOptions.direction.x,
            ByteBufCodecs.DOUBLE,
            bloodMistParticleOptions -> bloodMistParticleOptions.direction.y,
            ByteBufCodecs.DOUBLE,
            bloodMistParticleOptions -> bloodMistParticleOptions.direction.z,
            ByteBufCodecs.FLOAT,
            BloodMistParticleOptions::scale,
            BloodMistParticleOptions::new
    );

    public BloodMistParticleOptions(String color, Double x, Double y, Double z, Float scale) {
        this(color, new Vec3(x, y, z), scale);
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_MIST_PARTICLE.get();
    }
}
