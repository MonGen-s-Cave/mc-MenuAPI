package com.mongenscave.mcmenuapi.builder;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for building dynamic menu content
 */
@FunctionalInterface
public interface DynamicMenuBuilder {

    /**
     * Build dynamic content for the menu
     *
     * @param context The build context containing player, inventory, and utilities
     */
    void build(@NotNull BuildContext context);

    /**
     * Context for building dynamic menu content
     */
    interface BuildContext {
        /**
         * Get the player viewing the menu
         */
        @NotNull Player player();

        /**
         * Get the inventory to populate
         */
        @NotNull Inventory inventory();

        /**
         * Get the menu file name
         */
        @NotNull String menuFileName();

        /**
         * Register a click handler for a slot
         *
         * @param slot The slot number
         * @param handler The click handler
         */
        void registerClickHandler(int slot, @NotNull com.mongenscave.mcmenuapi.handler.DynamicItemClickHandler handler);

        /**
         * Register a click handler for multiple slots
         *
         * @param slots The slot numbers
         * @param handler The click handler
         */
        void registerClickHandler(@NotNull int[] slots, @NotNull com.mongenscave.mcmenuapi.handler.DynamicItemClickHandler handler);
    }
}