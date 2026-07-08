package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRegistry {

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, BloodyBitsMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BloodSprayEntity>> BLOOD_SPRAY =
            ENTITY_TYPES.register("blood_spray", () -> EntityType.Builder.<BloodSprayEntity>of(BloodSprayEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .immuneTo(Blocks.POWDER_SNOW)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, "blood_spray").toString()));

//    public static ResourceKey<EntityType<?>> BLOOD_SPRAY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("blood_spray"));

//    public static final Supplier<EntityType<BloodSprayEntity>> BLOOD_SPRAY_OLD =
//            ENTITY_TYPES.register("blood_spray", () -> EntityType.Builder.<BloodSprayEntity>of(BloodSprayEntity::new, MobCategory.MISC)
//                    .fireImmune()
//                    .immuneTo(Blocks.POWDER_SNOW)
//                    .sized(0.5F, 0.5F)
//                    .clientTrackingRange(4)
//                    .updateInterval(20)
//                    .build("blood_spray"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
