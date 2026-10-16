> Historical reference. Use [the master checklist and recorded results](TESTING-CURRENT.md) for current testing; do not repeat passed cases from this document.

# Independent dual-wield: 0.14.0-alpha.1

Reported on alpha.2: **DW01 OK, DW02 provisionally OK, DW03 OK**. DW04-DW07 explicitly untested; later checks remain pending. The new alpha.3 checks below are untested. Use a disposable test world with matching client/server builds and keep existing calibration files. Start with two Glocks in different hotbar slots, visibly different ammo counts, and spare ammo in Survival. Enable Independent dual wield under Advanced Handling and assign one slot to each Visor hand.

| ID | Action | Expected |
|---|---|---|
| DW01 | Draw right, then left pistol with Grip near each holster. | Both remain visible and independently held. No inventory copies or selected-slot swaps. |
| DW02 | Fire right only, left only, then both. | Bullets follow the correct controller; only the fired weapon loses ammo. Confirm recoil and sound on each. |
| DW03 | Empty one pistol; Use on that hand while firing the other. | Reload timing and ammo deduction stay with that pistol; the other remains usable. |
| DW04 | Repeat with two identical guns and with a native burst pistol. Holster during a burst. | Same IDs do not share state. Delayed rounds stop after holstering/invalid tracking. |
| DW05 | Hold Trigger while drawing or resuming focus. Release and press again. | No automatic shot before a fresh Trigger release. |
| DW06 | Open menu, remove HMD, disconnect VD, switch flatscreen, leave/rejoin; repeat during reload. | Held input clears; no ghost fire, duplicate ammo, lost gun or stuck input. |
| DW07 | Holster both; use doors/buttons and Trigger world interaction. Disable dual option and use a pickaxe. | Inputs return to Visor. Existing single-gun handling works again. |
| DW08 | Change either hotbar assignment, replace a gun with another of the same ID, or remove it while reloading. | Previous callbacks cannot fire/reload the replacement; no duplicated ammunition. |
| DW09 | Assign two rifles, or a rifle plus pistol. Draw each in turn. | Chest/back mounts are reachable; both long guns remain drawn and fire independently (alpha.3). |
| DW10 | Test left-handed setting, room-scale crouch, turn and walk. | Holsters stay reachable and drawing uses the intended hand. Report position offsets. |
| DW11 | Have a flat player watch both pistols firing and holstering. | Two models, correct hands/origins, no duplicate vanilla gun model. |
| DW12 | Aim through a wall or lose controller tracking while firing. | Invalid muzzle poses stop shooting. |
| DW13 | Disable experimental option; retest existing Glock/M4 physical reload and M700 bolt. | Existing modes retain their prior behavior. |

Charged guns, physical dual reload, support aiming and independent optics are not part of this prototype. Report the test ID, exact gun IDs, loader, handedness, ammo before/after, and client/server logs for failures.

## Automated validation (2026-10-11)

- Forge: 229 tests passed; reobfuscated JAR built. Development dedicated server reached a loaded world with reference-map remapping enabled.
- Fabric: 229 tests passed; remapped JAR built. Production dedicated server used that JAR, unmodified published Visor/TaCZ dependencies, and Fabric Loader 0.17.2; loaded a world, spawned a zombie, and saved/shut down cleanly.
- These checks verify compilation, pure state tests and server startup. They do not verify two-player shooting, client rendering, controllers, headset behavior or gunpack scripts.
- Artifacts are in `build/libs` (Forge) and `fabric/build/libs` (Fabric), named `0.14.0-alpha.1`. They were not installed into a user instance.

## alpha.2 crash fix

The 2026-10-11 20:30 client/server crash was `DualScriptWeaponMixin`: its group allowed at most one invocation, while TaCZ's guard reads `getMainHandItem()` twice (identity and emptiness). Removed that upper bound while retaining the required-match check. This class loads lazily when a gun is equipped; a server reaching a world without a held gun did not cover it. The failure also affected ordinary flatscreen gun selection, even with dual mode disabled.

Switching to flatscreen makes the client dual mode inactive and resets its held/trigger state. The server cancels the separate reload operators on disable, inactive VR, logout, or a 500 ms input timeout. Guns remain in their original inventory slots; the selected main-hand slot becomes the ordinary TaCZ weapon. This does not pick the most recently drawn gun or migrate the offhand gun. Reload cancellation follows native TaCZ behavior, not a rollback of every ammo change already completed. VR-to-flat gameplay remains a manual test.

alpha.2 validation: 229 tests passed on each loader; Forge reobfuscated JAR and Fabric remapped/refmap checks passed. In isolated Forge development and Fabric production servers, a force-loaded, invulnerable zombie held a TaCZ M870 through server ticks, exercising `ModernKineticGunItem.tickHeat` and lazy `ModernKineticGunScriptAPI` transformation. Both remained operational. No user instance or save was changed.

## alpha.3: feedback and holster calibration

Client and server both need alpha.3 (network protocol 30).

- Use the existing status display setting: **GUN** puts a panel at each gun, **WRIST** puts each weapon's panel on its assigned wrist, **HUD** lists both in the normal overlay, **OFF** hides the addon panels. TaCZ's own original HUD remains main-hand-only.
- Ammo/chamber counts and reload/cycle states come from each native server operator. Reload start uses that gunpack's empty/tactical reload clip; manual cycling uses its bolt clip. A short quiet confirmation click marks completion. Each slot owns its sound, stopped on cancellation/reset. This does not yet reproduce animation-keyframe sounds for every custom pack.
- Both long guns may be drawn; drawing one no longer holsters the other. Physical support aiming is not enabled by this change.
- With dual enabled, open **Advanced > Calibrate holster**, then choose **Main** or **Offhand**. Assign the desired gun in Visor first. Each gun/profile and assignment has its own position/rotation entry in `config/visor_tacz-dual-holsters.json`, leaving single-gun calibration unchanged. Fine/coarse controls, live preview, Save/Cancel and Save-to-Close apply. Both mounts share the existing recenter/walking heading.
- Dual physical magazine/slide/cylinder handling, hand transfer and one-handed pump inertia are still pending. Dual currently uses button reload regardless of the selected single-gun handling mode.

| ID | New check | Expected |
|---|---|---|
| DW14 | Give the two guns different ammo counts; fire/reload one, then both. | Each panel shows its own ammo/chamber and reload/cycle state. |
| DW15 | Reload both identical pistols with a small delay; holster one midway. | Each reload has sound; stopping one does not stop the other's clip. No completion cue for the holstered/cancelled reload. |
| DW16 | Change only the offhand holster, Save, edit again and Cancel, leave/rejoin. | Main mount is unchanged; saved offhand position persists and unsaved edits are discarded. |
| DW17 | Equip two Kriss or other long guns; draw and fire both, then holster separately. | Both remain held; independent shots/ammo/reload feedback. |
| DW18 | Try GUN, WRIST, HUD, OFF display settings. | Two independent entries/panels, or none with OFF. |
| DW19 | Recenter facing forward, then look down/sideways; walk and crouch. | Both mounts remain reachable and recenter together. |

alpha.3 build validation: **233 tests passed per loader**; Forge JAR reobfuscation and Fabric JAR remapping/refmap verification passed. The new controller UI, per-hand audio and mount placement require headset verification. Builds were not installed automatically.


## alpha.4: ownership, feedback and Button ADS

Use matching alpha.4 client/server (protocol 31). This remains a button-based dual prototype; physical dual reload is pending.

Changes: per-hand hotbar rebinding, offhand-only gun rendering, per-operator firing interval lookup, independent shot audio, native reload animation sound keyframes, contextual ADS, and an opaque stereo-consistent HUD background. A holstered opposite gun counts as a free hand for the blue support zone. Holstered mining bypasses TaCZ's gun click blocker; normal bare-hand mining restrictions still apply.

Single-gun Button mode can hold Use for two seconds to clear a supported Durability jam, with progress under JAMMED. Release or change slot/focus to cancel. Dual jam clearing is deferred: the verified durability synchronization targets the main hand only. Disable dual mode for this test. Independent dual scope zoom is also pending.

| ID | Test | Expected |
|---|---|---|
| DW20 | Keep right gun drawn; change only left hotbar between guns, tool and empty slot. Repeat reversed. | Unchanged hand stays drawn; its reload is not cancelled. |
| DW21 | Main selected slot empty, offhand Glock; then two shotguns. | Each assigned gun visible and independently drawable. |
| DW22 | Fire shotgun and Glock, then two Glocks simultaneously. | Glock keeps its own firing interval; both shot sounds play. |
| DW23 | Reload Glock, Deagle and CZ75 independently and together; cancel one. | Native keyframe sounds play, cancelled gun stops its own sequence. |
| DW24 | Hold one gun near eye with opposite hand in blue support zone; holster opposite gun, then draw it or select a tool. | Purple + blue zones enable ADS; occupied support hand blocks two-hand ADS. |
| DW25 | Compare both eyes over snow/water using GUN and WRIST HUD. | Dark readable background; consistent text between eyes. |
| DW26 | Holster gun and mine dirt; draw it and repeat; switch to pickaxe. | Holstered gun allows fist-speed mining, drawn gun fires, pickaxe mines normally. |
| BJ01 | Single Button mode supported jam: hold Use, release early, then hold fully. | Progress cancels early; full two-second hold clears jam. |
| BJ02 | During clear, change slot, open menu, lose headset focus; test without durability installed. | No stale clearing/firing; absent optional mod causes no error. |

User reports DW07 passed, but switching back to pickaxe still needs testing. M1014 support-zone and SPAS charging-handle refinements remain pending; this release does not claim to fix them.

alpha.4 validation: 236 tests passed per loader; Forge reobfuscation and Fabric remapping/production-JAR checks passed. Headset audio, stereo rendering, actual mining and input behavior remain manual tests. Builds were not installed automatically.

Forge development and Fabric production servers both reached a world with an M870-equipped entity and exercised lazy gun mixins without a crash. Fabric emitted the native target_minecart missing-data-fixer warning; no addon mixin failure was observed.
