package com.github.eloaddon.gui;

import com.github.eloaddon.EloAddon;
import com.github.eloaddon.model.Milestone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the reward tier GUI creation.
 */
public class RewardGUI {

    private final EloAddon plugin;

    public RewardGUI(EloAddon plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui-title", "&6ELO Reward Tiers"));
        Inventory inv = Bukkit.createInventory(null, 45, title);

        // Decorative item in slot 13
        ItemStack deco = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta decoMeta = deco.getItemMeta();
        if (decoMeta != null) {
            decoMeta.setDisplayName(ChatColor.GOLD + "ELO Reward Tiers");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Unlock rewards by reaching ELO");
            decoMeta.setLore(lore);
            deco.setItemMeta(decoMeta);
        }
        inv.setItem(13, deco);

        // Fetch player ELO
        int playerElo = plugin.getPlayerElo(player);

        // Tier items
        for (int i = 1; i <= 5; i++) {
            Milestone m = plugin.getMilestoneManager().get(i);
            if (m == null)
                continue;

            inv.setItem(m.getSlot(), createTierItem(player, m, playerElo));
        }

        player.openInventory(inv);
    }

    private ItemStack createTierItem(Player player, Milestone m, int playerElo) {
        Material mat = Material.matchMaterial(m.getMaterial());
        if (mat == null)
            mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', m.getDisplayName()));
        List<String> lore = new ArrayList<>();
        for (String line : m.getLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        boolean claimed = plugin.getClaimManager().hasClaimed(player.getUniqueId(), m.getId());

        if (claimed) {
            lore.add(ChatColor.RED + "Already Claimed");
        } else if (playerElo >= m.getRequireElo()) {
            // Glow effect
            meta.addEnchant(Enchantment.SHARPNESS, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

}
