# 0.11.0: basic button support separated from physical handling

Matching client/server version 0.11.0, protocol 19, Minecraft 1.20.1 / Java 17 / pinned TaCZ 1.1.8-hotfix.

- 54 default-pack guns registered for VR button aiming, shooting and native reload. 18 also have physical handling; four of those are experimental cylinder/breech profiles. Full list: SUPPORTED-GUNS.md.
- Guns without physical mechanics automatically use Buttons, even if Physical is globally preferred. The menu/HUD/command show the effective fallback. Switching back restores the preference. Physical-only calibration pages and jam-test commands are not offered/applied to button-only guns.
- Button reload uses TaCZ's native timing, ammo and scripts; physical gestures cannot intercept it. The current VR renderer still suppresses camera-relative desktop reload transforms, so a working button reload does not imply full native reload animation.
- Seven exact saved calibrations merged into bundled defaults, with personal files retaining priority. See CALIBRATION-IMPORT-0.11.0.md.
- Muzzle reach validation accommodates long default-pack models (up to 3 metres at world scale 1), retaining both hand tracking bounds and all wall-segment checks. M320/RPG-7 muzzle geometry is a visual estimate; calibrate and test.

113 automated tests passed. All-gun headset behavior, long-gun alignment, launchers and native custom-script cases still require user testing. No gun pack was installed. External profile hot reload and automatic third-party registration are design work described in DATA-DRIVEN-GUN-SUPPORT.md, not features claimed by this build.

## Priority tests

1. Set handling to Physical, select AK47/UMP45: menu shows Buttons (Auto), trigger shoots, main Use reloads. Switch to Glock/M4: physical handling returns. Repeat with preference Buttons: every gun uses native reload.
2. Try representatives: AK47, UMP45, AWP/Kar98, AA12/DB Long, Minigun, M320/RPG-7. Check empty/tactical reload, native bolt cycle, trigger release/burst interruption, spin-up behavior and projectile direction. Check impact/muzzle alignment at close range, especially the two estimated launchers.
3. Test long guns at 100% and 150% scale; blocked walls must still prevent firing. The maximum applies to the calibrated muzzle as well, so unusually large offsets may be rejected.
4. Check grip, sight and scale defaults for the seven imported guns. Existing local values must remain unchanged after startup/reload/save. New users should see the bundled values.
5. Flat friend tests unchanged native gun handling and sees remote button-mode guns correctly. Reconnect, focus loss, gun switching mid-reload and native ammo totals remain required.
6. Spot-check remaining entries in SUPPORTED-GUNS.md. Being registered is not a recorded per-gun VR pass.

The completed 0.10.0 state was committed as a93eedf before these changes. This release is not automatically installed, committed or pushed.
