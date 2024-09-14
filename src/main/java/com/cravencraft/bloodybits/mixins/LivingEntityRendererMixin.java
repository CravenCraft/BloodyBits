package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.model.EntityDamage;
import com.cravencraft.bloodybits.config.ClientConfig;
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
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    @Shadow protected M model;
    @Shadow protected abstract boolean isBodyVisible(T pLivingEntity);
    @Shadow @Nullable protected abstract RenderType getRenderType(T pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing);
    @Shadow protected abstract float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks);
    private HashMap<UUID, EntityDamage> damagedEntities = new HashMap<>();
    private List<String> noInjuryTextureEntities = new ArrayList<>();

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
            UUID entityUUID = entity.getUUID();
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            // Don't know all the entities other than the player that could have an empty entity name.
            entityName = (entityName == null) ? "" : entityName;

            // Determines if the entity has any damage textures associated with it, then places that
            // entity within a blacklist or a map with its available textures. The blacklist is made
            // solely so this conditional is only ever accessed once per entity since this method is
            // executed every tick. Want to keep things as efficient as possible.
            if (!this.damagedEntities.containsKey(entityUUID) && !this.noInjuryTextureEntities.contains(entityName)) {
                // Best to add this check to avoid unexpected crashes.
                BloodyBitsMod.LOGGER.info("ENTITY NAME: {} ENTITY UUID: {}", entityName, entityUUID);
                EntityDamage entityDamage = new EntityDamage(entityName);
                BloodyBitsMod.LOGGER.info("ENTITY DAMAGE AVAILABLE TEXTURES SIZE: {}", entityDamage.getAvailableInjuryTextures().size());
                if (entityDamage.getAvailableInjuryTextures().isEmpty()) {
                    this.noInjuryTextureEntities.add(entityName);
                }
                else {
                    this.damagedEntities.put(entityUUID, new EntityDamage(entityName));
                }
            }

            // Will render a random assortment of injury textures on the given entity
            // if it is contained within the map.
            if (this.damagedEntities.containsKey(entityUUID)) {
                if (entity.getLastDamageSource() != null) {
//                    BloodyBitsMod.LOGGER.info("ENTITY LAST DAMAGE SOURCE INFO: {}", entity.getLastDamageSource().type().msgId());
                }

                String damageType = (entity.getLastDamageSource() != null) ? entity.getLastDamageSource().type().msgId() : "generic";

                EntityDamage entityDamage = this.damagedEntities.get(entityUUID);

                entityDamage.modifyInjuryTextures(damageType, (entity.getMaxHealth() - entity.getHealth()) / entity.getMaxHealth());

                for (NativeImage damageLayerTexture : entityDamage.getPaintedAppliedInjuryTextures().keySet()) {
                    this.renderDamageLayerToBuffer(damageLayerTexture, entity, buffer, poseStack, pPartialTicks, pPackedLight);
                }
            }
        }
        else {
            this.damagedEntities.remove(entity.getUUID());
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
