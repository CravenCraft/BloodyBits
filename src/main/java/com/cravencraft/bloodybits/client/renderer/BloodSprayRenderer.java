package com.cravencraft.bloodybits.client.renderer;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entities.BloodSprayEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BloodSprayRenderer extends ArrowRenderer<BloodSprayEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spray.png");

    public BloodSprayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BloodSprayEntity bloodSprayEntity) {
        BloodyBitsMod.LOGGER.info("TRYING TO RENDER BLOOD SPRAY {}", TEXTURE.getPath());
        BloodyBitsMod.LOGGER.info("MORE TEXTURE INFO: {}", TEXTURE.getNamespace());
        return TEXTURE;
    }
}
