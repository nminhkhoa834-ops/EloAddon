package com.github.eloaddon.gui;

import com.github.eloaddon.EloAddon;
import com.github.eloaddon.animation.RewardAnimation;
import com.github.eloaddon.manager.CooldownManager;
import com.github.eloaddon.manager.MessageManager;
import com.github.eloaddon.model.Milestone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

/**
 * Handles clicks in the reward GUI.
 */
public class GUIListener implements Listener {

    private final EloAddon plugin;

    public GUIListener(EloAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui-title", "&6ELO Reward Tiers"));

        if (!view.getTitle().equals(title))
            return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (event.getCurrentItem() == null)
            return;

        int slot = event.getRawSlot();
        Milestone milestone = null;

        for (int i = 1; i <= 5; i++) {
            Milestone m = plugin.getMilestoneManager().get(i);
            if (m != null && m.getSlot() == slot) {
                milestone = m;
                break;
            }
        }

        if (milestone == null)
            return;

        handleClaim(player, milestone);
    }

    private void handleClaim(Player player, Milestone m) {
        MessageManager msg = plugin.getMessageManager();
        CooldownManager cd = plugin.getCooldownManager();

        // 1) Cooldown check
        if (cd.isOnCooldown(player.getUniqueId())) {
            player.sendMessage(msg.getMessage("cooldown")
                    .replace("%time%", String.valueOf(cd.getRemainingSeconds(player.getUniqueId()))));
            return;
        }

        // 2) Already claimed check
        if (plugin.getClaimManager().hasClaimed(player.getUniqueId(), m.getId())) {
            player.sendMessage(msg.getMessage("already-claimed"));
            return;
        }

        // 3) ELO check
        int playerElo = plugin.getPlayerElo(player);
        if (playerElo < m.getRequireElo()) {
            player.sendMessage(msg.getMessage("not-enough-elo"));
            return;
        }

        // Success!
        // Execute perms
        for (String perm : m.getPermissions()) {
            String lpCommand = "lp user " + player.getName() + " permission set " + perm + " true";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), lpCommand);
        }

        // Save claim (async)
        plugin.getClaimManager().claim(player.getUniqueId(), m.getId());

        // Cooldown
        cd.setCooldown(player.getUniqueId());

        // Close and Animate
        player.closeInventory();
        RewardAnimation.play(player, m.getId());
        player.sendMessage(msg.getMessage("reward-success").replace("%id%", String.valueOf(m.getId())));
    }

}
