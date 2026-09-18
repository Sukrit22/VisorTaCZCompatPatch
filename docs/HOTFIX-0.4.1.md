# 0.4.1: panel, clear optics, and physical rack timing

Update both clients and server. Protocol is now 8 because handling sound feedback
includes the originating gun and player. Retain calibration and preferences.

## Ammo/ADS panel

The panel is automatically registered by this addon. It does not need to be added
to Visor's wrist HUD. The inspected client configuration already selected WRIST.
It now faces the actual VR eye pose, rather than the desktop camera orientation,
and renders from the gun hand's effect pass even when the offhand's world effect
is hidden. The wrist anchor still follows the offhand. Text is larger and draws
over nearby geometry so it cannot be hidden inside the arm; it may show through
occluding world geometry as an overlay.

Use `/visor_tacz display gun` or `/visor_tacz display wrist` (also available in
`/visor_tacz menu`). Other modpack HUDs are separate Visor overlay configuration;
this change does not enable or reposition those.

## Optics

During local VR gun rendering, ordinary opaque ocular meshes are hidden and their
previous visibility is restored after drawing. The custom optic pass can then
draw its aperture without an opaque black lens behind it. No stencil or framebuffer
changes are added by this visibility fix.

The inspected log had shader packs enabled. **Shader packs still disable custom
reticles and magnification.** In that case the fallback is now a clear, unmagnified
opening; the panel reports CLEAR ONLY (SHADERS). This is not working scoped aiming
with shader packs. For the custom dot/crosshair and lens magnification, turn shaders
off and leave `/visor_tacz optics on`. Optics OFF also leaves a clear opening.
Normal scope eye-relief blackout can still occur outside the valid eye position
when custom scope rendering is active. Test dot and scope separately in each eye.

## Rack timing and sound

Reaching the 5.5 cm backward threshold ejects the currently chambered round
immediately and once. The chamber stays empty while held back. Releasing closes
the action and feeds the next magazine round. A fast completed pull arriving only
in the release packet uses the same one-time transition. A short pull does nothing.
Interrupting after full pull does not restore the ejected round or consume a
magazine round; after tracking loss another complete rack may be needed to feed.

Mechanical sounds now use TaCZ's installed Glock/M4 sound assets: magazine out,
magazine in, action pull, and action return. No piston sample is used and no TaCZ
audio is copied into the addon. Sound/ejection events are server-confirmed.

## Test first

1. Set display GUN, then WRIST. Hold a supported gun and verify MAG/CH and ADS are
   readable without configuring a Visor wrist overlay. Raise/lower sights.
2. With shaders off, test dot and scope through both eyes. With shaders on, confirm
   the clear unmagnified fallback and panel message. Record the exact optic ID for
   any remaining black obstruction.
3. With CH=1, pull fully and keep holding: one live round ejects and CH becomes 0,
   while MAG stays unchanged. Release: CH becomes 1 and MAG decreases by one if
   ammo remains. A short pull should not eject or change ammo.
4. Listen for gun-specific magazine/pull/return sounds. Repeat with a flat observer.
5. Recheck flashing before/after Z and with shaders off/on. The user reported no
   flashing in their 0.4.0 session; this is positive feedback, not a universal result.

Validation: Java 17 build, 35 tests (including pull/eject/feed ammo transitions),
all eight selected TaCZ sound assets, and unchanged optic-shader/stencil diagnostics.
Panel and optical appearance are not headset-verified here.
