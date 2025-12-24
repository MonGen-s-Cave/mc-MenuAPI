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
import java.util.function.Predicate;

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
     * Visibility condition string from YAML (e.g., "{context.autoSellEnabled} == true")
     */
    @Nullable
    private final String visibilityCondition;

    /**
     * Programmatic visibility predicate
     */
    @Nullable
    private final Predicate<Player> visibilityPredicate;

    /**
     * Checks if this item should be visible for a player
     *
     * @param player the player
     * @param placeholders the resolved placeholders
     * @return true if visible
     */
    public boolean isVisible(@NotNull Player player, @NotNull Map<String, String> placeholders) {
        // Check programmatic predicate first
        if (visibilityPredicate != null) {
            return visibilityPredicate.test(player);
        }

        // Check YAML condition
        if (visibilityCondition == null || visibilityCondition.isEmpty()) {
            return true;
        }

        return evaluateVisibilityCondition(visibilityCondition, placeholders);
    }

    /**
     * Evaluates a visibility condition string
     */
    private boolean evaluateVisibilityCondition(@NotNull String condition, @NotNull Map<String, String> placeholders) {
        String evaluated = condition;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            evaluated = evaluated.replace(entry.getKey(), entry.getValue());
        }

        evaluated = evaluated.trim();

        if (evaluated.equalsIgnoreCase("true")) {
            return true;
        }
        if (evaluated.equalsIgnoreCase("false")) {
            return false;
        }

        if (evaluated.contains("==")) {
            String[] parts = evaluated.split("==", 2);
            if (parts.length == 2) {
                return parts[0].trim().equalsIgnoreCase(parts[1].trim());
            }
        }

        if (evaluated.contains("!=")) {
            String[] parts = evaluated.split("!=", 2);
            if (parts.length == 2) {
                return !parts[0].trim().equalsIgnoreCase(parts[1].trim());
            }
        }

        return true;
    }

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

        dynamicPlaceholders.forEach((key, function) -> allPlaceholders.put(key, function.apply(player)));

        ItemStack replaced = itemStack.clone();
        if (replaced.hasItemMeta() && replaced.getItemMeta() != null) {
            var meta = replaced.getItemMeta();

            if (meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                for (Map.Entry<String, String> entry : allPlaceholders.entrySet()) {
                    displayName = displayName.replace(entry.getKey(), entry.getValue());
                }
                meta.setDisplayName(displayName);
            }

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

        if (customClickHandler != null) {
            customClickHandler.accept(player, this);
        }

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