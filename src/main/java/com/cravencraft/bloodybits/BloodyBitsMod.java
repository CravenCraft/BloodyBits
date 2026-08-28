package com.cravencraft.bloodybits;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BloodyBitsMod.MODID)
public class BloodyBitsMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bloodybits";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static boolean isClientConfigLoaded;
    public static boolean isCommonConfigLoaded;

    public BloodyBitsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ParticleRegistry.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);
    }

    private void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ClientConfig.SPEC) {
            isClientConfigLoaded = true;
        }
        if  (event.getConfig().getSpec() == CommonConfig.SPEC) {
            isCommonConfigLoaded = true;
        }
    }

    private void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ClientConfig.SPEC) {
            isClientConfigLoaded = true;
        }
        if  (event.getConfig().getSpec() == CommonConfig.SPEC) {
            isCommonConfigLoaded = true;
        }
    }

//    @OnlyIn(Dist.CLIENT)
//    public static void registerConfigScreen(ModContainer modContainer) {
//        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
//    }

//    public static ResourceLocation id(@NotNull String path) {
//        return new ResourceLocation(BloodyBitsMod.MODID, path);
//    }
}