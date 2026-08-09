package com.cravencraft.bloodybits.client.particle.emitter;

import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BloodEmitterParticle {

    @FunctionalInterface
    public interface VariantFactory {
        Particle create(BloodSprayParticleOptions options, ClientLevel level,
                        double x, double y, double z,
                        double dx, double dy, double dz);
    }

    private BloodEmitterParticle() {}

    @OnlyIn(Dist.CLIENT)
    public record Provider(List<VariantFactory> variants) implements ParticleProvider<BloodSprayParticleOptions> {

        @Override
        public Particle createParticle(
                @NotNull BloodSprayParticleOptions options,
                @NotNull ClientLevel level,
                double x, double y, double z,
                double dx, double dy, double dz) {
            return variants.get(level.random.nextInt(variants.size())).create(options, level, x, y, z, dx, dy, dz);
        }
    }
}
