package com.cravencraft.bloodybits.client.particle.drip;

import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record BloodDripParticleOptions(String color, int direction, float alpha) implements ParticleOptions {

    public static final Codec<BloodDripParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
       instance.group(
               Codec.STRING.optionalFieldOf("color", ParticleRegistry.DEFAULT_BLOOD_COLOR).forGetter(BloodDripParticleOptions::color),
               Codec.INT.optionalFieldOf("location", Direction.DOWN.get3DDataValue()).forGetter(BloodDripParticleOptions::direction),
               Codec.FLOAT.optionalFieldOf("alpha", 1f).forGetter(BloodDripParticleOptions::alpha)
       ).apply(instance, BloodDripParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<BloodDripParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<BloodDripParticleOptions>() {
        @Override
        public @NotNull BloodDripParticleOptions fromCommand(@NotNull ParticleType<BloodDripParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            var color = reader.readString();
            reader.expect(' ');
            var direction = reader.readInt();
            reader.expect(' ');
            var alpha = reader.readFloat();
            return new BloodDripParticleOptions(color, direction, alpha);
        }

        @Override
        public @NotNull BloodDripParticleOptions fromNetwork(@NotNull ParticleType<BloodDripParticleOptions> type, @NotNull FriendlyByteBuf buffer) {
            return new BloodDripParticleOptions(buffer.readUtf(), buffer.readInt(), buffer.readFloat());
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_DRIP_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeUtf(this.color);
        pBuffer.writeInt(this.direction);
        pBuffer.writeFloat(this.alpha);
    }

    @Override
    public @NotNull String writeToString() {
        return color + "," + direction + "," + alpha;
    }
}
