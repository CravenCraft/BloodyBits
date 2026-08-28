package com.cravencraft.bloodybits.client.particle;

import com.cravencraft.bloodybits.client.events.BloodyBitsClientEvents;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public interface CustomParticleType {

    ParticleRenderType PARTICLE_SHEET_SMOOTH_TRANSLUCENT = new ParticleRenderType() {
        @Override
        public void begin(@NotNull BufferBuilder bufferBuilder, @NotNull TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.setShader(BloodyBitsClientEvents::getSmoothFadeShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(@NotNull Tesselator tesselator) {
            tesselator.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_SMOOTH_TRANSLUCENT";
        }
    };



}
