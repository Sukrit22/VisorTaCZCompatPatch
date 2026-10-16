# alpha.5 source: four-holster foundation

Built 2026-10-14 for Forge and Fabric, protocol 32. Keep matching client/server when building it yourself. Four-holster mode is opt-in, default OFF.

Open Advanced > Four holsters / assignment rules. Enabling it enables the independent dual path. Choose ascending 1→9, descending 9→1 or manual bindings (cycle 0/disabled through slots 1–9). First two handguns map physical right/left waist; first two long guns chest/back. Each mount has its own Calibrate button. Wrong-class/duplicate manual assignments stay empty and notify once per invalid transition. Held mounts reserve their slot before remaining mounts fill.

Require empty hand defaults ON: a selected mounted-but-unheld gun counts free for this addon's draw checks; actual held guns do not. OFF allows switching from a tool without moving/dropping that tool. Other mods still see the real selected inventory item; this is not a global virtual-empty-inventory API.

Lock other's selected gun defaults ON; radial collisions and main wheel selection preserve the owning hand. Held weapons are protected regardless. Non-TaCZ collisions retain native behavior. A fresh Grip press near the mounted handle draws; holding Grip outside and entering cannot draw. Already cancelled presses are ignored. Releasing owned Grip holsters. This does not use Advanced toss-catching grace.

Slots are references only: no gun copies, inventory insertion or restoration. Choosing a weapon changes hand selection. Existing single/dual calibration files remain unchanged; new per-gun/per-mount overrides save in config/visor_tacz-four-holsters.json with old values as fallback. Assignments/options are in the existing client TOML.

Implemented: four local mount rendering and calibration, scan/manual rules, draw/selection, optional empty requirement, symmetric slot protection, offhand-unselected dual server input support. Pending: automatic per-weapon physical handling from either hand, two-gun physical handling and remote display of all four mounted guns. Current dual reload remains Button. Do not claim that changing a global mode provides physical state isolation. Handset interaction remains unverified.

INV01 (real jammed Glock returning after inventory deletion) remains unresolved. This change does not claim a fix. Test in a copied world and retain the original report.

Focused tests are H4-01–H4-06 in TESTING-CURRENT.md. Earlier passed results remain passed within their reported scope.

Validation: Forge and Fabric compilation plus 257 tests each passed (0 failures/errors). Assignment/pinning and collision policy have focused tests. No headset/client rendering or inventory-return reproduction pass claimed. Forge build/reobfuscation and Fabric build/production-JAR verification passed; both ZIP archives verified and contain FourHolsters. Builds were not installed automatically.
