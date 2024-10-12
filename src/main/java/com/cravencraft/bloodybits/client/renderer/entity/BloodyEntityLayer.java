package com.cravencraft.bloodybits.client.renderer.entity;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BloodyEntityLayer <T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public BloodyEntityLayer(EntityRendererProvider.Context pContext, LivingEntityRenderer<T, M> pRenderer) {
        super(pRenderer);
        this.dispatcher = pContext.getEntityRenderDispatcher();
    }

    // TODO: Just to test rendering for now. Will make this more complex once I have a working POC.
    //       Will want to count the amount of times an entity is hit. As well as how much damage was
    //       done with each hit.
    protected float hits(T entity) {
        return entity.getMaxHealth() - entity.getHealth();
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
//        BloodyBitsMod.LOGGER.info("TRYING TO RENDER BLOODY ENTITY LAYER?");
        int i = (int) this.hits(pLivingEntity);
        RandomSource randomsource = RandomSource.create(pLivingEntity.getId());
        if (i > 0) {
            for(int j = 0; j < i; ++j) {
                pPoseStack.pushPose();
                float f = randomsource.nextFloat();
                float f1 = randomsource.nextFloat();
                float f2 = randomsource.nextFloat();
                f = -1.0F * (f * 2.0F - 1.0F);
                f1 = -1.0F * (f1 * 2.0F - 1.0F);
                f2 = -1.0F * (f2 * 2.0F - 1.0F);
                this.renderStuckItem(pPoseStack, pBuffer, pPackedLight, pLivingEntity, f, f1, f2, pPartialTick);
                pPoseStack.popPose();
            }

        }
    }

    protected void renderStuckItem(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Entity pEntity, float pX, float pY, float pZ, float pPartialTick) {
//        pEntity.isMultipartEntity()
        float f = Mth.sqrt(pX * pX + pZ * pZ);
        ResourceLocation resourceLocation = this.dispatcher.getRenderer(pEntity).getTextureLocation(pEntity);
        if (pEntity instanceof Villager villager) {
//            villager.
            BloodyBitsMod.LOGGER.info("VILLAGER RESOURCE LOCATION: {}", resourceLocation.getPath());
            BloodyBitsMod.LOGGER.info("IS MULTIPART MOB: {}", pEntity.isMultipartEntity());
            BloodyBitsMod.LOGGER.info("BOUNDING BOX: {}", pEntity.getBoundingBox());
        }
//        BloodSprayEntity bloodLayer = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), pEntity.level());
        Arrow bloodLayer = new Arrow(pEntity.level(), pEntity.getX(), pEntity.getY(), pEntity.getZ());
//        bloodLayer.setInGround(true);
        bloodLayer.setYRot((float)(Math.atan2(pX, pZ) * (double)(180F / (float)Math.PI)));
        bloodLayer.setXRot((float)(Math.atan2(pY, f) * (double)(180F / (float)Math.PI)));
        bloodLayer.yRotO = bloodLayer.getYRot();
        bloodLayer.xRotO = bloodLayer.getXRot();
        this.dispatcher.render(bloodLayer, 0.0D, 0.0D, 0.0D, 0.0F, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }
}
