# Pistol test session: 0.10.0

Use matching 0.10.0 client/server JARs (protocol 18). Keep your saved calibration. These are new, untested-in-VR profiles; do not mark them passed based on Glock alone. Start with shaders off, physical mode on, empty assigned offhand, survival inventory and visible regions.

## Magazine pistols

Order: M1911 first; P320/M9A4; Deagle/Golden/Timeless 50; B93R/CZ75/MK23. Existing Glock is the regression reference.

For EACH gun:

- [ ] P01: Correct model size, grip, muzzle/impact alignment; moving slide root includes the whole slide. No duplicate/hanging magazine. Calibrate and save before testing grabs.
- [ ] P02: Empty reload from pouch transfers correct inventory ammo immediately; main Use and cyan offhand release chamber exactly one round. Tactical reload preserves the chamber. Try partial/no spare ammo and capacity attachments.
- [ ] P03: Five complete back/front racks without letting go; one ejection per full rear stroke, no repeats when held back, exact remaining rounds. Native slide stays locked after last shot until released/reloaded.
- [ ] P04: Touch blue support without Grab; auto ADS enters/exits with alignment. Test right and left gun-hand settings. Save/reload/restart calibration, scale and casing size.
- [ ] P05: Burst/semi selector on B93R/MK23 at yellow region. CZ75 auto trigger release. Interrupt by reloading, focus loss, gun switching and jam: no delayed extra shots.
- [ ] P06: Stovepipe/double-feed/dud on supported gundb, each cleared separately; exact round counts and casing orientation. Compare without gundb.
- [ ] P07: Disconnect during reload, reconnect twice; no extra/lost reserved rounds. Flat friend can reload/fire normally and sees remote gun/magazine correctly.

## Rhino 357, Taurus 500/943 and Lonetrail

Orange grab + sideways 5 cm toggles open/close, once per grab. Open pink grab + backward 5 cm ejects ALL live rounds and spent cases. Open pouch grab -> green release loads ONE round. Loose ejected models are cosmetic, not recoverable ammo. Lonetrail uses the same experimental gesture contract for its breech; it is not a cylinder or speedloader simulation.

- [ ] V01: Open/close model and regions match sufficiently to calibrate. No firing while open, holding a round or manipulating action. Releasing halfway does not toggle. Holding after a completed toggle does not toggle repeatedly.
- [ ] V02: Fire two rounds; spent count increases twice. Open; still-occupied spent slots reject loading when live + spent equals capacity. Eject all: count discarded LIVE rounds separately from spent cases.
- [ ] V03: Load from pouch one at a time. Wrong zone, canceled grip, tracking loss, full action, or no ammo consumes nothing. Close, fire, verify native count. Test Taurus capacities and Lonetrail single-shot behavior separately.
- [ ] V04: Disconnect with action open and while holding a pouch preview. Saved ammo/open/spent survive; preview does not. Rejoin twice and count all ammunition.
- [ ] V05: Switch physical -> buttons -> flat -> VR. Native reload/fire works; temporary open/spent clears without adding/removing live rounds. Test removing addon in a copied test world if desired.
- [ ] V06: Generic `/visor_tacz_test jam` blocks firing; open/close clears it. Named dud/double-feed tests are intentionally rejected. Native durability remains optional.
- [ ] V07: Remote flat observer sees open/closed action and held ammo preview; correct ammo model/sound. Individual chamber cartridge mesh state is not implemented.

Report version + ID + test ID + result, and share calibration JSON for geometry failures. Full older outstanding tests remain in TESTING-CURRENT.md. No test in this document is pre-marked passed.
