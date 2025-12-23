package com.mongenscave.mcmenuapi.registry;

import com.mongenscave.mcmenuapi.builder.DynamicMenuBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for dynamic menu builders
 * Allows plugins to register dynamic content generators for menus
 */
public class DynamicMenuRegistry {

    private static final ConcurrentHashMap<String, DynamicMenuBuilder> BUILDERS = new ConcurrentHashMap<>();

    /**
     * Register a dynamic menu builder
     *
     * @param menuFileName The menu file name (e.g., "boxes-menu.yml")
     * @param builder The builder that generates dynamic content
     */
    public static void register(@NotNull String menuFileName, @NotNull DynamicMenuBuilder builder) {
        BUILDERS.put(menuFileName.toLowerCase(), builder);
    }

    /**
     * Unregister a dynamic menu builder
     *
     * @param menuFileName The menu file name
     */
    public static void unregister(@NotNull String menuFileName) {
        BUILDERS.remove(menuFileName.toLowerCase());
    }

    /**
     * Get the builder for a menu
     *
     * @param menuFileName The menu file name
     * @return The builder, or null if not registered
     */
    @Nullable
    public static DynamicMenuBuilder getBuilder(@NotNull String menuFileName) {
        return BUILDERS.get(menuFileName.toLowerCase());
    }

    /**
     * Check if a menu has a registered builder
     *
     * @param menuFileName The menu file name
     * @return true if registered
     */
    public static boolean hasBuilder(@NotNull String menuFileName) {
        return BUILDERS.containsKey(menuFileName.toLowerCase());
    }

    /**
     * Clear all registered builders
     */
    public static void clear() {
        BUILDERS.clear();
    }

    /**
     * Get the number of registered builders
     *
     * @return The count
     */
    public static int size() {
        return BUILDERS.size();
    }
}