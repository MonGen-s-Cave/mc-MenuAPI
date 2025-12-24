package com.mongenscave.mcmenuapi.handler;

import com.mongenscave.mcmenuapi.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for player inventory clicks when a menu is open.
 * This allows custom handling of clicks in the player's own inventory.
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * PlayerInventoryHandlerRegistry.register("charge-menu.yml", (context) -> {
 *     ItemStack clicked = context.getClickedItem();
 *     if (clicked == null) return ClickResult.CANCEL;
 *
 *     SellChest chest = context.getMenuContext(SellChest.class).orElse(null);
 *     if (chest == null) return ClickResult.CANCEL;
 *
 *     if (isChargeItem(clicked)) {
 *         processCharge(chest, clicked);
 *         clicked.setAmount(0);
 *         context.refreshMenu();
 *         return ClickResult.CANCEL;
 *     }
 *
 *     return ClickResult.CANCEL; // Don't allow other items
 * });
 * }</pre>
 */
@FunctionalInterface
public interface PlayerInventoryClickHandler {

    /**
     * Handles a click in the player's inventory while a menu is open
     *
     * @param context the click context
     * @return the result determining how to handle the event
     */
    @NotNull
    ClickResult onClick(@NotNull PlayerInventoryClickContext context);

    /**
     * Result of handling a player inventory click
     */
    enum ClickResult {
        /**
         * Cancel the event (default behavior)
         */
        CANCEL,

        /**
         * Allow the event to proceed normally
         */
        ALLOW,

        /**
         * Cancel but don't send update to client
         */
        CANCEL_SILENT
    }

    /**
     * Context for player inventory click handling
     */
    interface PlayerInventoryClickContext {
        /**
         * Gets the player who clicked
         */
        @NotNull
        Player getPlayer();

        /**
         * Gets the clicked item (may be null)
         */
        @Nullable
        ItemStack getClickedItem();

        /**
         * Gets the clicked slot in player inventory
         */
        int getSlot();

        /**
         * Gets the click type
         */
        @NotNull
        ClickType getClickType();

        /**
         * Gets the menu that is open
         */
        @NotNull
        Menu getMenu();

        /**
         * Gets the menu file name
         */
        @NotNull
        String getMenuFileName();

        /**
         * Gets the menu context with type checking
         *
         * @param type the expected type
         * @param <T> the type
         * @return optional containing the context
         */
        @NotNull
        <T> java.util.Optional<T> getMenuContext(@NotNull Class<T> type);

        /**
         * Refreshes the menu
         */
        void refreshMenu();

        /**
         * Closes the menu
         */
        void closeMenu();

        /**
         * Sends a message to the player
         */
        void sendMessage(@NotNull String message);

        /**
         * Plays a sound
         */
        void playSound(@NotNull String sound);
    }
}