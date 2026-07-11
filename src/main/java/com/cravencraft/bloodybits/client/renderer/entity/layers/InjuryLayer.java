package com.cravencraft.bloodybits.client.renderer.entity.layers;

import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;


public class InjuryLayer <S extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M>  {
    private static final float MAX_RGB_COLOR_VALUE = 255.0F;

    public InjuryLayer(net.minecraft.client.renderer.entity.RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, int pPackedLight, @NotNull S livingEntityState, float netHeadYaw, float headPitch) {
        // TODO: 1.21.11 Port - EntityRenderState does not have direct access to the live Entity (id, health, etc.).
        // To support InjuryLayer on all living entities, you must mixin to LivingEntityRenderer#extractRenderState
        // and inject the injury data from BloodyBitsUtils.INJURED_ENTITIES into a custom interface on LivingEntityRenderState.
        /*
        if (ClientConfig.showEntityDamage() && livingEntity.isAlive() && livingEntity.getHealth() < livingEntity.getMaxHealth()) {
            int entityId = livingEntity.getId();
            // ...
        }
        */
    }

    private void renderDamageLayerToBuffer(String injuryType, NativeImage damageLayerTexture, S entityState, MultiBufferSource buffer, PoseStack poseStack, int pPackedLight) {
        // TODO: Port InjuryLayer implementation
    }
}
