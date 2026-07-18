package com.cravencraft.bloodybits.particle;

import com.cravencraft.bloodybits.registries.ParticleRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.ClipContext.Block.VISUAL;
import static net.minecraft.world.level.ClipContext.Fluid.NONE;

public class BloodSprayParticle extends TextureSheetParticle {
    private final int color;
    float scaleTransition;
    private boolean mirrored;
    private boolean underwater;

    public BloodSprayParticle(
            ClientLevel level,
            double xCoord,
            double yCoord,
            double zCoord,
            SpriteSet spriteSet,
            int color,
            float scale,
            double xd,
            double yd,
            double zd
    ) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.color = color;
        this.xd = xd;
        this.yd = yd * 1.5 + .15f;
        this.zd = zd;
        this.quadSize *= 1f + (float) Math.random();
        this.scale(scale * 2.5f);
        this.lifetime = 100 + (int) (Math.random() * 40);
        this.gravity = 1.5F;
        this.pickSprite(spriteSet);

        this.rCol = BloodParticleOptions.red(color);
        this.gCol = BloodParticleOptions.green(color);
        this.bCol = BloodParticleOptions.blue(color);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.underwater) {
            this.gravity *= .99f;
        }

        if (this.onGround) {
            Vec3 groundLevel = level.clip(new ClipContext(this.getPos().add(0, 0.6, 0), this.getPos(), VISUAL, NONE, CollisionContext.empty())).getLocation();
            this.level.addParticle(new BloodGroundParticleOptions(this.color, this.getQuadSize(0.0F)), true, groundLevel.x, groundLevel.y, groundLevel.z, 0.0D, 0.0D, 0.0D);

            this.remove();
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return underwater ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType>, BloodEmitterParticle.VariantFactory {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new BloodSprayParticle(level, x, y, z, this.sprites, ParticleRegistry.DEFAULT_BLOOD_COLOR, 1f, dx, dy, dz);
        }

        @Override
        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new BloodSprayParticle(level, x, y, z, this.sprites, options.color(), options.scale(), dx, dy, dz);
        }
    }
}
