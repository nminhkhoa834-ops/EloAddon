package com.github.eloaddon.manager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages configurable messages from config.yml with color code support.
 */
public class MessageManager {

    private FileConfiguration config;

    public MessageManager(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Gets a translated message by key.
     * @param key the message key under "messages" section
     * @return the colorized message, or a default error message if not found
     */
    public String getMessage(String key) {
        String message = config.getString("messages." + key);
        if (message == null) {
            return ChatColor.RED + "Missing message: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Reloads with a fresh config.
     */
    public void reload(FileConfiguration config) {
        this.config = config;
    }
}
