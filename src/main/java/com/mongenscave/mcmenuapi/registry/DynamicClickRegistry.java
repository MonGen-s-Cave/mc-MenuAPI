package com.mongenscave.mcmenuapi.registry;

import com.mongenscave.mcmenuapi.handler.DynamicItemClickHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for dynamic item click handlers
 * Stores handlers for specific menu files and slots
 */
public class DynamicClickRegistry {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Integer, DynamicItemClickHandler>> HANDLERS = new ConcurrentHashMap<>();

    /**
     * Register a click handler for a specific menu and slot
     *
     * @param menuFileName The menu file name (e.g., "boxes-menu.yml")
     * @param slot The slot number
     * @param handler The click handler
     */
    public static void register(@NotNull String menuFileName, int slot, @NotNull DynamicItemClickHandler handler) {
        HANDLERS.computeIfAbsent(menuFileName.toLowerCase(), k -> new ConcurrentHashMap<>())
                .put(slot, handler);
    }

    /**
     * Register a click handler for multiple slots
     *
     * @param menuFileName The menu file name
     * @param slots Array of slot numbers
     * @param handler The click handler
     */
    public static void registerMultiple(@NotNull String menuFileName, @NotNull int[] slots, @NotNull DynamicItemClickHandler handler) {
        for (int slot : slots) {
            register(menuFileName, slot, handler);
        }
    }

    /**
     * Get the handler for a specific menu and slot
     *
     * @param menuFileName The menu file name
     * @param slot The slot number
     * @return The handler, or null if not registered
     */
    @Nullable
    public static DynamicItemClickHandler getHandler(@NotNull String menuFileName, int slot) {
        ConcurrentHashMap<Integer, DynamicItemClickHandler> menuHandlers = HANDLERS.get(menuFileName.toLowerCase());
        if (menuHandlers == null) {
            return null;
        }
        return menuHandlers.get(slot);
    }

    /**
     * Check if a handler exists for a menu and slot
     *
     * @param menuFileName The menu file name
     * @param slot The slot number
     * @return true if handler exists
     */
    public static boolean hasHandler(@NotNull String menuFileName, int slot) {
        return getHandler(menuFileName, slot) != null;
    }

    /**
     * Clear all handlers for a specific menu
     *
     * @param menuFileName The menu file name
     */
    public static void clearMenu(@NotNull String menuFileName) {
        HANDLERS.remove(menuFileName.toLowerCase());
    }

    /**
     * Clear all handlers
     */
    public static void clearAll() {
        HANDLERS.clear();
    }

    /**
     * Get the number of menus with registered handlers
     *
     * @return The count
     */
    public static int menuCount() {
        return HANDLERS.size();
    }

    /**
     * Get the total number of registered handlers across all menus
     *
     * @return The count
     */
    public static int handlerCount() {
        return HANDLERS.values().stream()
                .mapToInt(ConcurrentHashMap::size)
                .sum();
    }
}