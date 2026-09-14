package com.theindianvoid.moarxpjei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.common.Internal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JeiPlugin
public final class MoarXpJeiPlugin implements IModPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Moar XP JEI Compat");
    private static final Identifier UID = Identifier.fromNamespaceAndPath("moar_xp_jei_compat", "plugin");
    private static final Set<String> TARGET_PATHS = Set.of("xp_drain", "xp_spout", "xp_tank");
    private static final CraftingInput EMPTY_CRAFT = CraftingInput.of(0, 0, List.of());

    @Override
    public Identifier getPluginUid() {
        LOGGER.info("JEI plugin discovered");
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // All three WASD Moar XP devices are minecraft:item_frame stacks.
        // Without a subtype interpreter, JEI collapses XP Tank/Drain/Spout into the
        // normal Item Frame and shows unrelated Item Frame recipes.
        // ITEM_MODEL uniquely separates the three devices, while CUSTOM_DATA and
        // ENTITY_DATA preserve their WASD-specific identity/state.
        registration.registerFromDataComponentTypes(
                Items.ITEM_FRAME,
                DataComponents.ITEM_MODEL,
                DataComponents.CUSTOM_DATA,
                DataComponents.ENTITY_DATA
        );
        LOGGER.info("Registered Item Frame component subtypes for Moar XP devices");
    }

    private static List<RecipeHolder<CraftingRecipe>> findTargetRecipes() {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        for (RecipeHolder<?> holder : Internal.getClientSyncedRecipes().values()) {
            Identifier id = holder.id().identifier();
            String path = id.getPath();
            boolean target = TARGET_PATHS.stream().anyMatch(path::endsWith);
            if (target && holder.value() instanceof CraftingRecipe) {
                @SuppressWarnings("unchecked")
                RecipeHolder<CraftingRecipe> craftingHolder = (RecipeHolder<CraftingRecipe>) holder;
                recipes.add(craftingHolder);
                LOGGER.info("Found Moar XP recipe: {}", id);
            }
        }

        LOGGER.info("Found {} Moar XP crafting recipes in the client-synced recipe map", recipes.size());
        return recipes;
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> outputs = new ArrayList<>();

        for (RecipeHolder<CraftingRecipe> holder : findTargetRecipes()) {
            try {
                ItemStack result = holder.value().assemble(EMPTY_CRAFT);
                if (result != null && !result.isEmpty()) {
                    outputs.add(result.copy());
                    LOGGER.info("Adding JEI ingredient for {} -> {}", holder.id().identifier(), result.getHoverName().getString());
                }
            } catch (Exception e) {
                LOGGER.warn("Could not create output stack for recipe {}", holder.id().identifier(), e);
            }
        }

        if (!outputs.isEmpty()) {
            registration.addExtraItemStacks(outputs);
        }
        LOGGER.info("Registered {} Moar XP custom output stacks in JEI", outputs.size());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> recipes = findTargetRecipes();
        if (!recipes.isEmpty()) {
            registration.addRecipes(RecipeTypes.CRAFTING, recipes);
        }
        LOGGER.info("Registered {} Moar XP recipes in JEI", recipes.size());
    }
}
