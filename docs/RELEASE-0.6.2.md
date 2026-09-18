# 0.6.2 — M870 pouch fix

Install on clients and server. Protocol remains 10; includes the 0.6.1 stencil fix.

The M870 pouch used TaCZ's `hasInventoryAmmo` check, which returns false for guns
that do not fire directly from inventory. That rejected ordinary M870 shell grabs.
Pouch grabbing now creates a shell preview, like the magazine preview. Inserting
at the green loading port still validates capacity and consumes exactly one
compatible shell from inventory/ammo box (or dummy ammo). Creative follows TaCZ's
ammo-check setting. An unsuccessful insertion consumes nothing. A preview can
appear even without ammo; it is not an inventory item and grants no ammunition.

The yellow pump grip also supplies two-hand support when held fully forward.
The separate blue support point is now shown for M870 only in calibration, where
it remains an aiming alignment reference; it is not another grab zone.

The purple/magenta sight marker is used by automatic ADS to compare the sight
line with either eye and headset direction. It does not change the scope lens
geometry or its optical eye-relief check. Scope magnification remains disabled
with shader packs because the custom lens samples the ordinary eye framebuffer;
shader-pack framebuffer/compositing integration has not been implemented.

Retest: empty assigned offhand, compatible 12-gauge ammo in player inventory,
grab and hold in the cyan pouch zone, move to green loading port, release.
Confirm one shell is consumed and tube count increases by one. Repeat with no
ammo and a full tube: no ammo should be granted or consumed. Then pump back and
forward and fire. The unidentified black scope still requires its exact name
and gun to diagnose; this release does not claim to fix it.
