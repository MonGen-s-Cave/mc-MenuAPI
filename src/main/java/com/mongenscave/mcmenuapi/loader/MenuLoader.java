package com.mongenscave.mcmenuapi.loader;

import com.mongenscave.mcmenuapi.item.ItemFactory;
import com.mongenscave.mcmenuapi.menu.SimpleMenu;
import com.mongenscave.mcmenuapi.menu.action.Action;
import com.mongenscave.mcmenuapi.menu.action.ConditionalAction;
import com.mongenscave.mcmenuapi.menu.item.MenuItem;
import com.mongenscave.mcmenuapi.parser.ConditionParser;
import com.mongenscave.mcmenuapi.refresh.RefreshConfig;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@UtilityClass
public class MenuLoader {

    @Nullable
    public SimpleMenu loadMenu(@NotNull File file) {
        try {
            YamlDocument document = YamlDocument.create(file);

            String title = document.getString("title", "Menu");
            int size = document.getInt("size", 54);

            if (size % 9 != 0 || size < 9 || size > 54) {
                size = 54;
            }

            SimpleMenu menu = new SimpleMenu(title, size);

            // Load placeable slots
            String placeableSlotsStr = document.getString("placeable-slots", "");
            if (!placeableSlotsStr.isEmpty()) {
                List<Integer> placeableSlots = parseSlots(placeableSlotsStr);
                menu.setPlaceableSlots(placeableSlots);
            }

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

            // Load pagination
            if (document.contains("pagination.enabled") && document.getBoolean("pagination.enabled")) {
                int pages = document.getInt("pagination.pages", 1);
                menu.setPaginated(pages);
            }

            // ==================== NEW: Load auto-refresh config ====================
            Section refreshSection = document.getSection("auto-refresh");
            if (refreshSection != null && refreshSection.getBoolean("enabled", false)) {
                int intervalTicks = refreshSection.getInt("interval", 20);
                List<Integer> slots = parseSlotsList(refreshSection.get("slots"));

                RefreshConfig refreshConfig;
                if (slots.isEmpty()) {
                    refreshConfig = RefreshConfig.all(intervalTicks);
                } else {
                    refreshConfig = RefreshConfig.slots(intervalTicks, slots);
                }
                menu.setRefreshConfig(refreshConfig);
            }

            // ==================== NEW: Load player inventory config ====================
            Section playerInvSection = document.getSection("player-inventory");
            if (playerInvSection != null) {
                boolean enabled = playerInvSection.getBoolean("enabled", false);
                menu.setPlayerInventoryInteraction(enabled);

                String handler = playerInvSection.getString("handler", null);
                if (handler != null && !handler.isEmpty()) {
                    menu.setPlayerInventoryHandlerName(handler);
                }
            }

            // ==================== NEW: Load context-aware flag ====================
            boolean contextAware = document.getBoolean("context-aware", false);
            menu.setContextAware(contextAware);

            return menu;
        } catch (IOException exception) {
            return null;
        }
    }

    @Nullable
    private MenuItem loadMenuItem(@NotNull Section section, @NotNull String itemKey) {
        Optional<ItemStack> itemStackOpt = ItemFactory.buildItem(section);

        if (itemStackOpt.isEmpty()) {
            return null;
        }

        ItemStack itemStack = itemStackOpt.get();

        Object slotConfig = section.get("slot");
        List<Integer> slots = parseSlots(slotConfig);

        if (slots.isEmpty()) {
            return null;
        }

        List<Action> actions = new ArrayList<>();
        List<String> actionStrings = section.getStringList("actions");

        for (String actionString : actionStrings) {
            Action action = parseAction(actionString);
            if (action != null) {
                actions.add(action);
            }
        }

        int priority = section.getInt("priority", 0);
        boolean clickable = section.getBoolean("clickable", true);

        // Load visibility condition
        String visibleIf = section.getString("visible-if", null);

        Map<String, String> metadata = new HashMap<>();
        Section metadataSection = section.getSection("metadata");
        if (metadataSection != null) {
            for (String key : metadataSection.getRoutesAsStrings(false)) {
                metadata.put(key, metadataSection.getString(key, ""));
            }
        }

        MenuItem.MenuItemBuilder builder = MenuItem.builder()
                .itemStack(itemStack)
                .slots(slots)
                .actions(actions)
                .priority(priority)
                .clickable(clickable);

        // Add visibility condition if present
        if (visibleIf != null && !visibleIf.isEmpty()) {
            builder.visibilityCondition(visibleIf);
        }

        metadata.forEach(builder::placeholder);

        return builder.build();
    }

    /**
     * Parses slots from various config formats
     */
    @NotNull
    private List<Integer> parseSlots(@Nullable Object slotConfig) {
        if (slotConfig == null) {
            return Collections.emptyList();
        }

        if (slotConfig instanceof Integer integer) {
            return List.of(integer);
        }

        if (slotConfig instanceof String slotStr) {
            return parseSlotString(slotStr);
        }

        if (slotConfig instanceof List<?> list) {
            List<Integer> slots = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Integer i) {
                    slots.add(i);
                } else if (item instanceof String s) {
                    slots.addAll(parseSlotString(s));
                }
            }
            return slots;
        }

        return Collections.emptyList();
    }

    /**
     * Parses a slot string with ranges and special keywords
     */
    @NotNull
    private List<Integer> parseSlotString(@NotNull String slotStr) {
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
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                try {
                    slots.add(Integer.parseInt(part));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return slots;
    }

    /**
     * Parses slots list from config
     */
    @NotNull
    private List<Integer> parseSlotsList(@Nullable Object config) {
        if (config == null) {
            return Collections.emptyList();
        }

        if (config instanceof List<?> list) {
            List<Integer> slots = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Integer i) {
                    slots.add(i);
                } else if (item instanceof Number n) {
                    slots.add(n.intValue());
                }
            }
            return slots;
        }

        return Collections.emptyList();
    }

    @Nullable
    private Action parseAction(@NotNull String actionString) {
        actionString = actionString.trim();

        if (actionString.startsWith("[IF]")) {
            return parseConditionalAction(actionString);
        }

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
            case "PAGE" -> Action.page(value);
            case "ACTION" -> new ContextAction(value);
            case "REFRESH" -> Action.refresh();
            default -> null;
        };
    }

    @Nullable
    private Action parseConditionalAction(@NotNull String actionString) {
        try {
            String remaining = actionString.substring(4).trim();

            int thenIndex = remaining.indexOf("[THEN]");
            if (thenIndex == -1) return null;

            String conditionStr = remaining.substring(0, thenIndex).trim();
            Predicate<org.bukkit.entity.Player> condition = ConditionParser.parse(conditionStr);
            if (condition == null) return null;

            int elseIndex = remaining.indexOf("[ELSE]");

            String thenPart;
            if (elseIndex != -1) {
                thenPart = remaining.substring(thenIndex + 6, elseIndex).trim();
            } else {
                thenPart = remaining.substring(thenIndex + 6).trim();
            }

            List<Action> thenActions = parseMultipleActions(thenPart);

            List<Action> elseActions = new ArrayList<>();
            if (elseIndex != -1) {
                String elsePart = remaining.substring(elseIndex + 6).trim();
                elseActions = parseMultipleActions(elsePart);
            }

            return ConditionalAction.builder()
                    .condition(condition)
                    .then(thenActions.toArray(new Action[0]))
                    .otherwise(elseActions.toArray(new Action[0]))
                    .build();

        } catch (Exception exception) {
            return null;
        }
    }

    @NotNull
    private List<Action> parseMultipleActions(@NotNull String actionsString) {
        List<Action> actions = new ArrayList<>();

        String[] parts = actionsString.split("(?=\\[)");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            Action action = parseAction(part);
            if (action != null) {
                actions.add(action);
            }
        }

        return actions;
    }

    /**
     * Custom action that triggers registered context action handlers
     */
    public static class ContextAction implements Action {
        private final String actionName;

        public ContextAction(@NotNull String actionName) {
            this.actionName = actionName.toUpperCase();
        }

        @Override
        public void execute(@NotNull org.bukkit.entity.Player player) {
            // This is handled by the MenuListener using ContextActionRegistry
        }

        @NotNull
        public String getActionName() {
            return actionName;
        }
    }

    /**
     * Legacy custom action for backwards compatibility
     */
    public static class CustomAction implements Action {
        private final String actionName;

        public CustomAction(@NotNull String actionName) {
            this.actionName = actionName.toUpperCase();
        }

        @Override
        public void execute(@NotNull org.bukkit.entity.Player player) {
        }

        @NotNull
        public String getActionName() {
            return actionName;
        }
    }
}