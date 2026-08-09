package com.cravencraft.bloodybits.sounds;

import com.cravencraft.bloodybits.BloodyBitsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BloodyBitsSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BloodyBitsMod.MODID);


    public static final Supplier<SoundEvent> BLOOD_SPATTER = registerSoundEvent("blood_spatter");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        var resourceLocation = ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(resourceLocation));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
