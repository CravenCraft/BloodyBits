package com.cravencraft.bloodybits.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class BloodSprayParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    // First four parameters are self-explanatory. The SpriteSet parameter is provided by the
    // ParticleProvider, see below. You may also add additional parameters as needed, e.g. xSpeed/ySpeed/zSpeed.
    public BloodSprayParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
//        this.xd = x;
//        this.yd = y;
//        this.zd = z;
        this.gravity = 0; // Our particle floats in midair now, because why not.
        this.lifetime = 600;
        this.setSize(2.0f, 2.0f);
        this.scale(3f);
        this.spriteSet = spriteSet;

        // We set the initial sprite here since ticking is not guaranteed to set the sprite
        // before the render method is called.
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        // Set the sprite for the current particle age, i.e. advance the animation.
        this.setSpriteFromAge(spriteSet);
        // Let super handle further movement. You may replace this with your own movement if needed.
        // You may also override move() if you only want to modify the built-in movement.
        super.tick();
//        if (this.onGround) {
//            this.level.addParticle(ParticleHelper.BLOOD_GROUND, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
//            this.remove();
//        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        // TODO: Probably want translucent to allow it to fade over time.
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        // A set of particle sprites.
        private final SpriteSet spriteSet;

        // The registration function passes a SpriteSet, so we accept that and store it for further use.
        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        // This is where the magic happens. We return a new particle each time this method is called!
        // The type of the first parameter matches the generic type passed to the super interface.
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            // We don't use the type and speed, and pass in everything else. You may of course use them if needed.
            return new BloodSprayParticle(level, x, y, z, spriteSet);
        }

//        @Override
//        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
//            return new BloodParticle(level, x, y, z, this.sprites, this.decalType, this.decalDirection, options.color(), options.scale(), dx, dy, dz);
//        }
    }
}
