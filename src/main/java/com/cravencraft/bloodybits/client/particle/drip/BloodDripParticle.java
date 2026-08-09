package com.cravencraft.bloodybits.client.particle.drip;

import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HexFormat;

public class BloodDripParticle extends TextureSheetParticle {
    private final Direction direction;
    private double ceiling;
    private double floor;
    private float thickness;

    protected BloodDripParticle(ClientLevel level, double x, double y, double z,
                                SpriteSet spriteSet, String color, int direction, float alpha) {
        super(level, x, y, z);

        this.gravity = 0.25f;
        this.lifetime = 270;
        this.direction = Direction.from3DDataValue(direction);
        this.rCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 1, 3));
        this.gCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 3, 5));
        this.bCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color.substring(5)));
        this.ceiling = y + 0.5F;
        this.floor = y - 2;
        this.thickness = 0.05F;
        this.alpha = alpha;

        this.pickSprite(spriteSet);
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {

        if (this.thickness <= 0.0001F) {
            this.remove();
            return;
        }

        var yPos = Math.max(this.y + 0.5F, this.floor);
        var shrinkAmount = (float) (this.thickness / Math.abs((this.ceiling - this.floor) / this.yd));
        this.thickness -= shrinkAmount;

        // Vectors for the x-axis blood drip.
        Vector3f xVector1;
        Vector3f xVector2;
        Vector3f xVector3;
        Vector3f xVector4;

        // Vectors for the z-axis blood drip.
        Vector3f zVector1;
        Vector3f zVector2;
        Vector3f zVector3;
        Vector3f zVector4;

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
            zVector1 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (yPos - camera.getPosition().y), (float) ((this.z + this.thickness) - camera.getPosition().z));
            zVector2 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (yPos - camera.getPosition().y), (float) ((this.z - this.thickness) - camera.getPosition().z));
            zVector3 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (this.ceiling - camera.getPosition().y), (float) ((this.z - this.thickness) - camera.getPosition().z));
            zVector4 = new Vector3f((float) (this.x - camera.getPosition().x), (float) (this.ceiling - camera.getPosition().y), (float) ((this.z + this.thickness) - camera.getPosition().z));

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
        buffer
                .vertex(x, y, z)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv(u, v)
                .uv2(this.getLightColor(partialTick));

    }

    @Override
    public @NotNull AABB getBoundingBox() {
        float size = this.quadSize;
        return new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size + this.ceiling, this.z + size);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public record Provider(SpriteSet spriteSet) implements ParticleProvider<BloodDripParticleOptions> {

        @Override
        public Particle createParticle(@NotNull BloodDripParticleOptions options, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new BloodDripParticle(level, x, y, z, this.spriteSet,
                    options.color(), options.direction(), options.alpha());
        }
    }
}
