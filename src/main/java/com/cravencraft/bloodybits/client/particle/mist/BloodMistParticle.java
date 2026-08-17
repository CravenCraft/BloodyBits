package com.cravencraft.bloodybits.client.particle.mist;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;

public class BloodMistParticle extends TextureSheetParticle {
    private final String color;
    private static final float INITIAL_ALPHA = 0.35f;
    private float scale;


    public BloodMistParticle(ClientLevel level,
                             double x, double y, double z,
                             SpriteSet spriteSet,
                             String color,
                             float scale,
                             double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);

        this.color = color;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.pickSprite(spriteSet);
        this.lifetime = 40;
        this.friction = 0.001f;
        this.gravity = 0.0f;
        this.alpha = INITIAL_ALPHA; // TODO: Why does the particle stop rendering below a certain IMAGE alpha level?
        this.scale = ClientConfig.getBloodMistScale() + 3;
        this.scale(this.scale);
        this.rCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 1, 3));
        this.gCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 3, 5));
        this.bCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color.substring(5)));
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();

//        this.xo = this.x;
//        this.yo = this.y;
//        this.zo = this.z;
//        if (this.age++ >= this.lifetime) {
//            this.remove();
//        } else {
//            this.yd -= 0.04D * (double)this.gravity;
//
//            this.move(this.xd, this.yd, this.zd);
//
//            this.xd *= this.friction;
//            this.yd *= this.friction;
//            this.zd *= this.friction;
//
//        }
//        var alphaSub = (INITIAL_ALPHA / this.lifetime);

//        this.alpha -= alphaSub;
//        this.scale += 1.1f;
//        this.scale(1.005f);
//        if (this.alpha <= 0.00f) {
//            this.remove();
//        }
        BloodyBitsMod.LOGGER.info("age: " + this.age);
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
        super.render(buffer, camera, partialTick);

        float f = this.age + partialTick; // Current age with the given partial tick

//        BloodyBitsMod.LOGGER.info("age: {} partial tick: {} age plus partial tick: {}", this.age, partialTick, f);
        var clamp = Mth.clamp((f / this.lifetime) * INITIAL_ALPHA, 0.000f, INITIAL_ALPHA);
//        BloodyBitsMod.LOGGER.info("clamp: {}", clamp);
        this.alpha = INITIAL_ALPHA - clamp;
//        this.setAlpha();
//        BloodyBitsMod.LOGGER.info("age: " + this.age);
        BloodyBitsMod.LOGGER.info("current alpha: " + this.alpha);
//        this.quadSize -= 0.001f;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public record Provider(SpriteSet spriteSet) implements ParticleProvider<BloodMistParticleOptions> {

        @Override
        public @NotNull Particle createParticle(@NotNull BloodMistParticleOptions options, @NotNull ClientLevel level,
                                                double x, double y, double z,
                                                double xd, double yd, double zd) {
            return new BloodMistParticle(level,
                    x, y, z,
                    this.spriteSet,
                    options.color(),
                    1.0f,
                    options.direction().x, options.direction().y, options.direction().z);
        }
    }
}
