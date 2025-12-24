package com.mongenscave.mcmenuapi.refresh;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.menu.Menu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages automatic menu refresh for all open menus.
 * Uses a single master task for efficiency.
 */
public class MenuRefreshManager {

    private final Plugin plugin;
    private final McMenuAPI menuAPI;
    private BukkitTask masterTask;
    private final AtomicLong currentTick = new AtomicLong(0);
    private final Map<UUID, Long> lastRefresh = new ConcurrentHashMap<>();

    @Getter private boolean running = false;

    public MenuRefreshManager(@NotNull Plugin plugin, @NotNull McMenuAPI menuAPI) {
        this.plugin = plugin;
        this.menuAPI = menuAPI;
    }

    /**
     * Starts the refresh manager
     */
    public void start() {
        if (running) return;
        running = true;

        masterTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long tick = currentTick.incrementAndGet();

            Map<UUID, Menu> openMenus = menuAPI.getOpenMenus();
            if (openMenus.isEmpty()) return;

            for (Map.Entry<UUID, Menu> entry : openMenus.entrySet()) {
                UUID playerId = entry.getKey();
                Menu menu = entry.getValue();

                RefreshConfig config = menu.getRefreshConfig();
                if (!config.isEnabled()) continue;

                int interval = config.getIntervalTicks();
                if (interval <= 0) continue;

                if (tick % interval == 0) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        try {
                            if (config.isRefreshAll()) {
                                menu.refresh(player);
                            } else {
                                menu.refreshSlots(player, config.getSlots());
                            }
                            lastRefresh.put(playerId, tick);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to refresh menu for " + player.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }, 1L, 1L);
    }

    /**
     * Stops the refresh manager
     */
    public void stop() {
        if (masterTask != null && !masterTask.isCancelled()) {
            masterTask.cancel();
            masterTask = null;
        }
        running = false;
        lastRefresh.clear();
        currentTick.set(0);
    }

    /**
     * Forces an immediate refresh for all open menus
     */
    public void forceRefreshAll() {
        Map<UUID, Menu> openMenus = menuAPI.getOpenMenus();

        for (Map.Entry<UUID, Menu> entry : openMenus.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                try {
                    entry.getValue().refresh(player);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to force refresh menu for " + player.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Forces an immediate refresh for a specific player
     *
     * @param player the player
     */
    public void forceRefresh(@NotNull Player player) {
        Menu menu = menuAPI.getOpenMenu(player);
        if (menu != null) {
            menu.refresh(player);
        }
    }

    /**
     * Gets the last refresh tick for a player
     *
     * @param player the player
     * @return the tick or -1 if never refreshed
     */
    public long getLastRefreshTick(@NotNull Player player) {
        return lastRefresh.getOrDefault(player.getUniqueId(), -1L);
    }

    /**
     * Gets the current tick count
     */
    public long getCurrentTick() {
        return currentTick.get();
    }

    /**
     * Called when a player's menu is closed
     *
     * @param playerId the player UUID
     */
    public void onMenuClose(@NotNull UUID playerId) {
        lastRefresh.remove(playerId);
    }
}