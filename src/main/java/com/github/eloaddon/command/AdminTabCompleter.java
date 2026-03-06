package com.github.eloaddon.command;

import com.github.eloaddon.manager.MilestoneManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer for the /eloaward admin command.
 */
public class AdminTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "addperm", "delperm", "remove", "reset",
            "resetall", "reload");
    private static final List<String> TIER_IDS = Arrays.asList("1", "2", "3", "4", "5");

    public AdminTabCompleter(MilestoneManager milestoneManager) {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("eloaddon.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filterStartsWith(SUBCOMMANDS, args[0]);
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create", "addperm", "delperm", "remove", "reset", "resetall" -> {
                if (args.length == 2) {
                    return filterStartsWith(TIER_IDS, args[1]);
                }
                if (sub.equals("reset") && args.length == 3) {
                    List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                    return filterStartsWith(playerNames, args[2]);
                }
            }
        }

        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
