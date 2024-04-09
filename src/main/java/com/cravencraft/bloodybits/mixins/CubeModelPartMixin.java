package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelPart.Cube.class)
public abstract class CubeModelPartMixin {


//    @Redirect(method = "compile", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(FFFFFFFFFIIFFF)V"))
//    public void testRenderMod(VertexConsumer instance, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
//        BloodyBitsMod.LOGGER.info("RED GREEN BLUE VAL: {}, {}, {}", red, green, blue);
//        instance.vertex(x, y, z, red, green, blue, alpha, texU, texV, packedOverlay, packedLight, normalX, normalY, normalZ);
//    }
}
