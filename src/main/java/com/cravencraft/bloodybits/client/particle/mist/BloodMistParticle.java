package com.cravencraft.bloodybits.client.particle.mist;

import com.cravencraft.bloodybits.client.particle.CustomParticleType;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.events.BloodyBitsEvents;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;

public class BloodMistParticle extends TextureSheetParticle {
    private final String color;
    private static final float INITIAL_ALPHA = 0.35f;
    private static final int FALLBACK_INITIAL_SCALE = 3;
    private final int halfLife;

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
        this.lifetime = 60;
        this.halfLife = this.lifetime;
        this.friction = 0.001f;
        this.gravity = -0.01f;
        this.alpha = INITIAL_ALPHA;
        float modifiedScale = scale;
        modifiedScale *= (BloodyBitsEvents.isConfigLoaded) ? ClientConfig.getBloodMistScale() : FALLBACK_INITIAL_SCALE;
        this.scale(modifiedScale);
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
        this.alpha = INITIAL_ALPHA - Mth.clamp((((float) this.age) / this.halfLife) * INITIAL_ALPHA, 0.000f, INITIAL_ALPHA);
        this.scale(1.0025f);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return CustomParticleType.PARTICLE_SHEET_SMOOTH_TRANSLUCENT;
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
                    options.scale(),
                    options.direction().x, options.direction().y, options.direction().z);
        }
    }
}