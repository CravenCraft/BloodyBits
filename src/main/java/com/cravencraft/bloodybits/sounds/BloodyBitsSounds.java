package com.cravencraft.bloodybits.sounds;

import com.cravencraft.bloodybits.BloodyBitsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BloodyBitsSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BloodyBitsMod.MODID);


    public static final RegistryObject<SoundEvent> BLOOD_SPATTER = registerSoundEvents("blood_spatter");
//    public static final RegistryObject<SoundEvent> BLOOD_SPATTER_1 = registerSoundEvents("blood_spatter_1");
    public static final RegistryObject<SoundEvent> BODY_EXPLOSION = registerSoundEvents("body_explosion");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(BloodyBitsMod.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
