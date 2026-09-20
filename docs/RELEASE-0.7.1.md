# 0.7.1 — feedback fixes and optics candidates

Install on all clients and server: protocol **12**. Existing calibration numbers
are retained. No instance or world was modified automatically.

## Changes

- Scale page has **Scale link: LOCKED / UNLOCKED**, default LOCKED. Changing gun
  scale proportionally changes gun-mounted box dimensions when locked. Pouch
  dimensions stay independent. Toggling alone does not change any dimensions;
  loading an old 80% profile does not silently resize its existing boxes. The
  preference saves per gun in `scaleLock`. Dimension limits still apply.
- Shell preview and world ejections follow gun scale, including remote players.
  Glock/M4 live ejections now use the same casing asset as spent ejections, avoiding
  the unrelated projectile model's size. Dedicated bullet tips/live-shell caps
  are still not implemented; this is cosmetic, not collectible ammunition.
- Detached magazines explicitly show/hide the default model's `bullet_in_mag`
  according to available ammo (replacement) or reserved rounds (removed magazine).
  Remote observers receive that flag. Magazine bookkeeping remains pooled TaCZ
  ammunition, not independently tracked magazine items.
- M870 pouch checks loose ammo/ammo boxes/dummy ammo and the native creative
  ammo-check policy without consuming any ammo. No ammo produces OUT OF AMMO,
  without a shell preview. Actual insertion revalidates and consumes server-side.
- Open M870 pump blocks pouch pickup and tube insertion. Close the pump first.
  This is the requested game interaction rule, not direct breech loading.
- Glock physical ready state refreshes from the actual chamber after shots; an
  empty chamber keeps its slide visibly back through magazine handling. This is
  a candidate fix for the missing lock-back/extra-rack report; headset retest needed.
- Automatic ADS stops sprinting while sights align. Sprint-reloading remains
  unchanged. Lower sights to permit sprint input again.
- Calibration always displays guides even when normal-play Debug cubes is OFF.
- A narrow exception to TaCZ's client interaction block permits VR offhand item
  Use, targeting torch placement. It does not un-cancel another mod's restrictions,
  and does not change flatscreen handling or globally rebind trigger/Use.

## Optics candidates

The local VR gun now forces detailed attachment models. Its third-person render
context could previously select a low-detail model lacking the proper optical
aperture. Ordinary remote/flatscreen LOD selection is unchanged.

Without shader packs, the local gun draw now runs in Visor's after-world effect
stage, after particles. Scope capture can include bullet-hole particles and no
longer precedes those decals. The custom lens writes depth to prevent subsequent
geometry behind the aperture overwriting it. Red-dot apertures draw the captured
unmagnified scene plus dot rather than relying on opaque model interiors being
transparent. This needs headset validation on QMK-152, ACOG and the reported dots.

Shader-pack magnification remains unsupported; the shader path keeps its earlier
draw stage. This is still eye-image magnification, not a separate scope camera.
No guessed muzzle/aim adjustment was applied to the old-profile trajectory report.

## Focused retest (all pending)

1. **C01/C02:** lock ON, change 100% to 80%: gun boxes resize, pouch does not.
   Unlock and change scale: dimensions stay fixed. Save/restart and check lock
   preference and sizes; Cancel must discard preview changes. Check hit bounds.
2. **S03/G02:** 80% shell preview and ejections; compare Glock/M4 live versus spent
   dimensions. Check a friend's view if available.
3. **S02/G04:** loaded versus empty magazine preview; M870 no-ammo message; then
   add ammo and insert. Try ammo box, dummy ammo if used, full tube and open pump.
4. **G01:** fire Glock empty, reload, perform one full rack/release; verify visible
   lock-back and that a second rack is not needed. Count rounds.
5. **A01/C06:** raise sights while sprinting, then lower them; test sprint-reload.
   With guides OFF, enter calibration (visible) and exit (hidden again).
6. **I02:** torch in assigned offhand, gun in main hand; press offhand Use (not
   offhand attack). Repeat in Visor two-handed mode; confirm sword/grabs still work.
7. **O01/O02:** shaders OFF, QMK-152 x3 bullet marks and M4 front sight; then
   ACRO P-1, SRO mini, RMR mini, PK06, SRS02 Reflex, and TA31 ACOG, both eyes.
   Record remaining black apertures or duplicate/missing marks by exact optic.
8. **R03:** old world with scoped gun, flat/VR transition, Z and shader ON/OFF;
   check no new crash or flashing. Shader zoom is not expected.

See TESTING-CURRENT.md for older untested scenarios. Historical passes remain
historical; they are not automatically passes for this build.

## Validation

Java 17 release build/reobfuscation succeeded; **76 unit tests passed**. The
updated optic GLSL compiled, linked and passed uniform checks on the local GPU
using the standalone invisible-window tool. New mixin classes and bundled hints
were verified in the release JAR. No live Minecraft, headset or multiplayer
session was run for this build; these checks do not establish visual correctness.
