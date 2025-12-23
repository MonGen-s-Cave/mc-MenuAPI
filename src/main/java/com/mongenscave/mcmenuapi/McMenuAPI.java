package com.mongenscave.mcmenuapi;

import com.mongenscave.mcmenuapi.listener.MenuListener;
import com.mongenscave.mcmenuapi.loader.MenuLoader;
import com.mongenscave.mcmenuapi.menu.Menu;
import com.mongenscave.mcmenuapi.menu.SimpleMenu;
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
 * Main API class for managing menus
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
 *         // Now you can use the API
 *         menuAPI.openMenu(player, "main.yml");
 *     }
 * }
 * }</pre>
 *
 * @author coma112
 * @version 1.0.0
 */
@Getter
public class McMenuAPI {

    /**
     * Global instance of the API
     * <p><b>Note:</b> This is set automatically when you create a new McMenuAPI instance</p>
     */
    @Getter
    private static McMenuAPI instance;

    private final Plugin plugin;
    private final File menusFolder;
    private final Map<String, Menu> loadedMenus;
    private final Map<UUID, Menu> openMenus;

    /**
     * Creates a new MenuAPI instance
     *
     * <p><b>IMPORTANT:</b> Call this in your plugin's onEnable() method!</p>
     *
     * <p>This will:</p>
     * <ul>
     *   <li>Register event listeners</li>
     *   <li>Create the menus folder if it doesn't exist</li>
     *   <li>Load all YAML menus from the folder</li>
     * </ul>
     *
     * @param plugin the plugin using this API (usually 'this' from your plugin)
     * @param menusFolder the folder containing menu YAML files
     */
    public McMenuAPI(@NotNull Plugin plugin, @NotNull File menusFolder) {
        this.plugin = plugin;
        this.menusFolder = menusFolder;
        this.loadedMenus = new ConcurrentHashMap<>();
        this.openMenus = new ConcurrentHashMap<>();

        instance = this;

        Bukkit.getPluginManager().registerEvents(new MenuListener(this), plugin);
        loadAllMenus();

        plugin.getLogger().info("McMenuAPI initialized successfully!");
    }

    /**
     * Loads all menus from the menus folder
     * <p>This is called automatically during initialization</p>
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
                plugin.getLogger().info("Loaded menu: " + file.getName());
            }
        }

        plugin.getLogger().info("Loaded " + loadedMenus.size() + " menu(s)");
    }

    /**
     * Reloads all menus from the menus folder
     * <p>This will close all currently open menus</p>
     */
    public void reloadMenus() {
        openMenus.forEach((uuid, menu) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.closeInventory();
            }
        });

        openMenus.clear();
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
     * Opens a menu for a player
     *
     * @param player the player
     * @param fileName the menu file name
     * @return true if the menu was opened successfully
     */
    public boolean openMenu(@NotNull Player player, @NotNull String fileName) {
        Optional<Menu> menuOpt = getMenu(fileName);

        if (menuOpt.isEmpty()) {
            plugin.getLogger().warning("Menu not found: " + fileName);
            return false;
        }

        Menu menu = menuOpt.get();
        openMenus.put(player.getUniqueId(), menu);
        menu.open(player);

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
}