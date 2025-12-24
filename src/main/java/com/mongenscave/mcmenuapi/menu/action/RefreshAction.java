package com.mongenscave.mcmenuapi.menu.action;

import com.mongenscave.mcmenuapi.McMenuAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Action that refreshes the current menu
 */
public class RefreshAction implements Action {

    @Override
    public void execute(@NotNull Player player) {
        McMenuAPI.getInstance().refreshMenu(player);
    }
}