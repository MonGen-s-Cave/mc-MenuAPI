package com.mongenscave.mcmenuapi.menu.action;

import com.mongenscave.mcmenuapi.McMenuAPI;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class OpenMenuAction implements Action {

    private final String menuFile;

    @Override
    public void execute(@NotNull Player player) {
        McMenuAPI.getInstance().openMenu(player, menuFile);
    }
}