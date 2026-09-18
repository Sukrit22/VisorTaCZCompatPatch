# Sharing and installing calibration

Open `/visor_tacz calibrate` while holding the gun, adjust the pages, then choose
**Save & close**. Share this file from the Minecraft instance folder:

`config/visor_tacz-calibration.json`

In CurseForge, open the instance's folder, then open `config`. Send the JSON file,
along with your addon version, headset/controllers, main-hand choice, gun and gun
pack/display variant, attachments used, and what was tested (grip, muzzle, pump,
ADS, etc.). A short screenshot/video of alignment is helpful but optional.
This JSON contains gun profile keys and offsets, not the whole modpack config.

To install a shared file:

1. Save or cancel any open calibration preview and back up your existing JSON.
2. Put the shared file in the instance's `config` folder with the exact filename
   `visor_tacz-calibration.json`.
3. In 0.6.5+, choose **Reload calibration** in `/visor_tacz menu`, or run
   `/visor_tacz reload_calibration`. Older versions require a restart.
4. Hold the matching gun and open calibration to check it. Invalid files leave
   current calibration unchanged; fix the file and reload again.

Replacing the whole file replaces all locally saved profiles. To keep other
profiles, merge only the desired top-level gun entry into your JSON, keeping
valid JSON syntax. Do not put calibration in `mods`, a world folder, or a server
config folder. Calibration belongs to each player's client; the addon sends the
selected values to the server during play.

From 0.6.3, the tested player's September 18 Glock, M4A1, and M870 profiles are
bundled as starting values for `tacz:default`. Locally saved profiles override
the bundled values as a whole, including intentional zero offsets. Missing
profiles use the bundled profile, or zero if no matching bundled key exists.
Updates do not overwrite the existing file. Saving writes the effective profile
set to the JSON, making the bundled starting values shareable too.

Offsets remain explicit: a displayed Y -10 mm / Z +30 mm stays -10 / +30, not
zero. The file stores translations in metres (-0.01 / +0.03) and rotations in
degrees. **Zero this page** explicitly zeroes only the preview's current page;
it does not restore the bundled preset. Save commits it, Cancel discards it.

The bundled source is `src/main/resources/calibration-defaults.json`. Maintainers
can add reviewed profiles there without changing gun model pivots. These values
are starting points from Pico 4 Ultra testing, not universal calibration for
every controller or custom gun model. The TA31 black-scope issue is not fixed by
this release, and sight calibration does not reposition the rendered lens.
