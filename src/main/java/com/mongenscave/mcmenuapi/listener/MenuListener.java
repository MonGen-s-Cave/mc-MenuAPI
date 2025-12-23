package com.mongenscave.mcmenuapi.listener;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.handler.DynamicItemClickHandler;
import com.mongenscave.mcmenuapi.loader.MenuLoader;
import com.mongenscave.mcmenuapi.menu.Menu;
import com.mongenscave.mcmenuapi.menu.PaginatedMenu;
import com.mongenscave.mcmenuapi.menu.SimpleMenu;
import com.mongenscave.mcmenuapi.menu.action.Action;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.registry.ActionHandlerRegistry;
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

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= menu.getSize()) {
            return;
        }

        // KRITIKUS: Ellenőrizzük, hogy placeable slot-e
        if (menu instanceof SimpleMenu simpleMenu && simpleMenu.isSlotPlaceable(slot)) {
            // Ha placeable, NE canceljük az eventet - engedd a játékosnak!
            return;
        }

        // Ha NEM placeable, AKKOR cancel-eld
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        String menuFileName = getMenuFileName(menu);

        // Keressük meg a MenuItem-et
        MenuItem menuItem = null;

        if (menu instanceof PaginatedMenu paginatedMenu) {
            menuItem = paginatedMenu.getItemAtSlot(slot, player);
        } else if (menu instanceof SimpleMenu simpleMenu) {
            menuItem = simpleMenu.getItemAtSlot(slot);
        }

        // Ha van MenuItem és clickable, futtassuk le az action-öket
        if (menuItem != null && menuItem.isClickable()) {
            // 1. YAML-ből jövő action-ök (CLOSE, OPEN, SOUND, ACTION, stb.)
            for (Action action : menuItem.getActions()) {
                if (action instanceof MenuLoader.CustomAction customAction) {
                    // Custom ACTION handler
                    ActionHandlerRegistry.ActionHandler handler = ActionHandlerRegistry.getHandler(
                            player.getUniqueId(),
                            menuFileName != null ? menuFileName : "unknown",
                            customAction.getActionName()
                    );

                    if (handler != null && menuFileName != null) {
                        handler.handle(player, clickedItem, event.getClick(), menuFileName, slot);
                    }
                } else {
                    // Normál action (CLOSE, OPEN, SOUND, MESSAGE, stb.)
                    action.execute(player);
                }
            }

            // 2. Custom onClick handler (ha van)
            menuItem.onClick(player);
        }

        // 3. Dynamic handler (ha van) - ez fut UTOLJÁRA
        if (menuFileName != null) {
            DynamicItemClickHandler dynamicHandler = DynamicClickRegistry.getHandler(menuFileName, slot);
            if (dynamicHandler != null) {
                dynamicHandler.onClick(player, clickedItem, event.getClick());
            }
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
            ActionHandlerRegistry.clearPlayer(player.getUniqueId());
        }
    }

    private String getMenuFileName(@NotNull Menu menu) {
        return menuAPI.getLoadedMenus().entrySet().stream()
                .filter(entry -> entry.getValue() == menu)
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}