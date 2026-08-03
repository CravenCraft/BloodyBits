package com.cravencraft.bloodybits.particle.drip;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particle.BloodSprayParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BloodDripParticle extends TextureSheetParticle {
    private final Direction direction;
    private float dripAmount;
//    private final TextureAtlasSprite sprite;
    private double ceiling;
    private double floor;
    private float thickness;

    protected BloodDripParticle(ClientLevel level, double x, double y, double z,
                                SpriteSet spriteSet, int color, int direction, float scale) {
        super(level, x, y, z);

        this.gravity = 0.5f;
        this.dripAmount = 0.0F;
        this.lifetime = 270;
        this.direction = Direction.from3DDataValue(direction);
        this.rCol = BloodSprayParticleOptions.red(color);
        this.gCol = BloodSprayParticleOptions.green(color);
        this.bCol = BloodSprayParticleOptions.blue(color);
        this.ceiling = y + 0.5F;
        this.floor = y - 2;
        this.thickness = 0.05F;

        this.pickSprite(spriteSet);
        this.sprite.getU0();
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {

//        if (this.direction ==  Direction.UP) {
//            var yPos = Math.max(this.y + 0.5F, this.floor);
//            if (this.yd != 0) {
//                var shrinkAmount = (float) (this.thickness / Math.abs((this.ceiling - this.floor) / this.yd));
//                this.thickness -= shrinkAmount;
//            }
//
//            super.render(buffer, camera, partialTick);
//        }

        var yPos = Math.max(this.y + 0.5F, this.floor);
        if (this.yd != 0) {
            var shrinkAmount = (float) (this.thickness / Math.abs((this.ceiling - this.floor) / this.yd));
            this.thickness -= shrinkAmount;
        }
//        this.thickness -= 0.0001F;
//        var bb = this.getBoundingBox();
//        bb = bb.setMaxY(this.ceiling);
//        this.setBoundingBox(bb);
//        this.yo;
//        float x = (float) this.x;
//        float y = (float) this.y - dripAmount;
//        float z = (float) this.z;
        BloodyBitsMod.LOGGER.info("yd: {} thickness: {}", this.yd, this.thickness);
//        BloodyBitsMod.LOGGER.info("------- Rendering Blood Drip Particle -------");
        this.renderVertex(buffer, camera, (float) this.x + this.thickness, (float) yPos, (float) z, this.sprite.getU1(), this.sprite.getV0(), partialTick);
        this.renderVertex(buffer, camera, (float) this.x - this.thickness, (float) yPos, (float) z, this.sprite.getU0(), this.sprite.getV0(), partialTick);
        this.renderVertex(buffer, camera, (float) this.x - this.thickness, (float) this.ceiling, (float) z, this.sprite.getU0(), this.sprite.getV1(), partialTick);
        this.renderVertex(buffer, camera, (float) this.x + this.thickness, (float) this.ceiling, (float) z, this.sprite.getU1(), this.sprite.getV1(), partialTick);
    }

    private void renderVertex(VertexConsumer buffer, Camera camera, float x, float y, float z, float u, float v, float partialTick) {
//        BloodyBitsMod.LOGGER.info("Adding blood drip particle to position: {}, {}, {}", x, y, z);
//        BloodyBitsMod.LOGGER.info("camera facing position: {}", camera.getPosition());
//        BloodyBitsMod.LOGGER.info("actual vertex pos: {}, {}, {}", (float) (x - camera.getPosition().x), (float) (y - camera.getPosition().y), (float) (z - camera.getPosition().z));
//        BloodyBitsMod.LOGGER.info("bounding box: {}", this.getBoundingBox());

        if (this.direction == Direction.UP) {

            Vector3f front = new Vector3f(
                    (float) (x - camera.getPosition().x),
                    (float) (y - camera.getPosition().y),
                    (float) (z - camera.getPosition().z));

            buffer.addVertex(front)
                    .setUv(u, v)
                    .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                    .setLight(this.getLightColor(partialTick));
        }
        else {
            buffer
                    .addVertex((float) (x - camera.getPosition().x), (float) (y - camera.getPosition().y), (float) (z - camera.getPosition().z))
                    .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                    .setUv(u, v)
                    .setLight(this.getLightColor(partialTick));
        }

    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        float size = getQuadSize(partialTicks);
        return new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size + this.ceiling, this.z + size);
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
