# 0.7.0 — scale, interaction boxes, and gun-specific hints

Requires the same pinned Visor 0.5.0 and TaCZ 1.1.8-hotfix dependencies.
Network protocol is now 11: update the addon on all clients and the server.
Existing calibration JSON loads without zeroing offsets; missing scale defaults
to 100% and missing box sizes receive defaults. No instance was auto-updated.

## Calibration

Open `/visor_tacz calibrate` with a supported gun. Gun scale is uniform,
50–150%, anchored around the firing-hand grip. It scales the gun, muzzle,
sight, and gun-mounted interaction positions. The head-relative pouch and
interaction-box dimensions remain independently adjustable.

On an interaction page, switch between Position and Box size. Width, height,
and depth are full X/Y/Z dimensions, shown in millimetres. They follow the
gun axes, except the pouch, which follows head yaw. Detection uses these boxes
on both client and server. The previous spherical tolerances are replaced by
boxes, so check the corners as well as the center. VR world scale still applies.
Box sizing changes the grab area, not the required rack/pump stroke distance.

Save & close persists everything in `config/visor_tacz-calibration.json`.
Cancel discards the preview. Default box size resets only that box; Reset 100%
resets scale. Existing reload-calibration controls also reload these fields.

M870 support now uses the same position and box as its pump. The separate
support page is omitted. M4 has a support and selector page; Glock and M870
omit unsupported selector actions. The M870 pink ejection marker is visual,
not a casing-grab action, so it has no adjustable interaction box.

## Editing hints

Edit `src/main/resources/calibration-hints.toml`, then rebuild and restart.
It is bundled into the JAR, not loaded from the instance config folder.
Each gun/action has its own `title`, `hint`, and hexadecimal `color`:

```toml
["tacz:m870".rack]
title = "Pump / support"
color = "#FF9900"
hint = "Place this box around the pump where your offhand grabs it."
```

The menu displays up to three lines; hover the page title for the full hint.
Guide colors follow this file. Supported actions come from `CalibrationLayout`
and handling code: text cannot enable an unsupported mechanism. Adding a new
gun still requires a code profile. This keeps explanatory text independently
editable without accidentally changing server behavior.

The purple point is the calibrated sight reference. Put it at the intended
sight/eye alignment position, with its line pointing along the barrel. Bring
either eye into the box behind it and look along the gun for automatic ADS.
Box highlighting checks position only; ADS also checks look direction. This
does not move the rendered scope lens or fix black optics.

## Shotgun cartridge visual

Unfired M870 ejection and the pouch preview now use TaCZ's 12-gauge shell asset.
The generic projectile model caused the pistol-like appearance. Live and spent
shells currently reuse the casing asset; separate capped-live geometry remains
future work. Ejected visuals are not recoverable ammunition items.

## Test checklist

1. Load old calibration: existing numerical offsets should remain unchanged.
2. Try gun scale at 80%, 100%, and 120%: grip stays anchored; muzzle and shot
   origin should follow the resized gun. Check a remote flatscreen observer too.
3. Change one box dimension at a time; grabbing should match its visible bounds.
4. M870: hold the forward pump for support aiming, then rack back and forward.
   Test unfired ejection, fired ejection, and shell insertion from the pouch.
5. Align the purple sight guide; test both eyes and lowering the gun out of ADS.
6. Save, restart, and reload calibration. Check scale and box dimensions persist.
7. Turn debug cubes off: guides disappear while physical handling still works.

Build and 71 unit tests pass, including JSON backward compatibility, box
bounds, scale propagation, pump/support alignment, and network serialization.
Headset behavior and visual alignment still require user testing. ACOG and
shader-pack optics limitations are unchanged.
