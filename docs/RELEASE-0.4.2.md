# 0.4.2 — selectable normal HUD output

Includes all 0.4.1 fixes. Adds **HUD** to `/visor_tacz menu` → Ammo / ADS panel.
You can also use `/visor_tacz display hud`.

- **GUN:** direct world panel beside the gun.
- **WRIST:** direct world panel near the offhand; no overlay addon required.
- **HUD:** normal Forge GUI overlay named `visor_tacz:ammo_ads`, drawn at the
  top-left of the GUI (8, 8). This provides ordinary GUI content for overlay
  capture/remapping addons. It does not place itself on the wrist.
- **OFF:** no addon ammo/ADS panel.

Only the selected output is drawn. The setting persists in the client config;
existing Gun/Wrist/Off settings are preserved. HUD uses the same live ammo,
chamber, ADS, reload and grab state as the world panels. It is visible only in VR
with compatibility enabled and a supported gun held, with no menu open. F1 hides
the HUD output. Flatscreen behavior remains unchanged.

The supplied `callistooverlays.yml` is a base Visor overlay preset, not a separate
mod. Its custom `hud` uses Visor's built-in `hud` template and an OFFHAND pose.
It captures a framebuffer rectangle at (568, 871), size 789 x 209. That existing
bottom-screen crop does not include this addon's top-left HUD panel. Create a
separate HUD overlay or adjust its screen region to include the panel, then set
its arm pose in Visor. Screen-region coordinates are framebuffer pixels, whereas
our (8, 8) position uses scaled GUI coordinates, so do not copy those numbers
directly into the crop without accounting for GUI scale. No Callisto dependency
is required. In-game capture/placement remains untested.

## Test

1. Hold a Glock or M4 in VR, select HUD, then close the menu. Check the GUI output
   or Visor HUD overlay preview for MAG / CH and ADS / HIP FIRE.
2. Configure the Visor HUD overlay to place the panel on your arm. Shoot, aim, reload
   and rack the gun; the values should update live.
3. Cycle Gun → Wrist → HUD → Off: only one output should appear, and Off should
   hide all addon status panels. Restart to check that the chosen mode persists.
4. Switch to flatscreen or `/visor_tacz off`: the custom HUD should disappear.

Network protocol remains 8, so a 0.4.1 server can accept the 0.4.2 client. Servers
can also update to 0.4.2. Do not retain two addon JARs in the same mods folder.

The build and existing 35 unit tests pass. GUI capture and wrist placement require
in-game verification; no headset runtime test was performed.
