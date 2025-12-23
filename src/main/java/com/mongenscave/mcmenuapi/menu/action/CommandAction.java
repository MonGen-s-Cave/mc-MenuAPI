package com.mongenscave.mcmenuapi.menu.action;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class CommandAction implements Action {

    private final String command;

    @Override
    public void execute(@NotNull Player player) {
        String processedCommand = command.replace("{player}", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
    }
}