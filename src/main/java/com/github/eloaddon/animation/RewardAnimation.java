package com.github.eloaddon.animation;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Handles firework and sound animations for reward claims.
 */
public class RewardAnimation {

    public static void play(Player player, int tierId) {
        Location loc = player.getLocation();
        spawnFirework(loc, tierId);
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    private static void spawnFirework(Location loc, int tierId) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder();

        switch (tierId) {
            case 1 -> builder.with(FireworkEffect.Type.BALL).withColor(Color.GREEN);
            case 2 -> builder.with(FireworkEffect.Type.BALL).withColor(Color.AQUA, Color.BLUE);
            case 3 -> builder.with(FireworkEffect.Type.BALL_LARGE).withColor(Color.YELLOW).withFade(Color.ORANGE);
            case 4 -> builder.with(FireworkEffect.Type.STAR).withColor(Color.ORANGE, Color.YELLOW, Color.RED);
            case 5 -> builder.with(FireworkEffect.Type.BURST)
                    .withColor(Color.PURPLE, Color.FUCHSIA)
                    .withFade(Color.WHITE)
                    .flicker(true)
                    .trail(true);
            default -> builder.with(FireworkEffect.Type.BALL).withColor(Color.WHITE);
        }

        meta.addEffect(builder.build());
        meta.setPower(0); // Detonate immediately
        fw.setFireworkMeta(meta);
        fw.detonate();
    }
}
