# 0.12.0-alpha.7 — handling follow-up

Protocol changes from 22 to 23; install alpha.7 on both clients and server.

- One menu cycle: Button, Physical, Advanced. Existing saved booleans migrate without resetting user settings. Experimental prop ownership remains Advanced-only.
- Advanced magazine pistols can reload while held by the offhand or resting in their holster. Main Use releases the magazine; main Grip retrieves/inserts a replacement and operates the slide. These mounted states cannot shoot; airborne reloading is disabled. Existing reserved-ammo recovery is reused.
- A second Use after magazine removal can close a locked slide without inserting or inventing ammunition.
- Both hands can share a body carry and transfer ownership on release. Holstering uses the actual gun handle position. Offhand radial selection refuses the slot occupied by the held gun.
- Advanced bolt re-grip acquisition shrinks to 4 cm, with haptic re-arm beyond 5.5 cm. Physical re-grip restores snapping to the main controller.
- Holster downward crouch following is two-thirds of head displacement. Saved offsets remain unchanged.
- Inspection and generic carry use render poses instead of tick poses for smoother movement.
- Gun, holster and inspection calibration editors show only Close after Save; further edits restore Save + Cancel.
- Advanced revolvers support rightward wrist-flick cylinder closure and muzzle-up gravity unloading. Pink eject-zone pulls send the actual remaining live-round/case count backward with separate cosmetic props. Lonetrail retains its breech action, without flick closure. Loose props cannot be picked up; live rounds are removed from native gun ammo once.
- Bundled all 54 latest user gun calibration entries without normalizing offsets or scales. Glock inspection copied to seven conventional magazine pistols, Deagle to its three variants; Glock holster copied to all 14 pistols, M4 holster to the other 40 default guns. Local overrides win. Instance files were not overwritten.

See TESTING-CURRENT.md for carried-forward results, AH52–AH59 and detailed AH08 instructions. Headset and multiplayer testing is still required.

Validation: Java compilation and 157 unit tests passed (0 failures/errors/skips). Git whitespace check passed. Bundled gun calibration was compared structurally against the active instance and matches all 54 entries exactly. VR gestures, UI behavior and multiplayer still require in-game testing.
