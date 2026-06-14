# HardcoreWipe

**HardcoreWipe** is a Paper plugin (1.21.x) that turns your Minecraft server into a hardcore roguelike experience. When a player dies, a configurable doomsday countdown begins — after it expires, all worlds are wiped clean and the server restarts. Stats persist across runs. Players can cancel the wipe by sacrificing a Totem of Undying via `/soulinsure`.

---

## Features

- **Player death → doomsday countdown** — BossBar with real-time progress, per-second tick sounds, alarm in last 5 seconds, title broadcasts.
- **Automatic world wipe** — After the countdown expires, all world folders are deleted. The server stops and starts fresh on next launch.
- **Persistent stats** — Runs are logged in `plugins/HardcoreWipe/stats.json` (never deleted). Includes death message, player name, and timestamp.
- **`/deadlist`** — Shows a history of all wipe runs.
- **Scoreboard** — Sidebar displays total wipe count. Follows players across reconnects.
- **Soul insurance (`/soulinsure`)** — Cancel an active countdown by consuming a Totem of Undying from your inventory. The dead player is revived at spawn in survival mode with a cleared inventory.
- **Discord webhook notifications** — Sends embed messages for wipe triggered, wipe complete, and soul insurance used. Fully configurable.
- **MOTD & server.properties** — Automatically sets MOTD and hardcore mode + difficulty on startup (configurable).
- **Config versioning** — Auto-merges new config keys when updating the plugin — your custom values are never lost.
- **No external dependencies** — Only Paper API. Gson is provided by the server. Discord webhook uses bare `HttpURLConnection`.

---

## Requirements

| Dependency | Version |
|---|---|
| [Paper](https://papermc.io/) | 1.21.x (API 26.1.2.build.69-stable) |
| Java | 25+ |
| Maven | 3.9.x (for building) |

---

## Installation

1. **Build the plugin:**
   ```bash
   mvn clean package
   ```
   The JAR will be in `target/HardcoreWipe-2.1.0.jar`.

2. **Place the JAR** in your server's `plugins/` folder.

3. **Start the server.** The plugin creates `plugins/HardcoreWipe/config.yml` with all default values.

4. **Edit `config.yml`** to your liking (see [Configuration](#configuration)).

5. **Restart the server** for changes to take effect.

---

## Configuration

The config is located at `plugins/HardcoreWipe/config.yml` and contains **Czech comments** explaining every option. Key sections:

| Section | Description |
|---|---|
| `countdown-seconds` | Seconds before the world wipe triggers (default: 30) |
| `bossbar` | BossBar color (`RED`, `BLUE`, etc.), style (`SOLID`, `SEGMENTED_10`), title template |
| `sounds` | Tick sound (plays every second), alarm sound (last 5 seconds) |
| `scoreboard` | Scoreboard title and display name |
| `worlds` | List of world folder names to delete on wipe |
| `motd` | Whether to override MOTD on startup and what text to use |
| `server-properties` | Whether to enforce `hardcore=true` and `difficulty=hard` on startup |
| `discord-webhook` | Webhook URL, enabled flag, and customizable embed messages for trigger/complete/soul-insure events |
| `messages` | All chat messages (prefix, countdown broadcasts, soul insurance messages, etc.) |

### Config versioning

When you update the plugin JAR, the config version is compared. If the version in the built-in config is higher than your saved one, **only the missing keys are added** — your existing custom values remain untouched.

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/hc stat reset` | `hardcorewipe.admin` | Reset the wipe counter to 0 |
| `/soulinsure` | (everyone) | Cancel active countdown by consuming a Totem of Undying |
| `/deadlist` | (everyone) | Show history of all past wipe runs |

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `hardcorewipe.admin` | `op` | Access to `/hc stat reset` |

---

## How it works

### Death → Countdown → Wipe

1. A player dies → `PlayerDeathEvent` is caught.
2. A `DoomsdayCountdown` task starts (synchronous `runTaskTimer`, 1-second intervals).
3. BossBar shows remaining time with a red progress bar.
4. Every second a tick sound plays. In the last 5 seconds, an alarm sound + title appear.
5. At 0: the countdown calls a wipe callback in `HardcorePlugin`.

### Wipe procedure

1. Statistics are updated and saved.
2. A `wipe.flag` marker file is created in the plugin folder.
3. The server is stopped via `Bukkit.shutdown()`.
4. A **JVM shutdown hook** (registered in `onDisable()`) runs after all server save operations complete:
   - It detects `wipe.flag`.
   - Deletes all world folders listed in `config.yml`.
   - Deletes the flag file.
5. On next server start, world folders are regenerated fresh by Paper.

### Soul insurance (`/soulinsure`)

- Only works while a countdown is active.
- Checks the player's inventory for a Totem of Undying (any slot, including off-hand).
- Consumes one totem, cancels the countdown, sets the player to survival mode, teleports to world spawn, clears inventory, and sends a broadcast.

### Stats persistence

- Stored in `plugins/HardcoreWipe/stats.json` (JSON array of run entry objects).
- Survives world wipes and server restarts.
- Each entry: `runId`, `playerName`, `deathMessage`, `timestamp`.
- `/hc stat reset` sets the wipe count to 0 but does **not** delete the run history.

---

## Files

### Plugin data folder (`plugins/HardcoreWipe/`)

| File | Purpose |
|---|---|
| `config.yml` | Main configuration (auto-created on first run) |
| `stats.json` | Persistent run history and wipe count |
| `wipe.flag` | Temporary marker (deleted automatically) |

### Source structure (`src/main/java/cz/deefeen/hardcore/`)

| File | Purpose |
|---|---|
| `HardcorePlugin.java` | Main class — lifecycle, listeners, commands, wipe callback, config merge, MOTD |
| `DoomsdayCountdown.java` | Countdown timer with BossBar, sounds, titles |
| `StatsManager.java` | JSON persistence — runs, wipe count |
| `RunEntry.java` | Data record for a single run |
| `HcScoreboard.java` | Sidebar scoreboard management |
| `DiscordWebhook.java` | HTTP POST to Discord webhook (plain & embed) |

---

## Building

```bash
# Clone
git clone https://github.com/deefeen/hardcore-wipe.git
cd hardcore-wipe

# Build
mvn clean package

# Output: target/HardcoreWipe-2.1.0.jar
```

---

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/awesome-idea`).
3. Commit your changes (`git commit -am 'Add awesome idea'`).
4. Push to the branch (`git push origin feature/awesome-idea`).
5. Open a Pull Request.

---

## License

[MIT](LICENSE)

---

## Author

- **deefeen** — [GitHub](https://github.com/deefeen)
