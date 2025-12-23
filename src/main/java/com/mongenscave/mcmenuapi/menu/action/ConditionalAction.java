package com.mongenscave.mcmenuapi.menu.action;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Conditional action that executes different actions based on a condition
 * <p>
 * Example YAML:
 * actions:
 *   - "[IF] {page} == 0 [THEN] [OPEN] main.yml [ELSE] [PAGE] -1"
 */
@RequiredArgsConstructor
public class ConditionalAction implements Action {

    private final Predicate<Player> condition;
    private final List<Action> thenActions;
    private final List<Action> elseActions;

    @Override
    public void execute(@NotNull Player player) {
        if (condition.test(player)) {
            thenActions.forEach(action -> action.execute(player));
        } else {
            elseActions.forEach(action -> action.execute(player));
        }
    }

    /**
     * Builder for creating conditional actions
     */
    public static class Builder {
        private Predicate<Player> condition;
        private final List<Action> thenActions = new ArrayList<>();
        private final List<Action> elseActions = new ArrayList<>();

        public Builder condition(Predicate<Player> condition) {
            this.condition = condition;
            return this;
        }

        public Builder then(Action... actions) {
            thenActions.addAll(List.of(actions));
            return this;
        }

        public Builder otherwise(Action... actions) {
            elseActions.addAll(List.of(actions));
            return this;
        }

        public ConditionalAction build() {
            return new ConditionalAction(condition, thenActions, elseActions);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}