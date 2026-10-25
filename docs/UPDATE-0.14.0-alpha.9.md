# 0.14.0-alpha.9 - alpha.8 feedback follow-up

## Changes

- Dual gun feedback is driven by confirmed shots in each inventory slot: muzzle flash, spent-case ejection and shot-cycle moving parts. Glock Physical magazine/rack cues now resolve even without optional pistol metadata. A manual rack produces live-round feedback only when a chambered round was actually removed. Effects are local to the VR owner; this does not add remote effect replication or the rest of Advanced dual handling.
- Nonmagnifying sight ADS geometry uses the actual aperture through native gun mount and attachment transforms. TaCZ's desktop `scope_view` for the 552 and EXP3 sits far behind the physical lens; it is not the lens center. Magnifying scopes retain their authored view marker. Unknown/unreadable aperture geometry falls back to the native view marker.
- Dual long-gun ADS support eligibility now matches supported aiming instead of retesting a smaller box after the gun has already rotated toward the support hand.
- M1014 exposes separate Support position/size calibration, independent of Rack. Its blue guide is shown alongside the action guide.
- Debug mode shows the purple ADS guide outside calibration, including Button handling and held dual guns.

## Calibration explanation

Gun baseline and attachment/view edits contribute to one effective purple guide; changing the edit page does not show a separate base-only marker. Current corrections remain per gun/display + attachment + view. Native mount composition accounts for different rail placement on each gun, so normal attachments should not require copying a fixed world or gun-local offset across every gun.

Saved values are preserved. After this geometry change, an old negative Z workaround on a red-dot sight may put its marker too far forward. Back up calibration and zero only that workaround context when checking the new default. Do not reset unrelated grip, muzzle or holster calibration. A future shared attachment correction should be expressed in attachment-local coordinates; the current per-gun override is not such a universal offset.

The remaining ADS acquisition/retention margins are deliberate hysteresis: retention accepts an extra 20mm lateral and 100mm depth, and 25 degrees of head angle versus 15 degrees on entry. Losing ADS while at that outer margin can require moving back into the entry region. This update fixes confirmed geometry/support mismatches; it does not claim every reported reacquisition issue is resolved without headset testing.

## Reload and lifecycle tests

Button-mode stow requests TaCZ reload interruption. TaCZ delegates interruption to a gun script; its default implementation does not forcibly cancel every reload. Some magazine reloads therefore finish while holstered. That policy is unchanged here. A future universal cancellation option must preserve any ammunition already committed by the native reload.

A8-LIFE remains a targeted safety test for removed-magazine reservations, tracking loss, reselect, disconnect and deletion. There is no confirmed alpha.8 duplication report. The purpose is to validate inventory and session transitions that pure reservation accounting tests cannot reproduce. Test on a copy of a survival world if preserving counts matters. Do not rerun every historical A7 RETEST: use the consolidated statuses and narrow A9 checks in [TESTING-CURRENT.md](TESTING-CURRENT.md).

No release JAR is packaged by this update. Network protocol remains 34; matching builds are recommended for feedback behavior.

## Validation

All four targets compile. Tests passed: 314 each on Forge/Fabric 1.20.1 and Fabric 1.21.1, 315 on NeoForge 1.21.1. Headset appearance, audio timing, and ADS reacquisition remain pending.
