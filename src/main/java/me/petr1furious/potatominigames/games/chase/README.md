# Chase

Join with `/play chase` or the lobby compass. Hunters pursue runners across freshly generated terrain. Each possible set of hunters gets one round, in random order. The default is one hunter, a 100-block-wide arena and a round timer of 60 seconds plus 15 seconds per player.

## Playing

Hunters and runners start at opposite corners. Hunters receive diamond tools, building blocks, steak and the tracking compass in slot 9. The default extra gadgets are Radar and Locator. Runners share a round's random pair drawn from a wooden pickaxe, wooden axe, stone shovel and 16 cobblestone; they also receive steak.

A caught runner spectates until the next round. At the end of each round, each hunter scores for every runner caught and each surviving runner scores once per hunter. The highest score after all rounds wins. Players can dig and build, but staying too far above or below the local ground first makes a player glow, then deals damage.

## Commands and settings

- `/chase start [players]` starts a match; `/chase test` lets an admin hunt alone until the round timer expires without awarding points. `/chase stop` ends the match.
- `/chase config` edits `games/chase.yml`: `hunterCount`, `borderSize`, `roundLengthSeconds`, `roundLengthPerPlayerSeconds`, `freezeSeconds`, `intermissionSeconds`, `allowedDepth`, `allowedHeight`, `glowAfterSeconds`, `damageAfterSeconds` and `hunterGadgets` (comma-separated [gadget IDs](../../gadgets/README.md)).
- Chase uses [throwaway worlds](../../../../../../../../README.md#throwaway-worlds), not a template arena.
