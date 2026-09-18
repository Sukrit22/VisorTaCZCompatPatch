# 0.6.0 — M870 physical-handling demo

Install on **all clients and the server**. Protocol is now 10. Existing Glock/M4
features and calibration JSON remain supported. This is an untested-in-headset
demo for the default TaCZ M870 model, not arbitrary replacement gun packs.

## M870 controls

Enable physical handling with `/visor_tacz menu`. Existing Use/Trigger grab
selection applies. The assigned offhand must be empty.

1. Grab at the waist pouch to show one shell in the offhand.
2. Move to the loading port under the receiver and release. One compatible shell
   is consumed and added to the tube. Repeat to load more shells.
3. Grab the fore-end. Pull rearward at least 8 cm, then push forward to within
   approximately 2 cm of its starting position. Feeding happens on the forward
   stroke, not when releasing the grip.
4. Keep holding the fore-end for two-hand support and shooting. After each shot,
   repeat the back-and-forward pump cycle before the next shot.

The normal capacity is derived from TaCZ, including capacity-changing attachments.
Ammo extraction uses TaCZ's inventory/ammo-box and dummy-ammo helpers. Its creative
ammo-check setting is respected. The tube and chamber are counted separately.
Shell loading does not directly load the chamber; pump to chamber after an empty
reload. Direct breech loading is not included in this demo.

The held shell is a preview until insertion. An invalid release, lost tracking,
focus change, full tube, or missing inventory ammo cannot consume a shell. There
is no dropped inventory shell to recover on cancellation. One valid insertion
consumes one shell; repeat grabs are required for additional shells.

## Pump state

- Partial rear strokes do not eject or feed.
- Full rear stroke ejects at most once. Keeping the pump back does not repeat it.
- After a shot, the spent shell is retained until the pump opens. The nine pellets
  do not produce nine shell ejections.
- Pumping a live chamber ejects that live shell instead.
- Releasing after opening leaves the pump open. Re-grab the rearward fore-end and
  push forward to finish; cancellation never silently chambers a shell.
- TaCZ's timed automatic bolt action is suppressed only for M870 in physical VR
  mode, on client and server. Button mode and flatscreen retain native behavior.
- Leaving physical VR clears the extra pose/spent markers; native chamber/tube
  ammunition values remain intact for normal TaCZ controls.

## Visuals, calibration and compatibility

The default M870 profile uses its model's hand and muzzle pivots. The pump moves
the `slide2` part used by TaCZ's own bolt animation. The original floating reload
hand/ammo group is hidden during physical rendering. The shell in the offhand is
rendered from TaCZ's ammo entity model when available, then the shell model, then
a simple red fallback. Pump and insertion sounds reuse M870 sound samples.

The normal local/remote gun rendering paths both include the pump and held shell.
The live status panel shows **TUBE**, **PUMP TO CHAMBER**, **PUMP OPEN**, or
**INSERT SHELL** where appropriate.

Calibration has contextual **Shell loading port** and **Pump grip** page labels.
Grip, muzzle, ejection port, pouch, sight and support offsets remain available.
The starting pump/port grab zones need headset testing and may need calibration.

Gun Durability is optional. With verified gundb 2.2.2, completing a pump cycle can
clear its generic jam. M870 does **not** inherit the Glock/M4 detachable-magazine
double-feed sequence or their three visual jam types. `/visor_tacz_test jam`
can apply a generic M870 test jam; double_feed/dud variants are rejected for M870.

## Test when VR is available

1. Empty gun: insert one shell, confirm TUBE increases but chamber stays empty.
2. Try a partial pump: no ejection/feeding. Complete back + forward: one shell
   moves from tube to chamber.
3. Fire: check one shot, no immediate casing. Pump back: exactly one spent shell.
4. Hold rearward for several seconds: no extra ejections. Release while open:
   firing remains blocked. Re-grab and push forward: chambering resumes.
5. Load to capacity, try one more insertion: no extra inventory consumption.
6. Cancel a shell grab, change weapons, lose focus, and reconnect: no duplication
   or lost preview shells. Test live-round ejection separately.
7. Check fore-end aiming and the flatscreen friend's view of pump/shell motion.
8. Switch to button mode / flatscreen: verify normal TaCZ reload and bolt action.
9. Regression-check Glock/M4 magazine handling and existing jam types.

## Verification

Build and 57 unit tests pass, including ordered pump strokes, partial/interrupted
strokes, repeated rear holds, empty-tube feeding and insertion capacity checks.
New injection targets were checked against the installed TaCZ production JAR;
sound resource names were checked against the default pack. Dedicated-server
startup reached the EULA gate without a launch error; it did not load a world.
No live Minecraft gameplay, VR, or multiplayer visual test was performed.
