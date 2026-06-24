# Changelog

## 2.1.0 (2026-06-24)

### Changed
- Plugin renamed to **DeeFeen HC** — JAR is now `DeeFeenHC-2.1.0.jar`.

### Added
- **Damage notifications** — every hit a player takes is broadcast to chat with damage in hearts, cause (fall, lava, etc.), and attacker name (mob or player).
- **World uptime** — action bar displays time since last wipe (`Xd Xh Xm Xs`). Pauses when no players are online. Resets on wipe. Persists across restarts.
- **Locale system** — all text moved from config.yml to `LocaleCZ.java` / `LocaleEN.java` in `locales/` subpackage. Switch language with `locale: cz` / `locale: en` in config.
- **Config versioning** — auto-merges new config keys on update without touching existing values.
- `UptimeManager.java` — handles uptime counting, formatting, and action bar display.
- `LocaleManager.java` — loads appropriate locale class, provides `get()` and `format()`.

### Changed
- Config.yml stripped of all text strings — now contains only behavioural settings.
- Config schema simplified: removed `messages:`, `scoreboard.title/line`, `bossbar.title`, `damage-notifications.prefix`, discord webhook text fields.
- All hardcoded command responses moved to locale files.
- Damage notification format now fully locale-driven.
- Discord webhook field labels localized.
- Updated to **Paper API 26.2.build.31-alpha** (Minecraft 1.21.5).

### Fixed
- Damage notifications now show hearts (final damage ÷ 2) instead of raw damage values.
- Uptime timer correctly pauses when server is empty.
- Config merge no longer overwrites user-customized values — only adds missing keys.

## 2.0.0 — Initial public release

### Added
- Player death → doomsday countdown with BossBar, sounds, titles.
- Automatic world wipe via JVM shutdown hook (deletes world folders after server stops).
- Persistent stats in `stats.json` (runs, wipe count).
- `/deadlist` — history of all wipe runs.
- `/soulinsure` — cancel countdown by consuming Totem of Undying, revive player.
- Scoreboard sidebar showing wipe count.
- Discord webhook notifications (embed + plain text) for trigger/complete/insured events.
- MOTD override and `server.properties` auto-configuration (`hardcore=true`, `difficulty=hard`).
- `DoomsdayCountdown.java`, `StatsManager.java`, `RunEntry.java`, `HcScoreboard.java`, `DiscordWebhook.java`.
- Full config.yml with Czech comments.
