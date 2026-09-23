# Bundled pistol calibration audit

All 14 base-pack pistols have a gun preset and a holster preset. All 10 magazine pistols also have inspection presets. Cylinder/breech guns have no pistol slide-inspection preset. Presence is not proof of an individually fitted or headset-tested profile.

**Reported gun: P320 (`tacz:p320`).** Its gun preset still matches the older Glock-derived starting preset used by M1911/M9A4/B93R/MK23. It is not missing from the JAR, but it needs its own grip/zone fit check. Its newer inspection and holster presets are copied from Glock. No blind offsets were applied for this report.

| Gun ID | Gun scale | Grip XYZ (mm) | Inspection | Holster |
|---|---:|---|---|---|
| `glock_17` | 65.0% | 0.0, 60.0, -10.0 | Bundled | Bundled |
| `m1911` | 75.0% | 0.0, 80.0, -20.0 | Bundled | Bundled |
| `p320` | 75.0% | 0.0, 80.0, -20.0 | Bundled | Bundled |
| `m9a4` | 75.0% | 0.0, 80.0, -20.0 | Bundled | Bundled |
| `b93r` | 75.0% | 0.0, 80.0, -20.0 | Bundled | Bundled |
| `cz75` | 65.0% | 0.0, 50.0, 30.0 | Bundled | Bundled |
| `hk_mk23` | 75.0% | 0.0, 80.0, -20.0 | Bundled | Bundled |
| `deagle` | 65.0% | 0.0, 55.0, 10.0 | Bundled | Bundled |
| `deagle_golden` | 75.0% | 0.0, 55.0, 10.0 | Bundled | Bundled |
| `timeless50` | 75.0% | 0.0, 55.0, 10.0 | Bundled | Bundled |
| `rhino357` | 75.0% | 0.0, 0.0, 0.0 | Not applicable | Bundled |
| `taurus500` | 75.0% | 0.0, 0.0, 0.0 | Not applicable | Bundled |
| `taurus943` | 65.0% | 0.0, 60.0, 30.0 | Not applicable | Bundled |
| `lonetrail` | 75.0% | 0.0, 0.0, 0.0 | Not applicable | Bundled |

The bundle also includes gun calibration for the other 40 base guns. Exact source values remain in `src/main/resources/calibration-defaults.json`, `inspection-defaults.json`, and `holster-defaults.json`.

## Checking the player report

1. Confirm item ID and default display (P320 is `tacz:p320`, `tacz:default`). External packs or alternate displays need their own keyed entry.
2. Back up the instance config files. Open `/visor_tacz menu` -> Advanced Handling -> Apply bundled calibration to this gun to explicitly replace only that gun’s normal calibration. This does not replace inspection or holster overrides.
3. A saved `config/visor_tacz-calibration.json` entry wins over bundled values. Updating a JAR does not overwrite personal calibration. Applying the bundle may still leave P320 needing adjustment because the bundled profile is a starting fit.
4. Adjust grip/rotation/scale, then magazine, rack, muzzle and sight zones. Save. Send the `tacz:p320|tacz:default` entry from the instance calibration JSON, with addon version and handling mode. Share inspection/holster files separately if those poses are wrong.

Do not delete the entire calibration file to fix one pistol.
