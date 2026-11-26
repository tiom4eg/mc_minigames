# Warfare

Join with `/play war classic` or `/play war team_beacon`, or use the lobby compass. Warfare uses the shared [gadgets](../../gadgets/README.md).

- **Classic:** An invisible free-for-all. The sidebar buckets other players by distance; use your kit and gadgets to hunt them. The last one standing wins.
- **Team Beacon:** Protect your team's wool beacon while trying to break the others. Players respawn at their beacon while it stands. Once it is destroyed, those still alive have only their current life. Players awaiting respawn cannot scout via the spectator camera.

## Map rotation

A map is a slime template (`slime_worlds/<template>.slime`), a center, a spawn radius and its own beacon positions. Matches play in disposable copies so destruction does not persist. Queued players vote with `/play map <map|any>`; admins can force a map, for example `/war start classic @a mibc` or `/war test classic mibc`. The `noHunger` setting is enabled by default.

1. Set up [AdvancedSlimePaper](../../../../../../../../README.md#slime-world-arenas-asp) and import the world template with `/swp import`.
2. Run `/swp goto <template>`, stand at the center and run `/war map add <name> [radius]`.
3. Look at each desired beacon position and run `/war beacon set <team>`. Team Beacon offers only maps with at least two beacons, and only team colors shared by all eligible maps.
4. Inspect with `/war map list` and `/war beacon info`; edit with `/war map remove <name>` or `/war beacon clear` as needed.

An example from `games/warfare.yml`:

```yaml
maps:
  mibc:
    template: mibc
    center: 880.0 4.0 760.0
    radius: 250
    beacons:
      red: 900.0 5.0 700.0
      blue: 860.0 5.0 820.0
```

Without rotation, Warfare uses `gameWorld` and `gameCenter`; `/war setcenter` sets both to where you stand. To use one loaded, read-only ASP template instead of map rotation:

```text
/pm config asp.enable true
/pm config asp.templateWorld <template>
/pm config asp.gameWorld <arena>
/pm config asp.resetOnStart true
```

When enabled, `asp.gameWorld` overrides `gameWorld`. Place beacon coordinates in the clone, not the source.

## Kit and settings

`/war kit save` and `/war kit load` manage the game kit. `/war config` manages mode, arena, border, distance buckets, lifts and team-beacon rules; edit structured `maps` and `beacons` through their commands or the YAML.

`/war test [mode] [map]` starts an unscored solo session. Classic practice does not finish merely for being the last player, and Team Beacon practice accepts one team. `/war stop` ends the session.
