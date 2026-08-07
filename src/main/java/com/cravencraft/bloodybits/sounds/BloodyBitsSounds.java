package com.cravencraft.bloodybits.sounds;

import com.cravencraft.bloodybits.BloodyBitsMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BloodyBitsSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BloodyBitsMod.MODID);


    public static final Supplier<SoundEvent> BLOOD_SPATTER = registerSoundEvent("blood_spatter");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        var resourceLocation = ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(resourceLocation));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
