package com.mongenscave.mcmenuapi.item;

import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builder class for creating ItemStacks with fluent API
 */
public class ItemBuilder implements ItemFactory {

    private final ItemStack itemStack;
    private final ItemMeta meta;
    private boolean finished = false;

    public ItemBuilder(@NotNull ItemStack item) {
        this.itemStack = item.clone();
        this.meta = itemStack.getItemMeta();
    }

    ItemBuilder(@NotNull Material type) {
        this(type, 1);
    }

    public ItemBuilder(@NotNull Material type, @Range(from = 0, to = 64) int amount) {
        this.itemStack = new ItemStack(type, amount);
        this.meta = itemStack.getItemMeta();
    }

    @Override
    public ItemBuilder setType(@NotNull Material material) {
        itemStack.setType(material);
        return this;
    }

    @Override
    public ItemBuilder setCount(@Range(from = 0, to = 64) int newCount) {
        itemStack.setAmount(newCount);
        return this;
    }

    @Override
    public ItemBuilder setName(@NotNull String name) {
        if (meta != null) {
            meta.setDisplayName(ColorProcessor.process(name));
        }
        return this;
    }

    @Override
    public ItemBuilder setLore(@NotNull List<String> lore) {
        if (meta != null) {
            List<String> processedLore = lore.stream()
                    .map(ColorProcessor::process)
                    .toList();
            meta.setLore(processedLore);
        }
        return this;
    }

    @Override
    public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    @Override
    public ItemBuilder addLore(@NotNull String... lores) {
        if (meta != null) {
            List<String> loreList = Arrays.stream(lores)
                    .map(ColorProcessor::process)
                    .toList();

            List<String> currentLores = meta.getLore();
            currentLores = currentLores == null ? new ArrayList<>() : new ArrayList<>(currentLores);

            currentLores.addAll(loreList);
            meta.setLore(currentLores);
        }
        return this;
    }

    @Override
    public ItemBuilder setUnbreakable() {
        if (meta != null) {
            meta.setUnbreakable(true);
        }
        return this;
    }

    @Override
    public ItemBuilder addFlag(@NotNull ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    @Override
    public ItemBuilder removeLore(int line) {
        if (meta != null) {
            List<String> lores = meta.getLore();
            lores = lores == null ? new ArrayList<>() : new ArrayList<>(lores);

            if (line >= 0 && line < lores.size()) {
                lores.remove(line);
                meta.setLore(lores);
            }
        }
        return this;
    }

    @Override
    public ItemStack finish() {
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
        finished = true;
        return itemStack;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}