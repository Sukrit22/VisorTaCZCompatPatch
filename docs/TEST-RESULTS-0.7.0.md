# User test results — 0.7.0, September 20, 2026

Hardware: Pico 4 Ultra, Virtual Desktop, SteamVR. Results are user observations,
not independently reproduced. This report preserves detail behind TESTING-CURRENT.md.

## Accepted results

C01 scale, C03 contextual menu, S01 M870 support, S02 survival shell insertion,
S03 M870 live/spent visual consistency, D01 all display locations and status,
A01 ADS, C04 persistence, C06 guide toggle, I04 flat/VR transitions, G01 ammunition
accounting, and G04 pouch ownership reported OK. These passes do not imply every
unmentioned subcase in a grouped checklist was exercised.

C02 boxes worked in a new world/new mod profile. In the older setup, highlight
followed the new region but successful grabbing appeared tied to the old point.
Do not declare old config the cause without comparing files and reproduction.

I01 full Virtual Desktop disconnect/reconnect previously worked; focus-only
recovery now works too. Historical stuck firing was not proven to be a jam.
Menus/death/slot-switch variants were not separately reported this session.

## Partial results and open issues

- I02: torch placement fails. Sword swinging and bare-hand punching work;
  punching is suppressed while holding the shotgun's interaction grip as intended.
- I03: handedness switches the gun correctly. Visor Essential menu positioning
  makes broader testing difficult; no root-cause attribution yet.
- G02: nothing unusual noticed yet; user intends further observation.
- G03: wall rejection works; sprinting and reloading together works. Preserve
  ordinary sprint-reload behavior. Not all firing-mode cases separately verified.
- S04: pump stays open after rear stroke and requires manual forward motion.
  Holding rearward means holding at that position for several seconds to check
  that no additional shells eject. That count check remains unconfirmed.
- S05: disconnect preserves shell/ammo counts. Other cancellation cases pending.
- C05, C07, S06 explicitly not tested. J01–J07, R01–R03 and M01–M02 have no new
  explicit results in this report; earlier partial stability evidence remains.

## Requested changes / next investigation

1. Optional link/lock between gun scale and interaction-box dimensions. Preserve
   existing independent-size behavior when unlinked; avoid a silent migration.
2. Scale held ammunition preview/ejection visuals with the gun. M870 at 80% leaves
   a disproportionately large shell. Glock/M4 live cartridges and spent casings
   also have inconsistent size; scaling alone may not normalize different assets.
3. Inventory-aware pouch feedback: loaded-looking magazine preview when ammo is
   available, and OUT OF AMMO for unavailable shotgun shells. Use TaCZ-compatible
   inventory/ammo-box/dummy-ammo rules; do not accidentally reintroduce the old
   inventory-fed-gun check that broke M870 pouch grabs.
4. Glock after empty reload: slide does not initially look locked back; first pull
   can reveal open state and second pull closes it. Trace native animation and
   physical state transitions; do not treat the extra pull as intended behavior.
5. Requested M870 rule: prevent tube-shell insertion while pump is open. Treat
   this as a game-mechanism choice pending implementation, not a universal claim
   about real shotguns. Client target/feedback and server acceptance must agree.
6. Raising sights into ADS should stop sprinting. Current AutoAds explicitly
   excludes sprinting, so this requires changing the input/state priority.
7. Debug guides should always appear while calibrating, even with normal-play
   guides OFF. Current renderer's early return prevents this.
8. Diagnose offhand torch placement without breaking successful swings/grabs.

## Optics evidence (keep separate from calibration)

- QMK-152 x3: world bullet marks appear over/through scope at their unzoomed
  position. Earlier run appeared to show both normal and zoomed marks. Clean test
  still showed the outside mark, but no bullet mark in the magnified view.
- Clean optics test included Visor, Visor Essential Plus, TaCZ, Tweaks, Durability,
  Sophisticated Backpacks/Core, its Visor addon, and this compatibility addon.
  It was not a Visor + TaCZ only isolation test.
- M4 muzzle/front sight visible inside QMK-152 x3 magnified view.
- Black/opaque and apparently low-resolution optics reported for ACRO P-1,
  SRO mini, RMR mini, PK06, SRS02 Reflex. User suspects wrong model; not established.
- Older setup: shots appeared roughly 1–2 degrees above irons/scope. Clean profile
  showed no obvious error, with M4 at 80% in both setups. Scope mark visibility
  prevents confident confirmation. Retain both calibration files for comparison;
  do not reset the old file or apply a guessed aim correction.

Next optics investigation should examine depth/render ordering for decals,
captured-eye-image content, lens masks and attached optic geometry. No fix is
claimed by this report. No new JAR was built for this documentation update.
