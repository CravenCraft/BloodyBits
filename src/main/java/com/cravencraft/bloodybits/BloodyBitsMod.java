package com.cravencraft.bloodybits;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.cravencraft.bloodybits.sounds.BloodyBitsSounds;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BloodyBitsMod.MODID)
public class BloodyBitsMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bloodybits";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public BloodyBitsMod() {
        var modLoadingContext = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = modLoadingContext.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);

        ParticleRegistry.register(modEventBus);
        BloodyBitsSounds.register(modEventBus);

//        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, String.format("%s-client.toml", BloodyBitsMod.MODID));
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, String.format("%s-common.toml", BloodyBitsMod.MODID));
//        registerConfigScreen(modContainer);

    }

//    @OnlyIn(Dist.CLIENT)
//    public static void registerConfigScreen(ModContainer modContainer) {
//        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
//    }

//    public static ResourceLocation id(@NotNull String path) {
//        return new ResourceLocation(BloodyBitsMod.MODID, path);
//    }
}