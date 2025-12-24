package com.mongenscave.mcmenuapi.refresh;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Configuration for menu auto-refresh behavior.
 */
public class RefreshConfig {
    @Getter private final boolean enabled;
    @Getter private final int intervalTicks;
    private final List<Integer> slots;

    public static final RefreshConfig DISABLED = new RefreshConfig(false, 0, Collections.emptyList());

    public RefreshConfig(boolean enabled, int intervalTicks, @NotNull List<Integer> slots) {
        this.enabled = enabled;
        this.intervalTicks = intervalTicks;
        this.slots = slots;
    }

    /**
     * Creates a refresh config that refreshes all slots
     *
     * @param intervalTicks the interval in ticks
     * @return the config
     */
    public static RefreshConfig all(int intervalTicks) {
        return new RefreshConfig(true, intervalTicks, Collections.emptyList());
    }

    /**
     * Creates a refresh config that refreshes specific slots
     *
     * @param intervalTicks the interval in ticks
     * @param slots the slots to refresh
     * @return the config
     */
    public static RefreshConfig slots(int intervalTicks, @NotNull List<Integer> slots) {
        return new RefreshConfig(true, intervalTicks, slots);
    }

    /**
     * Creates a disabled refresh config
     *
     * @return the config
     */
    public static RefreshConfig disabled() {
        return DISABLED;
    }

    /**
     * Gets the slots to refresh (empty = all slots)
     */
    @NotNull
    public List<Integer> getSlots() {
        return slots;
    }

    /**
     * Checks if all slots should be refreshed
     */
    public boolean isRefreshAll() {
        return slots.isEmpty();
    }
}