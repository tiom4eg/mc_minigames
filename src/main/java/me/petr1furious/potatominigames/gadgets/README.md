# Gadgets

Gadgets are shared by games that enable them. Admins can use `/pm give <gadget>`; games such as [Warfare](../games/warfare/README.md) and [Chase](../games/chase/README.md) can include them in kits.

| ID | Gadget |
| --- | --- |
| `infinite-crossbow` | Infinite Crossbow |
| `oreshnik` | Oreshnik |
| `himars` | HIMARS |
| `locator` | Locator |
| `fpv-drone` | FPV Drone |
| `radar` | Radar |
| `grapple-bow` | Grapple Bow |
| `tracker-compass` | Tracker Compass |

Each has its own section in `gadgets.yml` in the plugin data folder. For example, `/pm config gadgets.himars.cooldown 5` changes the HIMARS cooldown. `allowOutsideGame` defaults to true except for Oreshnik. Gadgets never work in the lobby.
