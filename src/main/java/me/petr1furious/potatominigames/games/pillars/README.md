# Pillars

Join from the lobby compass or with `/play pillars [mode]`. Start on a bedrock pillar in the sky, use each random drop and be the last one standing. Ordinary enchantable drops have a one-in-three enchantment chance by default (`enchantChance`). Pillars is adapted from [MarcPG's PillarPeril](https://github.com/MarcPG1905/PillarPeril) via [Petr1Furious's fork](https://github.com/Petr1Furious/PillarPeril); see [NOTICE](../../../../../../../../NOTICE) for attribution.

## Modes

| Mode | Rules |
| --- | --- |
| `original` | Circular layout, 5-second item interval and five-minute limit. |
| `chaos` | Random layout, items every three seconds and an eight-minute limit. |
| `item-shuffle` | Replace the last set every ten seconds by default; see below. |
| `lucky` | Lucky blocks instead of bedrock. Needs [New Amazing Lucky Blocks](https://github.com/I2000C/NewAmazingLuckyBlocks) in whitelist world mode and a lucky block type matching `pillarMaterial`. |
| `tactical` | Everyone gets the same drops (`sameItems` can also be set on other modes). |
| `big_house` | Chaos-speed items with pillars spaced 2.5× farther apart (`distanceMultiplier`). |
| `sniper_duel` | Pillars 1.5× farther apart. First five drops are random logs; the sixth is a plain crossbow. Then select arrow, explosive firework or ordinary weighted loot with equal probability. Forced drops and ammunition bypass the blacklist. Progress is per player and survives reconnecting. |
| `party` | Vote for Party or Rave. Rave has faster drops and more frequent events. |
| `casino` | Vote Lucky, Neutral or Unlucky. Each adds perks and events about every 40 seconds; good events are weighted 75%, 50% or 25% when both good and bad categories are available. |
| `adhd` | Ordinary Pillars with periodic shuffle and event bursts. |
| `perks` | Ordinary drops sometimes become special items. |
| `flood` | Lethal rising water, even inside a sealed shelter. |
| `fusion` | Independently roll chaos, shuffle, tactical, big house, ADHD, perks and flood at 50% each, plus Party or Rave at 25% each. The result is announced. Tactical shares full shuffle sets; shuffle uses its own pace outside ADHD bursts. Sniper Duel is not a Fusion modifier. |

### Shuffle, ADHD and Flood

Shuffle replaces inventories (armor, offhand, cursor and crafting grids included), dropped items, container contents and stored/worn items on entities. Boats, carts, hanging decorations and armor stands vanish. Placed blocks stay, but containers empty. Unloaded chunks are cleaned on load and reconnecting does not restore old items. Ender chests are blocked without changing persistent contents; SUPERHOT cannot bring back old sets.

ADHD bursts last 30 seconds and start every 90–150 seconds by default, including before the first burst. Everyone immediately gets a fresh shuffle set. During a burst, sets change at three times the configured shuffle speed, and events roll at three times the Rave rate unless the base rate is already faster. A HUD timer shows the remaining burst. Burst events and warnings stop with the burst; base events continue. Ender chests are blocked for the entire match. Set `adhd.meanSeconds` and `adhd.durationSeconds` in `games/pillars.yml`, or add the modifier to another mode with `/pillars config modes.original.adhd true`.

Flood starts 20 blocks below pillar feet and rises one block every ten seconds after countdown by default (`flood.startBelow`, `flood.riseSeconds`). Reaching or crossing the surface kills a living player, including a stationary player in solid cover or under perk invulnerability. The water is visual only: it neither changes blocks nor provides buoyancy. The server still enforces lethal height beyond the visible surface and even if visual updates lag. The surface applies only to the match; following viewers and resent chunks receive the current level. Enable it in another mode with `/pillars config modes.<mode>.flood true` or add `flood` to a preset.

## Presets

**Pillars → Create a mode** sets the item interval and fixed modifiers. Edit each enabled modifier's parameters, then **Start game** without saving or **Save preset**. The perk pool is server-configured. **My modes** and **Other players' modes** list presets; select one to join its queue.

```text
/pillars preset save duel tactical big_house rave
/pillars preset save rising tactical flood
/pillars preset list
/play pillars duel
/pillars preset delete duel
```

Each player can own five presets. Names have 1–24 Latin letters, digits, `_` or `-`, starting with a letter or digit. They are unique case-insensitively within Pillars, and built-in IDs are reserved. Only the owner can replace/delete a preset, and not while it is queued or playing. Icons use `/give` item syntax or SNBT, for example `{id:"minecraft:diamond",count:1}`; blank uses bedrock. Presets are stored in `data/pillars-presets.yml`.

## Party and Rave events

`eventMeanSeconds` is a mean, not a schedule. Bundled Party and Rave means are 60 and 30 seconds. Events are drawn from those not already active and most warn five seconds ahead. Movement checks use the preceding ten ticks.

| Event | Duration | Effect |
| --- | --- | --- |
| Boat Rain | 15 s | Unbreakable boats fall; riders get Slow Falling when boats vanish. |
| Disco | 30 s | Standing still deals increasing damage and darkens the screen. |
| Block Ban | 7 s | Picks a placed block to remove; the chosen block stops dropping. |
| Stiff Neck | 20 s | Locks horizontal look only. |
| Swap | Instant | Two or three players trade positions without warning. |
| Low Gravity | 10 s | One-quarter gravity. |
| Giants & Gnomes | 20 s | Players become double or half size. |
| Slippery Floor | 20 s | Placed blocks turn to packed ice. |
| Spotlight | 15 s | Everyone glows. |
| Hangover | 15 s | Nausea III, Slowness II and a flash of Wither. |
| Wind | 60 s | Directional push; sneak to resist. |
| Knockback Party | 20 s | Everyone gets a temporary Knockback II stick. |
| Item Frenzy | 15 s | Items three times as often. |
| Hot Potato | 20 s | Hit someone to pass TNT before it explodes. |
| Darkness | 15 s | Darkness effect. |
| Vanishing Blocks | Rest of game | Placed blocks disappear after 15 s. |
| SUPERHOT | 30 s | Standing still takes items away until you move. |
| Airdrop | Instant | A chest of loot appears between players, 3–15 blocks above the pillars. |
| Contract Ad / Реклама | 5 s | Flashing central title suppresses other event titles. |
| Math / Контрольная | 10 s | Type the integer result in chat. Living players can retry until the deadline; unanswered or incorrect costs three hearts. |

## Perks

`/play pillars perks` or add `perks` to a mode/preset. An ordinary item draw has a 20% default chance (`perks.dropChance`) to become a perk. The 51 ordinary perks have weight 4 each; the four legendary perks have weight 1 (about 0.096% each per default item draw). Tactical shares the perk type, not the item or charges. Sniper Duel's forced opening drops are unchanged. Perk items show glint, localized instructions and remaining charges. Use active items in the main hand unless noted. Admins can grant a perk to themselves during a perks match after countdown with `/pillars perk <id>`.

| ID | Effect and controls |
| --- | --- |
| `save` | A 3×3 slime platform below you for 10 s; one use. |
| `reroll` | Replace the entire latest reward, including stacks or a bow/ammo bundle. All original parts must still be in your inventory unchanged. Three uses. |
| `steal` | Hook an opponent to take their entire held stack with its metadata. A miss or empty hand costs nothing; one theft. |
| `ghost` | 2.7 s of bounded spectator flight and phasing, then normal play resumes at a clear position. One use. |
| `anchor` | Wear boots to cancel knockback from three damaging enemy hits, not the damage. |
| `jump` | Carry the rabbit foot and jump again in midair. Three extra jumps; landing does not refill them. |
| `rewind` | Return to a clear position from three seconds ago, without changing health, inventory or charges. |
| `house` | Build a 3×3 wooden house with a one-player interior. Refuses a blocked footprint. |
| `freeze` | Ray up to 32 blocks; traps a hit opponent in protected ice for four seconds, without actions or damage. One attempt. |
| `bow` | Legendary bow with extreme knockback and one arrow. Breaks even on a missed shot. |
| `crossbow` | Legendary three-shot launcher of primed TNT with a two-second fuse; no ammo required. |
| `planks` | First equipping the boots places permanent planks underfoot for five seconds; re-equipping never restarts it. |
| `break` | Hold use aimed at one block within 12 blocks for three seconds. Destroys even bedrock, with no drops. |
| `sheep` | Summon a TNT sheep to chase an opponent, leap gaps and explode after five seconds. It can hurt its owner. |
| `smoke` | Five-second, four-block-radius cloud blinds enemies inside. |
| `fireball` | Throw a slow explosive fireball. |
| `pumpkin` | Direct hit equips a Curse of Binding pumpkin on the target and returns or drops the old helmet. One throw. |
| `atomic_bomb` | Place primed power-16 TNT in a free neighboring cell. Four-second fuse; can hurt the owner and destroys unprotected terrain. |
| `anvil_rain` | Aim within 192 blocks. After a one-second warning, nine anvils fall in a shuffled 3×3 pattern from 24 blocks up. |
| `transmute` | Aim within six blocks to replace a face-connected component with the offhand block or a random one. Releases container contents; skips protected ice and liquids. More than 4096 blocks or an unloaded chunk refuses the cast. |
| `blessing` | 60 s of Fire Resistance, Resistance I, Strength II, Regeneration II and Absorption IV. |
| `gravity` | Reverse only your gravity for three seconds, with a 15-second personal cooldown. |
| `fake_block` | Four visible but non-solid planks within six blocks; extend from fake faces or left-click to remove without drops. Real construction replaces them. |
| `pills` | One random opponent hears phantom movement and combat sounds every 4–6 s for 60 s. No damage, entities or stacking. |
| `decoy` | A mannequin with your appearance explodes once on enemy hit. At most two active per owner for 60 s; no owner damage, fire, block damage or gear drops. |
| `tree` | Grow a large oak at the targeted block without removing existing builds. |
| `aquarium` | Aim at an opponent within 32 blocks to form a breakable 5×5×5 aquarium with two pufferfish for 20 s. |
| `drone` | Shoot reusable guided arrow; steer with mouse for up to eight seconds. Your body remains vulnerable; damage, sneaking or impact ends control. Five-second cooldown. |
| `aura` | Repel opponents within 2.25 blocks for five seconds. |
| `invulnerability` | Ten seconds without damage or knockback, including the ordinary death-height check but not Flood. |
| `metro` | Build a 2×10 rail loop with three rideable carts; requires room ahead. Carts last 60 s; track stays. |
| `puppet` | A one-shot bow controls a hit opponent's movement and camera for five seconds. Both bodies are vulnerable; damage to the controller ends control. |
| `genie` | Write an English item ID on the sign for the nearest registry match. One wish; spaces and `minecraft:` accepted. |
| `infection` | Slime grows through neighboring air for up to 60 s; break it to contain it. One active seed per player, up to 512 blocks per seed. |
| `lightning` | One aimed strike within 48 blocks, for half target max health before absorption, ignoring armor/resistance. Invulnerability protects. |
| `apocalypse` | Aim within 192 blocks to rain 36 temporary zombies over a 6×6 area. They have no loot and can attack the caster. |
| `bunker` | Build a 3×3 obsidian shelter with an iron door and a lever on each side. |
| `bouncer` | Launch upward with ten seconds of fall protection. |
| `air` | Does nothing and stays in your hand. |
| `protractor` | Swap local X and Z around you within 50 blocks, including terrain and players. |
| `pause` | Pause the match for five seconds. |
| `time` | Toggle the arena between noon and midnight. |
| `weather` | Cycle clear weather, rain and thunderstorms. |
| `garden` | Build a garden with irrigated farmland, wheat and carrots. |
| `long_arm` | Hold in offhand for 24 extra blocks of interaction reach, not melee reach. |
| `hat` | Wear to get a bedrock foothold after falling at least five blocks and two seconds of fall protection. One use. |
| `pillar` | Build a pillar 5–8 blocks away at your current foot height. |
| `maze` | Build a 7×7 maze with two exits. |
| `shuffler` | Replace stacks held by living participants with random loot. |
| `agi` | Legendary allied bot: summons a new pillar, receives drops, bridges and uses some items and perks. |
| `super_apple` | Legendary apple granting 20 golden hearts. |
| `dance_floor` | A flashing 4×4 floor that plays Lava Chicken for 20 s. |
| `milk` | Drink to block bad events for the rest of the match. |
| `swapper` | One charge for a 50% chance to swap with a random opponent. |
| `scaffold` | Right-click a solid floor to build a ten-block scaffolding tower. |

Perk charges follow traded or stolen items; reconnecting does not refill them. Shuffle replaces perk items but active effects retain their original deadlines. Temporary terrain is cleaned without overwriting existing construction. Unsupported camera runtimes refuse remote camera actions rather than changing game modes.

## Arenas and sky

Pillars picks a random entry from `games/pillars.yml`, unless forced (e.g. `/pillars start chaos @a lodestone`). Each match uses a disposable copy of `slime_worlds/pillars_<arena>.slime`; template coordinates are preserved. Build an arena in a normal world, then run `/pillars arena import <arena|all> [world] [radius]` (default world `pillars`, radius 64). This copies chunks in range of each configured `x`/`z`; a blank selection becomes a void template. `/pillars arenas` lists templates. See [world services](../../../../../../../../README.md#slime-world-arenas-asp) for ASP setup.

Each match independently chooses fixed day, night or custom sky with equal probability and clear weather. Custom profiles pick one of 64 sky hues and one of four star brightnesses, using registered vanilla environments without a client add-on. Stars retain vanilla color. The sky overrides arena `time` and `advance_time` settings and affects only the match clone.

## Replays

Install compatible [Flashback Server](https://modrinth.com/plugin/flashback-server) to automatically record Pillars participants from countdown through elimination/spectating. Leaving saves a segment and reconnecting starts another. Unrelated players and manually started recordings are not taken over. Without a recorder, Pillars still works.

Files are packet/state replays, not video or omniscient logs. They include each player's received terrain (including Flood visuals) and can contain match messages. Keep them private; there is no automatic retention. A JVM crash can lose the unfinished in-memory segment.

```text
plugins/FlashbackServer/replays/pillars/<match>/<player-uuid>-part1.flashback
```

Set `clips.auto-clip-on-death: false` and `telemetry.enabled: false` in the recorder configuration. No client mod is needed to play; viewing a downloaded replay uses [Flashback](https://modrinth.com/mod/flashback) with Fabric API on a matching Minecraft client. Operators with `flashbackserver.replay` can decode-check a file using `/replay verify pillars/<match>/<player-uuid>-part1.flashback`.

## Commands and checks

Use `/pillars test [mode] [arena]` for unscored practice; `/pillars respawn [player]`, `/pillars time add|remove|set <time>` and `/pillars info` manage or inspect a match. `/pillars config` edits defaults, mode flags and item settings; structured `items.groups` and `items.custom` are edited in `games/pillars.yml`.

Gradle provides `checkPillars*` and `checkSniperDuel`. Native fixtures for [Flood, sky and replays](../../../../../../../../src/checks/native/pillars/) exercise those integrations on Paper.
