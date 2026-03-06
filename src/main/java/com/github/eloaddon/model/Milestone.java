package com.github.eloaddon.model;

import java.util.List;

/**
 * Represents an ELO milestone with GUI appearance and reward details.
 */
public class Milestone {

    private final int id;
    private final String material;
    private final int slot;
    private final String displayName;
    private final List<String> lore;
    private final List<String> permissions;
    private final int requireElo;

    public Milestone(int id, String material, int slot, String displayName, List<String> lore, List<String> permissions,
            int requireElo) {
        this.id = id;
        this.material = material;
        this.slot = slot;
        this.displayName = displayName;
        this.lore = lore;
        this.permissions = permissions;
        this.requireElo = requireElo;
    }

    public int getId() {
        return id;
    }

    public String getMaterial() {
        return material;
    }

    public int getSlot() {
        return slot;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public int getRequireElo() {
        return requireElo;
    }
}
