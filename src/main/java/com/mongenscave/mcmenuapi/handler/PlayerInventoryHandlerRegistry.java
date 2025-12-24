package com.mongenscave.mcmenuapi.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for player inventory click handlers.
 * Allows menus to define custom behavior for when players click in their own inventory.
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * // Register handler for charge menu
 * PlayerInventoryHandlerRegistry.register("charge-menu.yml", context -> {
 *     ItemStack clicked = context.getClickedItem();
 *     if (clicked == null || clicked.getType().isAir()) {
 *         return ClickResult.CANCEL;
 *     }
 *
 *     SellChest chest = context.getMenuContext(SellChest.class).orElse(null);
 *     if (chest == null) return ClickResult.CANCEL;
 *
 *     if (chargeService.isChargeItem(clicked)) {
 *         chargeService.addChargeFromItem(chest, clicked);
 *         context.sendMessage("&aCharge added!");
 *         context.refreshMenu();
 *     }
 *
 *     return ClickResult.CANCEL;
 * });
 * }</pre>
 */
public class PlayerInventoryHandlerRegistry {

    private static final ConcurrentHashMap<String, PlayerInventoryClickHandler> HANDLERS = new ConcurrentHashMap<>();

    /**
     * Registers a player inventory click handler for a menu
     *
     * @param menuFileName the menu file name
     * @param handler the handler
     */
    public static void register(@NotNull String menuFileName, @NotNull PlayerInventoryClickHandler handler) {
        HANDLERS.put(menuFileName.toLowerCase(), handler);
    }

    /**
     * Gets the handler for a menu
     *
     * @param menuFileName the menu file name
     * @return the handler or null
     */
    @Nullable
    public static PlayerInventoryClickHandler getHandler(@NotNull String menuFileName) {
        return HANDLERS.get(menuFileName.toLowerCase());
    }

    /**
     * Checks if a handler exists for a menu
     *
     * @param menuFileName the menu file name
     * @return true if handler exists
     */
    public static boolean hasHandler(@NotNull String menuFileName) {
        return HANDLERS.containsKey(menuFileName.toLowerCase());
    }

    /**
     * Unregisters a handler
     *
     * @param menuFileName the menu file name
     */
    public static void unregister(@NotNull String menuFileName) {
        HANDLERS.remove(menuFileName.toLowerCase());
    }

    /**
     * Clears all handlers
     */
    public static void clearAll() {
        HANDLERS.clear();
    }

    /**
     * Gets the number of registered handlers
     *
     * @return the count
     */
    public static int size() {
        return HANDLERS.size();
    }
}