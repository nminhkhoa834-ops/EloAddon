package com.github.eloaddon;

import com.github.eloaddon.command.AdminCommand;
import com.github.eloaddon.command.AdminTabCompleter;
import com.github.eloaddon.command.PlayerCommand;
import com.github.eloaddon.database.SQLiteManager;
import com.github.eloaddon.gui.GUIListener;
import com.github.eloaddon.gui.RewardGUI;
import com.github.eloaddon.manager.ClaimManager;
import com.github.eloaddon.manager.CooldownManager;
import com.github.eloaddon.manager.MessageManager;
import com.github.eloaddon.manager.MilestoneManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for EloAddon.
 */
public class EloAddon extends JavaPlugin {

    private SQLiteManager sqliteManager;
    private MilestoneManager milestoneManager;
    private ClaimManager claimManager;
    private MessageManager messageManager;
    private CooldownManager cooldownManager;
    private RewardGUI rewardGUI;

    @Override
    public void onEnable() {
        // Dependencies check...
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("EloAddon requires PlaceholderAPI to read ELO values.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        SQLiteManager.backupDatabase(getDataFolder(), getLogger());

        sqliteManager = new SQLiteManager(getDataFolder(), getLogger());
        if (!sqliteManager.isConnected()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        milestoneManager = new MilestoneManager(this);
        claimManager = new ClaimManager(sqliteManager);
        messageManager = new MessageManager(getConfig());
        cooldownManager = new CooldownManager(this);
        rewardGUI = new RewardGUI(this);

        // Register commands
        PluginCommand eloawardCmd = getCommand("eloaward");
        if (eloawardCmd != null) {
            eloawardCmd.setExecutor(new AdminCommand(this));
            eloawardCmd.setTabCompleter(new AdminTabCompleter(milestoneManager));
        }

        PluginCommand thuongeloCmd = getCommand("thuongelo");
        if (thuongeloCmd != null) {
            thuongeloCmd.setExecutor(new PlayerCommand(this));
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        getLogger().info("EloAddon enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (sqliteManager != null)
            sqliteManager.close();
    }

    public MilestoneManager getMilestoneManager() {
        return milestoneManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public RewardGUI getRewardGUI() {
        return rewardGUI;
    }

    /**
     * Parses the configured PlaceholderAPI ELO placeholder for a player.
     * Returns 0 if invalid or parsing fails.
     */
    public int getPlayerElo(org.bukkit.entity.Player player) {
        try {
            String placeholderFormat = getConfig().getString("papi-elo", "%hnybpvpelo_elo%");
            String raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholderFormat);
            if (raw == null || raw.isEmpty() || raw.equals(placeholderFormat)) {
                return 0;
            }
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
