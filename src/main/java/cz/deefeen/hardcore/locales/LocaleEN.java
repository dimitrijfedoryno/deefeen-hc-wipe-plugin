package cz.deefeen.hardcore.locales;

import java.util.HashMap;
import java.util.Map;

public class LocaleEN {

    public Map<String, String> getMessages() {
        Map<String, String> m = new HashMap<>();
        m.put("death-broadcast", "§c[DeeFeen HARDCORE] Player {player} died! World will be wiped and server restarts in {seconds} seconds!");
        m.put("countdown.ten-seconds", "§e[DeeFeen HARDCORE] Last 10 seconds remaining!");
        m.put("countdown.format", "§e[DeeFeen HARDCORE] {seconds}...");
        m.put("worlds-deleted", "§c[DeeFeen HARDCORE] Worlds will be deleted on next startup. Server shutting down.");
        m.put("insurance.success", "§a§l[INSURANCE] World has been redeemed! {player} sacrificed a Totem of Undying!");
        m.put("insurance.no-countdown", "§cNo wipe countdown is currently active.");
        m.put("insurance.no-totem", "§cYou must hold a Totem of Undying in your main hand!");
        m.put("player-only", "§cThis command can only be used by a player.");
        m.put("deadlist.header", "§6§l=== HALL OF SHAME - Wipe Run History ===");
        m.put("deadlist.empty", "§eNo previous runs found.");
        m.put("deadlist.format", "§7#{runId} §c{player} §7- {deathMessage} §8({timestamp})");
        m.put("hc.usage", "§cUsage: /hc stat reset");
        m.put("hc.reset-success", "§aWipe count has been reset.");
        m.put("bossbar.title", "§c§lDOOMSDAY: World wipe in {seconds} seconds!");
        m.put("scoreboard.title", "§6§lHARDCORE");
        m.put("scoreboard.line", "§cDeaths");
        m.put("damage.environment", "§c[DeeFeen HC] took damage ♥ -{damage} from {cause}");
        m.put("damage.entity", "§c[DeeFeen HC] took damage ♥ -{damage} from {entity}");
        m.put("discord.trigger-title", "☠ WIPE TRIGGERED");
        m.put("discord.trigger-description", "**{player}** died and triggered wipe #**{wipe_count}**");
        m.put("discord.complete-title", "✅ WIPE COMPLETE");
        m.put("discord.complete-description", "World was wiped after **{player}**'s death (wipe #**{wipe_count}**). Server restarting.");
        m.put("discord.insured-title", "💚 SOUL INSURANCE");
        m.put("discord.insured-description", "**{player}** sacrificed a Totem of Undying and saved wipe #**{wipe_count}**!");
        m.put("discord.field-player", "Player");
        m.put("discord.field-wipe", "Wipe #");
        m.put("discord.field-remaining", "Remaining");
        m.put("discord.field-savior", "Savior");
        m.put("discord.field-dead", "Dead player");
        return m;
    }
}
