# 0.4.3 — Gun Durability jam clearing and status

Includes 0.4.2 HUD/Gun/Wrist/Off options and all previous fixes.

## Physical jam clearing

Optional compatibility with the inspected Gun Durability (`gundb`) **2.2.2**.
With physical handling enabled, a supported Glock/M4 that is already jammed at
the start of a rack can be unjammed by a complete pull followed by a valid release.
The server validates the existing calibrated grab/travel and current held item.
Partial pulls, switching guns, losing valid tracking, or an occupied offhand do
not complete an unjam. Holding the action open does not clear it until release.

The adapter clears gundb's existing `Jammed` flag and sends its native
`SyncJammedPacket`. This mirrors the state change in its UnjamPacket handler;
it replaces the desktop unjam animation with the physical gesture. It does not
add a second jam chance system or repair durability. Normal rack ammunition
rules still apply: a chambered round ejects at full pull and another feeds on
release if available. It does not reset TaCZ heat or restore ammunition.

The addon remains usable without gundb. Unknown gundb versions keep their normal
unjam controls; physical unjamming is disabled with a log message rather than
guessing a changed integration API. Flatscreen controls remain unchanged.

## Status

All three display locations use the same labels, in priority order:
JAMMED, OVERHEATED, RELOADING, ACTION OPEN, NO MAGAZINE (or NO MAG | CHAMBER LOADED),
EMPTY, EMPTY CHAMBER. Otherwise the usual ADS/HIP FIRE state appears.
The empty-chamber requirement applies to physical handling; ordinary TaCZ button
mode is allowed to perform its own automatic chambering. These are state labels,
not a diagnostic for every possible network/mod/input rejection.

No new jam/empty audio cues or visual malfunction animations are added yet.
Existing rack sounds remain. Gun Durability's own cues may still play.

## Test later in VR

Install 0.4.3 on both client and server to obtain the new server behavior. Wire
protocol remains 8, but an older server does not implement physical unjamming.

1. Hold Glock/M4. Enable Physical handling and any status display.
2. With cheats/operator permission, use `/visor_tacz_test jam`. This applies a
   real gundb jam to the held gun; it requires gundb 2.2.2 and a matching profile.
3. Check JAMMED. A partial pull should leave it jammed.
4. Fully pull and hold: ejection happens once, and JAMMED remains until release.
5. Release with valid tracking: jam clears and feeding follows normal ammo rules.
   Check that gun durability did not increase.
6. Repeat and interrupt by switching guns or losing tracking: no unjam should
   complete. Confirm normal flatscreen unjam still works as a fallback.
7. Empty reload: after magazine insertion, expect EMPTY CHAMBER until charged.
8. Heat-capable guns keep TaCZ's original heat restriction; racking cannot cool
   them. This release does not add heat to guns without it.

Validation: compilation, unit tests, and installed gundb adapter signatures/state
handler inspected. No Minecraft runtime or headset test performed for this release.
