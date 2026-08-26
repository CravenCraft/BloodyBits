package com.cravencraft.bloodybits.client.particle.spray;

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

public record BloodSprayParticleOptions(String color, Vec3 direction, float scale) implements ParticleOptions {

    public BloodSprayParticleOptions(String color, Double x, Double y, Double z, Float scale) {
        this(color, new Vec3(x, y, z), scale);
    }

    public static final Codec<BloodSprayParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("color").forGetter(BloodSprayParticleOptions::color),
                    Vec3.CODEC.fieldOf("direction").forGetter(BloodSprayParticleOptions::direction),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodSprayParticleOptions::scale)
            ).apply(instance, BloodSprayParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<BloodSprayParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull BloodSprayParticleOptions fromCommand(@NotNull ParticleType<BloodSprayParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
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
            return new BloodSprayParticleOptions(color, x, y, z, scale);
        }

        @Override
        public @NotNull BloodSprayParticleOptions fromNetwork(@NotNull ParticleType<BloodSprayParticleOptions> type, @NotNull FriendlyByteBuf buffer) {
            return new BloodSprayParticleOptions(buffer.readUtf(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat());
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_SPRAY_PARTICLE.get();
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
