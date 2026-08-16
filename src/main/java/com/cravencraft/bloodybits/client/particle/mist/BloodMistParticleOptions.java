package com.cravencraft.bloodybits.client.particle.mist;

import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record BloodMistParticleOptions(String color, Vec3 direction, float scale) implements ParticleOptions {

    public BloodMistParticleOptions(String color, Double x, Double y, Double z, Float scale) {
        this(color, new Vec3(x, y, z), scale);
    }

    public static final Codec<BloodMistParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("color").forGetter(BloodMistParticleOptions::color),
                    Vec3.CODEC.fieldOf("direction").forGetter(BloodMistParticleOptions::direction),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodMistParticleOptions::scale)
            ).apply(instance, BloodMistParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<BloodMistParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<BloodMistParticleOptions>() {
        @Override
        public @NotNull BloodMistParticleOptions fromCommand(@NotNull ParticleType<BloodMistParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            var color = reader.readString();
            reader.expect(' ');
            var x = reader.readDouble();
            reader.expect(' ');
            var y = reader.readDouble();
            reader.expect(' ');
            var z = reader.readDouble();
            reader.expect(' ');
            var scale = reader.readFloat();
            return new BloodMistParticleOptions(color, x, y, z, scale);
        }

        @Override
        public @NotNull BloodMistParticleOptions fromNetwork(@NotNull ParticleType<BloodMistParticleOptions> type, @NotNull FriendlyByteBuf buffer) {
            return new BloodMistParticleOptions(buffer.readUtf(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat());
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_MIST_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeUtf(this.color);
        pBuffer.writeDouble(this.direction.x);
        pBuffer.writeDouble(this.direction.y);
        pBuffer.writeDouble(this.direction.z);
        pBuffer.writeFloat(this.scale);
    }

    @Override
    public @NotNull String writeToString() {
        return color + "," + direction.x + "," + direction.y + "," + direction.z + "," + scale;
    }
}
