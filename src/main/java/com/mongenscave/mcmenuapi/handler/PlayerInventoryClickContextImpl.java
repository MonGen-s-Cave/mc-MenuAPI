package com.mongenscave.mcmenuapi.handler;

import com.mongenscave.mcmenuapi.context.MenuContext;
import com.mongenscave.mcmenuapi.menu.Menu;
import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Implementation of PlayerInventoryClickContext
 */
public class PlayerInventoryClickContextImpl implements PlayerInventoryClickHandler.PlayerInventoryClickContext {

    private final Player player;
    private final ItemStack clickedItem;
    private final int slot;
    private final ClickType clickType;
    private final Menu menu;
    private final String menuFileName;

    public PlayerInventoryClickContextImpl(
            @NotNull Player player,
            @Nullable ItemStack clickedItem,
            int slot,
            @NotNull ClickType clickType,
            @NotNull Menu menu,
            @NotNull String menuFileName
    ) {
        this.player = player;
        this.clickedItem = clickedItem;
        this.slot = slot;
        this.clickType = clickType;
        this.menu = menu;
        this.menuFileName = menuFileName;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @Nullable ItemStack getClickedItem() {
        return clickedItem;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ClickType getClickType() {
        return clickType;
    }

    @Override
    public @NotNull Menu getMenu() {
        return menu;
    }

    @Override
    public @NotNull String getMenuFileName() {
        return menuFileName;
    }

    @Override
    public @NotNull <T> Optional<T> getMenuContext(@NotNull Class<T> type) {
        return MenuContext.get(player, type);
    }

    @Override
    public void refreshMenu() {
        menu.refresh(player);
    }

    @Override
    public void closeMenu() {
        player.closeInventory();
    }

    @Override
    public void sendMessage(@NotNull String message) {
        player.sendMessage(ColorProcessor.process(message));
    }

    @Override
    public void playSound(@NotNull String sound) {
        try {
            Sound soundEnum = Sound.valueOf(sound.toUpperCase());
            player.playSound(player.getLocation(), soundEnum, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }
}