package com.mongenscave.mcmenuapi.menu;

import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.refresh.RefreshConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a GUI menu that can be opened for players.
 * Supports context binding, auto-refresh, and player inventory interaction.
 */
public interface Menu {

    // ==================== CORE METHODS ====================

    /**
     * Opens this menu for the specified player
     *
     * @param player the player to open the menu for
     */
    void open(@NotNull Player player);

    /**
     * Closes this menu for the specified player
     *
     * @param player the player to close the menu for
     */
    void close(@NotNull Player player);

    /**
     * Refreshes the menu for the specified player
     *
     * @param player the player to refresh the menu for
     */
    void refresh(@NotNull Player player);

    /**
     * Refreshes only specific slots for the specified player
     *
     * @param player the player
     * @param slots the slots to refresh
     */
    void refreshSlots(@NotNull Player player, @NotNull List<Integer> slots);

    /**
     * Gets the menu title
     *
     * @return the menu title
     */
    @NotNull
    String getTitle();

    /**
     * Gets the menu size (number of slots)
     *
     * @return the menu size
     */
    int getSize();

    // ==================== ITEM MANAGEMENT ====================

    /**
     * Gets a menu item by its key
     *
     * @param key the item key
     * @return optional containing the menu item if found
     */
    @NotNull
    Optional<MenuItem> getItem(@NotNull String key);

    /**
     * Gets all menu items
     *
     * @return map of item keys to menu items
     */
    @NotNull
    Map<String, MenuItem> getItems();

    /**
     * Sets a menu item
     *
     * @param key the item key
     * @param item the menu item
     * @return this menu for chaining
     */
    @NotNull
    Menu setItem(@NotNull String key, @NotNull MenuItem item);

    /**
     * Removes a menu item
     *
     * @param key the item key
     * @return this menu for chaining
     */
    @NotNull
    Menu removeItem(@NotNull String key);

    // ==================== PLACEHOLDERS ====================

    /**
     * Sets global placeholders for all items in this menu
     *
     * @param placeholders the placeholders to set
     * @return this menu for chaining
     */
    @NotNull
    Menu setPlaceholders(@NotNull Map<String, String> placeholders);

    /**
     * Gets the Bukkit inventory for a specific player
     *
     * @param player the player
     * @return the inventory, or null if not opened
     */
    @Nullable
    Inventory getInventory(@NotNull Player player);

    // ==================== PAGINATION ====================

    /**
     * Checks if this menu is paginated
     *
     * @return true if paginated
     */
    boolean isPaginated();

    /**
     * Gets the current page for a player
     *
     * @param player the player
     * @return the current page (0-indexed)
     */
    int getCurrentPage(@NotNull Player player);

    /**
     * Sets the current page for a player
     *
     * @param player the player
     * @param page the page (0-indexed)
     */
    void setPage(@NotNull Player player, int page);

    /**
     * Gets the total number of pages
     *
     * @return the total pages
     */
    int getTotalPages();

    // ==================== EVENT HANDLERS ====================

    /**
     * Adds a close handler that runs when the menu is closed
     *
     * @param handler the close handler
     * @return this menu for chaining
     */
    @NotNull
    Menu onClose(@NotNull Consumer<Player> handler);

    /**
     * Adds an open handler that runs when the menu is opened
     *
     * @param handler the open handler
     * @return this menu for chaining
     */
    @NotNull
    Menu onOpen(@NotNull Consumer<Player> handler);

    /**
     * Adds a refresh handler that runs when the menu is refreshed
     *
     * @param handler the refresh handler
     * @return this menu for chaining
     */
    @NotNull
    Menu onRefresh(@NotNull Consumer<Player> handler);

    // ==================== AUTO-REFRESH ====================

    /**
     * Gets the refresh configuration for this menu
     *
     * @return the refresh config
     */
    @NotNull
    RefreshConfig getRefreshConfig();

    /**
     * Sets the refresh configuration
     *
     * @param config the refresh config
     * @return this menu for chaining
     */
    @NotNull
    Menu setRefreshConfig(@NotNull RefreshConfig config);

    // ==================== PLAYER INVENTORY INTERACTION ====================

    /**
     * Checks if player inventory interaction is enabled
     *
     * @return true if enabled
     */
    boolean isPlayerInventoryInteractionEnabled();

    /**
     * Sets whether player inventory interaction is enabled
     *
     * @param enabled true to enable
     * @return this menu for chaining
     */
    @NotNull
    Menu setPlayerInventoryInteraction(boolean enabled);

    /**
     * Gets the player inventory handler name (if defined in YAML)
     *
     * @return the handler name or null
     */
    @Nullable
    String getPlayerInventoryHandlerName();

    /**
     * Sets the player inventory handler name
     *
     * @param handlerName the handler name
     * @return this menu for chaining
     */
    @NotNull
    Menu setPlayerInventoryHandlerName(@Nullable String handlerName);

    // ==================== CONTEXT AWARENESS ====================

    /**
     * Checks if this menu is context-aware (expects context data)
     *
     * @return true if context-aware
     */
    boolean isContextAware();

    /**
     * Sets whether this menu is context-aware
     *
     * @param contextAware true if context-aware
     * @return this menu for chaining
     */
    @NotNull
    Menu setContextAware(boolean contextAware);
}