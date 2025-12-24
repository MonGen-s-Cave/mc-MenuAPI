package com.mongenscave.mcmenuapi.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Automatically discovers and applies placeholders from context objects using reflection.
 * No manual registration needed!
 */
public class AutoPlaceholderRegistry {

    private static final Map<Class<?>, Map<String, Method>> GETTER_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> PRIMITIVE_WRAPPERS = new HashSet<>(Arrays.asList(
            "java.lang.Boolean", "java.lang.Byte", "java.lang.Character",
            "java.lang.Short", "java.lang.Integer", "java.lang.Long",
            "java.lang.Float", "java.lang.Double", "java.lang.String"
    ));

    /**
     * Automatically resolves all placeholders from a context object
     *
     * @param player  the player (for player-specific placeholders)
     * @param context the context object (e.g., SellChest)
     * @return map of {placeholder} -> value
     */
    @NotNull
    public static Map<String, String> resolveAll(@NotNull Player player, @Nullable Object context) {
        Map<String, String> placeholders = new HashMap<>();

        if (context == null) {
            return placeholders;
        }

        Class<?> contextClass = context.getClass();
        Map<String, Method> getters = GETTER_CACHE.computeIfAbsent(contextClass,
                AutoPlaceholderRegistry::discoverGetters
        );

        for (Map.Entry<String, Method> entry : getters.entrySet()) {
            String placeholder = "{context." + entry.getKey() + "}";
            Method method = entry.getValue();

            try {
                Object value = method.invoke(context);
                String stringValue = convertToString(value);
                if (stringValue != null) {
                    placeholders.put(placeholder, stringValue);
                }
            } catch (Exception ignored) {}
        }

        placeholders.put("{player}", player.getName());
        placeholders.put("{player_uuid}", player.getUniqueId().toString());

        return placeholders;
    }

    /**
     * Applies placeholders to a string
     *
     * @param player  the player
     * @param context the context object
     * @param text    the text to process
     * @return text with placeholders replaced
     */
    @NotNull
    public static String apply(@NotNull Player player, @Nullable Object context, @NotNull String text) {
        if (context == null) {
            return text;
        }

        Map<String, String> placeholders = resolveAll(player, context);
        String result = text;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Discovers all getter methods in a class
     */
    @NotNull
    private static Map<String, Method> discoverGetters(@NotNull Class<?> clazz) {
        Map<String, Method> getters = new HashMap<>();

        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();

            if (!isGetter(method)) {
                continue;
            }

            String propertyName = extractPropertyName(methodName);
            getters.put(propertyName, method);
        }

        return getters;
    }

    /**
     * Checks if a method is a getter
     */
    private static boolean isGetter(@NotNull Method method) {
        String name = method.getName();

        if (!name.startsWith("get") && !name.startsWith("is")) {
            return false;
        }

        if (method.getParameterCount() != 0) {
            return false;
        }

        if (method.getReturnType().equals(Void.TYPE)) {
            return false;
        }

        return !name.equals("getClass") && !name.equals("hashCode");
    }

    /**
     * Extracts property name from getter method name
     */
    @NotNull
    private static String extractPropertyName(@NotNull String methodName) {
        String name;

        if (methodName.startsWith("is")) {
            name = methodName.substring(2);
        } else if (methodName.startsWith("get")) {
            name = methodName.substring(3);
        } else {
            name = methodName;
        }

        if (!name.isEmpty()) {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }

        return name;
    }

    /**
     * Converts various types to strings intelligently
     */
    @Nullable
    private static String convertToString(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        Class<?> type = value.getClass();

        if (type.isPrimitive() || PRIMITIVE_WRAPPERS.contains(type.getName())) {
            return String.valueOf(value);
        }

        if (value instanceof UUID) {
            return value.toString();
        }

        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
        }

        if (value instanceof Collection || value instanceof Map) {
            return null;
        }

        if (type.isArray()) {
            return null;
        }

        if (type.isEnum()) {
            return ((Enum<?>) value).name();
        }

        return value.toString();
    }

    /**
     * Clears the getter cache (useful for testing)
     */
    public static void clearCache() {
        GETTER_CACHE.clear();
    }

    /**
     * Gets the number of cached classes
     */
    public static int getCacheSize() {
        return GETTER_CACHE.size();
    }
}