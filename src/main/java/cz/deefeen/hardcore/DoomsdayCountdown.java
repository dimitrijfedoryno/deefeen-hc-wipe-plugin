package cz.deefeen.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class DoomsdayCountdown {

    private final HardcorePlugin plugin;
    private final String deadPlayerName;
    private final int totalSeconds;
    private final String bossBarTitle;
    private final BarColor bossBarColor;
    private final BarStyle bossBarStyle;
    private final String tickSound;
    private final float tickPitch;
    private final String alarmSound;
    private final float alarmPitch;
    private final Runnable onWipe;
    private BossBar bossBar;
    private int taskId = -1;
    private int secondsLeft;

    public DoomsdayCountdown(HardcorePlugin plugin, String deadPlayerName, int totalSeconds,
                             String bossBarTitle, BarColor bossBarColor, BarStyle bossBarStyle,
                             String tickSound, float tickPitch,
                             String alarmSound, float alarmPitch,
                             Runnable onWipe) {
        this.plugin = plugin;
        this.deadPlayerName = deadPlayerName;
        this.totalSeconds = totalSeconds;
        this.bossBarTitle = bossBarTitle;
        this.bossBarColor = bossBarColor;
        this.bossBarStyle = bossBarStyle;
        this.tickSound = tickSound;
        this.tickPitch = tickPitch;
        this.alarmSound = alarmSound;
        this.alarmPitch = alarmPitch;
        this.onWipe = onWipe;
        this.secondsLeft = totalSeconds;
    }

    public void start() {
        String initialTitle = bossBarTitle.replace("{seconds}", String.valueOf(totalSeconds));
        bossBar = Bukkit.createBossBar(initialTitle, bossBarColor, bossBarStyle);
        bossBar.setProgress(1.0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), tickSound, 1.0f, tickPitch);
        }

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L).getTaskId();
    }

    public void cancel() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    public boolean isRunning() {
        return taskId != -1;
    }

    public String getDeadPlayerName() {
        return deadPlayerName;
    }

    private void tick() {
        secondsLeft--;

        if (bossBar != null) {
            double progress = (double) Math.max(secondsLeft, 0) / totalSeconds;
            bossBar.setProgress(progress);
            String title = bossBarTitle.replace("{seconds}", String.valueOf(secondsLeft));
            bossBar.setTitle(title);
        }

        String sound;
        float pitch;

        if (secondsLeft <= 5 && secondsLeft > 0) {
            sound = alarmSound;
            pitch = alarmPitch;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle("§c§l" + secondsLeft, "", 0, 25, 5);
            }
        } else if (secondsLeft > 0) {
            sound = tickSound;
            pitch = tickPitch;
        } else {
            sound = null;
            pitch = 1.0f;
        }

        if (sound != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), sound, 1.0f, pitch);
            }
        }

        if (secondsLeft == 10) {
            Bukkit.broadcastMessage(plugin.getConfig().getString("messages.ten-seconds", "§e[HARDCORE] Zbývá posledních 10 sekund světa!"));
        } else if (secondsLeft <= 5 && secondsLeft > 0) {
            String msg = plugin.getConfig().getString("messages.countdown-format", "§e[HARDCORE] {seconds}...");
            Bukkit.broadcastMessage(msg.replace("{seconds}", String.valueOf(secondsLeft)));
        }

        if (secondsLeft <= 0) {
            cancel();
            if (onWipe != null) {
                onWipe.run();
            }
        }
    }
}
