package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.client.renderer.entity.layers.InjuryLayer;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityDamageMessage;
import com.cravencraft.bloodybits.network.messages.EntityHealMessage;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    /**
     * TODO: Remove or comment out before building code for release.
     * Just a simple method made to test blood sprays by right clicking on blocks.
     */
//    @SubscribeEvent
//    public static void testBloodSpray(PlayerInteractEvent.RightClickBlock event) {
//        if (!event.getEntity().level().isClientSide()) {
//            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
//                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
//                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
//            }
//
//            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level());
//            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
//
//            bloodSprayEntity.setDeltaMovement(event.getEntity().getLookAngle());
//            event.getEntity().level().addFreshEntity(bloodSprayEntity);
//
//            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
//                    new EntityMessage(bloodSprayEntity.getId(), event.getEntity().getId()));
//        }
//    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityDeathEvent(LivingDeathEvent deathEvent) {
        if (!deathEvent.isCanceled() && deathEvent.getEntity() != null && deathEvent.getEntity().level().isClientSide()) {
            BloodyBitsUtils.INJURED_ENTITIES.remove(deathEvent.getEntity().getId());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityHealEvent(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        if (!event.isCanceled() && entity != null) {
            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new EntityHealMessage(entity.getId(), event.getAmount()));
        }
    }

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players. Will break out of the loop the second a player is found,
     * which should optimize this somewhat.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bloodOnEntityDamage(LivingDamageEvent event) {

        LivingEntity entity = event.getEntity();
        if (!event.isCanceled() && entity != null) {
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!event.getEntity().level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(event.getSource().type().msgId())) {
                int maxDamage = (int) Math.min(20, event.getAmount());
                createBloodSpray(entity, event.getSource(), maxDamage, false);
            }

            // For adding damage textures to the given entity.
            if (!ClientConfig.blackListInjurySources().contains(event.getSource().type().msgId())) {
                boolean isBurn = ClientConfig.burnDamageSources().contains(event.getSource().type().msgId());

                BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new EntityDamageMessage(entity.getId(), event.getAmount(), !isBurn, isBurn));
            }
        }
    }

//    @SubscribeEvent
//    public static void testRenderLayer(RenderLivingEvent) {
//
//    }

//    @SubscribeEvent
//    public static void registerRenderLayer(EntityRenderersEvent.AddLayers addLayersEvent) {
//        addLayersEvent.getContext().getEntityRenderDispatcher().getRenderer().ad
//        BloodyBitsMod.LOGGER.info("Registering renderers.");
//        Map<EntityType<?>, EntityRenderer<?>> rendererMap = addLayersEvent.getContext().getEntityRenderDispatcher().renderers;
//
//        for (var renderer : rendererMap.entrySet()) {
//            BloodyBitsMod.LOGGER.info("Entity: {}", renderer.getKey().getDescriptionId());
//        }
////        addLayersEvent.getContext().
//    }

    @SubscribeEvent
    public static void testRenderEvent(RenderLivingEvent.Pre event) {

        LivingEntity livingEntity = event.getEntity();
        if (ClientConfig.showEntityDamage() && livingEntity.isAlive() && livingEntity.getHealth() < livingEntity.getMaxHealth()) {

//            BloodyBitsMod.LOGGER.info("Rendering {} on client side? {}", event.getEntity(), event.getEntity().level().isClientSide());
            PoseStack poseStack = event.getPoseStack();

            MultiBufferSource bufferSource = event.getMultiBufferSource();
            float partialTicks = event.getPartialTick();
            int packedLight = event.getPackedLight();

//            poseStack.pushPose();
//            poseStack.scale(-1.0F, -1.0F, 1.0F);
//            event.getRenderer().scale(pEntity, pPoseStack, pPartialTicks);
//            poseStack.translate(0.0F, -1.501F, 0.0F);
            int entityId = livingEntity.getId();

            // Will render a random assortment of injury textures on the given entity
            // if it is contained within the map.
            if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {
                LivingEntityRenderer livingEntityRenderer = event.getRenderer();
                InjuryLayer injuryLayer = new InjuryLayer(livingEntityRenderer);


//                livingEntityRenderer
                BloodyBitsMod.LOGGER.info("Render layers: {} Does it contain it? {}", livingEntityRenderer.layers.size(), livingEntityRenderer.layers.contains(injuryLayer));

//                if (livingEntityRenderer.layers.contains())
                livingEntityRenderer.addLayer(injuryLayer);

//                EntityInjuries entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);
//
//                if (entityInjuries.smallInjuries != null && !entityInjuries.smallInjuries.isEmpty()) {
//                    for (var smallInjury : entityInjuries.smallInjuries.entrySet()) {
//                        renderDamageLayerToBuffer(event.getRenderer(), smallInjury.getKey(), livingEntity, bufferSource, poseStack, partialTicks, packedLight);
//                    }
//                }
//
//                // TODO:
//                //  - Textures are a bit too dark. Use some lighter greys.
//                //  - Actually, just have heals slowly change the opacity of the image. Once the image is at 0, then remove it.
//                //      Think about that more and see if that will work.
//                if (entityInjuries.mediumInjuries != null && !entityInjuries.mediumInjuries.isEmpty()) {
//                    for (var mediumInjury : entityInjuries.mediumInjuries.entrySet()) {
//                        renderDamageLayerToBuffer(event.getRenderer(), mediumInjury.getKey(), livingEntity, bufferSource, poseStack, partialTicks, packedLight);
//                    }
//                }
//
//                if (entityInjuries.largeInjuries != null && !entityInjuries.largeInjuries.isEmpty()) {
//                    for (var largeInjury : entityInjuries.largeInjuries.entrySet()) {
//                        renderDamageLayerToBuffer(event.getRenderer(), largeInjury.getKey(), livingEntity, bufferSource, poseStack, partialTicks, packedLight);
//                    }
//                }
            }
//            poseStack.popPose();
        }


    }

    /**
     * All the code needed to render the new entity damage layer. Essentially copied from the original render method that is being mixed into.
     */
    private static void renderDamageLayerToBuffer(LivingEntityRenderer renderer, NativeImage damageLayerTexture, LivingEntity livingEntity, MultiBufferSource buffer, PoseStack poseStack, float pPartialTicks, int pPackedLight) {
        DynamicTexture dynamicTexture = new DynamicTexture(damageLayerTexture);
        VertexConsumer customVertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(Minecraft.getInstance().getTextureManager().register("damage_layer", dynamicTexture)));

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        boolean isEntityVisible = !livingEntity.isInvisible();
        boolean canPlayerSeeInvisibleEntity;
        if (player != null) {
            canPlayerSeeInvisibleEntity = !isEntityVisible && !livingEntity.isInvisibleTo(player);
        }
        else {
            canPlayerSeeInvisibleEntity = false;
        }

//        boolean shouldEntityAppearGlowing = minecraft.shouldEntityAppearGlowing(pEntity);
//        this.getParentModel().renderType(this.getTextureLocation(pEntity));
//        RenderType rendertype = this.getParentModel().renderType(this.getTextureLocation(pEntity));
        int i = LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F);
        renderer.getModel().renderToBuffer(poseStack, customVertexConsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, canPlayerSeeInvisibleEntity ? 0.15F : 1.0F);
    }

    @SubscribeEvent
    public static void entityBleedWhenDamaged(LivingEvent event) {
        if (CommonConfig.bleedWhenDamaged() && event.getEntity() != null && !event.getEntity().level().isClientSide() && !event.getEntity().isDeadOrDying()) {
            LivingEntity entity = event.getEntity();
            double remainingHealthPercentage = entity.getHealth() / entity.getMaxHealth();
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!CommonConfig.blackListEntities().contains(entityName) && remainingHealthPercentage < 0.5) {
                double randomNumber = remainingHealthPercentage * Math.random();

                if (randomNumber < 0.001) {
                    createBloodSpray(entity, entity.damageSources().genericKill(), 1, true);
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void creeperExplosionEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity != null && !event.isCanceled() && entity instanceof Creeper creeper) {

        }
    }

    private static void createBloodSpray(LivingEntity entity, DamageSource damageSource, int damageAmount, boolean isBleedingDamage) {
        if (entity != null && damageSource != null) {
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!entity.level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(damageSource.type().msgId())) {
                //TODO: Currently, creepers don't produce blood when exploding because it's not registered as a LivingAttackEvent on THEMSELF.
                //      So, maybe have an exception happen in a damage event?
                for (int i = 0; i < damageAmount; i++) {
                    if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                        BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                        BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
                    }

                    BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), entity, entity.level());
                    BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
                    Vec3 sourceAngle;
                    if (damageSource.getEntity() != null) {
                        sourceAngle = (damageSource.getDirectEntity() != null) ? damageSource.getDirectEntity().getLookAngle() : damageSource.getEntity().getLookAngle();
                    }
                    else {
                        sourceAngle = entity.getLookAngle();
                    }

                    double xAngle = sourceAngle.x;
                    double yAngle = (isBleedingDamage) ? -sourceAngle.y : -sourceAngle.y + Math.random();
                    double zAngle = sourceAngle.z;
                    double adjustedDamage = damageAmount * CommonConfig.bloodSprayDistance();

                    // Ensure the angles are always going where they are expected to go.
                    xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random());
                    zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random());

                    xAngle *= adjustedDamage;
                    zAngle *= adjustedDamage;

                    bloodSprayEntity.setDeltaMovement(xAngle, yAngle * 0.35, zAngle);
                    entity.level().addFreshEntity(bloodSprayEntity);

                    BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                            new EntityMessage(bloodSprayEntity.getId(), entity.getId()));
                }
            }
        }
    }
}
