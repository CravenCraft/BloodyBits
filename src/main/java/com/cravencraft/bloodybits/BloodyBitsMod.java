package com.cravencraft.bloodybits;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.sounds.BloodyBitsSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BloodyBitsMod.MODID)
public class BloodyBitsMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bloodybits";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public BloodyBitsMod(IEventBus modEventBus, ModContainer modContainer) {

        EntityRegistry.register(modEventBus);
        BloodyBitsSounds.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (SimpleStamina) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
//        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

//    public static ResourceLocation id(@NotNull String path) {
//        return new ResourceLocation(BloodyBitsMod.MODID, path);
//    }
}