package com.mongenscave.mcmenuapi.menu.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class SoundAction implements Action {

    private final String sound;
    private final float volume;
    private final float pitch;

    public SoundAction(@NotNull String sound) {
        this(sound, 1.0f, 1.0f);
    }

    @Override
    public void execute(@NotNull Player player) {
        try {
            Sound soundEnum = Sound.valueOf(sound.toUpperCase());
            player.playSound(player.getLocation(), soundEnum, volume, pitch);
        } catch (IllegalArgumentException exception) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}