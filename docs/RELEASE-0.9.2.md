# 0.9.2: choose when M700 hand transfer is allowed

Install 0.9.2 on all clients/server (protocol 16). Existing calibration is unchanged.
Includes all 0.9.1 features and fixes; see RELEASE-0.9.1.md for those controls.

Open `/visor_tacz menu` and select **M700 transfer**:

- **BOLT NEEDED** (default): begin support-hand transfer only after a shot
  leaves a spent case, with an empty chamber, an open/lifted bolt, or a jam.
  When ready to fire, the gun stays attached to the main controller.
- **ANYTIME**: previous behavior; transfer whenever the support grip is held.

This governs both automatic transfer by moving the main hand away and explicit
main-hand Use. It does not restrict tracked real-world hand movement. Once a
transfer begins, completing the bolt cycle does not snap the rifle back: return
using main-hand Use near the grip, or release offhand Grab as before.

Saved in `config/visor_tacz-client.toml` as `m700TransferAnytime` and synchronized
per player to the server. Takes effect without restarting. Other guns and
flatscreen controls are unaffected.

## Focused headset tests

- [ ] Ready loaded M700, BOLT NEEDED: hold fore-end and move main hand away;
  gun remains on main hand. Main Use must not bypass the restriction.
- [ ] Fire once, keep holding fore-end: automatic transfer and main Use work.
  Complete bolt cycle; gun remains supported until explicitly returned.
- [ ] Repeat with empty chamber, partially lifted/open bolt, and generic jam.
- [ ] ANYTIME: transfer a ready loaded rifle, then return normally.
- [ ] Toggle, restart and reconnect; preference persists per player.

Build/unit results are reported in the delivery message; headset testing remains
pending. The existing 0.9.1 F01-F08 checklist remains open where not reported.
