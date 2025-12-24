package com.mongenscave.mcmenuapi;

import com.mongenscave.mcmenuapi.context.MenuContext;
import com.mongenscave.mcmenuapi.listener.MenuListener;
import com.mongenscave.mcmenuapi.loader.MenuLoader;
import com.mongenscave.mcmenuapi.menu.Menu;
import com.mongenscave.mcmenuapi.menu.SimpleMenu;
import com.mongenscave.mcmenuapi.refresh.MenuRefreshManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main API class for managing menus with context support, auto-refresh, and player inventory interaction.
 *
 * <h2>This is a LIBRARY - not a standalone plugin!</h2>
 *
 * <p>Other plugins must initialize this in their onEnable() method.</p>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     private McMenuAPI menuAPI;
 *
 *     @Override
 *     public void onEnable() {
 *         File menusFolder = new File(getDataFolder(), "menus");
 *         menuAPI = new McMenuAPI(this, menusFolder);
 *
 *         // Register context placeholders
 *         ContextPlaceholderRegistry.register(SellChest.class, "{earned}",
 *             (player, chest) -> String.valueOf(chest.getTotalEarned())
 *         );
 *
 *         // Register action handlers
 *         ContextActionRegistry.register("main-menu.yml", "TOGGLE_AUTO_SELL", ctx -> {
 *             SellChest chest = ctx.requireContext(SellChest.class);
 *             chest.setAutoSellEnabled(!chest.isAutoSellEnabled());
 *             ctx.refresh();
 *         });
 *
 *         // Open a menu with context
 *         menuAPI.openMenu(player, "main-menu.yml", sellChest);
 *     }
 * }
 * }</pre>
 *
 * @author coma112
 * @version 2.0.0
 */
@Getter
public class McMenuAPI {

    /**
     * Global instance of the API
     */
    @Getter
    private static McMenuAPI instance;

    private final Plugin plugin;
    private final File menusFolder;
    private final Map<String, Menu> loadedMenus;
    private final Map<UUID, Menu> openMenus;
    private final MenuRefreshManager refreshManager;

    /**
     * Creates a new MenuAPI instance
     *
     * @param plugin the plugin using this API
     * @param menusFolder the folder containing menu YAML files
     */
    public McMenuAPI(@NotNull Plugin plugin, @NotNull File menusFolder) {
        this.plugin = plugin;
        this.menusFolder = menusFolder;
        this.loadedMenus = new ConcurrentHashMap<>();
        this.openMenus = new ConcurrentHashMap<>();

        instance = this;

        // Register event listener
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), plugin);

        // Initialize refresh manager
        this.refreshManager = new MenuRefreshManager(plugin, this);
        this.refreshManager.start();

        // Load all menus
        loadAllMenus();
    }

    /**
     * Loads all menus from the menus folder
     */
    public void loadAllMenus() {
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        loadedMenus.clear();

        File[] files = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            SimpleMenu menu = MenuLoader.loadMenu(file);
            if (menu != null) {
                loadedMenus.put(file.getName(), menu);
            }
        }
    }

    /**
     * Reloads all menus from the menus folder
     */
    public void reloadMenus() {
        // Close all open menus
        openMenus.forEach((uuid, menu) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.closeInventory();
            }
        });

        openMenus.clear();
        MenuContext.clearAll();
        loadAllMenus();
    }

    /**
     * Gets a loaded menu by its file name
     *
     * @param fileName the menu file name (e.g., "menu1.yml")
     * @return optional containing the menu if found
     */
    @NotNull
    public Optional<Menu> getMenu(@NotNull String fileName) {
        return Optional.ofNullable(loadedMenus.get(fileName));
    }

    /**
     * Opens a menu for a player (without context)
     *
     * @param player the player
     * @param fileName the menu file name
     * @return true if the menu was opened successfully
     */
    public boolean openMenu(@NotNull Player player, @NotNull String fileName) {
        return openMenuInternal(player, fileName, null, false);
    }

    /**
     * Opens a menu for a player with context
     *
     * @param player the player
     * @param fileName the menu file name
     * @param context the context object to bind to this menu session
     * @param <T> the context type
     * @return true if the menu was opened successfully
     */
    public <T> boolean openMenu(@NotNull Player player, @NotNull String fileName, @NotNull T context) {
        return openMenuInternal(player, fileName, context, false);
    }

    /**
     * Opens a menu while preserving the existing context
     *
     * @param player the player
     * @param fileName the menu file name
     * @return true if the menu was opened successfully
     */
    public boolean openMenuPreserveContext(@NotNull Player player, @NotNull String fileName) {
        return openMenuInternal(player, fileName, null, true);
    }

    /**
     * Internal method to open a menu
     */
    private boolean openMenuInternal(
            @NotNull Player player,
            @NotNull String fileName,
            @Nullable Object context,
            boolean preserveContext
    ) {
        Optional<Menu> menuOpt = getMenu(fileName);

        if (menuOpt.isEmpty()) {
            plugin.getLogger().warning("Menu not found: " + fileName);
            return false;
        }

        Menu menu = menuOpt.get();

        // Handle context
        if (context != null) {
            MenuContext.set(player, context);
        } else if (!preserveContext) {
            // Clear context if not preserving and no new context provided
            MenuContext.clear(player);
        }
        // If preserveContext is true and context is null, we keep existing context

        // Store open menu reference
        openMenus.put(player.getUniqueId(), menu);

        // Open the menu
        if (menu instanceof SimpleMenu simpleMenu) {
            simpleMenu.openWithFileName(player, fileName);
        } else {
            menu.open(player);
        }

        return true;
    }

    /**
     * Closes the currently open menu for a player
     *
     * @param player the player
     */
    public void closeMenu(@NotNull Player player) {
        Menu menu = openMenus.remove(player.getUniqueId());
        if (menu != null) {
            menu.close(player);
        }
        MenuContext.clear(player);
    }

    /**
     * Gets the currently open menu for a player
     *
     * @param player the player
     * @return the menu, or null if no menu is open
     */
    @Nullable
    public Menu getOpenMenu(@NotNull Player player) {
        return openMenus.get(player.getUniqueId());
    }

    /**
     * Refreshes the currently open menu for a player
     *
     * @param player the player
     */
    public void refreshMenu(@NotNull Player player) {
        Menu menu = openMenus.get(player.getUniqueId());
        if (menu != null) {
            menu.refresh(player);
        }
    }

    /**
     * Gets the menu context for a player
     *
     * @param player the player
     * @param type the expected context type
     * @param <T> the type
     * @return optional containing the context
     */
    @NotNull
    public <T> Optional<T> getMenuContext(@NotNull Player player, @NotNull Class<T> type) {
        return MenuContext.get(player, type);
    }

    /**
     * Updates the context for a player's open menu
     *
     * @param player the player
     * @param context the new context
     * @param <T> the context type
     */
    public <T> void updateContext(@NotNull Player player, @NotNull T context) {
        MenuContext.set(player, context);
    }

    /**
     * Registers a menu programmatically
     *
     * @param fileName the file name to associate with this menu
     * @param menu the menu
     */
    public void registerMenu(@NotNull String fileName, @NotNull Menu menu) {
        loadedMenus.put(fileName, menu);
    }

    /**
     * Unregisters a menu
     *
     * @param fileName the menu file name
     */
    public void unregisterMenu(@NotNull String fileName) {
        loadedMenus.remove(fileName);
    }

    /**
     * Checks if a player has a menu open
     *
     * @param player the player
     * @return true if a menu is open
     */
    public boolean hasOpenMenu(@NotNull Player player) {
        return openMenus.containsKey(player.getUniqueId());
    }

    /**
     * Gets the menu file name for a player's open menu
     *
     * @param player the player
     * @return the file name or null
     */
    @Nullable
    public String getOpenMenuFileName(@NotNull Player player) {
        Menu menu = openMenus.get(player.getUniqueId());
        if (menu == null) return null;

        return loadedMenus.entrySet().stream()
                .filter(entry -> entry.getValue() == menu)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Shuts down the API (call in onDisable)
     */
    public void shutdown() {
        if (refreshManager != null) {
            refreshManager.stop();
        }

        // Close all open menus
        openMenus.forEach((uuid, menu) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.closeInventory();
            }
        });

        openMenus.clear();
        MenuContext.clearAll();
        loadedMenus.clear();

        instance = null;
    }
}