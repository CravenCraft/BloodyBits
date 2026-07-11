package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import net.neoforged.neoforge.registries.DeferredHolder;

public class EntityRegistry {

    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, BloodyBitsMod.MODID);

    public static final Supplier<EntityType<BloodSprayEntity>> BLOOD_SPRAY = ENTITY_TYPES.register("blood_spray",
            () -> EntityType.Builder.of((EntityType.EntityFactory<BloodSprayEntity>) BloodSprayEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .immuneTo(Blocks.POWDER_SNOW)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, BloodyBitsMod.id("blood_spray")))
    );
}
