package com.mongenscave.mcmenuapi.menu.action;

import com.mongenscave.mcmenuapi.processor.ColorProcessor;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MessageAction implements Action {

    private final String message;

    @Override
    public void execute(@NotNull Player player) {
        String processedMessage = message.replace("{player}", player.getName());
        player.sendMessage(ColorProcessor.process(processedMessage));
    }
}