package com.cravencraft.bloodybits.particles.custom;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BloodParticles extends TextureSheetParticle {

    float minX1 = -1.0F;
    float minX2 = -1.0F;
    float maxX1 = 1.0F;
    float maxX2 = 1.0F;
    float minY1 = -1.0F;
    float minY2 = -1.0F;
    float maxY1 = 1.0F;
    float maxY2 = 1.0F;

    protected BloodParticles(ClientLevel level, double xCoord, double yCoord, double zCoord,
                             SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = 0.8F;
        this.gravity = 0.36F;
        this.xd = xd;
        this.yd = yd;
        this.bbWidth = 3.0F;
        this.roll = 0.0F;
        this.zd = zd;
        this.quadSize *= 1.75F;
//        this.speedUpWhenYMotionIsBlocked = true;
        this.xo = 3.0;
        this.lifetime = 240;
        this.setSpriteFromAge(spriteSet);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();
//        BloodyBitsMod.LOGGER.info("WIDTH: {}", this.bbWidth);
//        this.setSize(5.0F, 2.5F);
        expandOnGround();
        fadeOut();
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
//        BloodyBitsMod.LOGGER.info("OVERRIDING RENDER SPEED Y and YO: {} - {}", this.y, this.yo);
        Vec3 vec3 = pRenderInfo.getPosition();
        float f = (float)(Mth.lerp((double)pPartialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp((double)pPartialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp((double)pPartialTicks, this.zo, this.z) - vec3.z());
//        BloodyBitsMod.LOGGER.info("F2: {}" + f2);
//        f2 += 2;
//        BloodyBitsMod.LOGGER.info("F2: {}", f2);
        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = pRenderInfo.rotation();
        } else {
            quaternionf = new Quaternionf(pRenderInfo.rotation());
            quaternionf.rotateZ(Mth.lerp(pPartialTicks, this.oRoll, this.roll));
        }

//        float minX1 = -1.0F;
//        float minX2 = -1.0F;
//        float maxX1 = 1.0F;
//        float maxX2 = 1.0F;

        if (this.y != this.yo) {
//            Math.abs(this.y - this.yo);
//            if (minX1 > -9.0F) {
//                minX1 -= 0.03F;
//            }
//            if (minX2 > -9.0F) {
//                minX2 -= 0.03F;
//            }
//            if (maxX1 < 9.0F) {
//                maxX1 += 0.03F;
//            }
//            if (maxX2 < 9.0F) {
                maxX2 += Math.abs(this.y - this.yo);

                BloodyBitsMod.LOGGER.info("OVERRIDING RENDER SPEED MAX X2: {}", maxX2);
//            }
//            if (minY1 > -2.0F) {
//                minY1 -= 0.03F;
//            }
////            minY2 -= 0.03F;
////            maxY1 += 0.03F;
//            if (maxY2 < 3.0F) {
//                maxY2 += 0.03F;
//            }
        }
        else {
            if (maxX2 > 1.0F) {
                maxX2 -= 0.03F;
            }
            if (maxX1 < 5.0F) {
                maxX1 += 0.03F;
            }
            if (minX1 > -5.0F) {
                minX1 -= 0.03F;
            }
        }

        // TODO: Just make variables for all of these vector values & modify them based on particle trajectory
        Vector3f[] avector3f = new Vector3f[]{
                new Vector3f(minX1, -1.0F, 0.0F),
                new Vector3f(minX1, maxX2, 0.0F),
                new Vector3f(maxX1, maxX2, 0.0F),
                new Vector3f(maxX1, -1.0F, 0.0F),
                new Vector3f(minX1, -1.0F, 0.0F),
                new Vector3f(minX1, maxX2, 0.0F),
                new Vector3f(maxX1, maxX2, 0.0F),
                new Vector3f(maxX1, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(pPartialTicks);

        for(int i = 0; i < 8; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        float f4 = this.getV0();
        float f5 = this.getV1();
        int j = this.getLightColor(pPartialTicks);
        pBuffer.vertex((double)avector3f[0].x(), (double)avector3f[0].y(), (double)avector3f[0].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[1].x(), (double)avector3f[1].y(), (double)avector3f[1].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[2].x(), (double)avector3f[2].y(), (double)avector3f[2].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[3].x(), (double)avector3f[3].y(), (double)avector3f[3].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[4].x(), (double)avector3f[4].y(), (double)avector3f[4].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[5].x(), (double)avector3f[5].y(), (double)avector3f[5].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[6].x(), (double)avector3f[6].y(), (double)avector3f[6].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double)avector3f[7].x(), (double)avector3f[7].y(), (double)avector3f[7].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
    }

    private void expandOnGround() {
        if (this.onGround) {
//            this.getBoundingBox()
//            this.yd = 0;
//            BloodyBitsMod.LOGGER.info("IS ON GROUND BOUNDING BOX: {}", this.getBoundingBox());
//            this.quadSize = (this.quadSize < 2.0F) ? this.quadSize + 0.1F  : this.quadSize;
        }
    }

    private void fadeOut() {
//        BloodyBitsMod.LOGGER.info("ARE WE FADING OUT");
        this.alpha = (-(1/(float)lifetime) * age + 1);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
//            BloodyBitsMod.LOGGER.info("REGISTERING THE NEW PARTICLE");
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
//            BloodyBitsMod.LOGGER.info("CREATING BLOOD PARTICLE");
            return new BloodParticles(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
