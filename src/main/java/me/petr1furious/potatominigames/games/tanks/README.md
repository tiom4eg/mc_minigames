# World of Tanks

Join with `/play tanks` or the lobby compass. Two balanced teams of up to six tanks fight in the generated Iron Valley arena. No client mod or resource pack is needed.

## Match rules

- One tank, one life. Eliminate the other team or capture its base. A defender in the circle pauses capture, even at 0%. A damaging hit or broken track resets your capture contribution.
- The default six-minute limit ends in a draw. A disconnected player's hull remains in the arena and can be hit; reconnecting restores the tank's current state. Leaving the game forfeits participation.
- Winners receive three points. `/tanks test` starts unscored solo practice. Settings are in `games/tanks.yml` and `/tanks config`.

## Driving and combat

Choose Light, Medium or Heavy before battle using slots 7–9 or `/tanks light`, `/tanks medium`, `/tanks heavy`. Light tanks are fast and benefit from concealment and long view range; mediums balance mobility and firepower; heavies trade traverse speed for bigger guns and thicker armor.

| Input | Action |
| --- | --- |
| W/S, A/D | Drive or reverse, and turn the hull |
| Mouse | Aim the independent turret |
| Slot 1 / 2 | Select AP / HE; right-click to fire, left-click to toggle zoom |
| Slot 3 | Repair tracks and 15% HP, once |
| Slot 4 | Deploy one six-second smoke screen |
| Slot 5 | Right-click to zoom, left-click to fire |
| F5 | Switch to Minecraft's third-person camera |

Zoom uses vanilla dynamic FOV; the client's **FOV Effects** setting controls its strength. In zoom the sidebar is replaced by shell, HP, reload and warnings in the action bar.

AP checks plate angle and can ricochet. HE is a direct-hit shell that trades penetration for reduced damage on armor impacts. Tread hits can disable a track for 3.5 seconds even on a ricochet. After automatic or field repair, tracks cannot break again for two seconds, but the hull still takes damage. The private gun marker previews central impact (green penetration, orange blocked armor, cyan ally) and a 95% dispersion contour; some shots can land outside it. Moving and turning widen dispersion. Muzzle walls, allied tanks and your own depressed barrel obstruct shots.

## Spotting

On the offhand map the arrow is your tank, squares are allies and diamonds are spotted enemies. Cover, concealment and smoke affect spotting. Proximity detection reveals enemies under ten blocks even through walls or smoke, and smoke does not stop shells. Destroyed players retain team spotting rather than receiving all enemy positions. The results credit the scouts who supplied the latest contact when a teammate hits a target they could only see through allied spotting at firing time.

## Checks

The Gradle `checkTanks*` tasks and `checkTankVisual` cover rules, geometry, ballistics, models, radar, scope, audio and session behavior. See the [native fixtures](../../../../../../../../src/checks/native/tanks/README.md) for Paper and client exercises.
