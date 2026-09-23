# Alpha.8 source changes

Not packaged yet. Network protocol 24; build matching client/server JARs before testing.

- Physical M700 waits for main Use near its handle before returning from offhand support. Advanced M700 keeps Grip-based pose-preserving re-grip. Both restore offhand aiming after re-grip, including the captured support contact.
- Advanced revolver closure now requires about 8 cm rightward movement with a deliberate speed and a stop; angular wrist rotation alone no longer closes it. Use on the carrying hand arms a roughly 2 cm leftward opening gesture for 1.2 seconds. Works with normal main grip or generic hand carry; Lonetrail is excluded from cylinder gestures. Native cylinder grabbing remains available.
- Audited all 14 pistol presets. P320 is bundled but still uses an older Glock-derived starting calibration; it needs its own fit check. No user calibration was silently changed.
- Added an opt-in Legacy/TOML profile menu switch. Ported all 18 physical profiles, with source geometry/parts/zone parity tests. Added experimental SCAR-L/H physical handling in TOML mode, with their own model nodes and sound clips. Native reload mechanisms still own ammunition.
- External files can define compatible guns without recompiling, after restarting both sides. Client/server digest validation, per-player selection, interrupted-reload cleanup and remote-render metadata prevent silently mixing profile sets. Legacy remains default. Arbitrary joints, scripts, pose rules and automatic gun-pack registration are not implemented in this subset.

[Runtime format and setup](RUNTIME-PROFILES.md) · [Calibration audit](CALIBRATION-AUDIT.md) · [Pending tests](TESTING-CURRENT.md)

Validation: compilation and 164 tests passed (zero failures/errors/skips). Bundled TOML files parse, all 18 migrated profiles retain their base geometry/zone positions, and the M700 pose regression confirms offhand steering after re-grip. No JAR packaged; headset/multiplayer testing remains pending.
