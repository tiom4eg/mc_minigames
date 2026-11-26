# Bingo

Join with `/play bingo [mode]` or the lobby compass. Teams race to obtain the items on the same generated board. Vote for a difficulty with `/play difficulty <level|any>` and optionally pick a team with `/play team <colour>`.

| Mode | Goal |
| --- | --- |
| `default` | Complete a row, column or diagonal of five items. |
| `butfast` | Complete a line on a shorter clock. |
| `collectall` | Collect every required item. |
| `lockout` | Only the first team to collect an item can claim its square. A line wins immediately; if every square is claimed or time expires, the team with the most squares wins. |

The match opens with a cage drop. Right-click the Bingo card to view your board; `/bingo returncard` restores a missing card. The sidebar tracks team progress. Death temporarily puts you in spectator mode; you return with your kit and inventory rather than being eliminated.

## Commands and world

- `/bingo start butfast @a hard` starts a particular mode and difficulty. `/bingo test [mode] [difficulty]` is unscored solo practice; `/bingo stop` ends a session.
- `/bingo config` edits `games/bingo.yml`. The `difficulty-levels` section gives the crafting-cost target and round time per mode for `baby`, `easy`, `medium`, `hard` and `insane`. The item and dependency graph behind board generation is `games/bingo-board-entries.yml` in the plugin data folder.
- Bingo starts at the origin in a fresh seed so vanilla strongholds and the End work as expected. The Nether is created up front; the End is created on the first portal entry. Matches use [throwaway worlds](../../../../../../../../README.md#throwaway-worlds) rather than a saved map.
