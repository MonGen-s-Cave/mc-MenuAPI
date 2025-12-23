package com.mongenscave.mcmenuapi.builder;

import com.mongenscave.mcmenuapi.menu.SimpleMenu;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Fluent builder for creating menus programmatically
 */
public class MenuBuilder {

    private final SimpleMenu menu;

    private MenuBuilder(@NotNull String title, int size) {
        this.menu = new SimpleMenu(title, size);
    }

    /**
     * Creates a new menu builder
     *
     * @param title the menu title
     * @param size the menu size (must be multiple of 9, between 9 and 54)
     * @return a new menu builder
     */
    public static MenuBuilder create(@NotNull String title, int size) {
        // Validate size
        if (size % 9 != 0 || size < 9 || size > 54) {
            throw new IllegalArgumentException("Menu size must be a multiple of 9 and between 9 and 54");
        }
        return new MenuBuilder(title, size);
    }

    /**
     * Creates a new menu builder with default size (54)
     *
     * @param title the menu title
     * @return a new menu builder
     */
    public static MenuBuilder create(@NotNull String title) {
        return create(title, 54);
    }

    /**
     * Adds an item to the menu
     *
     * @param key the item key
     * @param item the menu item
     * @return this builder
     */
    public MenuBuilder item(@NotNull String key, @NotNull MenuItem item) {
        menu.setItem(key, item);
        return this;
    }

    /**
     * Adds an item to the menu using a builder
     *
     * @param key the item key
     * @param itemBuilder consumer to configure the item builder
     * @return this builder
     */
    public MenuBuilder item(@NotNull String key, @NotNull Consumer<MenuItem.MenuItemBuilder> itemBuilder) {
        MenuItem.MenuItemBuilder builder = MenuItem.builder();
        itemBuilder.accept(builder);
        menu.setItem(key, builder.build());
        return this;
    }

    /**
     * Sets global placeholders for the menu
     *
     * @param placeholders the placeholders
     * @return this builder
     */
    public MenuBuilder placeholders(@NotNull Map<String, String> placeholders) {
        menu.setPlaceholders(placeholders);
        return this;
    }

    /**
     * Adds a global placeholder
     *
     * @param key the placeholder key
     * @param value the placeholder value
     * @return this builder
     */
    public MenuBuilder placeholder(@NotNull String key, @NotNull String value) {
        menu.setPlaceholders(Map.of(key, value));
        return this;
    }

    /**
     * Enables pagination
     *
     * @param totalPages the total number of pages
     * @return this builder
     */
    public MenuBuilder paginated(int totalPages) {
        menu.setPaginated(totalPages);
        return this;
    }

    /**
     * Adds a close handler
     *
     * @param handler the close handler
     * @return this builder
     */
    public MenuBuilder onClose(@NotNull Consumer<Player> handler) {
        menu.onClose(handler);
        return this;
    }

    /**
     * Adds an open handler
     *
     * @param handler the open handler
     * @return this builder
     */
    public MenuBuilder onOpen(@NotNull Consumer<Player> handler) {
        menu.onOpen(handler);
        return this;
    }

    /**
     * Builds the menu
     *
     * @return the built menu
     */
    public SimpleMenu build() {
        return menu;
    }
}