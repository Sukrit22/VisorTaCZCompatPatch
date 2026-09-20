# 0.9.1: reload feedback and two-hand ADS

Install `visor-compat-tacz-1.20.1-0.9.1.jar` on all clients and the server.
Protocol **15** requires matching addon versions. Replace the previous addon JAR;
keep calibration JSON and worlds. No headset test was performed by the developer.
Java 17 build/reobfuscation and **98 unit tests passed**.

## What happened to 0.9.0

The interrupted task left a successful build, with its source saved in commit
`0559b54`. User testing reported M4 and M870 working so far, and the M700
lift/back/forward/lower cycle working. M700 support transfer and MP5 release
were not working for the user. These are candidate fixes, pending headset retest.

## Controls

- **M4:** empty firing locks the bolt. Insert a magazine, then Grab the cyan
  bolt-release region, or operate the charging handle. Native reload timing
  still supplies magazine ammo; wait until LOADING completes.
- **Glock:** after magazine insertion, main-hand **Use** releases a locked slide.
  Offhand Use or configured Grab at the cyan release region also works. Racking
  remains available. A new blue support region beside/below the firing hand
  enables two-hand ADS while held. Aim direction remains the firing controller's
  direction; the close support hand does not steer the pistol around the grip.
- **M700:** hold offhand Grab in the blue fore-end region. Move the main hand
  more than 16 cm from its calibrated grip to transfer the rifle to the support
  hand, or press main-hand Use to transfer explicitly. Keep holding offhand Grab.
  Main trigger grabs the bolt: lift 35 mm, back 80 mm, forward while raised,
  then lower. Bring the main hand to the grip and press Use to return, or release
  offhand Grab. Firing and ADS stay blocked during transfer. The support grip
  now stays held even with a narrow calibrated region; acquisition still uses
  that region. Hand transfer is transient, not saved across disconnect.
- **MP5:** pull handle back at least 55 mm to eject and latch it; an upward
  gesture is no longer required. Release Grab, replace magazine, then sweep
  the empty offhand **from back to front through the cyan region**, without
  pressing a button. A downward slap also works. Detection sweeps between pose
  samples so a fast hand need not land inside the box on a single tick. Grab or
  offhand Use in that region remains a fallback. Empty firing alone does not
  latch the handle. A jam still has to be cleared before the action can release.
- **M870:** open pump, take a shell from pouch, release it inside the pink side
  port, close pump. The green underside port fills the tube with pump closed.
  Side-port loading uses native chamber ammo and does not consume a tube shell
  when closing. Partial/open state survives reconnect; current hand grabs do not.

## ADS and calibration

`/visor_tacz menu` has **ADS 2 hands**, on by default. In physical mode it
requires an engaged support grip; in button mode it requires an empty tracked
support hand in the calibrated support region. Sight alignment is still needed.
Releasing support exits ADS. Turning the option off restores alignment-only ADS.
Flatscreen ADS is unaffected.

Every gun has a **Casing size** calibration page, 10-300%, saved as `casingScale`
in `config/visor_tacz-calibration.json`. It multiplies gun scale and applies to
jam obstructions, ejected live/spent visuals, and the M870 held-shell preview.
A sample casing appears at the ejection port while editing this page in physical
mode. Existing grip, muzzle, zone, and scale values are retained; older profiles
default this new multiplier to 100%. Remote players receive the visual scale.

Stovepipe casing lies across the bore at 22.5 degrees above the gun's local
horizontal. Double feed now reuses casing geometry instead of the projectile
entity model. This visual reuse does not change the two-round jam ledger.

**Dud** means a chambered round failed to fire. The action looks closed; there
is deliberately no protruding casing. The blocked trigger plays the existing
TaCZ dry-fire click and HUD identifies DUD. Rack it to eject the reserved round
and clear it. A separate magazine-tap requirement or unique dud sound is not
implemented. With gundb 2.2.2, both `/visor_tacz_test jam dud` and the added
`/visor_tacz_test jam misfire` alias select this state.

## Focused tests (all new checks pending)

- [ ] **F01:** Glock empty reload, main Use slide release, repeated Use (no
  extra chambering); offhand Use at cyan region with Grab set to TRIGGER.
- [ ] **F02:** Glock blue grip + eye alignment enables ADS; releasing it exits
  ADS. Repeat on M4/MP5/M700/M870. Toggle ADS 2 hands off and compare.
- [ ] **F03:** M700 narrow blue region: acquire support, move main hand away,
  operate bolt with main trigger, then return using main Use near grip. Repeat
  explicit transfer with main Use. No shot during transfer or lifted/open bolt.
- [ ] **F04:** MP5 full rearward pull without lifting latches. After reload,
  forward sweep through cyan box releases once. Try slow/fast sweeps, downward
  slap, wrong direction, outside box, no magazine, and an active jam.
- [ ] **F05:** Compare stovepipe and double-feed models. Calibrate casing size,
  save/restart/reload calibration; verify ejections and held M870 shell match.
- [ ] **F06:** Dud: closed-looking action, click, HUD DUD, rack clears it. Count
  rounds before/after all three jam types; double feed still needs two ejections.
- [ ] **F07:** Disconnect, lose focus, change slot, and switch to flat during
  transfer or open action. No stuck grab, accidental shot, or duplicated ammo.
- [ ] **F08:** M4 release and M870 side-port/tube reload regressions. Count ammo
  across reconnect. Check remote friend sees transferred rifle/parts/case sizes.

Older untested cases remain in TESTING-CURRENT.md; this does not close optic,
multiplayer, custom-pack, or durability cases the user has not reported.
