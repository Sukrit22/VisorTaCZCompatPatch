# 0.4.0: physical controls and calibration

Install the same 0.4.0 addon on the server and every client. Protocol is now 7;
0.3.x cannot join a 0.4.0 server. Keep only one addon JAR. Back up and retain
`config/visor_tacz-calibration.json` and `config/visor_tacz-client.toml`.
Old grip/muzzle values migrate with zero offsets for the new interaction points.
The 0.3.2 Visor 0.5.0 main-menu transition guard is included.

## Controls menu and live panel

Run `/visor_tacz menu` once to open the settings screen. It offers grab input,
ammo/ADS display location, physical/button handling, automatic ADS, optics, and
calibration. Preferences persist. `/visor_tacz status` now writes directly to
client chat; use the live panel to test ADS without opening chat.

- `/visor_tacz display gun`: panel beside the gun (default).
- `/visor_tacz display wrist`: panel near the offhand wrist, following its pose.
- `/visor_tacz display off`: hide it.

The depth-tested panel shows magazine count, chamber count, ADS/hip-fire,
reload/handling state, and a nearby grab target. It faces the viewer for readability.
It uses Visor's hand-effect API, not a replacement for Visor's arm or a separate
framebuffer. It is display-only; use the settings screen for buttons. Its wrist
position is currently fixed relative to the controller, not calibrated to arm IK.

## Contextual grabbing and physical punching

Choose USE (existing offhand-use binding, X in the reported setup) or TRIGGER
(Visor's offhand attack action). Commands: `/visor_tacz grab use|trigger`.
This selects a Visor action; it does not globally rebind the hardware trigger.

The addon claims a press only with physical handling active, an empty assigned
offhand, and a valid magazine/rack/foregrip/selector/pouch target. The owning action
keeps its release even if the hand moves outside the zone. Away from these zones,
or while holding an item, the action passes through to Visor. GUI pointing takes
priority. Button mode no longer consumes offhand use while the offhand holds an item.

Visor's physical swing detection is separate from button input and can attack with
a bare hand. The addon suppresses offhand entity/block swings while grabbing or
near a valid interaction target with an empty hand. Elsewhere Visor decides whether
a swing punches, mines, or slashes. Torch/sword coexistence still needs headset and
two-handed-hotbar testing; this is not a guarantee about every Visor configuration.

The small yellow selector can now be operated directly with the selected grab
action. The older main-hand-use-near-selector shortcut remains available.

## Expanded calibration

Hold a supported gun and use the menu or `/visor_tacz calibrate`. Click the page
name to cycle: Grip, Bullet origin, Foregrip, Magazine, Slide/charging handle,
Fire selector, Reload pouch, Sight alignment. Save persists all pages; Reset only
resets the current page; Cancel discards unsaved edits.

Grip remains six axes; new interaction pages are XYZ offsets, limited to 500 mm
per axis. They are gun-local right/up/back, except pouch offsets, which use
head-yaw-local right/up/back. Fine/coarse steps remain 1/10 mm. The foregrip point
now drives two-hand aiming with its vertical/lateral offset, rather than forcing
the barrel along a horizontal controller-to-controller reference. It does not
teleport the tracked offhand or change Visor's arm IK.

Guides: green magazine, orange rack, yellow selector, blue foregrip, cyan pouch,
magenta sight reference. Calibration previews show them even in button mode.
Save and close before testing interaction: the server continues to use saved data.
The same saved points drive client selection and server acceptance. The magazine
page moves its interaction zone, not the physical geometry of the gun or magazine.

The reload pouch claims input only in the magazine-removed/needs-replacement stage.
It does not claim waist gestures while ready, empty-handed without a gun, or while
holding another offhand item. This leaves room for a future general item pouch.
If another addon wants the same action/location during reload, priority coordination
will still be needed; separate physical placement is supported by calibration.

## Sounds and ejection

Server-confirmed magazine removal/insertion and completed racks play short built-in
mechanical cues, audible to nearby observers. These are placeholder mechanical
sounds, not separate Glock/M4 recorded samples. TaCZ's native reload timer remains.

Firing creates cosmetic spent casings using TaCZ's shell models. Racking an occupied
chamber creates a cosmetic live round (a small generic brass model if the ammo pack
does not supply a single-round entity model). An empty-chamber rack does not eject
a live round. These world-space visuals have gravity and expire after 1.25 seconds;
they have no pickup item or collision/bounce simulation. Ejection position is an
initial offset near the calibrated rack zone, not a separately calibrated ejection port.
Ammo accounting is unchanged: each successful rack of an occupied chamber loses
that one round. The new display makes this count visible.

## Rendering, attachment menu and recoil

Gun/world and attachment-preview draws now avoid TaCZ's stencil enabling/clearing
and restore the previous stencil state. Accelerated deferred rendering is bypassed
inside that protected draw. The custom optic copy is skipped with no scope or with
a menu open. This addresses concrete shared-buffer interference, but **whole-world
flashing remains unconfirmed** and must be tested with and without shaders, before
and after opening Z. It was reported before Z as well.

Z is the physical keyboard binding for the TaCZ attachment menu in the supplied
instance. The same menu opens in VR, with our gun kept at the controller for preview;
it is not the desktop's floating camera-relative weapon pose. Firing is blocked
while the menu is open. Physical keyboard input remains available subject to
Minecraft focus and key conflicts.

VR still suppresses desktop camera recoil. Haptics remain, but this release does
not add simulated gun kick or force movement of the player's tracked controllers.

`/visor_tacz off` is not a full mod unload: rendering stability patches, remote
visuals, and the Visor lifecycle guard remain installed. For clean isolation,
restart a local single-player test world with just this addon JAR moved outside
mods, keeping Visor and TaCZ. Addon-required servers may reject a client without it.

## Prioritized test list

1. Startup, flat-to-VR menu transition; then check flashing empty-handed and with
   each gun, before/after Z, shaders off/on. Preserve each run's log.
2. Open `/visor_tacz menu`, choose wrist/gun panel, and restart to check persistence.
   Raise/lower sights for each eye: ADS should light up/down without typing. Open
   menus, sprint, reload and refocus the HMD to check it returns to hip-fire safely.
3. Calibrate the foregrip height, magazine and rack locations. Save/restart and
   verify guides, two-hand aim and server interactions agree. Cancel must revert.
4. Compare grab USE/TRIGGER. Empty-hand gestures near gun work; away from the gun
   punches still work if enabled in Visor. Offhand torch placement/sword swings and
   GUI pointing should pass through. Check both handedness settings.
5. Pass an empty offhand through the pouch while ready: no grab. Remove the magazine,
   then grab the pouch: replacement works. Move the pouch offset and repeat.
6. Observe removal/insertion/rack sounds. Count MAG+CH before and after five racks:
   only racks that started with a chambered round reduce total ammunition. Watch
   live-round ejection and spent casings through both eyes and from a flat client.
7. Test ammo conservation across switching gun, disconnect, death and mode changes;
   repeat previous aim tests and mixed VR/flatscreen observer tests.

Validation completed: Java 17 build/reobfuscation; 31 unit tests including migration,
interaction geometry and pouch ownership; production TaCZ method-descriptor checks;
standalone OpenGL stencil state restoration/nesting/error checks on this machine.
Minecraft startup, full modpack behavior and headset/multiplayer visuals remain
unverified here. No instance files were replaced automatically.
