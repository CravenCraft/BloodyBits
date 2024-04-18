package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.TextureMixinInterface;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Mixin(HttpTexture.class)
public class HttpTextureMixin extends SimpleTexture {

    @Shadow @Final private String urlString;
    @Shadow @Final private static int SKIN_WIDTH;
    private NativeImage nativeImage;

    public HttpTextureMixin(ResourceLocation pLocation) {
        super(pLocation);
    }


    @Inject(method = "load(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;", at = @At("RETURN"))
    private void getNativeImage(InputStream pStream, CallbackInfoReturnable<NativeImage> cir) throws IOException {
        BloodyBitsMod.LOGGER.info("INSIDE UPLOAD METHOD");
//        BloodyBitsMod.LOGGER.info("GET NATIVE IMAGE RGBA: {}", cir.getReturnValue().getPixelsRGBA());
        BloodyBitsMod.LOGGER.info("PLAYER IDENTIFIER: {}", Minecraft.getInstance().player.getName().getString());

        BloodyBitsUtils.setNativeImage(Minecraft.getInstance().player.getName().getString(), cir.getReturnValue());
        this.nativeImage = cir.getReturnValue();
    }

//    @Override
//    public NativeImage getTextureNativeImage() {
//        BloodyBitsMod.LOGGER.info("ADDITIONAL INFO BEFORE CRASH: {}, {}, {}", this.location, this.urlString, this.SKIN_WIDTH);
//        BloodyBitsMod.LOGGER.info("GET IN THE GET METHOD IMAGE RGBA: {}", this.nativeImage.getPixelsRGBA());
//        return this.nativeImage;
//    }
}
