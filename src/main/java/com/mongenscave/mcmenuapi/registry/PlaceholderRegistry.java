package com.mongenscave.mcmenuapi.registry;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Registry for dynamic placeholders
 * Supports global and player-specific dynamic placeholders
 */
public class PlaceholderRegistry {
    private static final Map<String, Function<Player, String>> GLOBAL_DYNAMIC = new HashMap<>();
    private static final Map<UUID, Map<String, Function<Player, String>>> PLAYER_DYNAMIC = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Function<Player, String>>> MENU_DYNAMIC = new ConcurrentHashMap<>();

    /**
     * Register a global dynamic placeholder
     * Example: registerGlobal("{balance}", player -> String.valueOf(economy.getBalance(player)))
     */
    public static void registerGlobal(@NotNull String placeholder, @NotNull Function<Player, String> function) {
        GLOBAL_DYNAMIC.put(placeholder, function);
    }

    /**
     * Register a player-specific dynamic placeholder
     */
    public static void registerPlayer(@NotNull UUID playerId, @NotNull String placeholder, @NotNull Function<Player, String> function) {
        PLAYER_DYNAMIC.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(placeholder, function);
    }

    /**
     * Register a menu-specific dynamic placeholder
     */
    public static void registerMenu(@NotNull String menuFileName, @NotNull String placeholder, @NotNull Function<Player, String> function) {
        MENU_DYNAMIC.computeIfAbsent(menuFileName, k -> new HashMap<>())
                .put(placeholder, function);
    }

    /**
     * Get a dynamic placeholder value
     * Priority: Player-specific > Menu-specific > Global
     */
    @Nullable
    public static String resolve(@NotNull Player player, @NotNull String menuFileName, @NotNull String placeholder) {
        Map<String, Function<Player, String>> playerMap = PLAYER_DYNAMIC.get(player.getUniqueId());
        if (playerMap != null && playerMap.containsKey(placeholder)) {
            return playerMap.get(placeholder).apply(player);
        }

        Map<String, Function<Player, String>> menuMap = MENU_DYNAMIC.get(menuFileName);
        if (menuMap != null && menuMap.containsKey(placeholder)) {
            return menuMap.get(placeholder).apply(player);
        }

        Function<Player, String> globalFunc = GLOBAL_DYNAMIC.get(placeholder);
        if (globalFunc != null) {
            return globalFunc.apply(player);
        }

        return null;
    }

    /**
     * Get all dynamic placeholders for a player in a menu
     */
    @NotNull
    public static Map<String, String> resolveAll(@NotNull Player player, @NotNull String menuFileName) {
        Map<String, String> resolved = new HashMap<>();
        GLOBAL_DYNAMIC.forEach((key, func) -> resolved.put(key, func.apply(player)));

        Map<String, Function<Player, String>> menuMap = MENU_DYNAMIC.get(menuFileName);
        if (menuMap != null) {
            menuMap.forEach((key, func) -> resolved.put(key, func.apply(player)));
        }

        Map<String, Function<Player, String>> playerMap = PLAYER_DYNAMIC.get(player.getUniqueId());
        if (playerMap != null) {
            playerMap.forEach((key, func) -> resolved.put(key, func.apply(player)));
        }

        return resolved;
    }

    /**
     * Clear all placeholders for a specific player
     */
    public static void clearPlayer(@NotNull UUID playerId) {
        PLAYER_DYNAMIC.remove(playerId);
    }

    /**
     * Clear all placeholders for a specific menu
     */
    public static void clearMenu(@NotNull String menuFileName) {
        MENU_DYNAMIC.remove(menuFileName);
    }

    /**
     * Clear all placeholders
     */
    public static void clearAll() {
        GLOBAL_DYNAMIC.clear();
        PLAYER_DYNAMIC.clear();
        MENU_DYNAMIC.clear();
    }
}