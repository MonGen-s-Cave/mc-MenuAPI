package com.mongenscave.mcmenuapi.menu;

import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple implementation of the Menu interface
 */
@Getter
public class SimpleMenu implements Menu {

    private final String title;
    private final int size;
    private final Map<String, MenuItem> items;
    private final Map<String, String> globalPlaceholders;
    private final Map<UUID, Inventory> openInventories;
    private final Map<UUID, Integer> playerPages;
    private final List<Consumer<Player>> closeHandlers;
    private final List<Consumer<Player>> openHandlers;

    private boolean paginated;
    private int totalPages;

    public SimpleMenu(@NotNull String title, int size) {
        this.title = ColorProcessor.process(title);
        this.size = size;
        this.items = new ConcurrentHashMap<>();
        this.globalPlaceholders = new ConcurrentHashMap<>();
        this.openInventories = new ConcurrentHashMap<>();
        this.playerPages = new ConcurrentHashMap<>();
        this.closeHandlers = Collections.synchronizedList(new ArrayList<>());
        this.openHandlers = Collections.synchronizedList(new ArrayList<>());
        this.paginated = false;
        this.totalPages = 1;
    }

    @Override
    public void open(@NotNull Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, title);

        items.values().stream()
                .sorted(Comparator.comparingInt(MenuItem::getPriority))
                .forEach(menuItem -> {
                    MenuItem replaced = menuItem.withReplacedPlaceholders(player, globalPlaceholders);
                    for (int slot : replaced.getSlots()) {
                        if (slot >= 0 && slot < size) {
                            inventory.setItem(slot, replaced.getItemStack().clone());
                        }
                    }
                });

        openInventories.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);

        openHandlers.forEach(handler -> handler.accept(player));
    }

    @Override
    public void close(@NotNull Player player) {
        openInventories.remove(player.getUniqueId());
        playerPages.remove(player.getUniqueId());
        player.closeInventory();

        closeHandlers.forEach(handler -> handler.accept(player));
    }

    @Override
    public void refresh(@NotNull Player player) {
        if (openInventories.containsKey(player.getUniqueId())) {
            Inventory inventory = openInventories.get(player.getUniqueId());
            inventory.clear();

            items.values().stream()
                    .sorted(Comparator.comparingInt(MenuItem::getPriority))
                    .forEach(menuItem -> {
                        MenuItem replaced = menuItem.withReplacedPlaceholders(player, globalPlaceholders);
                        for (int slot : replaced.getSlots()) {
                            if (slot >= 0 && slot < size) {
                                inventory.setItem(slot, replaced.getItemStack().clone());
                            }
                        }
                    });
        }
    }

    @Override
    public @NotNull Optional<MenuItem> getItem(@NotNull String key) {
        return Optional.ofNullable(items.get(key));
    }

    @Override
    public @NotNull Menu setItem(@NotNull String key, @NotNull MenuItem item) {
        items.put(key, item);
        return this;
    }

    @Override
    public @NotNull Menu removeItem(@NotNull String key) {
        items.remove(key);
        return this;
    }

    @Override
    public @NotNull Menu setPlaceholders(@NotNull Map<String, String> placeholders) {
        globalPlaceholders.putAll(placeholders);
        return this;
    }

    @Override
    public @Nullable Inventory getInventory(@NotNull Player player) {
        return openInventories.get(player.getUniqueId());
    }

    @Override
    public int getCurrentPage(@NotNull Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void setPage(@NotNull Player player, int page) {
        if (page >= 0 && page < totalPages) {
            playerPages.put(player.getUniqueId(), page);
            refresh(player);
        }
    }

    @Override
    public @NotNull Menu onClose(@NotNull Consumer<Player> handler) {
        closeHandlers.add(handler);
        return this;
    }

    @Override
    public @NotNull Menu onOpen(@NotNull Consumer<Player> handler) {
        openHandlers.add(handler);
        return this;
    }

    /**
     * Enables pagination for this menu
     *
     * @param totalPages the total number of pages
     * @return this menu
     */
    public SimpleMenu setPaginated(int totalPages) {
        this.paginated = true;
        this.totalPages = Math.max(1, totalPages);
        return this;
    }

    /**
     * Gets the menu item at a specific slot
     *
     * @param slot the slot
     * @return the menu item, or null if not found
     */
    @Nullable
    public MenuItem getItemAtSlot(int slot) {
        return items.values().stream()
                .filter(item -> item.getSlots().contains(slot))
                .findFirst()
                .orElse(null);
    }
}