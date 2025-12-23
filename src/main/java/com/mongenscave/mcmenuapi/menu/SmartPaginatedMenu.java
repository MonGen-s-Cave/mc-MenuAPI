package com.mongenscave.mcmenuapi.menu;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.loader.MenuLoader;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * SMART PAGINATED MENU
 * Automatically handles everything from YAML with enhanced features
 */
public class SmartPaginatedMenu {
    @Getter private final PaginatedMenu menu;
    private final YamlDocument config;

    private SmartPaginatedMenu(@NotNull String yamlFile, @NotNull Map<String, String> titlePlaceholders) {
        File file = new File(McMenuAPI.getInstance().getMenusFolder(), yamlFile);

        try {
            this.config = YamlDocument.create(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + yamlFile, e);
        }

        SimpleMenu baseMenu = MenuLoader.loadMenu(file);
        if (baseMenu == null) {
            throw new RuntimeException("Failed to parse " + yamlFile);
        }

        String finalTitle = baseMenu.getTitle();
        for (Map.Entry<String, String> entry : titlePlaceholders.entrySet()) {
            finalTitle = finalTitle.replace(entry.getKey(), entry.getValue());
        }

        int[] pageSlots = parsePageSlots();
        this.menu = new PaginatedMenu(ColorProcessor.process(finalTitle), baseMenu.getSize(), pageSlots);

        autoAddStaticItems(baseMenu);
    }

    /**
     * Parses page slots from YAML
     */
    private int[] parsePageSlots() {
        String slotsStr = config.getString("dynamic-lists.boxes.slots",
                config.getString("dynamic-lists.players.slots",
                        config.getString("dynamic-lists.rewards.slots", "10-43")));

        List<Integer> slots = new ArrayList<>();

        for (String part : slotsStr.split(",")) {
            part = part.trim();
            if (part.contains("-")) {
                String[] range = part.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                for (int i = start; i <= end; i++) slots.add(i);
            } else {
                slots.add(Integer.parseInt(part));
            }
        }

        return slots.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Automatically adds static items from YAML with smart handling
     */
    private void autoAddStaticItems(@NotNull SimpleMenu baseMenu) {
        baseMenu.getItems().forEach((key, item) -> {
            switch (key.toLowerCase()) {
                case "back" -> {
                    MenuItem smartBack = item.toBuilder()
                            .customClickHandler((p, mi) -> handleSmartBack(p))
                            .build();
                    menu.setPreviousPageItem(smartBack);
                }

                case "forward", "next" -> {
                    MenuItem smartForward = item.toBuilder()
                            .customClickHandler((p, mi) -> handleSmartForward(p))
                            .build();
                    menu.setNextPageItem(smartForward);
                }

                default -> menu.setItem(key, item);
            }
        });
    }

    /**
     * Smart back button handler
     */
    private void handleSmartBack(@NotNull Player player) {
        int currentPage = menu.getCurrentPage(player);

        if (currentPage == 0) {
            String previousMenu = config.getString("navigation.previous-menu", "");

            if (!previousMenu.isEmpty()) {
                McMenuAPI.getInstance().openMenu(player, previousMenu);
            } else {
                player.closeInventory();
            }
        } else {
            menu.setPage(player, currentPage - 1);
        }
    }

    /**
     * Smart forward button handler
     */
    private void handleSmartForward(@NotNull Player player) {
        int currentPage = menu.getCurrentPage(player);
        if (currentPage < menu.getTotalPages() - 1) {
            menu.setPage(player, currentPage + 1);
        }
    }

    /**
     * Adds a dynamic page item using template from YAML
     */
    public void addPageItem(@NotNull String itemId, @NotNull Consumer<ItemTemplate> configurator) {
        ItemTemplate template = new ItemTemplate(config, itemId);
        configurator.accept(template);

        MenuItem item = template.build();
        menu.addPageItem(item);
    }

    /**
     * Opens the menu
     */
    public void open(@NotNull Player player) {
        menu.open(player);
        McMenuAPI.getInstance().registerMenu(config.getString("_file", "menu.yml"), menu);
        McMenuAPI.getInstance().getOpenMenus().put(player.getUniqueId(), menu);
    }

    /**
     * Refreshes the menu
     */
    public void refresh(@NotNull Player player) {
        menu.refresh(player);
    }

    /**
     * Builder for SmartPaginatedMenu
     */
    public static class Builder {
        private String yamlFile;
        private Consumer<SmartPaginatedMenu> dynamicItemsGenerator;
        private final Map<String, String> titlePlaceholders = new HashMap<>();

        public Builder fromYAML(@NotNull String yamlFile) {
            this.yamlFile = yamlFile;
            return this;
        }

        public Builder dynamicItems(@NotNull Consumer<SmartPaginatedMenu> generator) {
            this.dynamicItemsGenerator = generator;
            return this;
        }

        public Builder titlePlaceholder(@NotNull String key, @NotNull String value) {
            this.titlePlaceholders.put(key, value);
            return this;
        }

        public SmartPaginatedMenu build() {
            SmartPaginatedMenu menu = new SmartPaginatedMenu(yamlFile, titlePlaceholders);

            if (dynamicItemsGenerator != null) {
                dynamicItemsGenerator.accept(menu);
            }

            return menu;
        }

        public void open(@NotNull Player player) {
            build().open(player);
        }
    }

    @NotNull
    @Contract(" -> new")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Item template for easy configuration
     */
    public static class ItemTemplate {
        private final YamlDocument config;
        private final Map<String, String> placeholders = new HashMap<>();
        private Consumer<Player> clickHandler;
        private BiConsumer<Player, ClickType> clickTypeHandler;
        private ItemStack customItemStack;
        private Material customMaterial;
        private Player skullOwner;

        public ItemTemplate(@NotNull YamlDocument config, @NotNull String itemId) {
            this.config = config;
        }

        public ItemTemplate placeholder(@NotNull String key, @NotNull String value) {
            placeholders.put(key, value);
            return this;
        }

        public ItemTemplate onClick(@NotNull Consumer<Player> handler) {
            this.clickHandler = handler;
            return this;
        }

        public ItemTemplate onClickWithType(@NotNull BiConsumer<Player, ClickType> handler) {
            this.clickTypeHandler = handler;
            return this;
        }

        public ItemTemplate setItemStack(@NotNull ItemStack itemStack) {
            this.customItemStack = itemStack;
            return this;
        }

        public ItemTemplate setMaterial(@NotNull Material material) {
            this.customMaterial = material;
            return this;
        }

        public ItemTemplate setPlayerHead(@NotNull Player player) {
            this.skullOwner = player;
            return this;
        }

        MenuItem build() {
            ItemStack item;

            if (customItemStack != null) {
                item = customItemStack.clone();
            } else {
                var section = config.getSection("dynamic-lists.boxes.template");
                if (section == null) {
                    section = config.getSection("dynamic-lists.players.template");
                }
                if (section == null) {
                    section = config.getSection("dynamic-lists.rewards.template");
                }

                if (section != null) {
                    item = com.mongenscave.mcmenuapi.item.ItemFactory.buildItem(section).orElse(new ItemStack(Material.STONE));
                } else {
                    item = new ItemStack(Material.STONE);
                }
            }

            if (customMaterial != null) {
                item.setType(customMaterial);
            }

            if (skullOwner != null && item.getType() == Material.PLAYER_HEAD) {
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(skullOwner);
                    item.setItemMeta(skullMeta);
                }
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    String name = meta.getDisplayName();
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        name = name.replace(entry.getKey(), entry.getValue());
                    }
                    meta.setDisplayName(ColorProcessor.process(name));
                }

                if (meta.hasLore()) {
                    List<String> lore = new ArrayList<>(meta.getLore());
                    List<String> newLore = new ArrayList<>();

                    for (String line : lore) {
                        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                            line = line.replace(entry.getKey(), entry.getValue());
                        }

                        if (line.contains("{custom_lore}")) {
                            String customLoreValue = placeholders.get("{custom_lore}");
                            if (customLoreValue != null && !customLoreValue.isEmpty()) {
                                String[] customLines = customLoreValue.split("\n");
                                for (String customLine : customLines) {
                                    newLore.add(ColorProcessor.process(customLine));
                                }
                            }
                        } else {
                            newLore.add(ColorProcessor.process(line));
                        }
                    }

                    meta.setLore(newLore);
                }

                item.setItemMeta(meta);
            }

            MenuItem.MenuItemBuilder builder = MenuItem.builder()
                    .itemStack(item)
                    .slot(0);

            if (clickTypeHandler != null) {
                builder.customClickHandler((p, mi) -> {
                    ClickType clickType = ClickType.LEFT;
                    clickTypeHandler.accept(p, clickType);
                });
            } else if (clickHandler != null) {
                builder.customClickHandler((p, mi) -> clickHandler.accept(p));
            }

            return builder.build();
        }
    }
}