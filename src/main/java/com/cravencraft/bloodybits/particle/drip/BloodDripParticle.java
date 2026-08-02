package com.cravencraft.bloodybits.particle.drip;

import com.cravencraft.bloodybits.particle.BloodSprayParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BloodDripParticle extends TextureSheetParticle {
    private final Direction direction;
    private float dripAmount;
//    private final TextureAtlasSprite sprite;

    protected BloodDripParticle(ClientLevel level, double x, double y, double z,
                                SpriteSet spriteSet, int color, int direction, float scale) {
        super(level, x, y, z);

        this.gravity = 0.0f;
        this.dripAmount = 0.0F;
        this.lifetime = 270;
        this.direction = Direction.from3DDataValue(direction);
        this.rCol = BloodSprayParticleOptions.red(color);
        this.gCol = BloodSprayParticleOptions.green(color);
        this.bCol = BloodSprayParticleOptions.blue(color);


        this.pickSprite(spriteSet);
        this.sprite.getU0();
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
//        this.yd -= 0.01F;
//        float x = (float) this.x;
//        float y = (float) this.y - dripAmount;
//        float z = (float) this.z;

        this.addVertex(buffer, camera, (float) this.x, (float) this.y - 0.5F, (float) z, this.sprite.getU1(), this.sprite.getV0(), partialTick);
        this.addVertex(buffer, camera, (float) this.x - 0.1F, (float) this.y - 0.5F, (float) z, this.sprite.getU0(), this.sprite.getV0(), partialTick);
        this.addVertex(buffer, camera, (float) this.x - 0.1F, (float) this.yo, (float) z, this.sprite.getU0(), this.sprite.getV1(), partialTick);
        this.addVertex(buffer, camera, (float) this.x, (float) this.yo, (float) z, this.sprite.getU1(), this.sprite.getV1(), partialTick);
    }

    private void addVertex(VertexConsumer buffer, Camera camera, float x, float y, float z, float u, float v, float partialTick) {
        buffer
                .addVertex((float) (x - camera.getPosition().x), (float) (y - camera.getPosition().y), (float) (z - camera.getPosition().z))
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setUv(u, v)
                .setLight(this.getLightColor(partialTick));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BloodDripParticleOptions> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(@NotNull BloodDripParticleOptions options, @NotNull ClientLevel level,
                                                 double x, double y, double z,
                                                 double xSpeed, double ySpeed, double zSpeed) {
            return new BloodDripParticle(level, x, y, z, this.spriteSet,
                    options.color(), options.direction(), options.scale());
        }
    }
}
