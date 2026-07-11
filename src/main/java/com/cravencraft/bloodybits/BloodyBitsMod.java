package com.cravencraft.bloodybits;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.sounds.BloodyBitsSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(BloodyBitsMod.MODID)
public class BloodyBitsMod
{
    public static final String MODID = "bloodybits";
    public static final Logger LOGGER = LogManager.getLogger("BloodyBitsMod");

    public BloodyBitsMod(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {

        CommonConfig.loadCommonConfig();
        ClientConfig.loadClientConfig();
        
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, CommonConfig.BUILDER.build());
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, ClientConfig.BUILDER.build());
        
        // Networking registration is now done via events in 1.21, but we can register the event listener here or in the handler
        modEventBus.addListener(BloodyBitsPacketHandler::register);
        
        if (net.neoforged.fml.loading.FMLLoader.getDist().isClient()) {
            modEventBus.addListener(com.cravencraft.bloodybits.client.ClientSetup::registerRenderers);
        }
        
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        BloodyBitsSounds.register(modEventBus);
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, path);
    }
}