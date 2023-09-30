package net.grid.vampiresdelight.common.registry;

import net.grid.vampiresdelight.VampiresDelight;
import net.grid.vampiresdelight.common.crafting.SpreadingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VDRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, VampiresDelight.MODID);

    public static final RegistryObject<RecipeSerializer<?>> SPREADING = RECIPE_SERIALIZERS.register("spreading", SpreadingRecipe.Serializer::new);
}