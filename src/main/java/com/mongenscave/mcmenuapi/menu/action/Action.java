package com.mongenscave.mcmenuapi.menu.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an action that can be executed when a menu item is clicked
 */
@FunctionalInterface
public interface Action {

    /**
     * Executes this action for the specified player
     *
     * @param player the player executing the action
     */
    void execute(@NotNull Player player);

    /**
     * Creates a command action
     *
     * @param command the command to execute (without /)
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action command(@NotNull String command) {
        return new CommandAction(command);
    }

    /**
     * Creates a player command action (executed by the player)
     *
     * @param command the command to execute (without /)
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action playerCommand(@NotNull String command) {
        return new PlayerCommandAction(command);
    }

    /**
     * Creates a console command action
     *
     * @param command the command to execute (without /)
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action consoleCommand(@NotNull String command) {
        return new ConsoleCommandAction(command);
    }

    /**
     * Creates a sound action
     *
     * @param sound the sound to play
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action sound(@NotNull String sound) {
        return new SoundAction(sound);
    }

    /**
     * Creates a sound action with volume and pitch
     *
     * @param sound the sound to play
     * @param volume the volume
     * @param pitch the pitch
     * @return the action
     */
    @NotNull
    @Contract("_, _, _ -> new")
    static Action sound(@NotNull String sound, float volume, float pitch) {
        return new SoundAction(sound, volume, pitch);
    }

    /**
     * Creates a message action
     *
     * @param message the message to send
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action message(@NotNull String message) {
        return new MessageAction(message);
    }

    /**
     * Creates a close menu action
     *
     * @return the action
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    static Action close() {
        return new CloseAction();
    }

    /**
     * Creates an open menu action
     *
     * @param menuFile the menu file to open (e.g., "menu2.yml")
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action open(@NotNull String menuFile) {
        return new OpenMenuAction(menuFile);
    }

    /**
     * Creates a broadcast action
     *
     * @param message the message to broadcast
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action broadcast(@NotNull String message) {
        return new BroadcastAction(message);
    }

    /**
     * Creates a page change action for paginated menus
     *
     * @param pageChange the page change (e.g., "+1", "-1", "0", "5")
     * @return the action
     */
    @NotNull
    @Contract("_ -> new")
    static Action page(@NotNull String pageChange) {
        return new PageAction(pageChange);
    }
}