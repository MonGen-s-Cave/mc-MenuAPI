package com.mongenscave.mcmenuapi.parser;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.menu.Menu;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Parses condition strings for conditional actions
 * <p>
 * Supported conditions:
 * - {page} == 0
 * - {page} > 0
 * - {page} < 5
 * - {health} >= 10
 * - {level} <= 20
 * - {permission} has some.permission
 * - {name} equals PlayerName
 */
@UtilityClass
public class ConditionParser {

    /**
     * Parses a condition string into a predicate
     *
     * @param conditionString the condition string
     * @return predicate that tests the condition
     */
    @Nullable
    public Predicate<Player> parse(@NotNull String conditionString) {
        conditionString = conditionString.trim();

        if (conditionString.contains("{page}")) {
            return parsePageCondition(conditionString);
        }

        if (conditionString.contains("{health}")) {
            return parseHealthCondition(conditionString);
        }

        if (conditionString.contains("{level}")) {
            return parseLevelCondition(conditionString);
        }

        if (conditionString.contains("{permission}")) {
            return parsePermissionCondition(conditionString);
        }

        if (conditionString.contains("{name}")) {
            return parseNameCondition(conditionString);
        }

        return null;
    }

    @NotNull
    private Predicate<Player> parsePageCondition(@NotNull String condition) {
        String[] parts = condition.replace("{page}", "").trim().split(" ", 2);
        if (parts.length < 2) return player -> false;

        String operator = parts[0];
        int value = Integer.parseInt(parts[1]);

        return player -> {
            Menu menu = McMenuAPI.getInstance().getOpenMenu(player);
            if (menu == null) return false;

            int currentPage = menu.getCurrentPage(player);

            return switch (operator) {
                case "==", "equals" -> currentPage == value;
                case "!=" -> currentPage != value;
                case ">" -> currentPage > value;
                case "<" -> currentPage < value;
                case ">=" -> currentPage >= value;
                case "<=" -> currentPage <= value;
                default -> false;
            };
        };
    }

    @NotNull
    private Predicate<Player> parseHealthCondition(@NotNull String condition) {
        String[] parts = condition.replace("{health}", "").trim().split(" ", 2);
        if (parts.length < 2) return player -> false;

        String operator = parts[0];
        double value = Double.parseDouble(parts[1]);

        return player -> {
            double health = player.getHealth();

            return switch (operator) {
                case "==", "equals" -> health == value;
                case "!=" -> health != value;
                case ">" -> health > value;
                case "<" -> health < value;
                case ">=" -> health >= value;
                case "<=" -> health <= value;
                default -> false;
            };
        };
    }

    @NotNull
    private Predicate<Player> parseLevelCondition(@NotNull String condition) {
        String[] parts = condition.replace("{level}", "").trim().split(" ", 2);
        if (parts.length < 2) return player -> false;

        String operator = parts[0];
        int value = Integer.parseInt(parts[1]);

        return player -> {
            int level = player.getLevel();

            return switch (operator) {
                case "==", "equals" -> level == value;
                case "!=" -> level != value;
                case ">" -> level > value;
                case "<" -> level < value;
                case ">=" -> level >= value;
                case "<=" -> level <= value;
                default -> false;
            };
        };
    }

    @NotNull
    private Predicate<Player> parsePermissionCondition(@NotNull String condition) {
        String permission = condition.replace("{permission}", "").replace("has", "").trim();
        return player -> player.hasPermission(permission);
    }

    @NotNull
    private Predicate<Player> parseNameCondition(@NotNull String condition) {
        String name = condition.replace("{name}", "").replace("equals", "").trim();
        return player -> player.getName().equalsIgnoreCase(name);
    }
}