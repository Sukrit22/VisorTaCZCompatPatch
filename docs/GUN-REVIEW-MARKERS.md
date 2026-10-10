# Gun support and calibration labels

Gun item tooltips show green/red **B · P · AH** with check/cross marks:

- **B:** the loaded gun/display profile supports VR button controls.
- **P:** that profile also implements physical reload/action handling.
- **AH:** Advanced holding, holsters and carry are available. A green AH with red P does **not** promise physical reloads: use button reload for that gun.

These describe implemented support, not the player's current mode, server acceptance, or headset test results. They are calculated from the same profiles used by gameplay. Adding a review entry cannot enable missing mechanics. A custom pack without an executable profile remains red; a matching external profile participates automatically.

## Mark guns you calibrated

Edit `<instance>/config/visor_tacz/gun-reviews.toml`. The addon creates it from the bundled template on first gun tooltip/reload. Before launching, you can copy [the template](../src/main/resources/gun-reviews.toml) there yourself. Use `/visor_tacz reload_calibration` or the menu's **Reload calibration** after edits. This metadata reload does not require restarting; executable gun-profile TOML still does.

The bundled checklist includes all 54 base gun IDs, initially unverified. Change `calibrated = true` only for the entries you have checked. Add more pack and gun blocks for custom packs.

```toml
schema = 1

[[packs]]
id = "base"
name = "TaCZ base pack"

[[guns]]
id = "tacz:glock_17"
display = "tacz:default"
pack = "base"
name = "Glock 17"
calibrated = true
notes = "Grip, muzzle and interaction zones checked"

[[packs]]
id = "community_pack"
name = "My community gun pack"

[[guns]]
id = "example:custom_pistol"
display = "tacz:default"
pack = "community_pack"
name = "Custom pistol"
calibrated = false
notes = "Muzzle still needs checking"
```

Use the exact TaCZ gun ID and display ID from the calibration JSON key (`gun_id|display_id`). The visible item name is not the lookup key; `name` is a note for you. `pack` links to your `[[packs]]` label. ZIP filenames and namespaces are not reliable pack identifiers: different packs can share a namespace, and filenames change between releases. Assign guns explicitly so those changes cannot silently mark the wrong gun.

Unlisted guns are **unverified**, even if bundled calibration exists. Different displays are separate entries. Saving calibration does not automatically certify it. Invalid/duplicate entries produce a file-error label, retain the last valid metadata in memory, and leave the file untouched for correction.

This file records your review, not numeric calibration. Share it **alongside** the calibration/holster/inspection/physics files. To ship your review list in a release, copy it to `src/main/resources/gun-reviews.toml` before building. Existing users' review files are never overwritten. It is client-only metadata and does not participate in the server profile hash.
