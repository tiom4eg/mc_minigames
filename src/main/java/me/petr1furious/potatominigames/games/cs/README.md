# Counter-Strike

Join from the main menu or use `/play cs [mode]`. Use `/cs test [mode]` for unscored solo practice; `/cs start @a` starts the default mode with selected players. `games/cs.yml` and `/cs config` set the round timings, half length and Deathmatch settings.

This is a Minecraft adaptation of [Valve's Counter-Strike](https://www.counter-strike.net/), with vanilla controls, graphics and sounds rather than exact weapon-stat or movement parity.

## Modes

- **Classic (`default`):** two balanced teams, 2–10 players. Attackers (T) can plant at A or B; defenders (CT) prevent or defuse the plant. First to 13 rounds, with a draw at 12:12. Teams change factions after 12 rounds; both teams reset equipment and economy. Classic practice gives $16,000 each round and does not end a round on elimination alone.
- **Wingman (`wingman`):** same rules, equipment and team limits as Classic, but A is the only bombsite. A Long and Short are open. B, lower CT-to-Mid, tunnels and Mid doors are sealed; red glass marks impassable gates that stop bullets.
- **Deathmatch (`deathmatch`):** timed free-for-all (2–16 players, five minutes by default). Rank by most kills, then fewest deaths. Respawn after three seconds at a point away from opponents, with three seconds of protection that ends on shooting or swinging the knife. Start with AK-47, USP-S, vest and helmet; `/cs buy` or the radar selects any weapon for free. Choices take effect immediately during countdown or spawn protection, otherwise on the next life. Reload normally with unlimited reserve ammunition. No C4, utility, team spotting or economy. Reconnects keep stats; non-test matches award 3/2/1 by place to players with kills, sharing exact ties.

## Bomb defusal and economy

- Rounds start with 15 frozen seconds, then 115 seconds of play. Buy in your faction's spawn during freeze time and the first 20 seconds of play. Players have 100 logical HP, no health regeneration and one life per round. Fall damage is disabled.
- T plant C4 while stationary at a site for 3.2 seconds; its fuse lasts 40 seconds. CT aim at the planted bomb within three blocks and hold use for 10 seconds (five with a kit). Movement, release, slot switch or death interrupts either action. Eliminating all T does not end a planted round. Positional plant, ticking and defuse sounds warn nearby players; starting and cancelling a defuse can be used as a feint.
- Start with $800 per half; carry at most $16,000. Kills pay by weapon. A round win pays $3,250, or $3,500 for a C4 outcome. Loss payouts grow from $1,900 toward $3,400; a win lowers the loss counter by one. Plant/defuse pays the player and a planted losing T team earns another $600 each. A T who survives a timeout gets no loss payout. Team damage is reduced; teamkills cost money and a kill.
- Equal teams receive the full rewards per person. Only an outnumbered team receives the `larger team / smaller team` multiplier, including to pistol-round cash. Online counts are taken at the round start; prices, cash cap and misconduct penalties do not scale.
- Survivors carry over weapons, armor, kits and utility, and restock gun ammunition at the next round. Death drops guns with their remaining ammo as well as utility, kits and C4. Survivors have five seconds to loot after a round. Drop an occupied weapon slot before picking up its replacement.
- Death shows a short death screen, then teammates' cameras only; sneak to switch. Reconnecting does not respawn you mid-round. If a team is absent for 60 seconds it forfeits. `/cs pause` requests one of four 30-second team pauses. Winners earn three points; a draw earns one; practice awards none.

## Controls

| Input | Action |
| --- | --- |
| Right-click | Fire, short grenade toss, heavy knife attack, or use the selected utility |
| Left-click | Long grenade throw, quick knife attack or toggle a scoped gun's zoom |
| F / `/cs reload` | Reload; the action bar shows ammo and remaining reload time |
| Q / `/cs drop` | Drop the selected weapon, grenade, kit or C4 |
| Slots 1–3 | Primary, secondary, knife (more damage from behind) |
| Slots 4–7 | HE, flash, smoke, fire grenade |
| Slot 8, hold right-click | Plant or defuse |
| Slot 9 / `/cs buy` | Right-click the radar to buy at spawn; select weapons in Deathmatch |
| Sneak + right-click radar / `/cs score` | Roster, alive status, K/D/A and MVPs; only your team can see your cash |
| `/cs help` | Controls |
| `!message` | Team-only chat, including dead teammates |

The radar shows allies and enemies seen by a living ally, briefly retaining their last known point. Smoke and flashes prevent spotting; CT learn the bomb location by seeing it. Teammates have private names and outlines, with outlines suppressed across smoke or while flashed. Uniform color follows the stable team across halftime; visible iron pieces indicate bought armor and helmets.

## Weapons and utility

| Category | Weapons |
| --- | --- |
| Pistols | Glock-18 (T), USP-S (CT), P250, Dual Berettas, Five-SeveN (CT), Tec-9 (T), CZ75-Auto, Desert Eagle |
| SMGs | MAC-10 (T), MP9 (CT), UMP-45, MP7, P90 |
| Rifles | Galil AR (T), FAMAS (CT), AK-47 (T), M4A1-S (CT), M4A4 (CT), AUG (CT), SG 553 (T) |
| Sniper rifles | SSG 08, AWP |
| Shotguns | Nova, XM1014, MAG-7 (CT), Sawed-Off (T) |
| Machine gun | Negev |

Faction restrictions apply to buying, not picking up. The shop shows price, ammunition, bullet or pellet damage, armor penetration and rate of fire. Semi-auto pistols fire once per click; CZ75-Auto supports held fire but has one reserve magazine, a long draw and a $100 kill reward. P90 kills pay $300, other SMGs $600. Nova, XM1014 and Sawed-Off reload shell by shell and can interrupt a reload; MAG-7 replaces its magazine. Negev stabilizes on its twelfth uninterrupted shot. FAMAS and Glock have no burst toggle; suppressors are fixed. USP-S and M4A1-S are quieter and have no visible tracer.

Movement, jumping and rapid follow-up shots make guns less accurate. Crouch or stop and pace shots to recover accuracy; holstering does not instantly reset spread. Held weapons change movement speeds, with knife/C4 fastest and Negev slowest. Vanilla sprint is off. AWP, SSG 08, AUG and SG 553 scope on left-click; right-click still fires. Visible zoom needs vanilla **FOV Effects**. AWP is slower when scoped, while Scout stays mobile. Head/body/leg hits, armor penetration, aim punch, falloff, limited wall penetration and breakable glass are simulated; the range limit is 256 blocks, but effective range differs substantially by weapon.

Short and long grenade throws both inherit player motion and can bounce. Carry up to four grenades, at most two flashes and one of any other kind. HE damage respects cover, flashes depend on distance and facing, and smoke conceals players and darkens their view while allowing shots through. Smoke extinguishes incendiary zones. Import geometry is otherwise protected; breakable glass resets each round.

## Dust II map setup and attribution

The plugin does not include or download map files. Provide these in the plugin data folder before starting any CS mode:

```text
maps/cs/dust2.bin.gz
maps/cs/radar.png
```

**[LesRaisins | CSGO] Dust II** is by xjqsh / xjqshm and LesRaisinsStudio, based on Valve's original map. Obtain the `dust II-0.1` world (file `5637196`) from the [source project and terms](https://www.curseforge.com/minecraft/worlds/lesraisins-csgo-dust-ii). Use the [converter](../../../../../../../../src/checks/tools/import_cs_dust2.py) locally, substituting the correct data folder if different:

```sh
data_dir=plugins/PotatoMinigames
python3 src/checks/tools/import_cs_dust2.py /path/to/dust2.zip "$data_dir/maps/cs/dust2.bin.gz"
python3 src/checks/tools/import_cs_dust2.py /path/to/dust2.zip "$data_dir/maps/cs/radar.png" --radar
```

The conversion crops Java block geometry, translates it up 64 blocks and updates block names. Source entities, inventories and executable world data are excluded. Spawns and play bounds are fitted to the existing architecture. Keep the author's credit and original usage terms with any redistributed map. The map is **non-commercial and attribution-required**, separate from the plugin's Apache-2.0 code; commercial use or re-creation requires the author's permission. See also [NOTICE](../../../../../../../../NOTICE).

## Checks

Run `sh ./gradlew checkCounterStrike` for rule and fixture checks. See the [native Paper fixtures](../../../../../../../../src/checks/native/cs/README.md) for spawn, scope, grenade and visual checks.
