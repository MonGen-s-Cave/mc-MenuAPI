package com.mongenscave.mcmenuapi.listener;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.handler.DynamicItemClickHandler;
import com.mongenscave.mcmenuapi.menu.Menu;
import com.mongenscave.mcmenuapi.menu.PaginatedMenu;
import com.mongenscave.mcmenuapi.menu.SimpleMenu;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.registry.DynamicClickRegistry;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MenuListener implements Listener {
    private final McMenuAPI menuAPI;

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        Menu menu = menuAPI.getOpenMenus().get(player.getUniqueId());
        if (menu == null) {
            return;
        }

        if (menu.getInventory(player) != event.getInventory()) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= menu.getSize()) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        String menuFileName = getMenuFileName(menu);

        if (menuFileName != null) {
            DynamicItemClickHandler dynamicHandler = DynamicClickRegistry.getHandler(menuFileName, slot);
            if (dynamicHandler != null) {
                dynamicHandler.onClick(player, clickedItem, event.getClick());
                return;
            }
        }

        MenuItem menuItem = null;

        if (menu instanceof PaginatedMenu paginatedMenu) {
            menuItem = paginatedMenu.getItemAtSlot(slot, player);
        } else if (menu instanceof SimpleMenu simpleMenu) {
            menuItem = simpleMenu.getItemAtSlot(slot);
        }

        if (menuItem != null && menuItem.isClickable()) {
            menuItem.onClick(player);
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Menu menu = menuAPI.getOpenMenus().get(player.getUniqueId());
        if (menu != null && menu.getInventory(player) == event.getInventory()) {
            menuAPI.getOpenMenus().remove(player.getUniqueId());
        }
    }

    /**
     * Get the menu file name for a menu instance
     *
     * @param menu The menu
     * @return The file name, or null if not found
     */
    private String getMenuFileName(@NotNull Menu menu) {
        return menuAPI.getLoadedMenus().entrySet().stream()
                .filter(entry -> entry.getValue() == menu)
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}