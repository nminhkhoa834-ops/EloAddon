package com.github.eloaddon.manager;

import com.github.eloaddon.EloAddon;
import com.github.eloaddon.model.Milestone;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages milestones with an in-memory cache backed by award.yml.
 */
public class MilestoneManager {

    private final EloAddon plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<Integer, Milestone> cache = new ConcurrentHashMap<>();

    public MilestoneManager(EloAddon plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "award.yml");
        reload();
    }

    /**
     * Logic to create or update a milestone.
     */
    public void create(int id, int requireElo, String firstPerm) {
        if (id < 1 || id > 5)
            return;

        List<String> perms = new ArrayList<>();
        perms.add(firstPerm);

        Milestone milestone = new Milestone(
                id,
                "WOODEN_SWORD", // Default
                28 + id, // Default slot (29-33)
                "&aTier " + id,
                Collections.singletonList("&7Reach " + requireElo + " ELO"),
                perms,
                requireElo);

        cache.put(id, milestone);
        saveToConfig(milestone);
        saveFile();
    }

    public void addPermission(int id, String perm) {
        Milestone m = cache.get(id);
        if (m == null)
            return;

        List<String> perms = new ArrayList<>(m.getPermissions());
        if (!perms.contains(perm)) {
            perms.add(perm);
            updateMilestonePermissions(m, perms);
        }
    }

    public void removePermission(int id, String perm) {
        Milestone m = cache.get(id);
        if (m == null)
            return;

        List<String> perms = new ArrayList<>(m.getPermissions());
        if (perms.remove(perm)) {
            updateMilestonePermissions(m, perms);
        }
    }

    private void updateMilestonePermissions(Milestone m, List<String> newPerms) {
        Milestone updated = new Milestone(
                m.getId(),
                m.getMaterial(),
                m.getSlot(),
                m.getDisplayName(),
                m.getLore(),
                newPerms,
                m.getRequireElo());
        cache.put(m.getId(), updated);
        saveToConfig(updated);
        saveFile();
    }

    /**
     * Removes a milestone by ID.
     */
    public boolean remove(int id) {
        if (cache.remove(id) != null) {
            config.set("id" + id, null);
            saveFile();
            return true;
        }
        return false;
    }

    /**
     * Gets a milestone by ID.
     */
    public Milestone get(int id) {
        return cache.get(id);
    }

    /**
     * Gets all milestones.
     */
    public Collection<Milestone> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

    /**
     * Gets all milestone IDs as strings (for tab completion).
     */
    public List<String> getAllIds() {
        List<String> ids = new ArrayList<>();
        for (Integer id : cache.keySet()) {
            ids.add(String.valueOf(id));
        }
        Collections.sort(ids);
        return ids;
    }

    /**
     * Reloads milestones from award.yml.
     */
    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("award.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        cache.clear();

        for (int i = 1; i <= 5; i++) {
            String key = "id" + i;
            if (config.contains(key)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null)
                    continue;

                int id = i;
                String material = section.getString("material", "STONE");
                int slot = section.getInt("slot", 0);
                String displayName = section.getString("display_name", "Tier " + id);
                List<String> lore = section.getStringList("lore");
                List<String> perms = section.getStringList("perm");
                int requireElo = section.getInt("require_elo", 0);

                Milestone m = new Milestone(id, material, slot, displayName, lore, perms, requireElo);
                cache.put(id, m);
            }
        }
    }

    private void saveToConfig(Milestone m) {
        String key = "id" + m.getId();
        config.set(key + ".material", m.getMaterial());
        config.set(key + ".slot", m.getSlot());
        config.set(key + ".display_name", m.getDisplayName());
        config.set(key + ".lore", m.getLore());
        config.set(key + ".perm", m.getPermissions());
        config.set(key + ".require_elo", m.getRequireElo());
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save award.yml: " + e.getMessage());
        }
    }
}
