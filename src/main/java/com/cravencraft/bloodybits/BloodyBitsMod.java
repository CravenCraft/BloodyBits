package com.cravencraft.bloodybits;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(BloodyBitsMod.MODID)
public class BloodyBitsMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bloodybits";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public BloodyBitsMod(IEventBus modEventBus, ModContainer modContainer) {

        ParticleRegistry.register(modEventBus);
//        BloodyBitsSounds.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, String.format("%s-client.toml", BloodyBitsMod.MODID));
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, String.format("%s-common.toml", BloodyBitsMod.MODID));

    }
}