package cz.deefeen.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class HardcorePlugin extends JavaPlugin implements Listener {

    private StatsManager statsManager;
    private DoomsdayCountdown countdown;
    private HcScoreboard hcScoreboard;
    private DiscordWebhook webhook;
    private UptimeManager uptimeManager;
    private LocaleManager locale;

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public LocaleManager getLocale() {
        return locale;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        checkConfigVersion();

        locale = new LocaleManager(getConfig().getString("locale", "cz"));

        statsManager = new StatsManager(getDataFolder());

        if (getConfig().getBoolean("discord-webhook.enabled", false)) {
            webhook = new DiscordWebhook(
                    getConfig().getString("discord-webhook.url", ""),
                    getConfig().getString("discord-webhook.bot-name", "HardcoreWipe"),
                    getConfig().getString("discord-webhook.avatar-url", "")
            );
        }

        String title = locale.get("scoreboard.title");
        String line = locale.get("scoreboard.line");
        hcScoreboard = new HcScoreboard(title, line, statsManager.getWipeCount());
        hcScoreboard.applyToAll();

        if (getConfig().getBoolean("motd.enabled", false)) {
            String motd = getConfig().getString("motd.text", "§6§lHARDCORE WIPE");
            Bukkit.getServer().setMotd(motd);
            getLogger().info("MOTD nastaveno: " + motd.replace("§", "&"));
        }

        if (getConfig().getBoolean("server-properties.enabled", false)) {
            applyServerProperties();
        }

        getServer().getPluginManager().registerEvents(this, this);

        if (getConfig().getBoolean("uptime.enabled", true)) {
            uptimeManager = new UptimeManager(this, statsManager.getUptimeSeconds());
            uptimeManager.start();
        }

        getLogger().info("HardcoreWipe plugin enabled.");
    }

    @Override
    public void onDisable() {
        if (uptimeManager != null) {
            statsManager.setUptimeSeconds(uptimeManager.getUptimeSeconds());
            statsManager.save();
            uptimeManager.stop();
            uptimeManager = null;
        }

        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }

        File marker = new File(getDataFolder(), "wipe.flag");
        if (marker.exists()) {
            getLogger().info("Wipe marker found – registering shutdown hook for world deletion...");
            try {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    List<String> worldNames = getConfig().getStringList("worlds.list");
                    if (worldNames.isEmpty()) {
                        worldNames = List.of("world");
                    }
                    for (String name : worldNames) {
                        File folder = new File(name);
                        if (folder.exists()) {
                            deleteDirectory(folder);
                        }
                    }
                    for (String name : getConfig().getStringList("worlds.remove-legacy-folders")) {
                        File folder = new File(name);
                        if (folder.exists()) {
                            deleteDirectory(folder);
                        }
                    }
                    marker.delete();
                }));
            } catch (IllegalStateException e) {
                getLogger().severe("Could not register shutdown hook: " + e.getMessage());
            }
        }

        getLogger().info("HardcoreWipe plugin disabled.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (countdown != null && countdown.isRunning()) return;

        Player player = event.getEntity();
        String name = player.getName();
        String deathMsg = event.getDeathMessage();
        if (deathMsg == null) {
            deathMsg = "unknown";
        }

        statsManager.addRun(name, deathMsg);

        int seconds = getConfig().getInt("countdown-seconds", 30);
        String msg = locale.format("death-broadcast", "player", name, "seconds", String.valueOf(seconds));
        Bukkit.broadcastMessage(msg);

        countdown = new DoomsdayCountdown(
                this, locale, name,
                seconds,
                locale.get("bossbar.title"),
                BarColor.valueOf(getConfig().getString("bossbar.color", "RED")),
                BarStyle.valueOf(getConfig().getString("bossbar.style", "SOLID")),
                getConfig().getString("sounds.tick", "block.note_block.click"),
                (float) getConfig().getDouble("sounds.tick-pitch", 1.0),
                getConfig().getString("sounds.alarm", "entity.experience_orb.pickup"),
                (float) getConfig().getDouble("sounds.alarm-pitch", 1.5),
                this::onWipeExecuted
        );
        countdown.start();

        if (webhook != null) {
            int wc = statsManager.getWipeCount() + 1;
            if (getConfig().getBoolean("discord-webhook.embed.enabled", true)) {
                webhook.sendEmbed(
                        resolve(locale.get("discord.trigger-title"), name, seconds, wc, deathMsg),
                        resolve(locale.get("discord.trigger-description"), name, seconds, wc, deathMsg),
                        getConfig().getInt("discord-webhook.embed.color", 16711680),
                        locale.get("discord.field-player"), name,
                        locale.get("discord.field-wipe"), String.valueOf(wc),
                        locale.get("discord.field-remaining"), seconds + "s"
                );
            } else {
                webhook.send(resolve(locale.get("discord.trigger-description"), name, seconds, wc, deathMsg));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (hcScoreboard != null) {
            hcScoreboard.applyToPlayer(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!getConfig().getBoolean("damage-notifications.enabled", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.isDead()) return;

        double damage = event.getFinalDamage();
        if (damage <= 0) return;

        double hearts = damage / 2.0;
        String damageStr = String.format("%.1f", hearts);

        String msg;
        if (event instanceof EntityDamageByEntityEvent entityEvent) {
            String entity = getEntityName(entityEvent.getDamager());
            msg = locale.format("damage.entity", "damage", damageStr, "entity", entity);
        } else {
            String cause = friendlyName(event.getCause().name());
            msg = locale.format("damage.environment", "damage", damageStr, "cause", cause);
        }
        Bukkit.broadcastMessage(msg);
    }

    private String getEntityName(Entity damager) {
        if (damager instanceof Player attacker) {
            return attacker.getName();
        }
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                return shooter.getName();
            }
            if (projectile.getShooter() instanceof LivingEntity shooter) {
                return friendlyName(shooter.getType().name());
            }
        }
        return friendlyName(damager.getType().name());
    }

    private String friendlyName(String enumName) {
        String[] parts = enumName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0)));
            sb.append(part.substring(1));
        }
        return sb.toString();
    }

    private void onWipeExecuted() {
        statsManager.incrementWipeCount();
        int wc = statsManager.getWipeCount();
        hcScoreboard.increment();
        hcScoreboard.applyToAll();

        if (uptimeManager != null) {
            uptimeManager.reset();
        }

        try {
            new File(getDataFolder(), "wipe.flag").createNewFile();
            getLogger().info("Wipe marker created.");
        } catch (IOException e) {
            getLogger().severe("Failed to create wipe marker: " + e.getMessage());
        }

        if (getConfig().getBoolean("worlds.create-wipe-flag", true)) {
            createWipeFlag();
        }

        Bukkit.broadcastMessage(locale.get("worlds-deleted"));

        if (webhook != null && countdown != null) {
            String player = countdown.getDeadPlayerName();
            if (getConfig().getBoolean("discord-webhook.embed.enabled", true)) {
                webhook.sendEmbed(
                        resolve(locale.get("discord.complete-title"), player, 0, wc, null),
                        resolve(locale.get("discord.complete-description"), player, 0, wc, null),
                        getConfig().getInt("discord-webhook.embed.color", 16711680),
                        locale.get("discord.field-player"), player,
                        locale.get("discord.field-wipe"), String.valueOf(wc)
                );
            } else {
                webhook.send(resolve(locale.get("discord.complete-description"), player, 0, wc, null));
            }
        }

        Bukkit.getScheduler().runTaskLater(this, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop"), 10L
        );
    }

    private void createWipeFlag() {
        File wipeFile = new File("wipe.txt");
        try {
            if (wipeFile.createNewFile()) {
                getLogger().info("Soubor wipe.txt byl úspěšně vytvořen.");
            } else {
                getLogger().warning("Soubor wipe.txt již existuje.");
            }
        } catch (IOException e) {
            getLogger().severe("Nepodařilo se vytvořit wipe.txt: " + e.getMessage());
        }
    }

    private String resolve(String template, String player, int seconds, int wipeCount, String deathMessage) {
        if (template == null) return "";
        return template
                .replace("{player}", player != null ? player : "")
                .replace("{seconds}", String.valueOf(seconds))
                .replace("{wipe_count}", String.valueOf(wipeCount))
                .replace("{death_message}", deathMessage != null ? deathMessage : "");
    }

    private void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }

    private void checkConfigVersion() {
        int builtinVer = getConfig().getInt("config-version", 2);
        int savedVer = getConfig().getInt("config-version", 1);
        if (savedVer < builtinVer) {
            getLogger().info("Config verze " + savedVer + " -> " + builtinVer + ". Slučuji nové klíče...");
            java.io.InputStream defStream = getResource("config.yml");
            if (defStream != null) {
                org.bukkit.configuration.file.YamlConfiguration defConfig =
                        org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                                new java.io.InputStreamReader(defStream));
                for (String key : defConfig.getKeys(true)) {
                    if (!getConfig().contains(key)) {
                        getConfig().set(key, defConfig.get(key));
                    }
                }
                getConfig().set("config-version", builtinVer);
                try {
                    getConfig().save(new File(getDataFolder(), "config.yml"));
                } catch (IOException e) {
                    getLogger().severe("Nepodařilo se uložit sloučený config: " + e.getMessage());
                }
                reloadConfig();
                getLogger().info("Config byl aktualizován na verzi " + builtinVer + " – tvoje nastavení zůstala zachována.");
            }
        }
    }

    private void applyServerProperties() {
        File props = new File("server.properties");
        if (!props.exists()) {
            getLogger().warning("server.properties nenalezen.");
            return;
        }
        try {
            List<String> lines = Files.readAllLines(props.toPath());
            List<String> out = new ArrayList<>();
            boolean hardcoreSet = false;
            boolean difficultySet = false;

            boolean hc = getConfig().getBoolean("server-properties.hardcore", true);
            String diff = getConfig().getString("server-properties.difficulty", "hard");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("hardcore=")) {
                    out.add("hardcore=" + hc);
                    hardcoreSet = true;
                } else if (trimmed.startsWith("difficulty=")) {
                    out.add("difficulty=" + diff);
                    difficultySet = true;
                } else {
                    out.add(line);
                }
            }
            if (!hardcoreSet) out.add("hardcore=" + hc);
            if (!difficultySet) out.add("difficulty=" + diff);

            Files.write(props.toPath(), out);
            getLogger().info("server.properties upraven: hardcore=" + hc + ", difficulty=" + diff);
        } catch (IOException e) {
            getLogger().severe("Chyba při úpravě server.properties: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("soulinsure")) {
            return handleSoulInsure(sender);
        }
        if (command.getName().equalsIgnoreCase("deadlist")) {
            return handleDeadlist(sender);
        }
        if (command.getName().equalsIgnoreCase("hc")) {
            return handleHc(sender, args);
        }
        return false;
    }

    private boolean handleHc(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("stat") || !args[1].equalsIgnoreCase("reset")) {
            sender.sendMessage(locale.get("hc.usage"));
            return true;
        }
        statsManager.resetWipeCount();
        hcScoreboard.reset();
        hcScoreboard.applyToAll();
        sender.sendMessage(locale.get("hc.reset-success"));
        return true;
    }

    private boolean handleSoulInsure(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(locale.get("player-only"));
            return true;
        }

        if (countdown == null || !countdown.isRunning()) {
            sender.sendMessage(locale.get("insurance.no-countdown"));
            return true;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() != Material.TOTEM_OF_UNDYING) {
            sender.sendMessage(locale.get("insurance.no-totem"));
            return true;
        }

        mainHand.setAmount(mainHand.getAmount() - 1);
        player.getInventory().setItemInMainHand(mainHand);

        String deadName = countdown.getDeadPlayerName();
        countdown.cancel();
        countdown = null;

        Player deadPlayer = Bukkit.getPlayerExact(deadName);
        if (deadPlayer != null && deadPlayer.isOnline()) {
            deadPlayer.getInventory().clear();
            deadPlayer.getEnderChest().clear();
            deadPlayer.setHealth(20.0);
            deadPlayer.setFoodLevel(20);
            deadPlayer.setSaturation(10.0f);
            deadPlayer.setGameMode(GameMode.SURVIVAL);
            World world = Bukkit.getWorlds().getFirst();
            Location spawn = world.getSpawnLocation();
            deadPlayer.teleport(spawn);
        }

        String successMsg = locale.format("insurance.success", "player", player.getName());
        Bukkit.broadcastMessage(successMsg);

        if (webhook != null) {
            int wc = statsManager.getWipeCount();
            if (getConfig().getBoolean("discord-webhook.embed.enabled", true)) {
                webhook.sendEmbed(
                        resolve(locale.get("discord.insured-title"), player.getName(), 0, wc, null),
                        resolve(locale.get("discord.insured-description"), player.getName(), 0, wc, null),
                        65280,
                        locale.get("discord.field-savior"), player.getName(),
                        locale.get("discord.field-dead"), deadName
                );
            } else {
                webhook.send(resolve(locale.get("discord.insured-description"), player.getName(), 0, wc, null));
            }
        }

        return true;
    }

    private boolean handleDeadlist(CommandSender sender) {
        var runs = statsManager.getRuns();
        if (runs.isEmpty()) {
            sender.sendMessage(locale.get("deadlist.empty"));
            return true;
        }

        sender.sendMessage(locale.get("deadlist.header"));
        String format = locale.get("deadlist.format");
        for (RunEntry run : runs) {
            sender.sendMessage(format
                    .replace("{runId}", String.valueOf(run.runId()))
                    .replace("{player}", run.playerName())
                    .replace("{deathMessage}", run.deathMessage())
                    .replace("{timestamp}", run.timestamp())
            );
        }
        return true;
    }
}
