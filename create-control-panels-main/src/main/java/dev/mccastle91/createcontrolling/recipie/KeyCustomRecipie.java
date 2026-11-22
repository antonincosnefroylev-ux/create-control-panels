package dev.mccastle91.createcontrolling.recipie;

import dev.mccastle91.createcontrolling.item.KeyItem;
import dev.mccastle91.createcontrolling.registry.CCContent;
import dev.mccastle91.createcontrolling.registry.CCRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class KeyCustomRecipie extends CustomRecipe {

    public KeyCustomRecipie(CraftingBookCategory category){
        super(category);

    }

    @Override
    public boolean matches(CraftingInput input, Level level){
        boolean hasKey = false;
        boolean hasColor = false;
        if (input.ingredientCount() == 2) {
            for (int i = 0; i < input.size(); i++){
                ItemStack item = input.getItem(i);
                if (item.getItem() instanceof DyeItem){
                    hasColor = true;
                }else if(item.getItem() instanceof KeyItem){
                    hasKey = true;
                }
            }
        }
        return hasKey && hasColor;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        String color = "";
        for (int i = 0; i < input.size(); i++){
            ItemStack item = input.getItem(i);
            if (item.getItem() instanceof DyeItem dye){
                DyeColor dyeItem = dye.getDyeColor();
                color = dyeItem.getName();
            }
        }
        ItemStack itemStack = CCContent.KEY.asStack();
        KeyItem.setColorKey(itemStack, color);
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CCRecipes.KEY_RECIPE.get();
    }
}
