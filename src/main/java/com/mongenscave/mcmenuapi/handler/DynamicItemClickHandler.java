package com.mongenscave.mcmenuapi.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for dynamic item clicks
 */
@FunctionalInterface
public interface DynamicItemClickHandler {

    /**
     * Handle a click on a dynamic item
     *
     * @param player The player who clicked
     * @param item The item that was clicked
     * @param clickType The type of click
     */
    void onClick(@NotNull Player player, @NotNull ItemStack item, @NotNull ClickType clickType);
}