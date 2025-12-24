package com.mongenscave.mcmenuapi.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for context-aware action handlers.
 * Supports global, menu-specific handlers with full ActionContext support.
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * // Register a global action
 * ContextActionRegistry.registerGlobal("CLOSE_WITH_SOUND", ctx -> {
 *     ctx.playSound("UI_BUTTON_CLICK");
 *     ctx.close();
 * });
 *
 * // Register a menu-specific action
 * ContextActionRegistry.register("main-menu.yml", "TOGGLE_AUTO_SELL", ctx -> {
 *     SellChest chest = ctx.requireContext(SellChest.class);
 *     chest.setAutoSellEnabled(!chest.isAutoSellEnabled());
 *     ctx.refresh();
 * });
 * }</pre>
 */
public class ContextActionRegistry {

    private static final Map<String, ContextActionHandler> GLOBAL_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, ContextActionHandler>> MENU_HANDLERS = new ConcurrentHashMap<>();

    /**
     * Registers a global action handler (available in all menus)
     *
     * @param actionName the action name (case-insensitive)
     * @param handler the handler
     */
    public static void registerGlobal(@NotNull String actionName, @NotNull ContextActionHandler handler) {
        GLOBAL_HANDLERS.put(actionName.toUpperCase(), handler);
    }

    /**
     * Registers a menu-specific action handler
     *
     * @param menuFileName the menu file name
     * @param actionName the action name (case-insensitive)
     * @param handler the handler
     */
    public static void register(
            @NotNull String menuFileName,
            @NotNull String actionName,
            @NotNull ContextActionHandler handler
    ) {
        MENU_HANDLERS.computeIfAbsent(menuFileName.toLowerCase(), k -> new HashMap<>())
                .put(actionName.toUpperCase(), handler);
    }

    /**
     * Gets an action handler
     * Priority: Menu-specific > Global
     *
     * @param menuFileName the menu file name
     * @param actionName the action name
     * @return the handler or null
     */
    @Nullable
    public static ContextActionHandler getHandler(@NotNull String menuFileName, @NotNull String actionName) {
        String upperAction = actionName.toUpperCase();

        // Check menu-specific first
        Map<String, ContextActionHandler> menuMap = MENU_HANDLERS.get(menuFileName.toLowerCase());
        if (menuMap != null) {
            ContextActionHandler handler = menuMap.get(upperAction);
            if (handler != null) {
                return handler;
            }
        }

        // Fall back to global
        return GLOBAL_HANDLERS.get(upperAction);
    }

    /**
     * Checks if an action handler exists
     *
     * @param menuFileName the menu file name
     * @param actionName the action name
     * @return true if handler exists
     */
    public static boolean hasHandler(@NotNull String menuFileName, @NotNull String actionName) {
        return getHandler(menuFileName, actionName) != null;
    }

    /**
     * Executes an action handler if it exists
     *
     * @param context the action context
     * @param actionName the action name
     * @return true if handler was found and executed
     */
    public static boolean execute(@NotNull com.mongenscave.mcmenuapi.action.ActionContext context, @NotNull String actionName) {
        ContextActionHandler handler = getHandler(context.getMenuFileName(), actionName);
        if (handler != null) {
            handler.handle(context);
            return true;
        }
        return false;
    }

    /**
     * Unregisters a global action handler
     *
     * @param actionName the action name
     */
    public static void unregisterGlobal(@NotNull String actionName) {
        GLOBAL_HANDLERS.remove(actionName.toUpperCase());
    }

    /**
     * Unregisters a menu-specific action handler
     *
     * @param menuFileName the menu file name
     * @param actionName the action name
     */
    public static void unregister(@NotNull String menuFileName, @NotNull String actionName) {
        Map<String, ContextActionHandler> menuMap = MENU_HANDLERS.get(menuFileName.toLowerCase());
        if (menuMap != null) {
            menuMap.remove(actionName.toUpperCase());
        }
    }

    /**
     * Clears all handlers for a specific menu
     *
     * @param menuFileName the menu file name
     */
    public static void clearMenu(@NotNull String menuFileName) {
        MENU_HANDLERS.remove(menuFileName.toLowerCase());
    }

    /**
     * Clears all handlers
     */
    public static void clearAll() {
        GLOBAL_HANDLERS.clear();
        MENU_HANDLERS.clear();
    }
}