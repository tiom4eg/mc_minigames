# The Finals

Join via the lobby compass or `/play finals` (2–12 players). Teams fight over vaults, cashboxes and cashout stations in a disposable arena. No client mod or resource pack is needed.

The mode adapts [Embark Studios' THE FINALS](https://www.reachthefinals.com/), including its [revive statue](https://www.thefinals.wiki/wiki/Revive_Statue) and [Defibrillator reconstruction](https://www.reachthefinals.com/patchnotes/200), with custom timings and vanilla Minecraft controls.

## Tutorial and match flow

`/finals tutorial` opens a private practice arena even while a match is running. Practice opening, delivering, defending and stealing a cashout and try all three classes with the real loadout menu; practice choices do not replace saved kits. `/finals tutorial help` repeats instructions and `/play leave` exits. On first queueing, **Play tutorial** or **Continue queueing** is offered once; closing cancels that queue attempt.

- At the start, teams spawn together. Players can edit their kits during the frozen loadout countdown (18 seconds by default; up to 60 in `countdownSeconds`). Slot 9 reopens the menu.
- Right-click the emerald vault to open it in five seconds, then touch the cashbox. Carriers glow and move slowly. Aim and press **Q** to throw, even with an empty hand. A throw arcs, stops at walls and automatically deposits into a cashout it reaches. Death drops the box.
- Right-click gold station **A** or **B** to deposit a carried box. After 90 seconds its current owner banks $10,000. **Sneak + right-click** an enemy station to steal it: remain within three blocks and keep sneaking for five seconds. Damage, death, moving out of range or releasing sneak interrupts the steal. Theft does not reset the timer; a payout wins if both finish on the same tick.
- Stations fall when their support breaks; labels, interactions and payouts follow them. The HUD shows ownership, payout or steal progress; an active steal sounds a siren. Nearby cashouts alternate vanilla Pigstep and Lava Chicken, within 32 blocks.
- First to $20,000 wins, or the richer team after ten minutes. Equal scores draw. Connected winners receive three points; practice, draws and aborted matches receive none. Teams stay fixed; if an entire team disconnects, it has 15 seconds to return before an unscored finish.

## Respawn and revival

Ordinary deaths respawn automatically after five seconds. Respawns have two seconds of protection and cannot fight or interact with objectives during it. While waiting, press **Sneak** or use `/finals loadout` to select equipment for the next normal respawn; revives keep the kit from the previous life.

Matches that start 2v2 or 3v3 instead leave a revive statue. Press **Space** to respawn after an eight-second unlock; it is not automatic. Teammates can right-click to carry or drop the statue, or press **Q** to throw it into cover. Sneak + right-click to start a four-second manual revive: stay crouched, aimed, nearby and in sight. Attacking, opening the loadout menu or taking damage cancels it. Manual revives restore full health next to the statue.

A Medium's Defibrillator needs one second held and aimed at the statue, then reconstructs the teammate in three seconds with half health and no gadgets or specialization for five seconds (weapons still work). An accepted charge is spent even if interrupted. Once reconstruction starts it can finish after the reviver dies, unless the target respawns/disconnects or the team wipes. In other match sizes, Defibrillator instantly revives a nearby teammate at their death position during the ordinary respawn delay.

A wiped duo or trio loses its statues, and returns together at a randomly selected team spawn after a shared 16-second delay, even if someone disconnects. Defibrillator cannot bypass a wipe.

## Five-player Game Master

The first four players in participant order form two teams of two; the last becomes a spectator Game Master with no match points. Once the loadout countdown ends, the Game Master alone can open `/finals events` or run these commands. Events last 30 seconds and cannot overlap; impacts respect map boundaries and protected blocks.

| Command | Effect |
| --- | --- |
| `/finals event low_gravity` | Higher jumps and slower falls |
| `/finals event meteor_shower` | Marked impacts hurt contestants and destroy cover |
| `/finals event orbital_lasers` | Standing still attracts a delayed strike; move after the warning |
| `/finals event status` | Show the current event and remaining time |
| `/finals event stop` | Clear active effects and pending strikes |

## Classes and controls

Use `/finals loadout`, `/finals class S|M|L` (or `light|medium|heavy`) and `/finals weapon <id>`. Pick a weapon, one specialization and three distinct class-compatible gadgets. Choices are saved in `finals/loadouts.yml`. Changes apply immediately in the countdown, or on respawn during combat. The menu does not protect you.

| Class | Health / speed / size | Weapons | Specializations |
| --- | --- | --- | --- |
| Light (S) | 15 HP / 120% / 80% | M11 (`m11`), XP-54 (`xp54`), SR-84 (`sr84`), Sword (`sword`), Dagger (`dagger`) | Evasive Dash, Cloaking Device, Grappling Hook |
| Medium (M) | 25 HP / 100% / 100% | AKM (`akm`), FCAR (`fcar`), Model 1887 (`model1887`), Riot Shield (`riotshield`), Dual Blades (`dualblades`) | Healing Beam, Guardian Turret, Dematerializer |
| Heavy (L) | 35 HP / 85% / 125% | M60 (`m60`), Lewis Gun (`lewisgun`), SA1216 (`sa1216`), Sledgehammer (`sledgehammer`), Spear (`spear`) | Charge 'n' Slam, Mesh Shield, Winch Claw |

All classes can use Frag, Smoke, Gas, Pyro and Goo Grenades or Flashbangs. Light adds Sonar Grenade, Vanishing Bomb and Breach Charge; Medium adds Sonar Grenade, Jump Pad, Defibrillator, Explosive Mine, Gas Mine and APS Turret; Heavy adds RPG-7, Dome Shield, Barricade, Explosive Mine and Pyro Mine. The menu shows charges, recharge and controls.

- Slots **1 / 2–4 / 5 / 9** hold weapon, gadgets, specialization and loadout menu. Right-click to fire or use equipment, left-click for melee, and **F** to reload. Magazines reload from unlimited reserves. Gadgets recharge and reset on respawn. Gunshots do not knock players back.
- Dagger rewards backstabs, spear has longer reach, and the sledgehammer destroys nearby cover on right-click. Hold right-click to guard with Riot Shield. Guarding with Dual Blades reflects bullets within 14° of the crosshair; guarded ranged hits still deal 70% damage and frontal melee hits 75%. Explosions cannot be reflected.
- Light/Medium/Heavy regenerate after 8/9/10 seconds without taking damage at 4 HP per second; firing and moving do not reset that delay. Fall damage is off. Heavy crouches through two-block doors.
- Healing Beam works while right-click is held and line of sight is clear (self-healing is slower); eight seconds of healing overheats it and four seconds fully cools it. Sonar reveals hidden enemies; attacking or carrying a box ends invisibility.
- Dome Shield stops attacks from outside, but not movement, and does not protect cashbox carriers. APS intercepts enemy grenades and rockets. Turrets and mines can be destroyed. Smoke obscures and extinguishes fire; fire clears gas. Explosives can hurt their owner.
- Mesh Shield blocks frontal attacks while held. Winch Claw pulls enemies, boxes or stations until blocked by terrain. Dematerializer holes restore once clear of players.
- Break glass by hand and right-click doors, trapdoors, gates and buttons normally (including iron doors/trapdoors). Pressure plates and buttons can trigger native redstone. Weapons and explosives break cover and barricades, but not spawn zones, objectives or the protected floor.

The HUD highlights the available vault or cashouts and marks cashboxes, distances and teammates even beyond ordinary entity view distance. The Tab list shows classes and kills; only allies see each other's HP or respawn status. Damage briefly reveals an opponent's health in sight, not through walls.

## Custom maps

The generated arena needs no map file. To create a custom one, import a vanilla Java overworld folder/ZIP from `finals/imports/` in the plugin data folder, register an existing persistent world, or import a public direct HTTP(S) ZIP URL. Imports create an editor copy. Wait for completion before `goto`; login pages, map listing pages and private-network URLs are not supported.

1. `/finals map import rooftop "Rooftop.zip"`, then `/finals map goto rooftop`. Alternatively, stand in an existing world and run `/finals map create rooftop`.
2. Look at opposite corners and run `/finals map pos1 rooftop` and `/finals map pos2 rooftop`. Selection is horizontal; the full world build height is included.
3. Stand at safe starting locations and run `/finals map spawn rooftop red` or `/finals map spawn rooftop blue`. Repeat to add points; `spawnclear rooftop red|blue` clears that team's draft list.
4. Stand at the objective spots and run `/finals map vault rooftop`, `/finals map cashout rooftop a` and `/finals map cashout rooftop b`. Use `vaultadd` and `cashoutadd` for up to 16 candidates of each type, or `vaultclear` and `cashoutclear` to clear their lists. `vault` replaces the whole vault list; `cashout … a|b` changes only the indicated initial candidate.
5. `/finals map save rooftop`; wait for success, then `/finals map use rooftop`. Inspect with `map info rooftop` or `map list`; select `generated` to use the fallback again.

Map commands are admin-only. Saving validates bounds, safe standing space and spacing: cashouts must be at least eight blocks from each other and every vault; objectives must be at least three blocks from team spawns. With multiple vault locations, the vault picks a different one after each payout. Two cashouts are active; with at least three candidates, only the station that paid out moves to a different unused position.

`create` and `goto` enable private numbered previews: green vaults, gold cashouts, red/blue spawns with facing arrows. Toggle with `/finals map preview rooftop on|off`. Use `vaultremove rooftop 2`, `cashoutremove rooftop 3` or `spawnremove rooftop red 1` after `/finals map` to remove one point by its 1-based preview number. Remaining points renumber; save to apply draft edits.

To divide an imported city, use `/finals map create city_north city` and `/finals map create city_south city`, or run `create <name>` while standing in the city's editor world. Each area has independent bounds, spawns, objectives and saved snapshots; terrain changes in the shared editor enter only the areas you save again.

### Jump pads, death floor and time

- Stand on the launch block and run `/finals map jumppad <name> up` for upward launch, or use `targeted`, then stand at the destination and run `/finals map jumppadtarget <name> <number>`. Remove a pad with `jumppadremove <name> <number>`. Endpoints must be safe; a targeted pad needs a destination. Targeted pads glow within 16 blocks and follow an arc that stops at cover. Upward pads preserve horizontal movement. Save after editing.
- `/finals map deathheight <name> 40` kills contestants below Y=40, not exactly at 40; use `off` to disable it. Boxes and statues that cross it return to the last safe departure point if possible. Save after editing.
- `/finals map time <name> night` fixes match time. Presets: `sunrise`, `day`, `noon`, `sunset`, `night`, `midnight`; any tick from 0 to 23999 also works. `default` restores noon. Save after editing; it does not change the editor's clock.

Native saves retain blocks, block states, chest contents and sign text, not entities. Only the overworld is imported, and snapshots reject command blocks and portals. Match damage never touches the editor, template or import. Custom maps use [AdvancedSlimePaper](../../../../../../../../README.md#slime-world-arenas-asp) `.slime` snapshots, with off-thread preparation and fresh copies for matches. Large maps need enough disk and memory; failed saves leave the previous snapshot usable.

## Commands and checks

Use `/finals test` for solo practice, `/finals start @a` or `/finals start @a rooftop` to start as an admin, and `/finals stop` to end the match. `games/finals.yml` and `/finals config` set timing and score defaults for subsequent matches. See the [native fixtures](../../../../../../../../src/checks/native/finals/README.md) for Paper checks in addition to the Gradle `checkFinals*` tasks.
