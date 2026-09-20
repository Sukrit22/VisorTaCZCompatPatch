# 0.9.3 handling follow-up

Install matching 0.9.3 client/server builds (protocol 17). Existing calibration is preserved. No automatic installation or Git push is performed.

- Held racks re-arm after a full forward return; holding at the rear does not repeatedly eject rounds.
- MP5 can close/feed while still holding the handle. Releasing at the rear retains the existing latched/slap workflow.
- M700 accepts a wider forward/down movement. A downward sweep over the closed, raised bolt handle lowers it without Grab. Returning the firing hand near the grip automatically restores main-hand ownership once the bolt is closed and unjammed; it does not snap to a hand still at the bolt.
- Pistol support is activated by touching the support region; Grab remains optional. Two-hand ADS still requires eye alignment.
- Physical magazine insertion transfers ammo immediately through TaCZ's inventory/dummy-ammo API, respecting capacity, creative/infinite settings and cancellable reload events. It preserves the chamber and removed magazine rounds. Empty reloads still require chambering. This bypasses the second timed native reload animation for supported physical profiles; custom reload scripts are not automatically generalized. Flat/button reloads retain their native path.
- Detached magazines turn sideways 30 degrees in the hand. Jam casing ends reverse 180 degrees. Existing casing-scale calibration remains available.
- Manual-action jam errors now explicitly explain that M870/M700 only implement generic durability jams. Dud/double-feed commands do not apply a jam to those guns. Use `/visor_tacz_test jam` for their generic test. Glock/M4/MP5 retain the three simulated jams. Dud means a failed round with the action closed, so no protruding obstruction is expected.

## Verification

Java 17 Gradle build passed with 103 tests, zero failures/errors. Headset gestures, native inventory integration under the user's modpack, casing orientation and reconnect ammo accounting require in-game verification. See TESTING-CURRENT.md for carried-forward results and priority tests.
