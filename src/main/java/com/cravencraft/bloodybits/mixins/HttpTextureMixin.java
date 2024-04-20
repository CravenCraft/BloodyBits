package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.PlayerSkinToServerMessage;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;

@Mixin(HttpTexture.class)
public class HttpTextureMixin extends SimpleTexture {
    public HttpTextureMixin(ResourceLocation pLocation) {
        super(pLocation);
    }

    @Inject(method = "load(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;", at = @At("RETURN"))
    private void getNativeImage(InputStream pStream, CallbackInfoReturnable<NativeImage> cir) throws IOException {
        BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new PlayerSkinToServerMessage(cir.getReturnValue().asByteArray(), Minecraft.getInstance().player.getName().hashCode()));
    }
}