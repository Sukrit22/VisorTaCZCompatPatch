# 0.8.0 — M700 and HK MP5A5 demos

Install on every client and the server: **protocol 13**. Includes 0.7.1 changes.
The earlier 0.7.1 JAR remains available for your current test session. No instance
files were changed automatically. Pinned TaCZ/Visor versions are unchanged.

## New default-pack profiles

| Gun | TaCZ ID | Ammunition | Physical handling |
| --- | --- | --- | --- |
| M700 | `tacz:m700` | `tacz:30_06` | Detachable magazine, two-hand support, manual back-forward bolt |
| HK MP5A5 | `tacz:hk_mp5a5` | `tacz:9mm` | Detachable magazine, left/front charging handle, support, selector |

Both include controller/muzzle aiming, calibration and scale lock, editable
per-gun TOML hints, inventory-backed pouch reload, remote model parts, and their
default-pack action sounds. Starting grip/muzzle pivots and model scale come from
the TaCZ models; interaction centers are initial estimates for headset calibration.
They have independent saved profiles. Existing three-gun calibrations are retained.

## M700

Reload the detachable magazine like the M4. Grab the orange bolt zone, pull back
8 cm to eject, then push forward near the starting position to chamber. Release
the grab after the cycle. The first version uses linear travel; lifting/rotating
the bolt handle is not required or animated as a separate action.

After firing, the spent-case marker blocks further firing until cycling. A full
rear stroke ejects one casing; a live chamber instead loses one live round. Holding
rearward does not eject repeatedly. Releasing open leaves it open: grab the orange
zone at its rearward position, push forward, and release. The panel shows BOLT OPEN
or CYCLE BOLT. A missing magazine or an uncleared jam cannot feed a new round.

Bolt-open/spent markers persist on the gun through reconnect; hand grabs do not.
Live ammunition stays in TaCZ's ordinary magazine/chamber fields. Switching to
button handling or flatscreen clears the extra markers and restores native bolt
controls. Existing interrupted-magazine recovery still applies.

With optional gundb 2.2.2, M700 supports a **generic jam**, cleared by a full
back-forward cycle. Use `/visor_tacz_test jam` in physical VR mode with operator
permission. It deliberately does not reuse detachable-magazine double-feed/dud
visual sequences or expose a casing-pluck interaction. The pink port is visual.

## HK MP5A5

Remove/stow the magazine, grab its replacement from the pouch, insert, and wait
for TaCZ reload completion. After an empty reload, grab the orange left/front
charging handle, pull rearward at least 5.5 cm, and release. Tactical reload retains
the chambered round. Support and selector use blue/yellow zones.

Uses the existing three-type optional jam handling with its own port and bolt
parts. There is no separate charging-handle locking notch or slap gesture yet.
No tracked magazine items are introduced.

## New checks — all pending headset testing

- [ ] **N01:** Calibrate both guns' grip/muzzle/rack/magazine/support. Scale to 80%,
  save/restart; check local and remote alignment. Check optics on M700 too.
- [ ] **N02:** M700: fire once, try again without cycling (blocked); pull rearward
  (one spent casing), hold rearward (no repeats), push forward (one round feeds).
- [ ] **N03:** M700: partial pull, release fully open, re-grab and close. Repeat
  after disconnect with bolt open. No free shot, automatic feed or duplicated ammo.
- [ ] **N04:** M700: tactical/empty magazine reload, cycle with no magazine,
  cycle a live chamber, and generic jam. Count gun + inventory ammunition.
- [ ] **N05:** MP5: semi/other native modes, trigger release, support aiming,
  selector, tactical and empty physical reloads, no spare ammo/full inventory.
- [ ] **N06:** MP5: charging-handle animation/ejection, all three test jam types
  with gundb, and interrupted reload recovery. Count discarded/reserved rounds.
- [ ] **N07:** Both: button mode and flatscreen native reload/fire/bolt behavior;
  friend observes VR magazines, support, bolt/handle motion and muzzle impacts.
- [ ] **N08:** Regression: Glock/M4/M870 still handle correctly. Continue the
  pending 0.7.1 optics, torch, ADS and scale-lock tests; they are not confirmed by
  adding these guns.

Limitations: default models/display only; no custom gun-pack inference, bolt
twisting, MP5 slap, direct M700 breech loading, or new gun-specific jam animations.
Native damage, spread, capacity, rate of fire and ammunition rules remain TaCZ's.

## Validation

Java 17 build/reobfuscation and **84 unit tests passed**. New tests cover model
pivots/capabilities, independent hint pages, single ejection/feed, partial and
interrupted strokes, missing magazine/jam feeding, and the rearward grab zone.
All eight selected new action-sound files exist in the pinned default pack.
No headset or live multiplayer test was performed; initial geometry needs your
calibration. Server marker cleanup now waits for the client's initial handling
mode message so login ticks do not prematurely discard a saved open action.
