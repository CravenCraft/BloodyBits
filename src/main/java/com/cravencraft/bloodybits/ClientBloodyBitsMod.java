package com.cravencraft.bloodybits;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;

@Mod(value = BloodyBitsMod.MODID)
public class ClientBloodyBitsMod {

    public ClientBloodyBitsMod(IEventBus eventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
