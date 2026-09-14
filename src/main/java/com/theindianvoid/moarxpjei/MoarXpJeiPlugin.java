package com.theindianvoid.moarxpjei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JeiPlugin
public final class MoarXpJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath("moar_xp_jei_compat", "plugin");
    private static final Set<String> TARGET_PATHS = Set.of("xp_drain", "xp_spout", "xp_tank");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        for (RecipeHolder<?> holder : minecraft.level.getRecipeManager().getRecipes()) {
            String path = holder.id().identifier().getPath();
            boolean target = TARGET_PATHS.stream().anyMatch(path::endsWith);
            if (target && holder.value() instanceof CraftingRecipe craftingRecipe) {
                @SuppressWarnings("unchecked")
                RecipeHolder<CraftingRecipe> craftingHolder = (RecipeHolder<CraftingRecipe>) holder;
                recipes.add(craftingHolder);
            }
        }

        if (!recipes.isEmpty()) {
            registration.addRecipes(RecipeTypes.CRAFTING, recipes);
        }
    }
}
