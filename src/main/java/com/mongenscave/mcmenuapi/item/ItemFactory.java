package com.mongenscave.mcmenuapi.item;

import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Factory for creating ItemStacks with fluent API
 */
public interface ItemFactory {

    @Contract("_ -> new")
    static @NotNull ItemFactory create(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    @Contract("_, _ -> new")
    static @NotNull ItemFactory create(@NotNull Material material, int count) {
        return new ItemBuilder(material, count);
    }

    @Contract("_ -> new")
    static @NotNull ItemFactory create(@NotNull ItemStack item) {
        return new ItemBuilder(item);
    }

    /**
     * Builds a custom item from configuration (Nexo, ItemsAdder, Oraxen)
     */
    static Optional<ItemStack> buildCustomItem(@NotNull Section section, @NotNull String materialName) {
        String[] parts = materialName.split(":", 2);
        if (parts.length != 2) return Optional.empty();

        String namespace = parts[0].toLowerCase();
        String itemId = parts[1];

        ItemStack baseItem = null;

        try {
            switch (namespace) {
                case "nexo" -> {
                    if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
                        try {
                            Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
                            Object builder = nexoItemsClass.getMethod("itemFromId", String.class).invoke(null, itemId);
                            if (builder != null) {
                                baseItem = (ItemStack) builder.getClass().getMethod("build").invoke(builder);
                            }
                        } catch (Exception e) {
                            return Optional.empty();
                        }
                    }
                }
                case "itemsadder" -> {
                    if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
                        try {
                            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
                            Object customStack = customStackClass.getMethod("getInstance", String.class).invoke(null, itemId);
                            if (customStack != null) {
                                baseItem = (ItemStack) customStackClass.getMethod("getItemStack").invoke(customStack);
                            }
                        } catch (Exception e) {
                            return Optional.empty();
                        }
                    }
                }
                case "oraxen" -> {
                    if (Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
                        try {
                            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
                            Object builder = oraxenItemsClass.getMethod("getItemById", String.class).invoke(null, itemId);
                            if (builder != null) {
                                baseItem = (ItemStack) builder.getClass().getMethod("build").invoke(builder);
                            }
                        } catch (Exception e) {
                            return Optional.empty();
                        }
                    }
                }
                default -> {
                    return Optional.empty();
                }
            }
        } catch (Exception exception) {
            return Optional.empty();
        }

        if (baseItem == null || baseItem.getType().isAir()) {
            return Optional.empty();
        }

        // Apply additional config
        int amount = section.getInt("amount", 1);
        amount = Math.max(1, Math.min(amount, 64));
        baseItem.setAmount(amount);

        String rawName = section.getString("name", "");
        List<String> lore = section.getStringList("lore");

        if (!rawName.isEmpty() || !lore.isEmpty()) {
            ItemMeta meta = baseItem.getItemMeta();
            if (meta != null) {
                if (!rawName.isEmpty()) {
                    meta.setDisplayName(ColorProcessor.process(rawName));
                }

                if (!lore.isEmpty()) {
                    List<String> processedLore = lore.stream()
                            .map(ColorProcessor::process)
                            .toList();
                    meta.setLore(processedLore);
                }

                baseItem.setItemMeta(meta);
            }
        }

        // Apply enchantments
        List<String> enchantmentStrings = section.getStringList("enchantments");
        for (String enchantmentString : enchantmentStrings) {
            String[] enchParts = enchantmentString.split(":");
            if (enchParts.length == 2) {
                try {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchParts[0].toLowerCase()));
                    if (enchantment != null) {
                        int level = Integer.parseInt(enchParts[1]);
                        baseItem.addUnsafeEnchantment(enchantment, level);
                    }
                } catch (Exception ignored) {}
            }
        }

        // Apply unbreakable
        boolean unbreakable = section.getBoolean("unbreakable", false);
        if (unbreakable) {
            baseItem.editMeta(meta -> meta.setUnbreakable(true));
        }

        return Optional.of(baseItem);
    }

    /**
     * Builds an item from configuration section
     */
    static Optional<ItemStack> buildItem(@NotNull Section section) {
        try {
            String materialName = section.getString("material");
            if (materialName == null || materialName.isEmpty()) return Optional.empty();

            // Check if it's a custom item
            if (materialName.contains(":")) {
                return buildCustomItem(section, materialName);
            }

            // Normal material
            Material material;
            try {
                material = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }

            int amount = section.getInt("amount", 1);
            amount = Math.max(1, Math.min(amount, 64));

            String rawName = section.getString("name", "");
            String processedName = rawName.isEmpty() ? "" : ColorProcessor.process(rawName);

            List<String> lore = section.getStringList("lore").stream()
                    .map(ColorProcessor::process)
                    .toList();

            ItemStack item = ItemFactory.create(material, amount)
                    .setName(processedName)
                    .addLore(lore.toArray(new String[0]))
                    .finish();

            // Apply enchantments
            List<String> enchantmentStrings = section.getStringList("enchantments");
            for (String enchantmentString : enchantmentStrings) {
                String[] parts = enchantmentString.split(":");
                if (parts.length == 2) {
                    try {
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(parts[0].toLowerCase()));
                        if (enchantment != null) {
                            int level = Integer.parseInt(parts[1]);
                            item.addUnsafeEnchantment(enchantment, level);
                        }
                    } catch (Exception ignored) {}
                }
            }

            // Apply unbreakable
            boolean unbreakable = section.getBoolean("unbreakable", false);
            if (unbreakable) {
                item.editMeta(meta -> meta.setUnbreakable(true));
            }

            // Apply model data
            item.editMeta(meta -> {
                int modelData = section.getInt("modeldata", 0);
                if (modelData > 0) meta.setCustomModelData(modelData);

                String modelKey = section.getString("modelkey", "");
                if (!modelKey.isEmpty()) {
                    try {
                        meta.setItemModel(new NamespacedKey("minecraft", modelKey));
                    } catch (Exception ignored) {}
                }
            });

            // Apply clickable flag
            boolean clickable = section.getBoolean("clickable", true);
            if (!clickable) {
                item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
            }

            return Optional.of(item);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    ItemFactory setType(@NotNull Material material);

    ItemFactory setCount(int newCount);

    ItemFactory setName(@NotNull String name);

    ItemFactory setLore(@NotNull List<String> lore);

    ItemFactory addEnchantment(@NotNull Enchantment enchantment, int level);

    ItemFactory addLore(@NotNull String... lores);

    ItemFactory setUnbreakable();

    ItemFactory addFlag(@NotNull ItemFlag... flags);

    ItemFactory removeLore(int line);

    ItemStack finish();

    boolean isFinished();
}