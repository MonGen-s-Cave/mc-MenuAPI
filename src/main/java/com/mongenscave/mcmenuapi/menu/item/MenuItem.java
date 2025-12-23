package com.mongenscave.mcmenuapi.menu.item;

import com.mongenscave.mcmenuapi.menu.action.Action;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a clickable item in a menu
 */
@Getter
@Builder(toBuilder = true)
public class MenuItem {

    /**
     * The base item stack
     */
    @NotNull
    private final ItemStack itemStack;

    /**
     * The slots this item occupies
     */
    @Singular
    @NotNull
    private final List<Integer> slots;

    /**
     * Actions to execute when clicked
     */
    @Singular
    @NotNull
    private final List<Action> actions;

    /**
     * Priority for placement (higher = placed later)
     */
    @Builder.Default
    private final int priority = 0;

    /**
     * Whether this item is clickable
     */
    @Builder.Default
    private final boolean clickable = true;

    /**
     * Custom click handler that runs alongside actions
     */
    @Nullable
    private final BiConsumer<Player, MenuItem> customClickHandler;

    /**
     * Placeholders specific to this item
     */
    @Singular("placeholder")
    @NotNull
    private final Map<String, String> placeholders;

    /**
     * Dynamic placeholder functions (evaluated at runtime)
     */
    @Singular("dynamicPlaceholder")
    @NotNull
    private final Map<String, Function<Player, String>> dynamicPlaceholders;

    /**
     * Creates a copy of this menu item with placeholders replaced
     *
     * @param player the player to evaluate dynamic placeholders for
     * @param globalPlaceholders global placeholders from the menu
     * @return a new MenuItem with replaced placeholders
     */
    public MenuItem withReplacedPlaceholders(@NotNull Player player, @NotNull Map<String, String> globalPlaceholders) {
        Map<String, String> allPlaceholders = new HashMap<>(globalPlaceholders);
        allPlaceholders.putAll(placeholders);

        // Add dynamic placeholders
        dynamicPlaceholders.forEach((key, function) -> {
            allPlaceholders.put(key, function.apply(player));
        });

        ItemStack replaced = itemStack.clone();
        if (replaced.hasItemMeta() && replaced.getItemMeta() != null) {
            var meta = replaced.getItemMeta();

            // Replace in display name
            if (meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                for (Map.Entry<String, String> entry : allPlaceholders.entrySet()) {
                    displayName = displayName.replace(entry.getKey(), entry.getValue());
                }
                meta.setDisplayName(displayName);
            }

            // Replace in lore
            if (meta.hasLore() && meta.getLore() != null) {
                List<String> lore = new ArrayList<>(meta.getLore());
                List<String> replacedLore = new ArrayList<>();

                for (String line : lore) {
                    for (Map.Entry<String, String> entry : allPlaceholders.entrySet()) {
                        line = line.replace(entry.getKey(), entry.getValue());
                    }
                    replacedLore.add(line);
                }

                meta.setLore(replacedLore);
            }

            replaced.setItemMeta(meta);
        }

        return this.toBuilder()
                .itemStack(replaced)
                .build();
    }

    /**
     * Executes the click action for this item
     *
     * @param player the player who clicked
     */
    public void onClick(@NotNull Player player) {
        if (!clickable) return;

        // Execute custom handler first
        if (customClickHandler != null) {
            customClickHandler.accept(player, this);
        }

        // Execute all actions
        actions.forEach(action -> action.execute(player));
    }

    /**
     * Creates a new MenuItem builder with basic setup
     *
     * @param itemStack the item stack
     * @param slots the slots
     * @return a new builder
     */
    public static MenuItemBuilder of(@NotNull ItemStack itemStack, @NotNull List<Integer> slots) {
        return MenuItem.builder()
                .itemStack(itemStack)
                .slots(slots);
    }

    /**
     * Creates a new MenuItem builder with basic setup and single slot
     *
     * @param itemStack the item stack
     * @param slot the slot
     * @return a new builder
     */
    public static MenuItemBuilder of(@NotNull ItemStack itemStack, int slot) {
        return MenuItem.builder()
                .itemStack(itemStack)
                .slot(slot);
    }
}