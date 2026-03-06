package com.github.eloaddon.command;

import com.github.eloaddon.EloAddon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /thuongelo player command to open the reward GUI.
 */
public class PlayerCommand implements CommandExecutor {

    private final EloAddon plugin;

    public PlayerCommand(EloAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        plugin.getRewardGUI().open(player);
        return true;
    }
}
