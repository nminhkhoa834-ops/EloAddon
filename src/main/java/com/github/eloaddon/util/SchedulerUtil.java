package com.github.eloaddon.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Utility class to handle scheduling tasks seamlessly across Spigot, Paper, and
 * Folia.
 */
public class SchedulerUtil {

    private static final boolean IS_FOLIA;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
        }
        IS_FOLIA = folia;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Runs an asynchronous task.
     */
    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Runs a delayed asynchronous task.
     */
    public static void runAsyncLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            // Folia AsyncScheduler uses milliseconds. 1 tick = 50ms
            Bukkit.getServer().getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks * 50,
                    java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    /**
     * Runs a global task (useful for commands or logic not tied to a specific
     * location/entity).
     */
    public static void runGlobalTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getServer().getGlobalRegionScheduler().execute(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a task tied to a specific location (useful for block manipulation).
     */
    public static void runLocationTask(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getServer().getRegionScheduler().execute(plugin, location, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a task tied to an entity (useful for players, mobs, GUI).
     */
    public static void runEntityTask(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            entity.getScheduler().execute(plugin, task, null, 1L);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a delayed task tied to an entity.
     */
    public static void runEntityTaskLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            entity.getScheduler().execute(plugin, task, null, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }
}
