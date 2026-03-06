package com.github.eloaddon.command;

import com.github.eloaddon.EloAddon;
import com.github.eloaddon.manager.ClaimManager;
import com.github.eloaddon.manager.MessageManager;
import com.github.eloaddon.manager.MilestoneManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

/**
 * Handles the /eloaward admin command.
 * Subcommands: create, addperm, delperm, remove, reset, resetall, reload
 */
public class AdminCommand implements CommandExecutor {

    private final EloAddon plugin;

    public AdminCommand(EloAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageManager msg = plugin.getMessageManager();
        MilestoneManager milestones = plugin.getMilestoneManager();

        if (!sender.hasPermission("eloaddon.admin")) {
            sender.sendMessage(msg.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> handleCreate(sender, args, msg, milestones);
            case "addperm" -> handleAddPerm(sender, args, msg, milestones);
            case "delperm" -> handleDelPerm(sender, args, msg, milestones);
            case "remove" -> handleRemove(sender, args, msg, milestones);
            case "reset" -> handleReset(sender, args, msg);
            case "resetall" -> handleResetAll(sender, args, msg);
            case "reload" -> handleReload(sender, msg);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args, MessageManager msg, MilestoneManager milestones) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /eloaward create <id 1-5> <require_elo> <perm>");
            return;
        }

        int id;
        int eloRequired;

        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid milestone ID. Must be a number (1-5).");
            return;
        }

        if (id < 1 || id > 5) {
            sender.sendMessage("§cInvalid milestone ID. Must be between 1 and 5.");
            return;
        }

        try {
            eloRequired = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid ELO value. Must be a number.");
            return;
        }

        String permission = args[3];
        milestones.create(id, eloRequired, permission);
        sender.sendMessage(msg.getMessage("award-created").replace("%id%", String.valueOf(id)));
    }

    private void handleAddPerm(CommandSender sender, String[] args, MessageManager msg, MilestoneManager milestones) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eloaward addperm <id> <perm>");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid milestone ID.");
            return;
        }

        if (milestones.get(id) == null) {
            sender.sendMessage(msg.getMessage("tier-not-found"));
            return;
        }

        milestones.addPermission(id, args[2]);
        sender.sendMessage(msg.getMessage("perm-added").replace("%id%", String.valueOf(id)));
    }

    private void handleDelPerm(CommandSender sender, String[] args, MessageManager msg, MilestoneManager milestones) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eloaward delperm <id> <perm>");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid milestone ID.");
            return;
        }

        if (milestones.get(id) == null) {
            sender.sendMessage(msg.getMessage("tier-not-found"));
            return;
        }

        milestones.removePermission(id, args[2]);
        sender.sendMessage(msg.getMessage("perm-removed").replace("%id%", String.valueOf(id)));
    }

    private void handleRemove(CommandSender sender, String[] args, MessageManager msg, MilestoneManager milestones) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /eloaward remove <id>");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid milestone ID.");
            return;
        }

        if (milestones.remove(id)) {
            sender.sendMessage(msg.getMessage("award-removed").replace("%id%", String.valueOf(id)));
        } else {
            sender.sendMessage(msg.getMessage("tier-not-found"));
        }
    }

    private void handleReload(CommandSender sender, MessageManager msg) {
        try {
            plugin.reloadConfig();
            msg.reload(plugin.getConfig());
            plugin.getMilestoneManager().reload();
            plugin.getClaimManager().clearCache();
            sender.sendMessage(msg.getMessage("reload-success"));
        } catch (Exception e) {
            sender.sendMessage(msg.getMessage("reload-unsuccess"));
            plugin.getLogger().severe("Failed to reload config: " + e.getMessage());
        }
    }

    private void handleReset(CommandSender sender, String[] args, MessageManager msg) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eloaward reset <id> <player>");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid tier ID.");
            return;
        }

        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        UUID targetUuid = target.getUniqueId();
        ClaimManager claims = plugin.getClaimManager();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            claims.resetClaim(targetUuid, id);
            Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(msg.getMessage("reset-success")
                    .replace("%player%", playerName)));
        });
    }

    private void handleResetAll(CommandSender sender, String[] args, MessageManager msg) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /eloaward resetall <id>");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid tier ID.");
            return;
        }

        ClaimManager claims = plugin.getClaimManager();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            claims.resetAllClaims(id);
            Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(msg.getMessage("resetall-success")
                    .replace("%id%", String.valueOf(id))));
        });
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6Usage:");
        sender.sendMessage("§e /eloaward create <id 1-5> <require_elo> <perm>");
        sender.sendMessage("§e /eloaward addperm <id> <perm>");
        sender.sendMessage("§e /eloaward delperm <id> <perm>");
        sender.sendMessage("§e /eloaward remove <id>");
        sender.sendMessage("§e /eloaward reset <id> <player>");
        sender.sendMessage("§e /eloaward resetall <id>");
        sender.sendMessage("§e /eloaward reload");
    }
}
