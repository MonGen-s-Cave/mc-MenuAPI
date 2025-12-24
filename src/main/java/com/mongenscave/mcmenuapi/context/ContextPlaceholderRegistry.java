package com.mongenscave.mcmenuapi.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Registry for context-aware placeholders.
 * These placeholders can access the menu context to resolve dynamic values.
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * // Register a placeholder for SellChest context
 * ContextPlaceholderRegistry.register(SellChest.class, "{earned}",
 *     (player, chest) -> FormatType.format(chest.getTotalEarned())
 * );
 *
 * // Register with prefix for all properties
 * ContextPlaceholderRegistry.registerWithPrefix(SellChest.class, "context",
 *     Map.of(
 *         "earned", (p, c) -> String.valueOf(c.getTotalEarned()),
 *         "sold", (p, c) -> String.valueOf(c.getTotalSold()),
 *         "owner", (p, c) -> c.getOwnerName()
 *     )
 * );
 * // This creates: {context.earned}, {context.sold}, {context.owner}
 * }</pre>
 */
public class ContextPlaceholderRegistry {

    // Map<ContextType, Map<Placeholder, Resolver>>
    private static final ConcurrentHashMap<Class<?>, Map<String, BiFunction<Player, Object, String>>> registry = new ConcurrentHashMap<>();

    /**
     * Registers a context-aware placeholder
     *
     * @param contextType the type of context this placeholder works with
     * @param placeholder the placeholder string (e.g., "{earned}")
     * @param resolver function that resolves the placeholder value
     * @param <T> the context type
     */
    @SuppressWarnings("unchecked")
    public static <T> void register(
            @NotNull Class<T> contextType,
            @NotNull String placeholder,
            @NotNull BiFunction<Player, T, String> resolver
    ) {
        registry.computeIfAbsent(contextType, k -> new ConcurrentHashMap<>())
                .put(placeholder, (player, context) -> resolver.apply(player, (T) context));
    }

    /**
     * Registers multiple placeholders with a common prefix
     *
     * @param contextType the type of context
     * @param prefix the prefix (e.g., "context" creates "{context.key}")
     * @param resolvers map of key to resolver functions
     * @param <T> the context type
     */
    @SuppressWarnings("unchecked")
    public static <T> void registerWithPrefix(
            @NotNull Class<T> contextType,
            @NotNull String prefix,
            @NotNull Map<String, BiFunction<Player, T, String>> resolvers
    ) {
        Map<String, BiFunction<Player, Object, String>> typeResolvers =
                registry.computeIfAbsent(contextType, k -> new ConcurrentHashMap<>());

        resolvers.forEach((key, resolver) -> {
            String placeholder = "{" + prefix + "." + key + "}";
            typeResolvers.put(placeholder, (player, context) -> resolver.apply(player, (T) context));
        });
    }

    /**
     * Resolves all registered placeholders for a player's context
     *
     * @param player the player
     * @return map of placeholder to resolved value
     */
    @NotNull
    public static Map<String, String> resolveAll(@NotNull Player player) {
        Map<String, String> resolved = new HashMap<>();

        Object context = MenuContext.getRaw(player);
        if (context == null) {
            return resolved;
        }

        Class<?> contextType = context.getClass();

        for (Map.Entry<Class<?>, Map<String, BiFunction<Player, Object, String>>> entry : registry.entrySet()) {
            if (entry.getKey().isAssignableFrom(contextType)) {
                for (Map.Entry<String, BiFunction<Player, Object, String>> placeholder : entry.getValue().entrySet()) {
                    try {
                        String value = placeholder.getValue().apply(player, context);
                        if (value != null) {
                            resolved.put(placeholder.getKey(), value);
                        }
                    } catch (Exception e) {
                        // Skip failed placeholders
                    }
                }
            }
        }

        return resolved;
    }

    /**
     * Resolves a single placeholder for a player
     *
     * @param player the player
     * @param placeholder the placeholder to resolve
     * @return the resolved value or null
     */
    @Nullable
    public static String resolve(@NotNull Player player, @NotNull String placeholder) {
        Object context = MenuContext.getRaw(player);
        if (context == null) {
            return null;
        }

        Class<?> contextType = context.getClass();

        for (Map.Entry<Class<?>, Map<String, BiFunction<Player, Object, String>>> entry : registry.entrySet()) {
            if (entry.getKey().isAssignableFrom(contextType)) {
                BiFunction<Player, Object, String> resolver = entry.getValue().get(placeholder);
                if (resolver != null) {
                    try {
                        return resolver.apply(player, context);
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Applies context placeholders to a string
     *
     * @param player the player
     * @param text the text to process
     * @return the text with placeholders replaced
     */
    @NotNull
    public static String apply(@NotNull Player player, @NotNull String text) {
        Map<String, String> placeholders = resolveAll(player);
        String result = text;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Unregisters all placeholders for a context type
     *
     * @param contextType the context type
     */
    public static void unregister(@NotNull Class<?> contextType) {
        registry.remove(contextType);
    }

    /**
     * Clears all registered placeholders
     */
    public static void clearAll() {
        registry.clear();
    }

    /**
     * Gets the number of registered context types
     *
     * @return the count
     */
    public static int size() {
        return registry.size();
    }
}