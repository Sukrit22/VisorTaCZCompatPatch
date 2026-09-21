# 0.10.0: default-pack pistol expansion

Minecraft 1.20.1, Java 17, matching client/server addon 0.10.0, protocol 18. Based on pinned TaCZ 1.1.8-hotfix. User M1911 registry/family/port work is retained and extended. Existing calibration is preserved; geometry for the new guns needs VR validation.

## Added

- Nine physical magazine pistols: M1911, P320, M9A4, Desert Eagle, Golden Desert Eagle, Timeless 50, B93R, CZ75 and HK MK23. Glock remains supported.
- Per-profile model pivots, moving slide roots, ejection references, initial interaction points, hints, action sounds and ammo previews. Golden Deagle shares Deagle sounds; MK23 temporarily borrows M1911 action sounds. Some available combined clips may need replacement after listening in VR.
- B93R/MK23 physical selector regions; CZ75 keeps its native auto-only mode. All pistols keep touch support and main-controller aiming.
- Experimental physical loading for Rhino 357, Taurus 500, Taurus 943 and Lonetrail. Separate server state uses native TaCZ live ammo counts and addon open/spent-case metadata. Lonetrail is a breech workflow, not a detachable-magazine pistol.

## Experimental loading controls

1. Grab the orange action region and move sideways at least 5 cm, then release. Either sideways direction toggles open/closed; this is a position gesture, not wrist-twist detection. One toggle per grab.
2. When open, grab the pink region and pull back 5 cm to eject **all live rounds and spent cases**. Live rounds are discarded; loose render objects are not pickups.
3. Grab a compatible round from the waist pouch, move to green, release. One inventory round is consumed only on valid insertion. Spent cases occupy slots until ejected; a full cylinder rejects insertion.
4. Orange grab + sideways motion closes. Fire is blocked while open or handling. Two-hand ADS uses the blue touch region.

Saved native ammo count survives reconnect. Open/spent metadata survives physical-mode reconnect; unfinished gestures/previews are canceled. Switching to button/flat mode clears only open/spent metadata and resumes native TaCZ reload. Removing the addon leaves native live ammo usable; addon metadata is ignored. Exact in-game round accounting and cross-mode integration remain test items.

## Limits and validation

110 automated tests pass; the read-only asset validator checks all 13 new model/mechanism/ammo/sound mappings against the pinned source. No headset validation is claimed.

Cylinder/breech support is experimental aggregate loading, not a per-chamber simulation. No chamber indexing, physical speedloaders, hammer cocking, or individually synchronized live/spent cartridge mesh layout. Open poses use native action-node angles; interaction regions start as estimates and can be calibrated separately. These four guns use generic durability jams cleared by open/close, not the three visual magazine-pistol jams. The aggregate spent count does not model every native reload-animation cartridge.

See TESTING-PISTOLS-0.10.0.md for controls and prioritized checks. Build is not automatically installed, committed or pushed.
