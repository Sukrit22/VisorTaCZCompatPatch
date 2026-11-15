# Profile/model review — 0.14.2-alpha.2

Reviewed all 70 bundled entries against the TaCZ 1.2 default pack, including model parts, parent chains, native animation channels, shot direction/travel and configured sound resources. This is static/code validation, not headset acceptance. The detailed [audit table](generated/PROFILE-AUDIT.md) links conceptually to the full evidence in [JSON](generated/PROFILE-AUDIT.json).

## Corrections

- **AA-12:** the old action referenced `bolt`, a small separate indicator, rather than the top `charge_handle`. The orange zone now targets the top handle geometry. Manual movement couples the separate indicator with ratio -0.5707143. Native shot motion uses negative bolt travel and a separate, shorter handle excursion. Jam and locked-open rendering also respect the signed ratio.
- **M1014:** preserve its `Bolt` action, with native shot excursion rather than the generic pistol travel. Its fore-end is not a moving pump.
- **SPAS-12:** preserve separate `pump` / `bolt2` parts. Shot cycling only runs in native BURST mode (the pack's self-loading mode), not SEMI (pump). An independent grab zone for its small side charging handle is **not implemented**; physical cycling still uses the pump template.
- **M700:** move `bullet_and_bolt`, the native animated group, so its chamber round follows the bolt. The group has the same pivot as the old child `bolt`; personal grip calibration is unchanged.
- Other configured shot excursions now use native animation peaks instead of a shared 0.055 m value. Shot timing remains the addon's existing envelope, not an exact replay of every native keyframe.
- Parent-owned bolts inherit movement once. Sibling bolts receive explicit movement; MP7/MK18/M4-style child bolts no longer need an extra stroke.
- Replaced nonexistent button-profile placeholders with actual part references or explicit empty fields where the part does not exist. This does not enable physical handling by itself.
- Short physical-action sounds can now be authored in TOML, including shared sound folders. AA-12 and UMP use explicitly documented borrowed short clips because their native pack primarily supplies full action tracks. Native clips that combine an entire charging action are played once, not once for each half.

## New profiles

All 16 TaCZ 1.2 additions now have explicit entries. Twelve use existing physical templates as **untested calibration candidates**:

| Gun | Template / key model parts |
| --- | --- |
| AK-12 | Magazine; `bolt` |
| AK-74M | Magazine; `bolt` |
| HCAR | Magazine; `pulls` + separate `bolt` |
| MP7A1 | Magazine; `bolt_pull` with child `bolt` |
| MCX Spear LT | Magazine; `bolt_pull` with child `bolt` |
| MK18 | Magazine; `mk18_pull` with child `mk18_bolt` |
| QR-24 | Magazine; `bolt_group` |
| SCAR Mk20 | Magazine; `bolt` |
| SVD | Magazine; `bolt_bullet` |
| URGI | Magazine; `mk18_pull` with child `mk18_bolt` |
| M1897 | Pump; `slide` already owns its bolt |
| TEC-9 | Native open-bolt magazine handling; forward shot excursion |

Four remain button-only:

- **HM50B:** native straight-pull action; the current sniper template requires a lift/turn cycle.
- **MRAD:** separate rotation group `bolt_fix` and translating child `bolt`, requiring independently configured joints.
- **M1866:** lever-action and tube-loading sequence.
- **PKM:** belt and cover/loading sequence.

Total: **39 physical-template profiles, 31 button-only profiles**. Advanced ownership can wrap supported physical templates, but this does not certify every new gun or dual physical mechanism in VR.

## Calibration and overrides

Existing calibration, holster and inspection entries were preserved. Sixteen new entries received initial defaults; none is marked calibrated. TEC-9 gets pistol holster/inspection defaults; long guns get the existing long-gun holster default. Generated grip/zone positions are starting estimates, not measured hand fits.

`config/visor_tacz/profiles/*.toml` overrides a bundled entry in its entirety. If an old custom AA-12 entry exists, merge the updated parts, visual fields and zone into it and restart. Calibration JSON remains a separate overlay, so an old rack offset can still shift the new orange zone. Do not reset all personal calibration to obtain this fix.

## Checks available without VR

1. Open desktop calibration for AA-12; inspect the orange zone on the top handle from side/top views. Existing user offsets may need adjustment.
2. Inspect M1014 and SPAS-12 action geometry, plus M700's bolt/chamber group.
3. Inspect the new guns' grip, muzzle, magazine, support and rack zones; save your own fits without marking untested physical behavior as verified.
4. Run `python scripts/audit-gun-profiles.py build/tacz120-default-pack.zip` against the same extracted pack, or pass another pack ZIP. It reports missing active nodes/sounds, reversed shot travel and overlapping parent/child shot transforms.

## Pending VR tests

- AA-12 top-handle acquisition, full rack, firing excursion, empty/locked state and both jam types; confirm the indicator does not move backward relative to the intended native animation.
- M1014 firing and manual charging; SPAS pump mode versus self-loading mode.
- M700 bolt lift/back/front/down with the chamber round; verify no regression in main/support hand ownership.
- MP7/MK18/MCX/URGI strokes: child bolt must not travel twice as far as its parent.
- Each new physical gun: ammunition counts during reload, live-round ejection, interruption/rejoin, sound cues, two-hand support and ADS.
- TEC-9 must not invent an extra chambered round; M1897 must retain pump/tube ammunition behavior.

No instance configuration or installed JAR was changed by this review.

## Build validation — 0.14.2-alpha.2

All four offline builds passed: Forge 1.20.1, Fabric 1.20.1 and Fabric 1.21.1 each passed 356 tests; NeoForge 1.21.1 passed 357. No tests were skipped. The model audit checked 70 profiles with zero errors; negative checks confirmed detection of a missing part, reversed shot travel and missing sound.

Artifacts and SHA-256 hashes are in `all/build/libs/0.14.2-alpha.2-tacz1.2/manifest.json`. Each packaged JAR was checked for all 70 profiles and the corrected AA-12 charging-handle mapping. These checks do not establish client startup or VR acceptance for alpha.2.
