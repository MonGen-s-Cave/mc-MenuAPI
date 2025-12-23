package com.mongenscave.mcmenuapi.builder;

import com.mongenscave.mcmenuapi.handler.DynamicItemClickHandler;
import com.mongenscave.mcmenuapi.registry.DynamicClickRegistry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of BuildContext
 */
public record BuildContextImpl(Player player, Inventory inventory, String menuFileName) implements DynamicMenuBuilder.BuildContext {

    @Override
    public void registerClickHandler(int slot, @NotNull DynamicItemClickHandler handler) {
        DynamicClickRegistry.register(menuFileName, slot, handler);
    }

    @Override
    public void registerClickHandler(@NotNull int[] slots, @NotNull DynamicItemClickHandler handler) {
        DynamicClickRegistry.registerMultiple(menuFileName, slots, handler);
    }
}