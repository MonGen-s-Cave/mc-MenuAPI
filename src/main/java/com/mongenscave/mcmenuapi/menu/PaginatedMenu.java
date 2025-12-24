package com.mongenscave.mcmenuapi.menu;

import com.mongenscave.mcmenuapi.context.ContextPlaceholderRegistry;
import com.mongenscave.mcmenuapi.context.MenuContext;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import com.mongenscave.mcmenuapi.refresh.RefreshConfig;
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
 * Advanced paginated menu implementation with automatic pagination
 */
@Getter
public class PaginatedMenu implements Menu {

    private final String title;
    private final int size;
    private final Map<String, MenuItem> staticItems;
    private final List<MenuItem> pageItems;
    private final Map<String, String> globalPlaceholders;
    private final Map<UUID, Inventory> openInventories;
    private final Map<UUID, Integer> playerPages;
    private final List<Consumer<Player>> closeHandlers;
    private final List<Consumer<Player>> openHandlers;
    private final List<Consumer<Player>> refreshHandlers;

    private final int[] pageSlots;
    private final int itemsPerPage;

    private MenuItem previousPageItem;
    private MenuItem nextPageItem;

    // New fields for enhanced features
    private RefreshConfig refreshConfig;
    private boolean playerInventoryInteractionEnabled;
    private String playerInventoryHandlerName;
    private boolean contextAware;

    public PaginatedMenu(@NotNull String title, int size, @NotNull int[] pageSlots) {
        this.title = ColorProcessor.process(title);
        this.size = size;
        this.pageSlots = pageSlots;
        this.itemsPerPage = pageSlots.length;
        this.staticItems = new ConcurrentHashMap<>();
        this.pageItems = Collections.synchronizedList(new ArrayList<>());
        this.globalPlaceholders = new ConcurrentHashMap<>();
        this.openInventories = new ConcurrentHashMap<>();
        this.playerPages = new ConcurrentHashMap<>();
        this.closeHandlers = Collections.synchronizedList(new ArrayList<>());
        this.openHandlers = Collections.synchronizedList(new ArrayList<>());
        this.refreshHandlers = Collections.synchronizedList(new ArrayList<>());

        // Initialize new fields
        this.refreshConfig = RefreshConfig.DISABLED;
        this.playerInventoryInteractionEnabled = false;
        this.playerInventoryHandlerName = null;
        this.contextAware = false;
    }

    @Override
    public void open(@NotNull Player player) {
        int page = playerPages.getOrDefault(player.getUniqueId(), 0);
        Inventory inventory = createInventory(player, page);

        openInventories.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);

        openHandlers.forEach(handler -> handler.accept(player));
    }

    @NotNull
    private Inventory createInventory(@NotNull Player player, int page) {
        Map<String, String> allPlaceholders = buildPlaceholders(player);

        String processedTitle = applyPlaceholders(title, allPlaceholders);

        Inventory inventory = Bukkit.createInventory(null, size, processedTitle);

        // Place static items
        staticItems.values().stream()
                .sorted(Comparator.comparingInt(MenuItem::getPriority))
                .forEach(menuItem -> {
                    MenuItem replaced = menuItem.withReplacedPlaceholders(player, allPlaceholders);
                    for (int slot : replaced.getSlots()) {
                        if (slot >= 0 && slot < size) {
                            inventory.setItem(slot, replaced.getItemStack().clone());
                        }
                    }
                });

        // Place paginated items
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, pageItems.size());

        for (int i = start; i < end; i++) {
            MenuItem menuItem = pageItems.get(i);
            MenuItem replaced = menuItem.withReplacedPlaceholders(player, allPlaceholders);
            int slotIndex = i - start;

            if (slotIndex < pageSlots.length) {
                inventory.setItem(pageSlots[slotIndex], replaced.getItemStack().clone());
            }
        }

        // Place navigation items
        if (previousPageItem != null && page > 0) {
            MenuItem replaced = previousPageItem.withReplacedPlaceholders(player, allPlaceholders);
            for (int slot : replaced.getSlots()) {
                inventory.setItem(slot, replaced.getItemStack().clone());
            }
        }

        if (nextPageItem != null && (page + 1) < getTotalPages()) {
            MenuItem replaced = nextPageItem.withReplacedPlaceholders(player, allPlaceholders);
            for (int slot : replaced.getSlots()) {
                inventory.setItem(slot, replaced.getItemStack().clone());
            }
        }

        return inventory;
    }

    /**
     * Builds the complete placeholder map for a player
     */
    @NotNull
    private Map<String, String> buildPlaceholders(@NotNull Player player) {
        Map<String, String> allPlaceholders = new HashMap<>(globalPlaceholders);

        // Add context placeholders
        if (MenuContext.has(player)) {
            Map<String, String> contextPlaceholders = ContextPlaceholderRegistry.resolveAll(player);
            allPlaceholders.putAll(contextPlaceholders);
        }

        // Add page placeholders
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        allPlaceholders.put("{page}", String.valueOf(currentPage + 1));
        allPlaceholders.put("{total_pages}", String.valueOf(getTotalPages()));

        return allPlaceholders;
    }

    /**
     * Applies placeholders to a string
     */
    @NotNull
    private String applyPlaceholders(@NotNull String text, @NotNull Map<String, String> placeholders) {
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
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
        int page = playerPages.getOrDefault(player.getUniqueId(), 0);
        Inventory newInventory = createInventory(player, page);

        openInventories.put(player.getUniqueId(), newInventory);
        player.openInventory(newInventory);

        refreshHandlers.forEach(handler -> handler.accept(player));
    }

    @Override
    public void refreshSlots(@NotNull Player player, @NotNull List<Integer> slots) {
        // For paginated menu, just do a full refresh (items might have moved)
        refresh(player);
    }

    @Override
    public @NotNull String getTitle() {
        return title;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public @NotNull Optional<MenuItem> getItem(@NotNull String key) {
        return Optional.ofNullable(staticItems.get(key));
    }

    @Override
    public @NotNull Map<String, MenuItem> getItems() {
        return new HashMap<>(staticItems);
    }

    @Override
    public @NotNull Menu setItem(@NotNull String key, @NotNull MenuItem item) {
        staticItems.put(key, item);
        return this;
    }

    @Override
    public @NotNull Menu removeItem(@NotNull String key) {
        staticItems.remove(key);
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
    public boolean isPaginated() {
        return true;
    }

    @Override
    public int getCurrentPage(@NotNull Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void setPage(@NotNull Player player, int page) {
        if (page >= 0 && page < getTotalPages()) {
            playerPages.put(player.getUniqueId(), page);
            refresh(player);
        }
    }

    @Override
    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) pageItems.size() / itemsPerPage));
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

    @Override
    public @NotNull Menu onRefresh(@NotNull Consumer<Player> handler) {
        refreshHandlers.add(handler);
        return this;
    }

    // ==================== NEW METHODS ====================

    @Override
    public @NotNull RefreshConfig getRefreshConfig() {
        return refreshConfig;
    }

    @Override
    public @NotNull Menu setRefreshConfig(@NotNull RefreshConfig config) {
        this.refreshConfig = config;
        return this;
    }

    @Override
    public boolean isPlayerInventoryInteractionEnabled() {
        return playerInventoryInteractionEnabled;
    }

    @Override
    public @NotNull Menu setPlayerInventoryInteraction(boolean enabled) {
        this.playerInventoryInteractionEnabled = enabled;
        return this;
    }

    @Override
    public @Nullable String getPlayerInventoryHandlerName() {
        return playerInventoryHandlerName;
    }

    @Override
    public @NotNull Menu setPlayerInventoryHandlerName(@Nullable String handlerName) {
        this.playerInventoryHandlerName = handlerName;
        if (handlerName != null) {
            this.playerInventoryInteractionEnabled = true;
        }
        return this;
    }

    @Override
    public boolean isContextAware() {
        return contextAware;
    }

    @Override
    public @NotNull Menu setContextAware(boolean contextAware) {
        this.contextAware = contextAware;
        return this;
    }

    // ==================== PAGINATION METHODS ====================

    /**
     * Adds a page item
     */
    public PaginatedMenu addPageItem(@NotNull MenuItem item) {
        pageItems.add(item);
        return this;
    }

    /**
     * Adds multiple page items
     */
    public PaginatedMenu addPageItems(@NotNull List<MenuItem> items) {
        pageItems.addAll(items);
        return this;
    }

    /**
     * Clears all page items
     */
    public PaginatedMenu clearPageItems() {
        pageItems.clear();
        return this;
    }

    /**
     * Sets the previous page button
     */
    public PaginatedMenu setPreviousPageItem(@NotNull MenuItem item) {
        this.previousPageItem = item;
        return this;
    }

    /**
     * Sets the next page button
     */
    public PaginatedMenu setNextPageItem(@NotNull MenuItem item) {
        this.nextPageItem = item;
        return this;
    }

    /**
     * Gets the menu item at a specific slot (including page items)
     */
    @Nullable
    public MenuItem getItemAtSlot(int slot, @NotNull Player player) {
        MenuItem staticItem = staticItems.values().stream()
                .filter(item -> item.getSlots().contains(slot))
                .findFirst()
                .orElse(null);

        if (staticItem != null) {
            return staticItem;
        }

        int page = getCurrentPage(player);
        int slotIndex = -1;

        for (int i = 0; i < pageSlots.length; i++) {
            if (pageSlots[i] == slot) {
                slotIndex = i;
                break;
            }
        }

        if (slotIndex >= 0) {
            int itemIndex = page * itemsPerPage + slotIndex;
            if (itemIndex < pageItems.size()) {
                return pageItems.get(itemIndex);
            }
        }

        if (previousPageItem != null && previousPageItem.getSlots().contains(slot)) {
            return previousPageItem;
        }

        if (nextPageItem != null && nextPageItem.getSlots().contains(slot)) {
            return nextPageItem;
        }

        return null;
    }
}