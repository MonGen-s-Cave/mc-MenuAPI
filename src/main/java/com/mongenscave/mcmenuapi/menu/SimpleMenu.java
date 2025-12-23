package com.mongenscave.mcmenuapi.menu;

import com.mongenscave.mcmenuapi.builder.BuildContextImpl;
import com.mongenscave.mcmenuapi.builder.DynamicMenuBuilder;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import com.mongenscave.mcmenuapi.registry.DynamicClickRegistry;
import com.mongenscave.mcmenuapi.registry.DynamicMenuRegistry;
import com.mongenscave.mcmenuapi.registry.PlaceholderRegistry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
    private List<Integer> placeableSlots;

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
        this.placeableSlots = new ArrayList<>();
        this.paginated = false;
        this.totalPages = 1;
    }

    public void setPlaceableSlots(@NotNull List<Integer> slots) {
        this.placeableSlots = new ArrayList<>(slots);
    }

    public boolean isSlotPlaceable(int slot) {
        return placeableSlots.contains(slot);
    }

    @Override
    public void open(@NotNull Player player) {
        Map<String, String> allPlaceholders = new HashMap<>(globalPlaceholders);
        String menuFileName = getMenuFileNameForPlayer(player);

        if (menuFileName != null) {
            Map<String, String> dynamicPlaceholders = PlaceholderRegistry.resolveAll(player, menuFileName);
            allPlaceholders.putAll(dynamicPlaceholders);
        }

        String processedTitle = title;
        for (Map.Entry<String, String> entry : allPlaceholders.entrySet()) {
            processedTitle = processedTitle.replace(entry.getKey(), entry.getValue());
        }

        Inventory inventory = Bukkit.createInventory(null, size, ColorProcessor.process(processedTitle));

        items.values().stream()
                .sorted(Comparator.comparingInt(MenuItem::getPriority))
                .forEach(menuItem -> {
                    MenuItem replaced = menuItem.withReplacedPlaceholders(player, allPlaceholders);
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

            Map<String, String> allPlaceholders = new HashMap<>(globalPlaceholders);
            String menuFileName = getMenuFileNameForPlayer(player);
            if (menuFileName != null) {
                Map<String, String> dynamicPlaceholders = PlaceholderRegistry.resolveAll(player, menuFileName);
                allPlaceholders.putAll(dynamicPlaceholders);
            }

            items.values().stream()
                    .sorted(Comparator.comparingInt(MenuItem::getPriority))
                    .forEach(menuItem -> {
                        MenuItem replaced = menuItem.withReplacedPlaceholders(player, allPlaceholders);
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
    public @NotNull Map<String, MenuItem> getItems() {
        return new HashMap<>(items);
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
    public boolean isPaginated() {
        return paginated;
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
    public int getTotalPages() {
        return totalPages;
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

    public SimpleMenu setPaginated(int totalPages) {
        this.paginated = true;
        this.totalPages = Math.max(1, totalPages);
        return this;
    }

    public void openWithFileName(@NotNull Player player, @NotNull String fileName) {
        DynamicMenuBuilder builder = DynamicMenuRegistry.getBuilder(fileName);

        if (builder != null) {
            DynamicClickRegistry.clearMenu(fileName);

            Map<String, String> allPlaceholders = new HashMap<>(globalPlaceholders);
            Map<String, String> dynamicPlaceholders = PlaceholderRegistry.resolveAll(player, fileName);
            allPlaceholders.putAll(dynamicPlaceholders);

            String processedTitle = title;
            for (Map.Entry<String, String> entry : allPlaceholders.entrySet()) {
                processedTitle = processedTitle.replace(entry.getKey(), entry.getValue());
            }

            Inventory inventory = Bukkit.createInventory(null, size, ColorProcessor.process(processedTitle));

            items.values().stream()
                    .sorted(Comparator.comparingInt(MenuItem::getPriority))
                    .forEach(menuItem -> {
                        MenuItem replaced = menuItem.withReplacedPlaceholders(player, allPlaceholders);
                        for (int slot : replaced.getSlots()) {
                            if (slot >= 0 && slot < size) {
                                inventory.setItem(slot, replaced.getItemStack().clone());
                            }
                        }
                    });

            BuildContextImpl context = new BuildContextImpl(player, inventory, fileName);

            builder.build(context);

            openInventories.put(player.getUniqueId(), inventory);
            player.openInventory(inventory);
            openHandlers.forEach(handler -> handler.accept(player));
        } else {
            open(player);
        }
    }

    @Nullable
    public MenuItem getItemAtSlot(int slot) {
        return items.values().stream()
                .filter(item -> item.getSlots().contains(slot))
                .findFirst()
                .orElse(null);
    }

    private String getMenuFileNameForPlayer(@NotNull Player player) {
        return com.mongenscave.mcmenuapi.McMenuAPI.getInstance()
                .getLoadedMenus()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == this)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}