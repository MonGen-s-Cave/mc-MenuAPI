package com.mongenscave.mcmenuapi.action;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.context.MenuContext;
import com.mongenscave.mcmenuapi.menu.Menu;
import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Provides full context for action handlers.
 * Contains all information about the click event and utility methods.
 */
public class ActionContext {

    private final Player player;
    private final Menu menu;
    private final String menuFileName;

    @Getter
    private final int slot;
    private final ItemStack clickedItem;
    private final ClickType clickType;

    public ActionContext(
            @NotNull Player player,
            @NotNull Menu menu,
            @NotNull String menuFileName,
            int slot,
            @Nullable ItemStack clickedItem,
            @NotNull ClickType clickType
    ) {
        this.player = player;
        this.menu = menu;
        this.menuFileName = menuFileName;
        this.slot = slot;
        this.clickedItem = clickedItem;
        this.clickType = clickType;
    }

    // === Getters ===

    /**
     * Gets the player who clicked
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the menu instance
     */
    @NotNull
    public Menu getMenu() {
        return menu;
    }

    /**
     * Gets the menu file name
     */
    @NotNull
    public String getMenuFileName() {
        return menuFileName;
    }

    /**
     * Gets the clicked item (may be null)
     */
    @Nullable
    public ItemStack getClickedItem() {
        return clickedItem;
    }

    /**
     * Gets the click type
     */
    @NotNull
    public ClickType getClickType() {
        return clickType;
    }

    // === Context Access ===

    /**
     * Gets the menu context with type checking
     *
     * @param type the expected context type
     * @param <T> the type
     * @return optional containing the context
     */
    @NotNull
    public <T> Optional<T> getContext(@NotNull Class<T> type) {
        return MenuContext.get(player, type);
    }

    /**
     * Gets the menu context, throwing if not present
     *
     * @param type the expected context type
     * @param <T> the type
     * @return the context
     * @throws IllegalStateException if context is not present or wrong type
     */
    @NotNull
    public <T> T requireContext(@NotNull Class<T> type) {
        return getContext(type).orElseThrow(() ->
                new IllegalStateException("Menu context of type " + type.getName() + " not found for player " + player.getName())
        );
    }

    /**
     * Checks if a context of specific type exists
     *
     * @param type the type to check
     * @return true if context exists and matches type
     */
    public boolean hasContext(@NotNull Class<?> type) {
        return MenuContext.has(player, type);
    }

    // === Click Type Helpers ===

    /**
     * Checks if this was a left click
     */
    public boolean isLeftClick() {
        return clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT;
    }

    /**
     * Checks if this was a right click
     */
    public boolean isRightClick() {
        return clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT;
    }

    /**
     * Checks if this was a shift click
     */
    public boolean isShiftClick() {
        return clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT;
    }

    /**
     * Checks if this was a middle click
     */
    public boolean isMiddleClick() {
        return clickType == ClickType.MIDDLE;
    }

    // === Utility Methods ===

    /**
     * Refreshes the current menu for the player
     */
    public void refresh() {
        menu.refresh(player);
    }

    /**
     * Closes the current menu
     */
    public void close() {
        player.closeInventory();
    }

    /**
     * Opens another menu for the player
     *
     * @param fileName the menu file name
     */
    public void open(@NotNull String fileName) {
        McMenuAPI.getInstance().openMenu(player, fileName);
    }

    /**
     * Opens another menu with a new context
     *
     * @param fileName the menu file name
     * @param context the new context
     * @param <T> the context type
     */
    public <T> void open(@NotNull String fileName, @NotNull T context) {
        McMenuAPI.getInstance().openMenu(player, fileName, context);
    }

    /**
     * Opens another menu, preserving the current context
     *
     * @param fileName the menu file name
     */
    public void openPreserveContext(@NotNull String fileName) {
        // Context is preserved in MenuContext, just open the new menu
        McMenuAPI.getInstance().openMenuPreserveContext(player, fileName);
    }

    /**
     * Sends a message to the player (with color processing)
     *
     * @param message the message
     */
    public void sendMessage(@NotNull String message) {
        player.sendMessage(ColorProcessor.process(message));
    }

    /**
     * Plays a sound at the player's location
     *
     * @param sound the sound name
     */
    public void playSound(@NotNull String sound) {
        playSound(sound, 1.0f, 1.0f);
    }

    /**
     * Plays a sound at the player's location with volume and pitch
     *
     * @param sound the sound name
     * @param volume the volume
     * @param pitch the pitch
     */
    public void playSound(@NotNull String sound, float volume, float pitch) {
        try {
            Sound soundEnum = Sound.valueOf(sound.toUpperCase());
            player.playSound(player.getLocation(), soundEnum, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Try as custom sound
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    /**
     * Updates the context and refreshes the menu
     *
     * @param context the new context
     * @param <T> the context type
     */
    public <T> void updateContext(@NotNull T context) {
        MenuContext.set(player, context);
        refresh();
    }

    // === Builder for internal use ===

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Player player;
        private Menu menu;
        private String menuFileName;
        private int slot;
        private ItemStack clickedItem;
        private ClickType clickType;

        public Builder player(@NotNull Player player) {
            this.player = player;
            return this;
        }

        public Builder menu(@NotNull Menu menu) {
            this.menu = menu;
            return this;
        }

        public Builder menuFileName(@NotNull String menuFileName) {
            this.menuFileName = menuFileName;
            return this;
        }

        public Builder slot(int slot) {
            this.slot = slot;
            return this;
        }

        public Builder clickedItem(@Nullable ItemStack clickedItem) {
            this.clickedItem = clickedItem;
            return this;
        }

        public Builder clickType(@NotNull ClickType clickType) {
            this.clickType = clickType;
            return this;
        }

        public ActionContext build() {
            return new ActionContext(player, menu, menuFileName, slot, clickedItem, clickType);
        }
    }
}