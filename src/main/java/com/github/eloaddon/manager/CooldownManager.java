package com.github.eloaddon.manager;

import com.github.eloaddon.EloAddon;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages claim cooldowns for players.
 */
public class CooldownManager {

    private final Map<UUID, Long> claimCooldown = new ConcurrentHashMap<>();
    private final int cooldownSeconds;

    public CooldownManager(EloAddon plugin) {
        this.cooldownSeconds = plugin.getConfig().getInt("cooldown-seconds", 60);
    }

    public void setCooldown(UUID uuid) {
        claimCooldown.put(uuid, System.currentTimeMillis());
    }

    public boolean isOnCooldown(UUID uuid) {
        if (!claimCooldown.containsKey(uuid))
            return false;
        long lastClaim = claimCooldown.get(uuid);
        return (System.currentTimeMillis() - lastClaim) < (cooldownSeconds * 1000L);
    }

    public long getRemainingSeconds(UUID uuid) {
        if (!claimCooldown.containsKey(uuid))
            return 0;
        long lastClaim = claimCooldown.get(uuid);
        long elapsed = System.currentTimeMillis() - lastClaim;
        long remaining = (cooldownSeconds * 1000L) - elapsed;
        return Math.max(0, remaining / 1000);
    }
}
