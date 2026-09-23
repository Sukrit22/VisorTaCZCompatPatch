# 0.12.0-alpha.2 — holster calibration, smoother tosses and input recovery

Protocol 20 (unchanged). Install matching client/server builds. Advanced Handling remains opt-in. This build does not implement cross-hand inventory-slot transfer or offhand firing; the alpha.1 limitations otherwise remain.

## Changes

- **Pistol holster:** forward of the hip, with a 30-degree forward cant from vertical. Looking sideways no longer rotates the holster around the player. Heading is captured once, follows VR stick/snap rotation, and can be recentered. Physical body turns without tracked torso orientation require recentering; we no longer treat a head turn as a torso turn.
- **Long-gun holster, including P90:** handle lower on the chest and farther forward; muzzle points diagonally down at 45 degrees. The right side of the gun faces forward and its left side faces the player. This is a common starting pose, not a separately measured fit for every gun.
- **Per-gun holster calibration:** Advanced Handling → **Calibrate holster**. Adjust side, height, depth, pitch, yaw and roll, preview live, then Save or Cancel. Translation steps are 1 cm; rotation steps are 5 degrees. Face forward before **Recenter**. `/visor_tacz recenter_holster` also works.
- **Persistence:** saved values live in `config/visor_tacz-holsters.json`, keyed by full gun/display ID. Share that file for importing holster settings. Reload through the existing Reload calibration menu/command. Bad files are not rewritten and failed parsing does not replace that file's current values. A preview must be saved/canceled before reload.
- **75% default gun size:** all 54 bundled profiles use 0.75. Mounted interaction boxes follow the change when scale linking is enabled; pouch dimensions remain independent. Existing custom offsets are retained in the original seven bundled profiles.
- **Pistol copies:** M1911, P320, M9A4, B93R, CZ75 and HK MK23 copy Glock's full calibration. Deagle Golden and Timeless 50 copy Deagle. Cylinder/breech pistols do not receive Glock/Deagle handling offsets. Their gun scale still defaults to 75%. Copied gun-specific zones/muzzle offsets are starting values and need in-game checking.
- **Toss rendering:** analytical motion is evaluated between 20 Hz physics ticks, including rotation, instead of displaying only each tick's pose. Catch grace is also polled per render frame; catch proximity tests the swept path to the displayed position. Physics/collision commits remain tick-based. This smooths the prop; it does not increase Minecraft's actual frame rate or change remote network update frequency.
- **Inspection:** cosmetic slide travel increased from 12 to 22 mm. It still does not consume, feed or eject ammunition.
- **Reconnect/menu input:** clear addon-owned button flags, pending catch radials and Visor hotbar task state on session focus/activity changes and logout. Screen input bypasses gun handlers, including release events. `/visor_tacz reset_input` provides manual recovery. This fixes identified stale-state paths; the reported Virtual Desktop reconnection symptom still needs headset verification.

## Applying the new defaults in an existing instance

Saved `visor_tacz-calibration.json` entries take precedence. They are not silently overwritten by this update. To test the new 75% and copied pistol defaults for an already-saved gun, select it and use Advanced Handling → **Apply bundled calibration to this gun**. That intentionally replaces the selected gun's saved calibration; other gun entries are retained. Keep a copy of your file if you want to compare the old profile.

Holster defaults apply automatically when that gun has no entry in `visor_tacz-holsters.json`.

## Focused retests — pending headset verification

| ID | Test | Expected |
| --- | --- | --- |
| AH20 | Select Glock, face forward, Recenter; look sideways/down | Holster remains ahead of the hip; 30-degree forward cant; no orbit following gaze |
| AH21 | Select P90 and apply new bundled calibration | 75% model, lower chest mount, muzzle diagonally down 45 degrees, stock/handle clear of the face |
| AH22 | Change all six holster values; save; change gun; reload/restart | Per-gun values persist, another gun stays unchanged; Cancel does not save preview |
| AH23 | Toss/catch at high headset refresh; repeat AH16–AH19 | Smooth inter-tick motion and responsive catch grace; no phantom hotbar selection |
| AH24 | Pistol inspection, then Grip while Use held | More visible partial slide movement; no ammo change, no accidental firing or magazine removal |
| AH25 | Close Virtual Desktop while holding Grip/Use or during catch radial; reconnect | Visor menu and addon menu buttons respond; no stuck owned inputs; gun remains in inventory |
| AH26 | Repeat reconnect with Advanced Handling OFF | Normal Visor/flat controls remain functional; report whether failure is specific to Advanced Handling |
| AH27 | Glock-derived and Deagle-derived pistols; cylinder exceptions | New scale and expected source calibration; verify physical zones, muzzle and sight alignment for each copied profile |

Existing outstanding tests remain in TESTING-CURRENT.md. Neither the reconnect symptom nor holster placement has been verified with a headset by the coding agent.

Build verification: `gradlew.bat build --console=plain` passed with **136 tests, zero failures/errors**. Packaged artifact: `build/libs/visor-compat-tacz-1.20.1-0.12.0-alpha.2.jar` (315377 bytes). The instance was not modified and no changes were committed or pushed.
