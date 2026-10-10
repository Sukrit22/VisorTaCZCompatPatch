ฃ# Visor TaCZ Compatibility — 0.12.0-alpha.14

Available for **Minecraft 1.20.1, Forge and Fabric**. This update improves gun input handling, adds Physical revolver gestures and a continuous M700 bolt-closing motion, and introduces gun-support and calibration-review labels.

These are changes since Forge alpha.12 / Fabric alpha.13. Automated checks passed; the new headset behavior still needs player testing.

## Gun controls and cursors

- **Holstered Trigger:** Advanced handling now lets Trigger pass through to Visor while the gun is holstered, without also firing the selected gun.
- **Switching to tools:** trigger ownership is cleared when changing gun/slot, and cleanup avoids clearing the shooting key after leaving gun control. This is a candidate fix for the reported inability to hit or mine with a pickaxe; that player's exact failure has not been reproduced or confirmed resolved.
- **World aiming cursors:** the gun-holding hand's world cursor is hidden. The offhand cursor also hides when entering or engaging the physical support zone. Cursors return when the hands are free; GUI/menu cursors remain available. Advanced carry and support transfers are handled per hand.

## Weapon handling

### MK23 slide sounds

Physical and Advanced modes suppress the automatic bolt sounds embedded in MK23 shooting-animation clips. Gunshot audio and deliberate physical-action feedback remain available; Button/flat animation audio is unchanged.

The manual-mode clip was confirmed to contain an automatic bolt sound. The reported auto-mode last-round sound still needs checking with the player's installed gun pack.

### Revolver gestures in Physical mode

Supported swing-out-cylinder revolvers now share these gestures between Physical and Advanced:

- Press the holding hand's **Use**, then move about **2 cm left within 1.2 seconds** to open the cylinder.
- Move about **8 cm right**, then stop deliberately, to close it.
- With the cylinder open and muzzle raised, **shake** to unload. Tilting alone does not unload it.
- Pull the **pink ejection zone** to eject manually.

Lonetrail keeps its existing breech controls rather than adopting cylinder flicks.

**Physical mode automatically recovers live rounds** when unloading; no catching hand is required. Inventory overflow drops native ammunition. Spent cases never become usable ammo. **Advanced mode still requires an empty hand under the cylinder to recover live rounds.** Cosmetic ejections are not pickups.

For comparison, ordinary flat TaCZ reloads retain remaining live ammunition and consume only the amount needed to refill; they do not implement this physical catching workflow.

### M700 forward-and-down bolt motion

An open bolt can now be pushed forward and locked down with one continuous sweep through the bolt-handle area, in Physical or Advanced mode. The forward part chambers once before the downward part locks. A downward motion at the rear does not close an open bolt. Slow threshold crossings are supported, and the manipulating hand must be free of another item.

## Gun-support labels

Gun item tooltips now show **B · P · AH** in green/red, with check/cross marks:

| Label | Meaning |
|---|---|
| B | VR button controls are supported by the loaded gun/display profile. |
| P | Physical reload/action handling is implemented. |
| AH | Advanced holding, holsters and carry are available. |

A green **AH** with red **P** means Advanced holding is available, but reloads still use button controls. These labels describe implemented local profile support, not the current handling mode, server acceptance or completed headset testing.

Custom gun packs participate when they have a matching executable profile. Adding a calibration-review entry alone cannot enable missing mechanics.

## Editable calibration-review list

The new client file is:

```text
<instance>/config/visor_tacz/gun-reviews.toml
```

It is created on the first gun tooltip or calibration reload and includes **54 base gun entries**, initially marked unverified. Change `calibrated = true` for guns you have checked, and optionally add notes. Existing numeric calibration is preserved; “unverified” does not mean the gun has no saved calibration.

Entries match exact **gun ID + display ID**. Pack names are editable labels linked explicitly to each entry, so packs sharing a namespace cannot accidentally share verification status. Custom pack entries can be added without recompiling.

Apply edits with **Reload calibration** in the menu or:

```text
/visor_tacz reload_calibration
```

This file records human verification only. It does not change numeric calibration or the server's executable-profile hash. Saving calibration does not automatically mark a gun verified. Share the review list alongside the actual calibration files.

See [Gun support and calibration labels](GUN-REVIEW-MARKERS.md) for the file format, custom-pack examples and bundling instructions. Executable gun-profile TOML still requires a restart after editing.

## Installation

Replace the previous compatibility-addon JAR with the appropriate loader build:

- Forge: `visor-compat-tacz-1.20.1-0.12.0-alpha.14.jar`
- Fabric: `visor-compat-tacz-1.20.1-fabric-0.12.0-alpha.14.jar`

Use matching alpha.14 addon builds on client and server, each for its loader. Network protocol remains **27**. Keep existing calibration files; no world reset or calibration reset is required by this update.

## Validation and remaining checks

Both Forge and Fabric builds passed **205 automated tests each**. Packaged version metadata and bundled calibration/profile/review resources were checked; Fabric's production-JAR mapping check passed. These checks do not establish headset, QuestCraft or multiplayer gameplay compatibility.

Priority in-game checks:

1. Trigger mining with a pickaxe before and after using a gun, in all three handling modes. If it fails, compare `/visor_tacz off` and record the affected hand/mode.
2. Holstered Trigger and Use passthrough, world-cursor visibility, and GUI interaction.
3. MK23 manual-shot and auto-last-round sounds.
4. Physical revolver gestures and survival ammo totals; compare Advanced catching.
5. M700 forward/down sweep, with and without support transfer.
6. Tooltip badges, custom-pack entries and review-file reloads.

**Remote gun height remains unresolved.** No arbitrary model offset was added. Compare several guns and a vanilla held item from the same observer, recording both clients' Visor versions, body mode and world scale before deciding whether a remote calibration menu is appropriate.

**Intermittent whole-world flicker also remains unresolved.** Reports that restarting the PC helps do not establish the cause.

The [consolidated testing checklist](TESTING-CURRENT.md) records previous results, remaining subcases and the new N01–N06 tests.
