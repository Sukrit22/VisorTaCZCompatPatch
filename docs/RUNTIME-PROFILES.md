# Runtime TOML gun profiles

Alpha.9 source uses TOML as the only gun-definition catalog. There is no Legacy backend or menu switch. The bundled catalog contains all 54 base guns: 20 physical profiles and 34 button-only profiles. Button / Physical / Advanced remains a separate handling preference. Existing calibration JSON remains an additive user overlay; its values are not reset.

The server independently loads profiles and checks the client's file digest. Different or invalid files pause VR compatibility with a diagnostic, rather than silently selecting different mechanisms. Flat observers need matching files for custom remote rendering; otherwise they see native rendering. Restart both sides after editing profiles. Network protocol is 25; client and server need the same addon release.

TOML makes parts, geometry, sounds and cosmetic motion editable without compiling. Validation and keeping server/client files identical are the main costs. Java still implements the tested state machines and ammo operations: arbitrary new mechanisms require code. Legacy offered a comparison baseline but duplicated defaults and allowed stack-replacement races; it is removed.

## Author without recompiling

Bundled file: `src/main/resources/gun-profiles.toml` (also `/gun-profiles.toml` inside the JAR).

1. Copy a complete `[[guns]]` block, with all its following tables, into `<instance>/config/visor_tacz/profiles/my-guns.toml`. Start the file with `schema = 1`. Create the directory if needed.
2. Edit the gun ID, original model-pixel grip/muzzle geometry, part node aliases and six interaction positions. Keep the supported mechanism template and compatible variant. SCAR-L/H are examples of a conventional new closed-bolt magazine gun. Glock is the simplest migrated pistol example.
3. Install the same TOML files, with the same filenames and contents, in the server's config directory and on clients. Restart Minecraft and server after editing. This phase loads files once; the existing Reload calibration command reloads calibration JSON, not TOML.
4. TOML loads automatically. Watch the log for validation failures. Test native ammo counts and interrupted reload recovery before treating a new entry as supported.

Files ending `.toml` load in filename order, after bundled profiles; later entries override the same ID. Each file is limited to 256 KiB and 128 guns. A malformed file disables the catalog atomically and pauses compatibility; native TaCZ remains available. No partial broken set is installed.

## Implemented format

- `id`, `display = "tacz:default"`, `model = "tacz_resolved_display"` reuse TaCZ's loaded gun model. Alternate display geometry is not yet supported.
- `mechanism` selects a tested engine: `closed_bolt_magazine`, `pump`, `lifted_bolt`, `locking_charging_handle`, `cylinder`, `breech`, or `button`. `variant` preserves existing asset-specific behavior. New conventional rifles use `MAGAZINE`; this does not make unusual scripted ammo behavior compatible.
- `ammo_adapter = "native_rounds"` uses existing inventory/ammo reservation logic. It does not implement tracked individual magazines or one-item-fills-many-charge guns.
- `geometry.scale`, `grip_pixels`, `muzzle_pixels` are TaCZ model inputs; `support_distance` is in metres.
- `parts.magazine.node`, `parts.action.node`, optional `parts.bolt.node` are names in the native model hierarchy. An empty bolt node means there is no separately animated bolt. Action and bolt must not target the same node.
- `joints.action.kind = "template"`, `frame = "gun"` preserve the tested mechanism's motion and travel. Arbitrary axes, limits, return modes, custom bindings and state/pose scripts are rejected in this subset.
- `zones.magazine/rack/support/release/selector/port.position` are unscaled gun-local metres, +X right, +Y up, +Z rearward. Existing per-gun calibration offsets and gun scale apply on top.
- Zone dimensions, visible colors/hints and the user's fitted grip/holster/inspection values remain in their existing calibration/hint files. TOML zone sizes are deliberately rejected rather than silently ignored.

The richer TOML files in `experiments/profile-v1/examples` remain design examples and are not accepted by this first runtime subset. Persistent poses, ammunition changes and endpoint handling are still implemented by the shared, tested Java mechanisms. Pistol asset/sound mappings now also live in `[guns.pistol]`; there is no Java per-gun fallback. This is not a general animation scripting engine.

## SCAR-L / SCAR-H starting support

Both use TaCZ's closed-bolt, native-round magazine reload contract. Their own `bolt` and `magazine` nodes are bound; the bolt is moved only once. Magazine/rack/port positions are derived from their source model pivots plus a side offset for acquiring the charging handle. They have support aiming, physical magazine reload, racking, bolt release and existing jam handling. Physical sounds reuse their actual TaCZ SCAR clips. Fit, scope alignment, controller reach and attachments still need headset testing.

## Cosmetic and input metadata

- `[guns.pistol]`: cylinder/selector flags, ammo preview ID, cylinder-open angle, and native handling sound names. Copy the complete block from a related gun. Runtime ammo still comes from TaCZ.
- `[guns.visual]`: `shot_node` and `shot_travel` (metres at 100% gun scale) select a moving slide/bolt. Motion is cosmetic and never chambers another round. Physical racking, jams and locked-open actions take priority.
- Cylinder visuals use `cylinder`, `hammer`, `hammer_degrees`, ordered `rounds`, optional corresponding `heads`, optional `loader` and `hidden` node arrays. Empty loader means one-round loading only. Model 943 has no native speedloader mesh; Taurus 500 and Rhino do.
- `manual_cycle_modes = ["SEMI"]` on the MK23 requires a physical stroke after firing in that mode. The native next-round count is preserved; the gesture releases a compatibility latch. Other modes cycle automatically. Switching to Button/flat removes this latch.
- Prefire cylinder/hammer movement currently reads the local player's native TaCZ charge progress. Remote shot pulses work, but remote prefiring charge motion is not yet transmitted.
- Visible cylinder rounds use native live count plus saved spent count. They are aggregate visuals, not individually tracked chamber identities. The Rhino mesh does not expose separate projectile heads for its in-cylinder rounds.

## Focused tests

See the alpha.9 section of [the consolidated checklist](TESTING-CURRENT.md). For a custom profile, copy Glock's complete block, edit the rack zone by 1 cm, install identical files on both sides and restart. Confirm acquisition and guide move together. Test mismatched/invalid files on a spare instance: expect a message and paused compatibility, with no Java fallback. Restore matching files and restart.


## TaCZ 1.2 audited profiles (0.14.2-alpha.2)

`joints.action.bolt_ratio` optionally sets the signed displacement of a separate bolt relative to a manual action stroke (default `1`, range -4 to 4). Child bolts inherit parent movement and do not receive a second displacement. It does not change server ammo or the gesture threshold. AA-12 uses `charge_handle` plus a reverse-moving bolt indicator.

`visual.shot_travel` is now signed, bounded to ±0.3 metres at the profile model scale. `visual.shot_followers = [{node = "charge_handle", travel = 0.01875}]` gives a separate part its own shot excursion. Do not list a parent and its child: inherited translation would double the motion. `visual.shot_modes = ["BURST"]` limits the visual cycle to native TaCZ modes; SPAS uses BURST for self-loading and SEMI for pump mode. Missing new fields preserve previous single-part behavior.

Optional `[guns.sounds]` contains full resource IDs for `mag_out`, `mag_in`, `action_back`, `action_close`, and `eject`. These are short physical-action cues, not entire reload tracks. An absent section preserves legacy selection. With a section present, omitted/empty cues stay silent. Shared pack folders are allowed (e.g. AK74M uses AK47 clips). No resources are copied into the addon.

An absent physical part can use `node = ""` for button profiles or a non-detachable magazine mechanism. Physical action nodes and detachable-magazine nodes remain mandatory. Correct part names in a button profile do **not** enable physical handling; the mechanism still controls capability.

Audit the actual pack without changing it or personal calibration:

```powershell
python scripts/audit-gun-profiles.py path/to/tacz_default_gun.zip
```

See [the review and test notes](PROFILE-REVIEW-1.2.md). Config overrides in `config/visor_tacz/profiles/*.toml` replace the entire matching gun entry and still take precedence over bundled fixes. Merge your customized entry explicitly; do not delete calibration JSON. Restart after changing descriptor TOML.
