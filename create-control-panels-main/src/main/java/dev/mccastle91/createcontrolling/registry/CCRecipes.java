package dev.mccastle91.createcontrolling.registry;

import dev.mccastle91.createcontrolling.recipie.KeyCustomRecipie;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CCRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "create_controlling");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KeyCustomRecipie>> KEY_RECIPE = RECIPE
            .register("key_color", () -> new SimpleCraftingRecipeSerializer<>(KeyCustomRecipie::new));
}
