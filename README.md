# DeeFeen HC

**DeeFeen HC** is a Paper plugin (1.21.5) that turns your Minecraft server into a hardcore roguelike experience. When a player dies, a configurable doomsday countdown begins — after it expires, all worlds are wiped clean and the server restarts. Stats persist across runs. Players can cancel the wipe by sacrificing a Totem of Undying via `/soulinsure`.

---

## Features

- **Player death → doomsday countdown** — BossBar with real-time progress, per-second tick sounds, alarm in last 5 seconds, title broadcasts.
- **Automatic world wipe** — After the countdown expires, all world folders are deleted. The server stops and starts fresh on next launch.
- **Persistent stats** — Runs are logged in `plugins/HardcoreWipe/stats.json` (never deleted). Includes death message, player name, timestamp, and world uptime.
- **World uptime** — Action bar displays time since last wipe. Pauses when no players are online. Resets on wipe. Format: `Xd Xh Xm Xs`.
- **Damage notifications** — Every hit is broadcast to chat with damage in hearts, cause (fall, lava, etc.), and attacker name (mob or player).
- **`/deadlist`** — Shows a history of all wipe runs.
- **Scoreboard** — Sidebar displays total wipe count. Follows players across reconnects.
- **Soul insurance (`/soulinsure`)** — Cancel an active countdown by consuming a Totem of Undying from your inventory. The dead player is revived at spawn in survival mode with a cleared inventory.
- **Discord webhook notifications** — Sends embed messages for wipe triggered, wipe complete, and soul insurance used.
- **Locale system** — All text in CZ or EN, switchable with `locale: cz/en` in config.
- **MOTD & server.properties** — Automatically sets MOTD and hardcore mode + difficulty on startup (configurable).
- **Config versioning** — Auto-merges new config keys when updating the plugin — your custom values are never lost.
- **No external dependencies** — Only Paper API. Gson is provided by the server. Discord webhook uses bare `HttpURLConnection`.

---

## Requirements

| Dependency | Version |
|---|---|
| [Paper](https://papermc.io/) | 1.21.5 (API 26.2.build.31-alpha) |
| Java | 25+ |
| Maven | 3.9.x (for building) |

---

## Installation

1. **Build the plugin:**
   ```bash
   mvn clean package
   ```
   The JAR will be in `target/DeeFeenHC-2.1.0.jar`.

2. **Place the JAR** in your server's `plugins/` folder.

3. **Start the server.** The plugin creates `plugins/HardcoreWipe/config.yml`.

4. **Edit `config.yml`** to your liking (see [Configuration](#configuration)).

5. **Restart the server** for changes to take effect.

---

## Configuration

The config is at `plugins/HardcoreWipe/config.yml` and contains only behavioural settings — all text is in locale files (switch with `locale: cz` or `locale: en`).

| Section | Description |
|---|---|
| `locale` | Language: `cz` (Czech) or `en` (English) |
| `countdown-seconds` | Seconds before the world wipe triggers (default: 30) |
| `uptime` | Enable/disable world uptime action bar |
| `bossbar` | BossBar color (`RED`, `BLUE`, etc.) and style (`SOLID`, `SEGMENTED_10`) |
| `sounds` | Tick sound (every second), alarm sound (last 5 seconds) |
| `worlds` | List of world folder names to delete on wipe |
| `motd` | Whether to override MOTD on startup and what text to use |
| `server-properties` | Whether to enforce `hardcore=true` and `difficulty=hard` on startup |
| `discord-webhook` | Webhook URL, enabled flag, embed color |

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

### World uptime

- Action bar shows `Xd Xh Xm Xs` above the hotbar.
- Time only counts when at least one player is online (pauses when empty).
- Resets to 0 on world wipe.
- Persists across server restarts in `stats.json`.

### Damage notifications

- Every hit a player takes is broadcast to all players.
- Environmental damage: `[DeeFeen HARDCORE] dostal damage ♥ -1.2 z Fall damage`
- Mob damage: `[DeeFeen HARDCORE] dostal damage ♥ -5.0 od Zombie`
- PvP: `[DeeFeen HARDCORE] dostal damage ♥ -4.0 od PlayerName`
- Shows actual hearts lost (final damage ÷ 2).

### Stats persistence

- Stored in `plugins/HardcoreWipe/stats.json` (JSON).
- Survives world wipes and server restarts.
- Each entry: `runId`, `playerName`, `deathMessage`, `timestamp`, `uptimeSeconds`.
- `/hc stat reset` sets the wipe count to 0 but does **not** delete the run history.

---

## Files

### Plugin data folder (`plugins/HardcoreWipe/`)

| File | Purpose |
|---|---|
| `config.yml` | Configuration (settings only, no text) |
| `stats.json` | Persistent run history, wipe count, world uptime |
| `wipe.flag` | Temporary marker (deleted automatically) |

### Source structure (`src/main/java/cz/deefeen/hardcore/`)

| File | Purpose |
|---|---|
| `HardcorePlugin.java` | Main class — lifecycle, listeners, commands, wipe callback, config merge, MOTD |
| `DoomsdayCountdown.java` | Countdown timer with BossBar, sounds, titles |
| `UptimeManager.java` | World uptime action bar (pauses when empty, resets on wipe) |
| `StatsManager.java` | JSON persistence — runs, wipe count, uptime |
| `RunEntry.java` | Data record for a single run |
| `HcScoreboard.java` | Sidebar scoreboard management |
| `DiscordWebhook.java` | HTTP POST to Discord webhook (plain & embed) |
| `LocaleManager.java` | Loads locale class, provides `get()` and `format()` |
| `locales/LocaleCZ.java` | All Czech translations |
| `locales/LocaleEN.java` | All English translations |

---

## Building

```bash
# Clone
git clone https://github.com/deefeen/hardcore-wipe.git
cd hardcore-wipe

# Build
mvn clean package

# Output: target/DeeFeenHC-2.1.0.jar
```

---

## Adding a new language

1. Create `src/main/java/cz/deefeen/hardcore/locales/LocaleXX.java` with a `getMessages()` method returning `Map<String, String>`.
2. In `LocaleManager.java`, add an `else if` branch for your language code.
3. Done — no config changes needed beyond setting `locale: xx`.

---

## License

[MIT](LICENSE)

---

## Author

- **deefeen** — [GitHub](https://github.com/deefeen)
