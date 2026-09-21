# Calibration import: 0.11.0

Imported from the active `only Visor+TaCZ+addons-noTaCZcompat` instance, `config/visor_tacz-calibration.json`, modified 2026-09-21 00:46:35 local time. Original source SHA-256: `82a746c826d0fe44dcbb653254bdf20ec9166535bf7e5155b8c23c369ea9172d`.

All seven objects were merged into `src/main/resources/calibration-defaults.json`, preserving every saved value, including zero values, grip offsets, interaction positions, dimensions, scale lock, gun scale and casing scale. Nothing was baked into model pivots or reset to zero. The instance file was not modified.

- `tacz:deagle|tacz:default`
- `tacz:glock_17|tacz:default`
- `tacz:hk_mp5a5|tacz:default`
- `tacz:m4a1|tacz:default`
- `tacz:m700|tacz:default`
- `tacz:m870|tacz:default`
- `tacz:taurus943|tacz:default`

Local user calibration entries take precedence over bundled defaults. New users receive these starting defaults; existing users retain their saved values. To intentionally adopt a bundled profile, back up the local JSON and remove only that profile entry, then use Reload calibration. Saving first may recreate the old override.
