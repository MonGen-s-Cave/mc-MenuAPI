package com.mongenscave.mcmenuapi.action;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for handling menu actions with full context support.
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * ContextActionRegistry.register("main-menu.yml", "TOGGLE_AUTO_SELL", ctx -> {
 *     SellChest chest = ctx.requireContext(SellChest.class);
 *
 *     chest.setAutoSellEnabled(!chest.isAutoSellEnabled());
 *
 *     ctx.playSound("UI_BUTTON_CLICK");
 *     ctx.sendMessage("&aAuto-sell toggled!");
 *     ctx.refresh();
 * });
 * }</pre>
 */
@FunctionalInterface
public interface ContextActionHandler {

    /**
     * Handles the action
     *
     * @param context the full action context
     */
    void handle(@NotNull com.mongenscave.mcmenuapi.action.ActionContext context);
}