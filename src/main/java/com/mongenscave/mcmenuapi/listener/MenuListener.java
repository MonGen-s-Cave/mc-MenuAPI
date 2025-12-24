package com.mongenscave.mcmenuapi.listener;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.action.ActionContext;
import com.mongenscave.mcmenuapi.action.ContextActionHandler;
import com.mongenscave.mcmenuapi.action.ContextActionRegistry;
import com.mongenscave.mcmenuapi.context.MenuContext;
import com.mongenscave.mcmenuapi.handler.DynamicItemClickHandler;
import com.mongenscave.mcmenuapi.handler.PlayerInventoryClickContextImpl;
import com.mongenscave.mcmenuapi.handler.PlayerInventoryClickHandler;
import com.mongenscave.mcmenuapi.handler.PlayerInventoryHandlerRegistry;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MenuListener implements Listener {
    private final McMenuAPI menuAPI;

    @EventHandler(priority = EventPriority.HIGH)
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

        int rawSlot = event.getRawSlot();
        int topSize = menu.getSize();

        // ==================== PLAYER INVENTORY CLICK HANDLING ====================
        if (rawSlot >= topSize) {
            handlePlayerInventoryClick(event, player, menu);
            return;
        }

        // ==================== MENU INVENTORY CLICK HANDLING ====================
        // Check placeable slots
        if (menu instanceof SimpleMenu simpleMenu && simpleMenu.isSlotPlaceable(rawSlot)) {
            return; // Allow interaction
        }

        // Cancel by default for menu clicks
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        String menuFileName = getMenuFileName(menu);

        // Find the MenuItem
        MenuItem menuItem = findMenuItem(menu, rawSlot, player);

        // If MenuItem exists and is clickable, execute actions
        if (menuItem != null && menuItem.isClickable()) {
            // Build ActionContext for context-aware handlers
            ActionContext actionContext = ActionContext.builder()
                    .player(player)
                    .menu(menu)
                    .menuFileName(menuFileName != null ? menuFileName : "unknown")
                    .slot(rawSlot)
                    .clickedItem(clickedItem)
                    .clickType(event.getClick())
                    .build();

            // Execute actions
            for (Action action : menuItem.getActions()) {
                if (action instanceof MenuLoader.ContextAction contextAction) {
                    // New context-aware action handler
                    ContextActionHandler handler = ContextActionRegistry.getHandler(
                            menuFileName != null ? menuFileName : "unknown",
                            contextAction.getActionName()
                    );
                    if (handler != null) {
                        handler.handle(actionContext);
                    }
                } else if (action instanceof MenuLoader.CustomAction customAction) {
                    // Legacy action handler (backwards compatibility)
                    ActionHandlerRegistry.ActionHandler handler = ActionHandlerRegistry.getHandler(
                            player.getUniqueId(),
                            menuFileName != null ? menuFileName : "unknown",
                            customAction.getActionName()
                    );
                    if (handler != null) {
                        handler.handle(player, clickedItem, event.getClick(), menuFileName, rawSlot);
                    }
                } else {
                    // Normal action
                    action.execute(player);
                }
            }

            // Custom onClick handler
            menuItem.onClick(player);
        }

        // Dynamic handler (if registered)
        if (menuFileName != null) {
            DynamicItemClickHandler dynamicHandler = DynamicClickRegistry.getHandler(menuFileName, rawSlot);
            if (dynamicHandler != null) {
                dynamicHandler.onClick(player, clickedItem, event.getClick());
            }
        }
    }

    /**
     * Handles clicks in the player's inventory while a menu is open
     */
    private void handlePlayerInventoryClick(
            @NotNull InventoryClickEvent event,
            @NotNull Player player,
            @NotNull Menu menu
    ) {
        // Check if player inventory interaction is enabled
        if (!menu.isPlayerInventoryInteractionEnabled()) {
            event.setCancelled(true);
            return;
        }

        String menuFileName = getMenuFileName(menu);
        if (menuFileName == null) {
            event.setCancelled(true);
            return;
        }

        // Check for registered handler
        PlayerInventoryClickHandler handler = PlayerInventoryHandlerRegistry.getHandler(menuFileName);
        if (handler == null) {
            // No handler registered, check if we should allow by default
            String handlerName = menu.getPlayerInventoryHandlerName();
            if (handlerName == null) {
                // No handler name specified, cancel by default
                event.setCancelled(true);
                return;
            }

            // Handler name specified but not registered, cancel
            event.setCancelled(true);
            return;
        }

        // Create context and call handler
        PlayerInventoryClickContextImpl context = new PlayerInventoryClickContextImpl(
                player,
                event.getCurrentItem(),
                event.getSlot(),
                event.getClick(),
                menu,
                menuFileName
        );

        PlayerInventoryClickHandler.ClickResult result = handler.onClick(context);

        switch (result) {
            case CANCEL -> event.setCancelled(true);
            case ALLOW -> { /* Let event proceed */ }
            case CANCEL_SILENT -> {
                event.setCancelled(true);
                // Don't update inventory
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

            // Clear context
            MenuContext.clear(player);

            // Notify refresh manager
            if (menuAPI.getRefreshManager() != null) {
                menuAPI.getRefreshManager().onMenuClose(player.getUniqueId());
            }
        }
    }

    /**
     * Finds the MenuItem at a specific slot
     */
    private MenuItem findMenuItem(@NotNull Menu menu, int slot, @NotNull Player player) {
        if (menu instanceof PaginatedMenu paginatedMenu) {
            return paginatedMenu.getItemAtSlot(slot, player);
        } else if (menu instanceof SimpleMenu simpleMenu) {
            return simpleMenu.getItemAtSlot(slot);
        }
        return null;
    }

    /**
     * Gets the menu file name from the loaded menus
     */
    private String getMenuFileName(@NotNull Menu menu) {
        return menuAPI.getLoadedMenus().entrySet().stream()
                .filter(entry -> entry.getValue() == menu)
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}