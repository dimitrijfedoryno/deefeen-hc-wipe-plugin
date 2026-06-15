package cz.deefeen.hardcore.locales;

import java.util.HashMap;
import java.util.Map;

public class LocaleCZ {

    public Map<String, String> getMessages() {
        Map<String, String> m = new HashMap<>();
        m.put("death-broadcast", "§c[DeeFeen HARDCORE] Hráč {player} zemřel! Celý svět bude smazán a server se restartuje za {seconds} sekund!");
        m.put("countdown.ten-seconds", "§e[DeeFeen HARDCORE] Zbývá posledních 10 sekund světa!");
        m.put("countdown.format", "§e[DeeFeen HARDCORE] {seconds}...");
        m.put("worlds-deleted", "§c[DeeFeen HARDCORE] Světy budou smazány při příštím startu. Server se vypíná.");
        m.put("insurance.success", "§a§l[POJISTKA] Svět byl vykoupen! {player} obětoval Totem oživení!");
        m.put("insurance.no-countdown", "§cŽádný wipe odpočet právě neprobíhá.");
        m.put("insurance.no-totem", "§cMusíš držet Totem oživení v hlavní ruce!");
        m.put("player-only", "§cTento příkaz může použít pouze hráč.");
        m.put("deadlist.header", "§6§l=== SÍŇ HANBY - Historie wipe runů ===");
        m.put("deadlist.empty", "§eŽádné předchozí runy nebyly nalezeny.");
        m.put("deadlist.format", "§7#{runId} §c{player} §7- {deathMessage} §8({timestamp})");
        m.put("hc.usage", "§cPoužití: /hc stat reset");
        m.put("hc.reset-success", "§aStatistiky smrtí byly resetovány.");
        m.put("bossbar.title", "§c§lDOOMSDAY: Smazání světa za {seconds} sekund!");
        m.put("scoreboard.title", "§6§lHARDCORE");
        m.put("scoreboard.line", "§cSmrtí");
        m.put("damage.environment", "§c[DeeFeen HC] dostal damage ♥ -{damage} z {cause} damage");
        m.put("damage.entity", "§c[DeeFeen HC] dostal damage ♥ -{damage} od {entity}");
        m.put("discord.trigger-title", "☠ WIPE TRIGGERED");
        m.put("discord.trigger-description", "**{player}** zemřel a spustil wipe č. **{wipe_count}**");
        m.put("discord.complete-title", "✅ WIPE COMPLETE");
        m.put("discord.complete-description", "Svět smazán po smrti **{player}** (wipe č. **{wipe_count}**). Server se restartuje.");
        m.put("discord.insured-title", "💚 SOUL INSURANCE");
        m.put("discord.insured-description", "**{player}** obětoval Totem oživení a zachránil wipe č. **{wipe_count}**!");
        m.put("discord.field-player", "Hráč");
        m.put("discord.field-wipe", "Wipe č.");
        m.put("discord.field-remaining", "Zbývá");
        m.put("discord.field-savior", "Zachránce");
        m.put("discord.field-dead", "Mrtvý hráč");
        return m;
    }
}
