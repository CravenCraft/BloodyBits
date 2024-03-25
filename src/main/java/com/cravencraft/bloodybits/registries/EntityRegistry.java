package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {

    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BloodyBitsMod.MODID);

    public static final RegistryObject<EntityType<BloodSprayEntity>> BLOOD_SPRAY = ENTITY_TYPES.register("blood_spray",
            () -> EntityType.Builder.of((EntityType.EntityFactory<BloodSprayEntity>) BloodSprayEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).build("blood_spray"));
}
