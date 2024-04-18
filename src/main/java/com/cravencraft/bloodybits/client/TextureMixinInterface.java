package com.cravencraft.bloodybits.client;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.File;
import java.io.IOException;

public interface TextureMixinInterface {

    NativeImage getTextureNativeImage() throws IOException;
}
