# Party / Вечеринка

Use **Party** in the lobby compass or `/play party <mode>`. Quick Games queues individual rounds; Party Games strings them together. Admins can use `/party start <mode> @a`, `/party test <mode>` for unscored solo practice, or `/party stop`. Timing settings live in `games/party.yml` and apply to the next session.

## Party Games tournament

`/play party party_games` draws six distinct rounds for 2–12 players from the 21-mode default pool. It includes the Quick Games below plus PvP, Sumo, Arms Race, One Shot, TNT Run, Mob Count, Rhino and Miners. PvP and Sumo are simultaneous one-life rounds in the tournament; their standalone modes keep BO3 rules. Eliminated players spectate until the next round.

Each round awards **3/2/1 points** for first/second/third, without extra kill points. Shared places skip occupied positions (`1, 2, 2, 4` → `3, 2, 2, 0`). The final ranking compares points, first places, then second places; exact ties share the title. Intermission and start countdown are four seconds each by default. Mixed Arms Race uses five weapons (diamond sword → iron axe → wooden sword → golden shovel → bow) and a three-minute timer. Mixed Miners uses 25 s preparation, 120 s combat and 60 s finale.

Set `partyGames.modes` to comma-separated eligible mode IDs and `partyGames.rounds` to the preferred count, capped by pool size. **Party → Create a mode** builds a fixed sequence of 1–21 rounds, with repeats allowed; click a numbered round to move/remove it. Start without saving or save a preset under **My modes** / **Other players' modes** and queue with `/play party <name>`. See the shared [preset rules](../pillars/README.md#presets); Party presets are stored in `data/party-presets.yml`.

`/party points party_games` shows the mixed leaderboard; admins can `reset` it. Standalone rounds use `/party points`. Interrupted or test tournaments do not save points. Disconnecting forfeits the current round's awards, even if the player returns in time to play the next round; leaving withdraws them permanently. Late arrivals spectate. With fewer than two participants between rounds, the tournament waits 15 seconds for reconnects before ending without points.

## Quick Games

| Mode ID | How to play |
| --- | --- |
| `hot_potato` | Left- or right-click a nearby player to pass the potato before its fuse runs out. Cut under the Clockworks bridge, climb to the gallery or escape down a stairway. |
| `jump_rope` | Try the practice sweep, then survive 50 scored sweeps. Watch for quick returns, long pauses and mid-sweep speed changes. |
| `drop_tower` | Race through 20 jumps in identical lanes on a new course. White sparks mark your next landing. Sprint-jump from orange pads; brake for the last precision landing. A miss returns you to the checkpoint without changing your view. |
| `chicken_shooter` | Hit targets across ten waves in your own lane. After a stationary opener, targets get smaller and faster and waves get shorter. |
| `less_is_better` | Stand on raised red or blue before time runs out. Other players are hidden until reveal. The smaller occupied side scores; with fewer than three players neutral votes fill out the ballot. |
| `wait_for_it` | Race through the same six tasks from a pool of 24. A completed task immediately opens the next one in your room. First to finish wins; after three minutes unfinished players rank by progress. |
| `color_floor` | A concrete block in hand shows the target color; reach it before other tiles vanish. Jump to safety before falling below the floor. Twelve stages; timers shrink from six to two seconds and the palette grows from six to twelve colors. |
| `anvils` | Dodge ten staggered anvil waves on a 42×42 floor or shelter under slabs. Relocation time shrinks from ten to six seconds, roofs move and become fewer and smaller. Anvil contact eliminates; punches push without damage from `anvils.pvpWave`. |
| `build_memory` | Copy the jury's model from memory on a screened private pad. First two models are small; later ones are larger, rotated or recolored. Study time shrinks from 12 to 5 s; build time grows from 30 to 65 s. Slots 7–9 hold a pickaxe, axe and shears; breaking your own blocks returns them. See scoring below. |
| `tool_race` | Mine exactly 50 blocks in a private shaft with the same shuffled wood/dirt/stone sequence as everyone else. Use iron axe, shovel and pickaxe (slots 1–3); a wrong tool is slower, not fatal. Finish time ranks first; after three minutes unfinished racers rank by progress. |
| `hole_in_the_wall` | Dodge moving walls on Plus, Snake or Donut. The floor shrinks at one and two thirds of the round with three-second warnings; the central Donut pit remains lethal. Walk, jump or crouch through shortcuts; precision routes appear after ten walls. Moving walls can be ridden but side contact pushes. No PvP; falling eliminates. Default two minutes (`holeInTheWall.durationSeconds`). |
| `furry_quest` | One sniper watches 40 animals from a farm watchtower; hiders blend in and complete secret tasks. See below. |
| `parkour_race` | Race through identical vanilla movement and building tasks in private lanes. See [Parkour Sprint](#parkour-sprint). |

**Build Memory:** the guardian highlights missing, wrong and extra blocks. Orientation and upper/lower placement count; sign text and automatic connections do not. The player with most errors leaves, with ties broken by the time of the last edit, then roster order. Perfect stages 1–5 eliminate nobody; from stage 6 the slowest perfect builder leaves. Tool Race, Anvils, Color Floor and Build Memory have fixed clear daytime.

**Furry Quest / Фурри-квест:** hold the spyglass to aim and release to shoot. The hunter's slot-2 item marks an animal on right-click and unmarks on left-click. Hitting livestock scatters nearby animals for three seconds and blinds the hunter for ten seconds; the fourth livestock hit gives hiders the win. Hiders move and look normally from animal eye height. Each gets four distinct tasks, exactly one high-risk, in any order. The sidebar and private markers show destinations; nearby hints explain controls. Low-risk tasks are drinking, walking on six water bowls and foraging. Medium tasks are circling the oak three times, collecting/weaving wool for five seconds per station, catching three fish or swapping the scarecrow's hat. High-risk tasks are ringing the bell, playing the three audible chimes or staying near the purple-outlined double agent. Contact needs 15 seconds within four blocks, pausing outside and resetting after five seconds away. Once surviving hiders finish within the 150-second round, the sniper gets a final ten seconds; any hiders left alive share the win.

## Gartic Phone

`/play party gartic_phone` is standalone for 2–12 players and excluded from Party Games pools. Write a private prompt in chat, then alternate building the latest text and guessing the latest build. Chains rotate one player each turn; only the previous contribution is visible. Play one turn per player (at least three in practice and two-player games), then reveal chains and authors in order. There is no winner or points.

In the private **11×8×11** Creative plot, use **E**, pick-block and an unrestricted hotbar. Press **Sneak + swap hands** (Shift+F) to lock your build. Snapshots retain block states, signs and container contents. Chat answers lock immediately and are limited to 120 characters. Ready players can finish a turn early together. On timeout missing prompts get a starter, unfinished builds are saved, and missing guesses are marked. Leaving withdraws you; reconnect to spectate.

`garticPhone.promptSeconds`, `buildSeconds`, `guessSeconds`, `revealSeconds` default to 30/90/30/6 respectively, with reveal counted per contribution.

## PvP and Sumo

### Round-robin BO3

`pvp` and `sumo` support 2–12 players. Each pair plays once in rotating tours; odd rosters have byes. Duels run one at a time and waiting players spectate. Win two rounds for three tournament points. Draws replay; tied points compare direct meetings, then tied leaders play randomly seeded extra BO3s for the title with no additional league points. Leaving/disconnecting withdraws for the tournament and incomplete matches are forfeits; if both withdraw neither scores. Rejoining only spectates.

PvP draws two distinct classes, swaps kits and sides in round two, and gives both players the same random class in a deciding round. Equipment, health, abilities and the arena reset each round. Five-second countdowns freeze movement, building and combat; deaths pause results for three seconds and same-tick mutual kills draw. After 90 seconds the border shrinks for 30 seconds; if both survive, health plus absorption decides. Set `pvp.roundSeconds`, `pvp.countdownSeconds` and `pvp.intermissionSeconds`.

Admins can run `/party duel <class_A> <class_B>` with exactly two free players. `/party classes` lists IDs 1–15; `-1` draws independently and duplicate classes are allowed. The command author is A (console uses alphabetical order). Chosen kits open the BO3, then swap; round three is still a shared random kit. For example `/party duel 11 14` is Lancer against Thunderer.

Sumo gives a Knockback I stick on a round 19-block platform with no health damage, armor, building or hidden floor. Sprinting hits push farther; bare hands are weaker. A fall loses the round; a same-tick double fall replays it. Starts are eight blocks apart and swap in round two. The arena loses its outer ring after 45 seconds, then one every three seconds; it is restored between fights. The five-second countdown and three-second result pause apply. Set `sumo.roundSeconds`, `sumo.countdownSeconds` and `sumo.intermissionSeconds`.

### One-life variants

`pvp_ffa`, `sumo_ffa` and `pvp_select` support 2–16 players. Everyone starts around a circle. Last survivor wins three points, a simultaneous final elimination draws, and a reconnect does not grant another life. Group PvP rolls classes independently; repeat classes are possible. Fire and frost abilities pick nearby visible opponents, while lightning can hit multiple targets. After the three-second death screen, use spectator controls to follow survivors. Group Sumo spectators move immediately when they fall.

**Choose Kits** (`pvp_select`) uses the same group PvP rules but opens a private kit menu first. Click a class and use the hotbar star to reopen/change it. Choices lock when all are ready or 30 seconds pass; missing players receive a random kit. The ordinary countdown then begins. Duplicate choices are allowed.

PvP overtime starts after 180 seconds and shrinks the border for 30 seconds, then continues to the last survivor without a health tie-break. Group Sumo starts shrinking after 45 seconds. Both are configurable via `pvpFfa.overtimeSeconds` and `sumoFfa.overtimeSeconds`.

### PvP classes and terrain

PvP, group PvP and Arms Race randomly choose Stone Courtyard or Desert Ruins, symmetric protected **49×49** arenas. The same map persists within a session. PvP players can place wool up to five blocks above the floor, but cannot damage original cover or escape via the open roof. Placed blocks and projectiles reset between rounds. Crafting and dropping are disabled.

All classes have 20 HP, unbreakable gear, 32 wool (Engineer 64), shears and 16 steak in slot 9. Eat for vanilla regeneration. Class descriptions appear in chat and the slot-8 ability tooltip. Helpful splashes affect the caster only, harmful ones active opponents only.

| Class | Kit and slot-8 ability |
| --- | --- |
| Guardian | Shield, stone sword and heavy armor. Bastion: Resistance II and self Slowness I for 4 s, 22 s cooldown. |
| Ranger | Power I bow and wooden sword. Retreat: backward dash + Speed I for 4 s, 16 s cooldown. Special arrow stock below. |
| Berserker | Iron axe and backup stone sword; melee heals half a heart every 3 s at most. Blood Fury: costs a heart for Strength I + Speed I for 4 s, 22 s cooldown; cannot kill the caster. |
| Duelist | Sharpness I iron sword and two pearls; backstabs add 2 damage every 5 s at most. Lunge: forward dash + Speed I for 3 s, 14 s cooldown. |
| Mage | Knockback I ritual blade and 14 splash potions. Magic Stock: restores one each harming, poison, slowness and nausea (cap two each), not healing; 24 s cooldown. |
| Engineer | Quick Charge III/Piercing I crossbow, stone sword, 64 wool. Fortify: four absorption hearts for 6 s and up to 16 more wool (cap 64), 24 s cooldown. |
| Aerial Knight | Density I mace, backup sword, eight wind charges and Feather Falling III boots. Wind Leap: jump upward and refill up to four charges (cap eight); no Slow Falling to interfere with mace attacks. 20 s cooldown. |
| Paladin | Iron sword and shield. Purify: heal 1.5 hearts, clear debuffs/fire and add 2 s of Resistance I, 24 s cooldown. |
| Cryomancer | Sharpness I stone sword and 16 snowballs. Snowballs deal no health damage but inflict Slowness IV for 4 s and Weakness I for 3 s on players/mounts, at most once per half second. Frost: refill up to eight snowballs (cap 16), gain Speed I and slow a visible nearby opponent, 20 s cooldown. |
| Pyromancer | Fire Aspect I sword, Flame I bow and permanent Fire Resistance. Flare ignites a visible opponent within eight blocks for 5 s; 20 s cooldown, no target costs nothing. |
| Lancer | Netherite spear, backup stone sword and one saddled, armored horse on Go. Horse cannot be resummoned/healed. Spur boosts the horse or gives Speed I on foot for 3 s, 10 s cooldown. |
| Harpooner | Loyalty III trident (+2 melee damage) and backup sword. Dry Riptide spins for 0.6 s with moderate knockback and up to 6 pre-armor damage once per target; shields block. 20 s cooldown. Wait until the trident returns to activate. |
| Thunderer | Iron axe gives Glowing for 3 s on an unblocked hit, every 8 s at most. Thunderstrike targets a block within 48; preview is gold when ready, red on cooldown. After a 1 s warning, exposed enemies within three blocks take 8→3 damage; the strike cannot harm its owner or change terrain. 3 s cooldown. |
| Demolitionist | Blast Protection II chestplate, stone sword and 12 primed TNT charges. Right-click slot-4 TNT to place; left-click to throw. Carpet Bombing previews a 7×7 zone within 48 blocks and drops nine weaker TNT after a half-second warning; also refills up to four charges (cap 12), 18 s cooldown. |
| Shadow | Diamond sword, light leather and two private Invisibility splashes (up to 20 s each). Invisibility hides armor, not held items; fire/Glowing still reveal you, hits do not cancel it. Hit with the Dark Wand in slot 8 for 2 s of Blindness and 3 s of Slowness, no damage, 20 s cooldown; blocked hits do not spend it. |

Ranger, Engineer and Pyromancer get renewable ordinary arrows. Ranger also starts with ten of each special arrow (finite): damage, slowness, poison, weakness, nausea, spectral, then ordinary in inventory slots 10–16. Move a stack to the offhand to prioritize it. Mage stock is two each of Harming I, Poison I, Slowness I, Nausea and Weakness I, three Healing I, and one Speed I; its refill only affects the four designated offensive stocks.

Demolitionist can have four manual charges live. Place every 0.3 s or throw every second; a successful placement can still occur while throwing is on cooldown. The 1.5-second fuse deals up to 16 damage before armor/cover. Direct contact with an enemy or their horse detonates thrown TNT early for half damage, with no extra direct hit. Airstrike charges have separate tracking and a lower damage cap. No PvP TNT breaks blocks; explosives may hurt their owner.

## Arms Race and One Shot

**Arms Race** (`arms_race`, 2–16) is a ten-minute FFA. A kill with your current weapon advances one stage and heals two hearts. Everyone wears chainmail; arrows refill, but passive regeneration is off. Bare hands, bow melee and earlier-stage projectiles cannot advance or deal damage. Environmental deaths credit a valid last hit within eight seconds if the attacker still has the same stage.

The chain is diamond sword → iron sword → diamond axe → stone sword → iron axe → bow → crossbow → stone axe → wooden sword → wooden axe → golden sword → **golden shovel**, whose kill wins. Timeout ranks by kills, then fewer deaths, then earliest arrival at the current stage. No kills is a draw. Standalone scoring is one point per qualifying kill plus three for the winner. Settings: `armsRace.durationSeconds`, `countdownSeconds`, `respawnSeconds`, `spawnProtectionSeconds` under `armsRace`.

**One Shot** (`one_shot`, 2–16) uses Desert Ruins, a wooden sword, a bow and one arrow, no armor. Any bow hit is lethal even partially charged. Kills (including sword kills) grant one arrow. Missed arrows cannot be recovered; if empty for ten seconds one comes back. Picking up or earning an arrow cancels refill. An arrow or shield pickup appears every six seconds by default, at most three at once, and lasts 30 seconds. A shield blocks one damage event from any side and cannot stack; arrows stack to 64. Full players leave the pickup for others. Placements compare kills only, with 3/2/1 points. Default two minutes; set `oneShot.durationSeconds` and `oneShot.bonusSeconds`.

Both modes respawn after at least three seconds at a spawn away from opponents, with two seconds of protection that ends when attacking. Arms Race keeps the weapon stage; One Shot starts each life with one arrow and no shield. Old-life projectiles are removed. Reconnects preserve progress/statistics and use the respawn timer; late joiners spectate. A lone remaining player triggers a 15-second reconnect window outside solo practice. Arena editing, item drops and crafting are off.

## TNT Run, Mob Count and Rhino

All three are one-life games for 2–16 players with a shared five-second `survival.countdownSeconds`. Health damage, PvP, building and crafting are off. Falling or leaving eliminates you; reconnects spectate. The last survivor wins and simultaneous final falls draw. At a time or round limit, surviving players share victory, three points each. Solo practice lasts until falling or reaching the limit.

- **TNT Run (`tnt_run`):** run over three circular TNT layers, six blocks apart. A stepped-on tile turns red and vanishes after half a second; standing still does not refresh it. Every 30 s the collapse delay shortens to a minimum of three ticks. There is no hidden support beneath the last layer. Set `tntRun.durationSeconds` (default 180) and `tntRun.collapseTicks`.
- **Mob Count / Пересчёт (`mob_count`):** count the harmless mobs by species. After they vanish, the number prompt asks which species appeared that many times. From the hub, run to the corresponding labeled sector; the other sectors, separators and hub fall away. Starts at four species/ten mobs; grows to five species/up to 60 mobs. Count time goes from ten to six seconds, with a three-second hub freeze and ten seconds to answer. Set `mobCount.countSeconds`, `answerSeconds` and `rounds` (default twelve) under `mobCount`.
- **Rhino / Носорожий переполох (`rhino`):** glowing ravagers commit to a straight charge that knocks you back. Two start, up to six join as charges accelerate every 45 s. After 45 s the floor shrinks a ring every 15 s to radius five; new holes open every 45 s while the central refuge remains solid. Pick up one orange mine bottle at a time and right-click to throw. Its fuse starts at spawn, defaults to eight seconds, and never resets on pickup, throw, drop or bounce. Explosions push but do not hurt or break blocks. Set `rhino.durationSeconds` (default 240), `ravagers`, `mineFuseSeconds` and `mineSpawnSeconds` under `rhino`.

## Parkour Sprint

`parkour_race` draws seven or eight distinct tasks from 24, with the same order for everyone. Sealed parallel lanes are identical; you can see but cannot interact with other racers. The default four-minute limit is `parkourRace.durationSeconds` (60–600 s). Land grounded, not gliding or riding, on the next quartz checkpoint. Clock in slot 9: left-click for a hint, right-click to reset the current attempt. A private green outline, direction arrow, distance and height difference show the next checkpoint.

Every checkpoint grants a fresh exclusive kit and clears previous building, water, items, vehicles, effects and projectiles. Clock reset restores the current kit and clears that attempt without resetting the race timer. Native damage applies; a lethal or out-of-bounds attempt resets before it can claim the destination. Checkpoints cannot be skipped with Elytra. PvP, drops and crafting are off. Finish order comes first; after timeout, rank unfinished racers by checkpoint and progress in section, sharing exact ties. Leaving withdraws; reconnecting only spectates.

| Task | Kit and route |
| --- | --- |
| Bridge | 22 wool and shears; cross an 18-block gap. |
| Wind ascent | 36 wool and eight wind charges; climb 28 blocks at the wall. |
| Water drop | One bucket for a 44-block drop. |
| Midair chest clutch | Jump to the chest six blocks past the edge, take its bucket (hover and press hotbar 1), then clutch the 56-block fall. Place water in the bottom three rows only. |
| Scaffolding tower | 30 scaffolding for a 24-block climb. |
| Ladder wall | 24 ladders for a 20-block climb. |
| Slime rebound | Four slime blocks; place on the bare landing pad 36 blocks down and bounce to the next island. |
| Pearl throw | Three pearls for a 32-block gap. |
| Swim tunnel | One bucket and a dry one-block-high tunnel with alternating openings. Place and move the water source. |
| Ladder clutch | Eight ladders for a last-second catch on the three-block bottom support. |
| Cobweb clutch | Three cobwebs and shears for a 28-block drop. |
| Elytra flight | Elytra and one Flight Duration 2 rocket; gain altitude first, then glide to the landing. |
| Ladder traverse | 12 wool + 12 ladders to cross 20 blocks from narrow ledges. Alternate blocks and side-mounted ladders. |
| Block staircase | 32 wool and shears; bridge up and forward by 14 blocks. |
| Flower-pot staircase | 16 wool and 18 pots. Place a pot on each wool block and attach the next wool to its forward face, one block up and forward. Build only in the central marked three columns; broken supports do not drop. |
| Low-ceiling hops | Cross short holes under a two-block ceiling, without items. |
| Riptide launch | Riptide III trident and a starting pool. |
| Water ascent | Bucket and a wall for a 20-block climb. |
| Powder-snow clutch | Powder snow bucket for a 40-block drop. |
| Wind-charge clutch | Three wind charges for a 44-block drop; burst one into the floor just before landing. |
| Honey-wall slide | Catch the six-block-high honey side near the bottom of the 36-block drop. The solid cap is not a safe landing. |
| Boat slalom | Oak boat, water and three offset gates. Dismount to claim the checkpoint. |
| Ice-boat chicane | Oak boat and an 80-block packed-ice road with long curves and guardrails; walking is legal. |
| Wind-charge hops | Four wind charges and a raised middle ledge. |

## Magic Battle / Магическая битва

`magic_battle` is standalone for 2–12 players, outside the Party Games pool. One life, no native melee or vanilla regeneration. Last survivor wins; podium awards are 3/2/1. Start with 64 wool and up to two chosen basic wands. You have 30 seconds to choose and confirm one or two of Firebolt, Ice Lance, Gale, Stone Shot, Mend and Rift. The slot-5 book reopens selection until lock. Timeout keeps a partial choice, or gives Firebolt + Ice Lance if empty. Players stay frozen until the combat countdown ends.

Ordinary wands cast one fixed spell on right-click. Glinting wands have two from the same school: right-click first, left-click second, sneak to preview the second aimed spell. Cooldowns are per player and spell, even across wands; item tooltips show both costs/timers. Mana starts at 100 and regenerates 7/s, plus 5/s near the central lighthouse. Aimed spells show a private preview and a public warning after committing. Projectiles generally travel at 3.2 blocks/tick (Fireball and Frozen Orb at 2), with 96-block maximum range.

Each chest offers two random wands and 16 wool, shared among all viewers. Any spell can appear; each wand has a 25% chance to roll a second spell. Chests refresh when reopened after 45 seconds; they do not restore mana. Claim items by clicking; if full, they remain. Sneak + right-click to cast or place wool without opening a chest. Wands can be moved, dropped and looted in combat.

Cover blocks projectiles and offensive areas. Mine terrain/builds with a non-wand item; Fireball, Meteor, Earthquake and Erosion can also break terrain. Temporary structures expire; the arena is disposable. Death gives a three-second death screen, then spectating; reconnecting does not give a second life. After 180 s the border shrinks toward the lighthouse over 30 s and increasing damage checks camping. Set `magicBattle.roundSeconds`, `countdownSeconds` and `chestRechargeSeconds` under `magicBattle`.

### Spells

Damage and healing are in HP (two per heart).

| School | Spell | Mana / cooldown | Effect |
| --- | --- | --- | --- |
| Fire | Firebolt | 16 / 1.8 s | 6 damage and 3 s burning. |
| Fire | Fireball | 32 / 5 s | 10→4 damage in radius 3, 4 s burning and terrain damage. |
| Fire | Meteor | 50 / 12 s | 1.2 s warning, 14→4 damage in radius 4.5. |
| Fire | Flame Cone | 26 / 4 s | Visible cone up to 8 blocks: 7 damage and 4 s burning. |
| Fire | Cinder Rain | 38 / 9 s | 1 s warning, radius-4 zone up to 48 blocks away for 5 s; 3 damage/s and fire. |
| Fire | Phoenix Step | 24 / 6 s | Dash and leave a 3 s flame trail, 3 damage/s and fire; no fall immunity. |
| Fire | Combustion | 30 / 7 s | Consume burning on visible enemies within 10 blocks for 10 instant damage. |
| Ice | Ice Lance | 14 / 2 s | 5 damage and Slowness II for 3 s. |
| Ice | Frost Nova | 30 / 6 s | Visible foes within six blocks take 6 damage and Slowness III for 2 s. |
| Ice | Ice Wall | 25 / 7 s | Temporary 5×3 wall within 12 blocks for 8 s; no suffocation. |
| Ice | Blizzard | 36 / 9 s | 0.7 s warning, radius-4 blizzard within 48 for 5 s; 2 damage/s and Slowness II. |
| Ice | Frozen Orb | 28 / 5 s | Reflectable explosion: 6→3 damage in radius 3.5 and Slowness II for 4 s. |
| Ice | Ice Armor | 26 / 10 s | Six temporary absorption HP for 5 s. |
| Ice | Shatter | 28 / 6 s | Cone up to 9 blocks: 11 damage if slowed and consumes Slowness, otherwise 4. |
| Storm | Gale | 18 / 3.5 s | 4 damage and strong knockback. |
| Storm | Wind Step | 20 / 5 s | Dash and lift, usable in midair. |
| Storm | Thunderstrike | 38 / 6 s | 0.8 s warning, 10→4 damage in radius 3, 4 s Glowing. |
| Storm | Chain Lightning | 36 / 8 s | Aim within 36, up to two jumps within eight blocks each, 8/6/4 damage. Cover blocks. |
| Storm | Updraft | 28 / 9 s | 0.7 s warning, radius 3 within 48 for 4 s; lift everyone and deal 1 damage/s to foes. |
| Storm | Dispel | 30 / 9 s | Remove visible enemy spells/zones within seven blocks, push nearby enemies and deal 2 damage. |
| Storm | Skyhook | 22 / 6 s | Launch toward a block within 32; no wall bypass or fall immunity. |
| Earth | Stone Shot | 14 / 2 s | Accurate projectile for 7 damage. |
| Earth | Earth Bridge | 25 / 6 s | Up to 12 planks ahead, lasting 20 s. |
| Earth | Earthquake | 40 / 8 s | 0.7 s warning; 8 damage and launch in radius 5. |
| Earth | Stone Dome | 34 / 12 s | Breakable 5×5 roofed shelter for 5 s; skips occupied blocks. |
| Earth | Fault Line | 30 / 7 s | Narrow ground wave up to 20 blocks, 8 damage and launch; stops at cover. |
| Earth | Erosion | 24 / 7 s | 0.7 s warning, then break blocks in radius 4 up to 48 blocks away; no damage. |
| Earth | Stone Pillar | 22 / 6 s | Lift yourself three blocks for 8 s; needs clear space. |
| Life | Mend | 24 / 7 s | Heal 6 HP; full health spends nothing. |
| Life | Aegis | 28 / 8 s | Reflect projectiles once each and halve other damage for 3 s. |
| Life | Sanctuary | 40 / 18 s | Radius-4 zone for 6 s; caster gains 1 HP and 8 mana/s while inside. |
| Life | Purify | 18 / 6 s | Clear a harmful effect, fire or freezing and heal 2 HP; needs something to cleanse. |
| Life | Second Wind | 55 / 30 s | Next lethal hit within 8 s leaves 6 HP and extinguishes you; no void protection. |
| Life | Life Tether | 24 / 6 s | Reflectable projectile deals 5 damage and heals caster 2 HP on a damaging hit. |
| Life | Mana Channel | 8 / 18 s | At ≤65 mana, stand still without casting for 2 s to regain 48. Damage or motion interrupts without refund. |
| Void | Rift Bolt | 20 / 3 s | 7 damage, drain up to 8 mana and take half for yourself. |
| Void | Blink | 26 / 6 s | Up to ten blocks along gaze, respecting walls, bodies and border. |
| Void | Gravity Well | 45 / 12 s | 0.7 s warning; pull visible foes within five blocks and deal 2 damage/s for 3 s. |
| Void | Silence | 28 / 9 s | Reflectable projectile deals 3 damage and blocks casting for 2 s, not movement or building. |
| Void | Exchange | 42 / 14 s | Swap with an enemy within 24 blocks; both must be grounded, clear and inside the border. |
| Void | Rewind | 30 / 12 s | Return to position three seconds ago, not health, mana or cooldowns. Needs an unoccupied in-bounds destination. |
| Void | Dark Veil | 32 / 10 s | 5 s invisibility, held items visible; leave a radius-5 veil that briefly blinds visible enemies inside. |

### Lighthouse map setup and attribution

**Lighthouse (SW-Lighthouse)** was built by **JangoMango**, published by **NetherGames Network**, and is licensed [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). Obtain the [pinned Bedrock world](https://github.com/NetherGamesMC/assets/tree/5cfda13ef2873c3a048d4a665fe100d1d24542b1/Skywars/arenas/SW-Lighthouse) and its spawn metadata from [`Skywars/arenas.yml`](https://github.com/NetherGamesMC/assets/blob/5cfda13ef2873c3a048d4a665fe100d1d24542b1/Skywars/arenas.yml) at the same revision. Keep attribution and identify the conversion when redistributing it; see [NOTICE](../../../../../../../../NOTICE). The converted terrain uses Java block states and coordinates, with twelve original island spawn columns and chest positions; it does not import source entities, chest contents or executable code.

The map is not bundled or downloaded at runtime. Place the conversion at `maps/magic/lighthouse.bin.gz` under the plugin data folder. Put the Bedrock world's `db/` and the pinned `arenas.yml` side by side in a local source directory, then run the [converter](../../../../../../../../src/checks/tools/import_magic_lighthouse.py) from the repository root:

```sh
data_dir=plugins/PotatoMinigames
python3.12 -m venv /tmp/magic-map-converter
/tmp/magic-map-converter/bin/pip install amulet-leveldb==1.0.7 PyYAML==6.0.3
/tmp/magic-map-converter/bin/python src/checks/tools/import_magic_lighthouse.py /path/to/lighthouse-source "$data_dir/maps/magic/lighthouse.bin.gz"
```

The plugin constructs a fresh match copy in slices; no client pack or WorldEdit is needed.

## Miners / Битва шахтёров

`miners` supports 2–12 players. Start in a private 16×16×16 stone box with the same ore quotas as everyone else, but a different layout. You get an Efficiency III iron pickaxe, stone sword, leather armor, 64 cobblestone, Night Vision and Haste I, but no food. After a frozen countdown, mine privately for 25 seconds. Then the protected partitions become mineable diorite and PvP begins. Use the tracking compass to find the nearest survivor.

Equipment upgrades replace only a weaker item of the same type, wherever it was moved. Consumables and ammo drop for pickup. Ordinary/good/rare rolls are 70/25/5%; armor and combat enchants reach II, mining Efficiency V. There is no guaranteed upgrade from each ore.

| Ore | Count per box | Rewards |
| --- | ---: | --- |
| Coal | 80 | Food or upgraded pickaxe. |
| Iron | 64 | Armor; 6% chance of a shield. |
| Diamond | 24 | Sword/axe/bow/crossbow upgrades; 1/60 chance of a Density mace with four wind charges. Ranged finds include regular and spectral ammo. |
| Lapis | 24 | Combat splashes, regeneration/fire resistance and rare strength/absorption. Detection Potion (2.5%) reveals all other survivors for 5 s after preparation. |
| Redstone | 32 | TNT, pearls, wind charges, cobwebs, Gust Feathers (dash) or Leap Charms (upward launch). Right-click charms in combat; shared one-second cooldown, no fall immunity. |
| Emerald | 8 | 10% chance of one Totem of Undying (hold it to revive); otherwise good/rare rewards or a golden apple. |
| Gold | 16 | +1 (80%) or +2 (20%) max hearts, capped at fourteen total. Only new hearts are filled. |
| Lapis block | 8 | One random finite mining perk. |

| Mining perk | Use |
| --- | --- |
| Drill | Mine normally for a 3×3 plane perpendicular to aim; three completed breaks. |
| Tunnel Borer | Right-click to dig ten cells along the aimed line. |
| Ore Vision | Private ore outlines within eight blocks for three seconds. |
| Lucky Vein | Next three ordinary ore rolls use 50/40/10 odds. Gold and perk blocks do not spend it. |
| Sifter | Next twenty natural stone blocks each have a 15% consumable chance. |
| Mining Bomb | Right-click placement mines a three-block-radius sphere. In combat it can damage any player within four blocks, including the owner. |
| Geological Audit | For twenty seconds, count ore within five blocks, including neighbors. |

Perks except the Drill are main-hand right-click items. Invalid targets or attempts to activate an already active perk do not spend a charge. Walls stop mining; an ore only pays once. In combat, right-click TNT to place or left-click to throw, with a 2.5-second fuse, 0.3 s placement and 2 s throw cooldowns, up to four live charges. TNT mines editable terrain and uses the same loot rules. The arena shell is protected and placed cobblestone does not pay loot. Mining cobblestone is capped at 128 carried blocks.

After three minutes of combat, survivors glow and the zone shrinks for two minutes to 4×4. The translucent walls do not block mining or movement; outside damage starts at 2 HP/s and increases after the shrink. Nametags are hidden before the finale; Detection Potions and spectral hits temporarily reveal opponents. Last survivor gets three points; simultaneous final deaths draw. Death shows the killer and a three-second screen; reconnecting only spectates. Set `miners.preparationSeconds`, `combatSeconds` and `finaleSeconds` under `miners`.

## Checks

Run the affected Gradle `checkParty*` / mode-specific check. See [Paper fixtures for Parkour and PvP](../../../../../../../../src/checks/native/parkour/README.md) and MockBukkit fixtures for [One Shot](../../../../../../../../src/checks/mock/one-shot/README.md) and the [Party menu](../../../../../../../../src/checks/mock/party-menu/README.md).
