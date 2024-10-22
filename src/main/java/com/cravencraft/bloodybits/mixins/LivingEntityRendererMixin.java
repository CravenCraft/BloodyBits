package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    @Shadow protected M model;
    @Shadow protected abstract boolean isBodyVisible(T pLivingEntity);
    @Shadow @Nullable protected abstract RenderType getRenderType(T pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing);
    @Shadow protected abstract float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks);

    protected LivingEntityRendererMixin(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    /**
     * TODO: Update this method javadoc.
     *
     * A monumental redirect method that I didn't think was possible. If the Client Config is set to show mob damage,
     * then this method will retrieve native image (texture) of an entity via its resource location.
     * <p>
     * Custom blood colors are then mapped to the given entity via a client config (similar to how the blood spatters are made).
     * <p>
     * The amount of pixels on the entity's texture to turn into blood textures (configurable) are determined by the
     * amount of health remaining for the entity.
     * <p>
     * A pattern map is created for the particular entity via its unique UUID. Textures are selected at random to apply
     * the blood pattern to the map, then saved. As the damage to the entity grows, more will be added to the map, and
     * as the entity heals more will be taken away.
     * <p>
     * The texture for the entity is then converted into a Dynamic Texture and applied.
     * <p>
     * NOTE: This seems to work perfectly well for most vanilla and modded entities. There are issues with certain
     * modded entities such as MCDOOM enemies and Ice and Fire dragons. I will be looking into these to see how they
     * render the textures for their entities differently to try and add compatibility for most vanilla and modded mobs.
     * <p>
     * EXTRA NOTE: The Epic Fight Mod also renders its entities differently than normal Minecraft, which allows for its
     * incredible animations. This is incompatible with this feature. So, for now it will be incompatible until I add in
     * optional support later down the line.
     */
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
              at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    private void renderBloodLayer(T entity, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource buffer, int pPackedLight, CallbackInfo ci) {
        if (ClientConfig.showEntityDamage() && entity.isAlive() && entity.getHealth() < entity.getMaxHealth()) {
            int entityId = entity.getId();

            // Will render a random assortment of injury textures on the given entity
            // if it is contained within the map.
            if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {

                EntityInjuries entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);

                if (entityInjuries.smallInjuries != null && !entityInjuries.smallInjuries.isEmpty()) {
                    for (var smallInjury : entityInjuries.smallInjuries.entrySet()) {
                        this.renderDamageLayerToBuffer(smallInjury.getKey(), entity, buffer, poseStack, pPartialTicks, pPackedLight);
                    }
                }

                // TODO:
                //  - Textures are a bit too dark. Use some lighter greys.
                //  - Actually, just have heals slowly change the opacity of the image. Once the image is at 0, then remove it.
                //      Think about that more and see if that will work.
                if (entityInjuries.mediumInjuries != null && !entityInjuries.mediumInjuries.isEmpty()) {
                    for (var mediumInjury : entityInjuries.mediumInjuries.entrySet()) {
                        this.renderDamageLayerToBuffer(mediumInjury.getKey(), entity, buffer, poseStack, pPartialTicks, pPackedLight);
                    }
                }

                if (entityInjuries.largeInjuries != null && !entityInjuries.largeInjuries.isEmpty()) {
                    for (var largeInjury : entityInjuries.largeInjuries.entrySet()) {
                        this.renderDamageLayerToBuffer(largeInjury.getKey(), entity, buffer, poseStack, pPartialTicks, pPackedLight);
                    }
                }
            }
        }
    }

    /**
     * All the code needed to render the new entity damage layer. Essentially copied from the original render method that is being mixed into.
     */
    private void renderDamageLayerToBuffer(NativeImage damageLayerTexture, T pEntity, MultiBufferSource buffer, PoseStack poseStack, float pPartialTicks,  int pPackedLight) {
        DynamicTexture dynamicTexture = new DynamicTexture(damageLayerTexture);
        VertexConsumer customVertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(Minecraft.getInstance().getTextureManager().register("damage_layer", dynamicTexture)));

        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = this.isBodyVisible(pEntity);
        boolean flag1 = !flag && !pEntity.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(pEntity);

        RenderType rendertype = this.getRenderType(pEntity, flag, flag1, flag2);
        if (rendertype != null) {
            int i = LivingEntityRenderer.getOverlayCoords(pEntity, this.getWhiteOverlayProgress(pEntity, pPartialTicks));
            this.model.renderToBuffer(poseStack, customVertexConsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }
    }
}
