# 0.3.0 upgrade and headset checklist

For Pico 4 Ultra + Virtual Desktop + SteamVR, upgrading from 0.1.1 / 0.1.3.
Use default TaCZ Glock 17 and M4A1 first. Record each result as pass/fail.

## Install

Close Minecraft, replace the old visor-compat-tacz JAR with
`visor-compat-tacz-1.20.1-0.3.0.jar`, and keep only one version in mods.
Install the same version on the server and your friend's client (protocol 6).
Keep `config/visor_tacz-calibration.json` and `config/visor_tacz-client.toml`.
Existing calibration and OFF/physical-mode preferences are preserved.
Start without an Oculus shader pack for the optics checks.

## Test these first: reported regressions

1. **Startup:** client reaches the menu and joins; dedicated server starts. No
   RemoteGunMixin/renderByItem injection failure in logs.
2. **Last active hand:** in button mode, hit a block with the left hand, then fire
   a gun held in the logical main/right hand at a different target. Bullets follow
   the gun, regardless of which hand interacted last. Repeat after using menus.
3. **M4 support aim:** keep the trigger hand nearly still and move the support hand
   around the foregrip. The barrel and impact direction turn together. Button mode
   engages by proximity; physical mode requires holding offhand-use at the blue guide.
   Account for normal spread and gravity; the Visor interaction cursor may still
   point elsewhere and is not the authoritative gun sight.
4. **HMD resume:** release the trigger, lift/remove the headset until unfocused,
   then put it back on without disconnecting. Press the trigger afresh: it should
   shoot. Repeat with full disconnection/reconnection, Glock and automatic M4 fire.
   No shot should occur merely because focus returned. Capture status/logs if stuck.
5. **Comparison:** `/visor_tacz off` restores upstream behavior; `/visor_tacz auto`
   restores the addon. Returning to flatscreen disables VR behavior automatically.

## Sights and optics

6. **Auto ADS:** `/visor_tacz ads auto`. Raise ironsights to either eye and check
   `/visor_tacz status` for aiming; lower/turn away and check lowered. Repeat while
   sprinting, reloading, opening a menu and resuming the HMD. No whole-view zoom.
7. **Red dot:** equip a compatible TaCZ red dot. Inspect left and right eyes
   separately. The dot stays aligned with the distant aim and is clipped to the
   optic window as you move your head; no duplicate desktop overlay or eye flicker.
8. **Scope:** equip a compatible magnified M4 optic. Only the lens magnifies; the
   world outside remains normal. Move your eye off-axis/away to check blackout.
   Check both eyes, close targets, distant targets and performance. Report optic ID
   and zoom setting. This is a screen-space magnifier, not a separate scope camera.
9. Toggle `/visor_tacz optics off` and `on`; `/visor_tacz ads off` and `auto`.
   Restart and confirm preferences persist. Custom optics skip rendering when an
   Oculus shader pack is active; test shader compatibility separately.

## Calibration and physical handling

10. Run `/visor_tacz calibrate`, adjust grip XYZ/rotation, then Bullet origin XYZ.
    Check cyan muzzle marker, save, fire, restart and confirm offsets persist.
    Cancel must discard only unsaved changes. Test both gun profiles.
11. Enable `/visor_tacz handling physical`; keep Visor's assigned offhand empty
    (including its two-handed-mode hotbar assignment). Pull the green magazine
    guide down at least 6.5 cm while holding offhand-use. Release to stow; grab at
    the cyan waist pouch and release at the magwell. Wait for TaCZ's reload timer.
12. After an empty reload, pull the orange slide/charging-handle guide back at least
    5.5 cm and release. Tactical reloads retain a chambered round. Verify magazine
    removal does not duplicate rounds; racking a loaded chamber loses that one round.
13. Interrupt with weapon switching, menus, HMD removal, mode OFF, reconnect and
    death in a test world. No stuck input or duplicated/lost reserved magazine ammo.
    Return to `/visor_tacz handling buttons` to compare ordinary reload controls.
14. Test Visor left-handed and right-handed settings. This release follows the
    logical main hand, not the last active hand. It does not add an independent
    handedness option or dual-wield support. If a single gun cannot occupy the
    expected physical hand, report handedness and two-handed hotbar assignments.

## Flatscreen friend

15. Your friend fires/reloads normally, including unsupported guns; no VR input or
    camera changes apply to them. They observe your calibrated gun, foregrip aim,
    magazine and slide movements. Confirm no duplicate/missing gun after switching,
    moving out of view, teleporting, reconnecting and enabling/disabling the addon.
16. Have them watch impacts while you point away from your face. Server bullets
    originate at the calibrated muzzle. TaCZ remote tracer styling may hide the
    initial segment, so judge collision/impact as well as the visible tracer.

## Send back

Version, failed step, gun/attachment IDs, mode (buttons/physical), Visor handedness
and two-handed settings, shader pack, and what each eye/friend saw. Include the
calibration JSON for grip/muzzle feedback. For crashes include the timestamped
crash report and matching archived log; latest.log is replaced on the next launch.

Validation performed here: Java 17 release build/reobfuscation; 25 unit tests;
production TaCZ renderer/accessor method inspection; standalone OpenGL optic
shader compilation, linking and uniform checks. No live Minecraft/headset or
multiplayer acceptance test was performed for this release.
