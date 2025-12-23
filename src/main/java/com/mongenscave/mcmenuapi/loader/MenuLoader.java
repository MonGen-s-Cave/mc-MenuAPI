package com.mongenscave.mcmenuapi.loader;

import com.mongenscave.mcmenuapi.item.ItemFactory;
import com.mongenscave.mcmenuapi.menu.SimpleMenu;
import com.mongenscave.mcmenuapi.menu.action.Action;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for loading menus from YAML files
 */
@UtilityClass
public class MenuLoader {

    /**
     * Loads a menu from a YAML file
     *
     * @param file the YAML file
     * @return the loaded menu, or null if failed
     */
    @Nullable
    public SimpleMenu loadMenu(@NotNull File file) {
        try {
            YamlDocument document = YamlDocument.create(file);

            String title = document.getString("title", "Menu");
            int size = document.getInt("size", 54);

            // Validate size
            if (size % 9 != 0 || size < 9 || size > 54) {
                size = 54;
            }

            SimpleMenu menu = new SimpleMenu(title, size);

            // Load items
            Section itemsSection = document.getSection("items");
            if (itemsSection != null) {
                for (String key : itemsSection.getRoutesAsStrings(false)) {
                    Section itemSection = itemsSection.getSection(key);
                    if (itemSection == null) continue;

                    MenuItem menuItem = loadMenuItem(itemSection, key);
                    if (menuItem != null) {
                        menu.setItem(key, menuItem);
                    }
                }
            }

            // Load pagination settings if exists
            if (document.contains("pagination.enabled") && document.getBoolean("pagination.enabled")) {
                int pages = document.getInt("pagination.pages", 1);
                menu.setPaginated(pages);
            }

            return menu;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a menu item from a configuration section
     *
     * @param section the configuration section
     * @param key the item key
     * @return the loaded menu item, or null if failed
     */
    @Nullable
    private MenuItem loadMenuItem(@NotNull Section section, @NotNull String key) {
        // Build the item using ItemFactory
        Optional<ItemStack> itemStackOpt = ItemFactory.buildItem(section);

        if (itemStackOpt.isEmpty()) {
            return null;
        }

        ItemStack itemStack = itemStackOpt.get();

        // Parse slots
        Object slotConfig = section.get("slot");
        List<Integer> slots = parseSlots(slotConfig);

        if (slots.isEmpty()) {
            return null;
        }

        // Parse actions
        List<Action> actions = new ArrayList<>();
        List<String> actionStrings = section.getStringList("actions");

        for (String actionString : actionStrings) {
            Action action = parseAction(actionString);
            if (action != null) {
                actions.add(action);
            }
        }

        // Parse priority
        int priority = section.getInt("priority", 0);

        // Parse clickable
        boolean clickable = section.getBoolean("clickable", true);

        // Build the menu item
        return MenuItem.builder()
                .itemStack(itemStack)
                .slots(slots)
                .actions(actions)
                .priority(priority)
                .clickable(clickable)
                .build();
    }

    /**
     * Parses slot configuration
     *
     * @param slotConfig the slot configuration
     * @return list of slots
     */
    @NotNull
    private List<Integer> parseSlots(@Nullable Object slotConfig) {
        if (slotConfig == null) {
            return Collections.emptyList();
        }

        if (slotConfig instanceof Integer) {
            return List.of((Integer) slotConfig);
        }

        if (slotConfig instanceof String) {
            String slotStr = (String) slotConfig;
            List<Integer> slots = new ArrayList<>();

            String[] parts = slotStr.split(",");
            for (String part : parts) {
                part = part.trim();

                if (part.contains("-")) {
                    String[] range = part.split("-");
                    if (range.length == 2) {
                        try {
                            int start = Integer.parseInt(range[0].trim());
                            int end = Integer.parseInt(range[1].trim());

                            for (int i = Math.min(start, end); i <= Math.max(start, end); i++) {
                                slots.add(i);
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                } else {
                    try {
                        slots.add(Integer.parseInt(part));
                    } catch (NumberFormatException ignored) {}
                }
            }

            return slots;
        }

        return Collections.emptyList();
    }

    /**
     * Parses an action string
     *
     * @param actionString the action string (e.g., "[COMMAND] say hello")
     * @return the parsed action, or null if invalid
     */
    @Nullable
    private Action parseAction(@NotNull String actionString) {
        if (!actionString.contains("]")) {
            return null;
        }

        int bracketEnd = actionString.indexOf(']');
        String type = actionString.substring(1, bracketEnd).toUpperCase();
        String value = actionString.substring(bracketEnd + 1).trim();

        return switch (type) {
            case "COMMAND", "CONSOLE" -> Action.consoleCommand(value);
            case "PLAYER" -> Action.playerCommand(value);
            case "SOUND" -> {
                String[] parts = value.split(" ");
                if (parts.length == 3) {
                    try {
                        float volume = Float.parseFloat(parts[1]);
                        float pitch = Float.parseFloat(parts[2]);
                        yield Action.sound(parts[0], volume, pitch);
                    } catch (NumberFormatException e) {
                        yield Action.sound(parts[0]);
                    }
                } else {
                    yield Action.sound(value);
                }
            }
            case "MESSAGE" -> Action.message(value);
            case "CLOSE" -> Action.close();
            case "OPEN" -> Action.open(value);
            case "BROADCAST" -> Action.broadcast(value);
            default -> null;
        };
    }
}