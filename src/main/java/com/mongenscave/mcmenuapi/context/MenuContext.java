package com.mongenscave.mcmenuapi.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages context objects associated with open menus for players.
 * Context allows passing arbitrary data to menus that persists during the menu lifecycle.
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * // Opening a menu with context
 * menuAPI.openMenu(player, "chest-menu.yml", sellChest);
 *
 * // Retrieving context in an action handler
 * SellChest chest = MenuContext.get(player, SellChest.class).orElseThrow();
 * }</pre>
 */
public class MenuContext {

    private static final ConcurrentHashMap<UUID, Object> playerContexts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Class<?>> contextTypes = new ConcurrentHashMap<>();

    /**
     * Sets the context for a player's menu
     *
     * @param player the player
     * @param context the context object
     * @param <T> the type of context
     */
    public static <T> void set(@NotNull Player player, @NotNull T context) {
        playerContexts.put(player.getUniqueId(), context);
        contextTypes.put(player.getUniqueId(), context.getClass());
    }

    /**
     * Gets the context for a player's menu
     *
     * @param player the player
     * @param type the expected type of context
     * @param <T> the type
     * @return optional containing the context if present and matches type
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> get(@NotNull Player player, @NotNull Class<T> type) {
        Object context = playerContexts.get(player.getUniqueId());
        if (context == null) {
            return Optional.empty();
        }
        if (type.isInstance(context)) {
            return Optional.of((T) context);
        }
        return Optional.empty();
    }

    /**
     * Gets the raw context for a player without type checking
     *
     * @param player the player
     * @return the context object or null
     */
    @Nullable
    public static Object getRaw(@NotNull Player player) {
        return playerContexts.get(player.getUniqueId());
    }

    /**
     * Gets the type of context stored for a player
     *
     * @param player the player
     * @return optional containing the context class
     */
    @NotNull
    public static Optional<Class<?>> getType(@NotNull Player player) {
        return Optional.ofNullable(contextTypes.get(player.getUniqueId()));
    }

    /**
     * Checks if a player has a context set
     *
     * @param player the player
     * @return true if context exists
     */
    public static boolean has(@NotNull Player player) {
        return playerContexts.containsKey(player.getUniqueId());
    }

    /**
     * Checks if a player has a context of specific type
     *
     * @param player the player
     * @param type the type to check
     * @return true if context exists and matches type
     */
    public static boolean has(@NotNull Player player, @NotNull Class<?> type) {
        Object context = playerContexts.get(player.getUniqueId());
        return type.isInstance(context);
    }

    /**
     * Removes the context for a player
     *
     * @param player the player
     */
    public static void clear(@NotNull Player player) {
        playerContexts.remove(player.getUniqueId());
        contextTypes.remove(player.getUniqueId());
    }

    /**
     * Clears all stored contexts
     */
    public static void clearAll() {
        playerContexts.clear();
        contextTypes.clear();
    }

    /**
     * Gets the number of active contexts
     *
     * @return the count
     */
    public static int size() {
        return playerContexts.size();
    }
}