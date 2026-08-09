package com.cravencraft.bloodybits.client.particle.spatter;

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

public record BloodSpatterParticleOptions(String color, int direction, float scale) implements ParticleOptions {

    public static final Codec<BloodSpatterParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
       instance.group(
               Codec.STRING.optionalFieldOf("color", ParticleRegistry.DEFAULT_BLOOD_COLOR).forGetter(BloodSpatterParticleOptions::color),
               Codec.INT.optionalFieldOf("location", Direction.DOWN.get3DDataValue()).forGetter(BloodSpatterParticleOptions::direction),
               Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodSpatterParticleOptions::scale)
       ).apply(instance, BloodSpatterParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<BloodSpatterParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<BloodSpatterParticleOptions>() {
        @Override
        public @NotNull BloodSpatterParticleOptions fromCommand(@NotNull ParticleType<BloodSpatterParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            var color = reader.readString();
            reader.expect(' ');
            var direction = reader.readInt();
            reader.expect(' ');
            var scale = reader.readFloat();
            return new BloodSpatterParticleOptions(color, direction, scale);
        }

        @Override
        public @NotNull BloodSpatterParticleOptions fromNetwork(@NotNull ParticleType<BloodSpatterParticleOptions> type, @NotNull FriendlyByteBuf buffer) {
            return new BloodSpatterParticleOptions(buffer.readUtf(), buffer.readInt(), buffer.readFloat());
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_SPATTER_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeUtf(this.color);
        pBuffer.writeInt(this.direction);
        pBuffer.writeFloat(this.scale);
    }

    @Override
    public @NotNull String writeToString() {
        return color + "," + direction + "," + scale;
    }
}
