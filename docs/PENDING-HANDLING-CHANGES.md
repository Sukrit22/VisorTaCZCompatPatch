# Handling changes included in 0.12.0-alpha.6

Included in **0.12.0-alpha.6**, protocol **22**. Update client and server together. Headset and multiplayer verification is still pending.

## Inspection calibration

Select a magazine pistol, then `/visor_tacz menu` → Advanced Handling → **Calibrate inspection grip**. Set X/Y/Z and pitch/yaw/roll while holding the controller in the inspection pose you want. These offsets move the gun relative to the normal calibrated hand pose; Visor's individual fingers are not animated. The forced alpha.5 inspection rotation was removed; the default is neutral.

The screen provides live preview, partial-slide/closed-slide preview, fine 1 mm / 1° and coarse 1 cm / 10° steps. Save writes per-gun values to `config/visor_tacz-inspections.json` without closing. The button becomes Close until another edit. Cancel discards changes since the last Save. Calibration reload includes this file. Share that JSON when reporting the pose; normal grip, muzzle and holster calibration are separate.

## Handling changes

- M4 no longer transfers automatically when the trigger hand moves away. That new path could make the offhand the owner and prevent it operating the charging handle. Explicit transfer remains available. In Advanced Handling, offhand Trigger can now hold the charging handle as well as Grip; releasing Trigger releases the action.
- On a support-held M700, main Grip near the bolt operates it rather than snapping the rifle to the main hand. Its acquisition region has 5 cm extra room per side. Stroke motion starts relative to the grab contact so grabbing near the edge does not immediately move the bolt. After closing the bolt, entering a 12 cm handle region gives one short main-hand haptic pulse. Press Grip there to regain firing control. The server preserves the existing gun-to-hand offset, including orientation and muzzle direction, instead of snapping to normal calibration. This offset is temporary. Move beyond 16 cm to re-arm the cue. The rifle then follows main-hand movement from the captured pose. Main Trigger still operates the bolt. Scope ADS can remain aligned while the support hand holds the rifle through a bolt cycle; firing remains blocked.
- Holsters follow head height through crouching. Existing stored Y values are retained, interpreted relative to a nominal 1.62 m standing eye height; the menu now displays **Y from eyes**. Example: saved Y=0.94 displays -0.68 m. Horizontal head movement gradually updates the heading toward gaze, while stationary head rotation alone does not update heading. This estimates movement, not torso tracking; leaning can also affect it. Recenter remains available.
- Advanced Handling allows contact-preserving carrying grabs across a coarse gun-body envelope: receiver/barrel/stock and grip. Existing action zones have priority when the trigger hand holds the gun. Grabbing elsewhere carries it without firing. Return to the actual handle to use it normally. This is not exact mesh/attachment collision and does not implement dual wield. Guns stay in their original inventory slots.

## Pending tests

| ID | Check |
|---|---|
| AH44 | Adjust inspection on Glock, Save, edit again, Cancel, reopen, restart and reload JSON. Verify signed values persist and normal aiming calibration does not change. |
| AH45 | M4 charging handle with offhand Grip and Trigger. Test Advanced on/off separately, normal support, and explicit support transfer. |
| AH46 | M700: shoot while scoped, retain support Grip, release main Grip, grab near the bolt edge, lift/back/forward/down. No handle snap; scope remains aligned. Return main hand to handle and Grip to fire. |
| AH47 | Crouch/stand with holstered pistol and rifle. Walk, stop and look sideways. Verify comfortable eye-relative height and movement recentering. |
| AH48 | Carry a holstered/tossed gun by barrel, stock and grip with either hand. Contact should remain in place. Action zones should still work when the trigger hand holds the gun. Generic carrying must not fire. |
| AH49 | Lose focus or tracking while carrying or operating the sniper bolt; switch slots and flat/VR. No stuck grab/fire ownership or copied items. |
| AH50 | Follow the cylinder instructions below; calibrate orange, pink, green and pouch independently for each gun. Count survival ammo. |

## Cylinder/breech pistol controls

Supported experimental profiles: **Rhino 357, Taurus 500, Taurus 943 and Lonetrail**. They share aggregate open/eject/load/close logic; they do not track individual cylinder chambers or speedloaders.

Enable Physical Handling and carry the gun in the firing hand. Keep the other hand empty and compatible ammunition in inventory. “Grab” means Grip with Advanced Handling on, otherwise the configured offhand grab button (Use or Trigger).

1. **Orange — open:** Grab the orange action region, move sideways about 5 cm, release. Either sideways direction works; one toggle per grab.
2. **Pink — eject:** With the action open, Grab pink and pull backward about 5 cm, then release. This discards **all remaining live rounds and spent cases**. Ejected objects are cosmetic, not collectible. Spent cases occupy capacity until ejected.
3. **Pouch — take one round:** With the action open, Grab the waist pouch. The preview needs compatible inventory ammo.
4. **Green — load:** Carry that round to the green loading region and release. One inventory round is consumed only after a valid insertion. Repeat to fill available capacity.
5. **Orange — close:** Grab orange again, move sideways about 5 cm and release. Fire normally.

Calibrate **open/close action (orange)**, **eject all (pink)**, **loading point (green)** and **pouch**. Their position and box dimensions can be edited separately. Casing scale controls the cartridge/case preview size. Do not calibrate the green insertion point as the pink eject point: they serve different actions.

Additional pending test AH51: After closing the M700 bolt, enter the handle area at different offsets and angles. Verify a single pulse, no buzzing from boundary jitter, Grip restoring firing without a scope/muzzle jump, and correct subsequent controller movement and bullet direction. Repeat the bolt cycle and test slot/focus reset.
