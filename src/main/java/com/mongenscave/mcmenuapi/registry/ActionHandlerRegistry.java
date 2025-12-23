package com.mongenscave.mcmenuapi.registry;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for custom action handlers
 * Supports global, menu-specific, and player-specific actions
 */
public class ActionHandlerRegistry {
    private static final Map<String, ActionHandler> GLOBAL_HANDLERS = new HashMap<>();
    private static final Map<String, Map<String, ActionHandler>> MENU_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, ActionHandler>> PLAYER_HANDLERS = new ConcurrentHashMap<>();

    /**
     * Register a global action handler
     * Example: registerGlobal("TOGGLE_AUTO_SELL", handler)
     */
    public static void registerGlobal(@NotNull String actionName, @NotNull ActionHandler handler) {
        GLOBAL_HANDLERS.put(actionName.toUpperCase(), handler);
    }

    /**
     * Register a menu-specific action handler
     * Example: registerMenu("main-menu.yml", "TOGGLE_AUTO_SELL", handler)
     */
    public static void registerMenu(@NotNull String menuFileName, @NotNull String actionName, @NotNull ActionHandler handler) {
        MENU_HANDLERS.computeIfAbsent(menuFileName, k -> new HashMap<>())
                .put(actionName.toUpperCase(), handler);
    }

    /**
     * Register a player-specific action handler
     * Example: registerPlayer(playerUUID, "OPEN_CUSTOM_CHEST", handler)
     */
    public static void registerPlayer(@NotNull UUID playerId, @NotNull String actionName, @NotNull ActionHandler handler) {
        PLAYER_HANDLERS.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(actionName.toUpperCase(), handler);
    }

    /**
     * Get an action handler
     * Priority: Player-specific > Menu-specific > Global
     */
    @Nullable
    public static ActionHandler getHandler(@NotNull UUID playerId, @NotNull String menuFileName, @NotNull String actionName) {
        String upperAction = actionName.toUpperCase();

        Map<String, ActionHandler> playerMap = PLAYER_HANDLERS.get(playerId);
        if (playerMap != null && playerMap.containsKey(upperAction)) {
            return playerMap.get(upperAction);
        }

        Map<String, ActionHandler> menuMap = MENU_HANDLERS.get(menuFileName);
        if (menuMap != null && menuMap.containsKey(upperAction)) {
            return menuMap.get(upperAction);
        }

        return GLOBAL_HANDLERS.get(upperAction);
    }

    /**
     * Clear all handlers for a specific player
     */
    public static void clearPlayer(@NotNull UUID playerId) {
        PLAYER_HANDLERS.remove(playerId);
    }

    /**
     * Clear all handlers for a specific menu
     */
    public static void clearMenu(@NotNull String menuFileName) {
        MENU_HANDLERS.remove(menuFileName);
    }

    /**
     * Clear all handlers
     */
    public static void clearAll() {
        GLOBAL_HANDLERS.clear();
        MENU_HANDLERS.clear();
        PLAYER_HANDLERS.clear();
    }

    /**
     * Functional interface for action handlers
     */
    @FunctionalInterface
    public interface ActionHandler {
        /**
         * Handle the action
         * @param player The player who triggered the action
         * @param clickedItem The item that was clicked (can be null)
         * @param clickType The type of click
         * @param menuFileName The menu file name
         * @param slot The slot that was clicked
         */
        void handle(@NotNull Player player, @Nullable ItemStack clickedItem, @NotNull ClickType clickType, @NotNull String menuFileName, int slot);
    }
}