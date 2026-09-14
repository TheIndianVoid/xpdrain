package com.theindianvoid.moarxpjei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.Internal;
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
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        for (RecipeHolder<?> holder : Internal.getClientSyncedRecipes().values()) {
            String path = holder.id().identifier().getPath();
            boolean target = TARGET_PATHS.stream().anyMatch(path::endsWith);
            if (target && holder.value() instanceof CraftingRecipe) {
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
