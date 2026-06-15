package cz.deefeen.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UptimeManager {

    private final HardcorePlugin plugin;
    private long uptimeSeconds;
    private int taskId = -1;

    public UptimeManager(HardcorePlugin plugin, long initialSeconds) {
        this.plugin = plugin;
        this.uptimeSeconds = initialSeconds;
    }

    public void start() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L).getTaskId();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void reset() {
        uptimeSeconds = 0;
        plugin.getStatsManager().setUptimeSeconds(0);
        plugin.getStatsManager().save();
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    private void tick() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        uptimeSeconds++;

        String formatted = formatTime(uptimeSeconds);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(formatted);
        }
    }

    public static String formatTime(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long secs = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        sb.append(hours).append("h ");
        sb.append(minutes).append("m ");
        sb.append(secs).append("s");
        return sb.toString();
    }
}
