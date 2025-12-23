package com.mongenscave.mcmenuapi.menu.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CloseAction implements Action {

    @Override
    public void execute(@NotNull Player player) {
        player.closeInventory();
    }
}