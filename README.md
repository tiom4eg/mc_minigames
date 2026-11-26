# Potato Minigames

A Paper plugin for running several minigames side by side. Players queue from a shared lobby, and games start automatically. This is a fork of [Petr1Furious/potato-minigames](https://github.com/Petr1Furious/potato-minigames).

## Games

| Game | Queue | What you play |
| --- | --- | --- |
| [Pillars](src/main/java/me/petr1furious/potatominigames/games/pillars/README.md) | `/play pillars` | Survive on sky pillars with random items and optional modifiers. |
| [Bingo](src/main/java/me/petr1furious/potatominigames/games/bingo/README.md) | `/play bingo` | Race other teams to collect items on a generated board. |
| [Warfare](src/main/java/me/petr1furious/potatominigames/games/warfare/README.md) | `/play war` | Hunt invisible opponents or attack and defend team beacons. |
| [Chase](src/main/java/me/petr1furious/potatominigames/games/chase/README.md) | `/play chase` | Take turns hunting and evading opponents on fresh terrain. |
| [Party](src/main/java/me/petr1furious/potatominigames/games/party/README.md) | `/play party` | Play quick games on their own or combine them into a tournament. |
| [The Finals](src/main/java/me/petr1furious/potatominigames/games/finals/README.md) | `/play finals` | Open vaults and fight over cashouts in destructible arenas. |
| [Counter-Strike](src/main/java/me/petr1furious/potatominigames/games/cs/README.md) | `/play cs` | Bomb defusal, Wingman or Deathmatch on Dust II. |
| [World of Tanks](src/main/java/me/petr1furious/potatominigames/games/tanks/README.md) | `/play tanks` | Fight with tanks and capture the enemy base. |

## Playing: queues and the lobby

Use the lobby compass, a chat **[Join]** link, or `/play <game> [mode]`. You can be in only one queue at a time.

| Command | Action |
| --- | --- |
| `/play` | List queues. |
| `/play <game> [mode]` | Queue, or spectate if that game is already running. |
| `/play vote` | Vote to start early. |
| `/play team <colour>` | Pick a team where supported. |
| `/play difficulty <level\|any>`, `/play map <map\|any>` | Vote on a queued game's difficulty or map where supported. |
| `/play leave` | Leave a queue, match or spectator view. |
| `/lobby` | Return to the lobby. |

By default, the queue counts down from 60 seconds once its minimum is met, shortening to 15 if all free online players queue, or to five when enough players vote (all but one for queues of at least three; everyone otherwise). Dropping below the minimum cancels it. Disconnecting or staying AFK for five minutes leaves the queue. Map/difficulty votes precede team selection; ties use default difficulty or a random map.

Each game category runs one session at a time. Overflow players and those queued for its other modes spectate, with access to match chat and HUD. With `spectate.followPlayers` on, sneak to cycle through players' viewpoints; temporary respawn waits do not grant scouting. Play-by-play stays in the match; the server sees start and result announcements.

The lobby defaults to world `lobby`. Non-admins enter adventure mode with a menu compass, without damage, hunger or item drops. Leaving restores their original state. Players who queued from outside the lobby return to their original location after the game. Snapshots in `data/snapshots/` survive restarts; players who were offline at the end are restored on login.

## Administration and config

Each game's command root provides `start [mode] [players] [option]`, `test [mode] [option]`, `stop`, `restart`, `join`, `leave`, `list`, `points`, `team`, `config` and `reload` as applicable. Start arguments are ordered: mode, player selector, then a game-specific option if supported. Omit unsupported arguments. `/play team` applies to the queue; saved team assignments are for direct starts. Test and single-player starts are unscored practice. Administrative actions require `potatominigames.admin` (operators by default).

`/pm` provides cross-game `status`, `games`, `stop [game]`, `forcestart <game> [mode]`, `reload`, `config`, `inventory <save|load|copy>` and `give <gadget>`. `forcestart` bypasses queue minimums and timers.

All paths below are relative to the plugin data folder:

| Path | Contents |
| --- | --- |
| `config.yml` | Lobby, queue timers and limits, spectator options and ASP reset settings. |
| `games/*.yml` | Game modes, options and settings. |
| `gadgets.yml` | Shared [gadget settings](src/main/java/me/petr1furious/potatominigames/gadgets/README.md). |
| `data/` | Points, teams, inventories, player snapshots and presets. |

`/<game> config` edits game settings. `/pm config` edits core settings and `gadgets.*`. Call either with no arguments to browse; dotted paths tab-complete, and clickable values open editing. Use `default` to reset a declared setting, or `add <entry>`/`remove <entry>` for flat lists. Structured sections are edited in YAML or via game-specific commands. Consult the game guide for when a setting takes effect.

```text
/pm config queue.countdownSeconds 30
/pm config queues.<game>.<mode>.minPlayers 1
```

Worlds, third-party map files, player data and separate plugins are not included. See the relevant game guide for asset setup.

## World services

### Throwaway worlds

Bingo and Chase generate fresh terrain for each match. Managed worlds use `potato_tmp_bingo` or `potato_tmp_chase` and their dimension suffixes. Saving stays on so revisiting unloaded chunks preserves edits; the folders are removed at teardown, or swept on startup after a crash. Allow enough disk space for the whole match.

### Slime-world arenas (ASP)

Template-based arenas make a disposable copy for each game. Run AdvancedSlimePaper with its matching ASP plugin in `plugins/` to load templates and provide `/swp`. See the individual game guides for arena import and selection.

For a shared, loaded template, create and save it with `/swp create <template> file` and `/swp save <template>`, or import an existing world with `/swp import <world-folder> file <template>`. Set `loadOnStartup: true` and `readOnly: true` for the template in `plugins/SlimeWorldManager/worlds.yml`.

## Localization

Russian client locales use Russian; other clients and the console use English. Identifiers and user-authored names are not translated. Catalogs live under [`i18n/locales/`](i18n/locales/), with domains in `catalogs.txt`. Update both languages for every built-in text change, including item names, HUD text and errors.

- Use flat, quoted YAML values and stable semantic keys. Keep the `<arg:N>` indices the same in both catalogs, starting at zero. Translations may reorder arguments.
- Pass numbers, user text and rich Components to `I18n.tr` as arguments, never as string concatenation or user-authored MiniMessage. Use native Minecraft translation keys for vanilla items and entities.
- Keep shared messages and sidebar values untranslated until delivery. Render inventory titles with `I18n.render` and item metadata from a fresh template with `I18n.item`, per viewer.
- Run `sh ./gradlew checkI18n` to check catalog parity, placeholders and fallback.

## Building

Use Java 25 and the Gradle wrapper. The default target is Paper/AdvancedSlimePaper 26.3; use `-Pmc=26.2` for 26.2.

```sh
sh ./gradlew build
sh ./gradlew build -Pmc=26.2
```

Install `build/libs/potato-minigames-<version>-mc<api>.jar` in the server's `plugins/` folder. `build` includes automated checks; [optional fixtures](src/checks/) can be run separately as described in the game guides.

To format sources, run `bash src/checks/tools/format-code.sh`; add `--check` to check without writing. The script needs Java 25, Node.js with npm, `curl`, `sha256sum` and `black==26.5.1`. Set `BLACK` to the executable path if that Black version is not on `PATH`.

## License

The code is [Apache-2.0](LICENSE). See [NOTICE](NOTICE) for attribution and the game guides for separately licensed external maps.
