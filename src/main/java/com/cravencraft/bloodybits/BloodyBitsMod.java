package com.cravencraft.bloodybits;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(BloodyBitsMod.MODID)
public class BloodyBitsMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bloodybits";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogManager.getLogger("BloodyBitsMod");

    public BloodyBitsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        CommonConfig.loadCommonConfig();
        ClientConfig.loadClientConfig();
        BloodyBitsPacketHandler.register();
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(BloodyBitsMod.MODID, path);
    }
}