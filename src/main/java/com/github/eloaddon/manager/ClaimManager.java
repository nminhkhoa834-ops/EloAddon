package com.github.eloaddon.manager;

import com.github.eloaddon.database.SQLiteManager;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player milestone claims with lazy in-memory caching.
 */
public class ClaimManager {

    private final SQLiteManager db;
    private final Map<UUID, Set<String>> cache = new ConcurrentHashMap<>();

    public ClaimManager(SQLiteManager db) {
        this.db = db;
    }

    /**
     * Checks if a player has already claimed a milestone.
     */
    public boolean hasClaimed(UUID playerUuid, int milestoneId) {
        Set<String> claims = getPlayerClaims(playerUuid);
        return claims.contains(String.valueOf(milestoneId));
    }

    /**
     * Records a claim for a player.
     */
    public void claim(UUID playerUuid, int milestoneId) {
        String idStr = String.valueOf(milestoneId);
        db.addClaim(playerUuid.toString(), idStr);
        getPlayerClaims(playerUuid).add(idStr);
    }

    /**
     * Gets (and lazily loads) the set of claimed milestone IDs for a player.
     */
    private Set<String> getPlayerClaims(UUID playerUuid) {
        return cache.computeIfAbsent(playerUuid, uuid -> {
            Set<String> dbClaims = db.getClaimsForPlayer(uuid.toString());
            // Wrap in a concurrent set for thread safety
            Set<String> concurrentSet = ConcurrentHashMap.newKeySet();
            concurrentSet.addAll(dbClaims);
            return concurrentSet;
        });
    }

    /**
     * Clears the in-memory cache (e.g. on reload).
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Resets a specific claim for a player.
     */
    public void resetClaim(UUID playerUuid, int milestoneId) {
        String idStr = String.valueOf(milestoneId);
        db.resetClaim(playerUuid.toString(), idStr);
        Set<String> claims = cache.get(playerUuid);
        if (claims != null) {
            claims.remove(idStr);
        }
    }

    /**
     * Resets a specific milestone for ALL players.
     */
    public void resetAllClaims(int milestoneId) {
        db.resetAllClaims(String.valueOf(milestoneId));
        cache.clear(); // Clear all cache to be safe as many players were affected
    }

    /**
     * Resets all claims for a player in DB and evicts from cache.
     * 
     * @return the number of claims deleted
     */
    public int resetPlayer(UUID playerUuid) {
        int deleted = db.resetPlayerClaims(playerUuid.toString());
        cache.remove(playerUuid);
        return deleted;
    }

    /**
     * Checks if a player has any claims (DB-level check).
     */
    public boolean hasAnyClaims(UUID playerUuid) {
        return db.hasAnyClaims(playerUuid.toString());
    }
}
