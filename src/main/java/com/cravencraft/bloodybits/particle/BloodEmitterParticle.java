package com.cravencraft.bloodybits.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
    public static class Provider implements ParticleProvider<BloodSprayParticleOptions> {
        private final List<VariantFactory> variants;

        public Provider(List<VariantFactory> variants) {
            this.variants = variants;
        }

        @Override
        public Particle createParticle(
                @NotNull BloodSprayParticleOptions options,
                @NotNull ClientLevel level,
                double x, double y, double z,
                double dx, double dy, double dz
        ) {
            return variants.get(level.random.nextInt(variants.size())).create(options, level, x, y, z, dx, dy, dz);
        }
    }
}
