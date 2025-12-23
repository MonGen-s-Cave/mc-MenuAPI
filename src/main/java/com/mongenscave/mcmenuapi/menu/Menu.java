package com.mongenscave.mcmenuapi.menu;

import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a GUI menu that can be opened for players
 */
public interface Menu {

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
     * Gets the menu title
     *
     * @return the menu title
     */
    @NotNull String getTitle();

    /**
     * Gets the menu size (number of slots)
     *
     * @return the menu size
     */
    int getSize();

    /**
     * Gets a menu item by its key
     *
     * @param key the item key
     * @return optional containing the menu item if found
     */
    @NotNull Optional<MenuItem> getItem(@NotNull String key);

    /**
     * Gets all menu items
     *
     * @return map of item keys to menu items
     */
    @NotNull Map<String, MenuItem> getItems();

    /**
     * Sets a menu item
     *
     * @param key the item key
     * @param item the menu item
     * @return this menu for chaining
     */
    @NotNull Menu setItem(@NotNull String key, @NotNull MenuItem item);

    /**
     * Removes a menu item
     *
     * @param key the item key
     * @return this menu for chaining
     */
    @NotNull Menu removeItem(@NotNull String key);

    /**
     * Sets global placeholders for all items in this menu
     *
     * @param placeholders the placeholders to set
     * @return this menu for chaining
     */
    @NotNull Menu setPlaceholders(@NotNull Map<String, String> placeholders);

    /**
     * Gets the Bukkit inventory for a specific player
     *
     * @param player the player
     * @return the inventory, or null if not opened
     */
    @Nullable Inventory getInventory(@NotNull Player player);

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

    /**
     * Adds a close handler that runs when the menu is closed
     *
     * @param handler the close handler
     * @return this menu for chaining
     */
    @NotNull Menu onClose(@NotNull Consumer<Player> handler);

    /**
     * Adds an open handler that runs when the menu is opened
     *
     * @param handler the open handler
     * @return this menu for chaining
     */
    @NotNull Menu onOpen(@NotNull Consumer<Player> handler);
}