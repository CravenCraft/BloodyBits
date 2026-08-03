package com.cravencraft.bloodybits.particle.drip;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particle.BloodSprayParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BloodDripParticle extends TextureSheetParticle {
    private static final float DEGREES_90 = Mth.PI / 2f;
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
//            Quaternionf quaternionf = new Quaternionf();
//            this.getFacingCameraMode().setRotation(quaternionf, camera, partialTick);
//            if (this.roll != 0.0F) {
//                quaternionf.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
//            }
//
////            var yPos = Math.max(this.y + 0.5F, this.floor);
////            if (this.yd != 0) {
////                var shrinkAmount = (float) (this.thickness / Math.abs((this.ceiling - this.floor) / this.yd));
////                this.thickness -= shrinkAmount;
////            }
////
////            super.render(buffer, camera, partialTick);
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
//        BloodyBitsMod.LOGGER.info("yd: {} thickness: {}", this.yd, this.thickness);
//        BloodyBitsMod.LOGGER.info("------- Rendering Blood Drip Particle -------");

        Vector3f xVector1;
        Vector3f xVector2;
        Vector3f xVector3;
        Vector3f xVector4;
        // TODO: Simplify this in the future. Maybe find a way to use rotations?
        if (this.direction == Direction.UP) {

            // Render X-Axis blood drip.
            xVector1 = new Vector3f((float) ((this.x + this.thickness) - camera.getPosition().x), (float) (yPos - camera.getPosition().y), (float) (this.z - camera.getPosition().z));
            xVector2 = new Vector3f((float) ((this.x - this.thickness) - camera.getPosition().x), (float) (yPos - camera.getPosition().y), (float) (this.z - camera.getPosition().z));
            xVector3 = new Vector3f((float) ((this.x - this.thickness) - camera.getPosition().x), (float) (this.ceiling - camera.getPosition().y), (float) (this.z - camera.getPosition().z));
            xVector4 = new Vector3f((float) ((this.x + this.thickness) - camera.getPosition().x), (float) (this.ceiling - camera.getPosition().y), (float) (this.z - camera.getPosition().z));

            this.renderVertex(buffer, camera, xVector1.x, xVector1.y, xVector1.z, this.sprite.getU1(), this.sprite.getV0(), partialTick);
            this.renderVertex(buffer, camera, xVector2.x, xVector2.y, xVector2.z, this.sprite.getU0(), this.sprite.getV0(), partialTick);
            this.renderVertex(buffer, camera, xVector3.x, xVector3.y, xVector3.z, this.sprite.getU0(), this.sprite.getV1(), partialTick);
            this.renderVertex(buffer, camera, xVector4.x, xVector4.y, xVector4.z, this.sprite.getU1(), this.sprite.getV1(), partialTick);

            this.renderVertex(buffer, camera, xVector4.x, xVector4.y, xVector4.z, this.sprite.getU1(), this.sprite.getV1(), partialTick);
            this.renderVertex(buffer, camera, xVector3.x, xVector3.y, xVector3.z, this.sprite.getU0(), this.sprite.getV1(), partialTick);
            this.renderVertex(buffer, camera, xVector2.x, xVector2.y, xVector2.z, this.sprite.getU0(), this.sprite.getV0(), partialTick);
            this.renderVertex(buffer, camera, xVector1.x, xVector1.y, xVector1.z, this.sprite.getU1(), this.sprite.getV0(), partialTick);

            // Render Z-Axis blood drip
            var zVector1 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (yPos - camera.getPosition().y), (float) ((this.z + this.thickness) - camera.getPosition().z));
            var zVector2 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (yPos - camera.getPosition().y), (float) ((this.z - this.thickness) - camera.getPosition().z));
            var zVector3 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (this.ceiling - camera.getPosition().y), (float) ((this.z - this.thickness) - camera.getPosition().z));
            var zVector4 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (this.ceiling - camera.getPosition().y), (float) ((this.z + this.thickness) - camera.getPosition().z));

            this.renderVertex(buffer, camera, zVector1.x, zVector1.y, zVector1.z, this.sprite.getU1(), this.sprite.getV0(), partialTick);
            this.renderVertex(buffer, camera, zVector2.x, zVector2.y, zVector2.z, this.sprite.getU0(), this.sprite.getV0(), partialTick);
            this.renderVertex(buffer, camera, zVector3.x, zVector3.y, zVector3.z, this.sprite.getU0(), this.sprite.getV1(), partialTick);
            this.renderVertex(buffer, camera, zVector4.x, zVector4.y, zVector4.z, this.sprite.getU1(), this.sprite.getV1(), partialTick);


            this.renderVertex(buffer, camera, zVector4.x, zVector4.y, zVector4.z, this.sprite.getU1(), this.sprite.getV1(), partialTick);
            this.renderVertex(buffer, camera, zVector3.x, zVector3.y, zVector3.z, this.sprite.getU0(), this.sprite.getV1(), partialTick);
            this.renderVertex(buffer, camera, zVector2.x, zVector2.y, zVector2.z, this.sprite.getU0(), this.sprite.getV0(), partialTick);
            this.renderVertex(buffer, camera, zVector1.x, zVector1.y, zVector1.z, this.sprite.getU1(), this.sprite.getV0(), partialTick);
        }
        else {

        }
    }

    private void renderVertex(VertexConsumer buffer, Camera camera, float x, float y, float z, float u, float v, float partialTick) {
//        BloodyBitsMod.LOGGER.info("Adding blood drip particle to position: {}, {}, {}", x, y, z);
//        BloodyBitsMod.LOGGER.info("camera facing position: {}", camera.getPosition());
//        BloodyBitsMod.LOGGER.info("actual vertex pos: {}, {}, {}", (float) (x - camera.getPosition().x), (float) (y - camera.getPosition().y), (float) (z - camera.getPosition().z));
//        BloodyBitsMod.LOGGER.info("bounding box: {}", this.getBoundingBox());
        buffer
                .addVertex(x, y, z)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setUv(u, v)
                .setLight(this.getLightColor(partialTick));

    }

    private List<Vector3f> createBloodDripXVectors(float dripLength, Camera camera) {
        var dripVectors = new ArrayList<Vector3f>(4);

        var vector1 = new Vector3f(
                (float) ((this.x + this.thickness) - camera.getPosition().x),
                (float) (dripLength - camera.getPosition().y),
                (float) (this.z - camera.getPosition().z));
        var vector2 = new Vector3f(
                (float) ((this.x - this.thickness) - camera.getPosition().x),
                (float) (dripLength - camera.getPosition().y),
                (float) (this.z - camera.getPosition().z));
        var vector3 = new Vector3f(
                (float) ((this.x - this.thickness) - camera.getPosition().x),
                (float) (this.ceiling - camera.getPosition().y),
                (float) (this.z - camera.getPosition().z));
        var vector4 = new Vector3f(
                (float) ((this.x + this.thickness) - camera.getPosition().x),
                (float) (this.ceiling - camera.getPosition().y),
                (float) (this.z - camera.getPosition().z));

        dripVectors.add(vector1);
        dripVectors.add(vector2);
        dripVectors.add(vector3);
        dripVectors.add(vector4);

        return dripVectors;
    }

    private List<Vector3f> createBloodDripZVectors(float x, float dripLength, float z) {
        var dripVectors = new ArrayList<Vector3f>(4);

        return dripVectors;
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
