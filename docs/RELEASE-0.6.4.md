# 0.6.4 — hide debug cubes

Open `/visor_tacz menu` and select **Debug cubes: OFF** to hide all colored
interaction guides, including the pouch and calibration markers. Switch ON
again when you want to calibrate. The default remains ON for existing behavior.

The preference is saved locally as `debugCubes` in
`config/visor_tacz-client.toml` and persists across restarts. It only affects
guide rendering: grabbing, pumping, loading, ADS, and saved offsets still work.
It does not hide the ammo panel or held magazines/shells.

Includes 0.6.3 bundled calibrations and earlier fixes. Network protocol remains
10. The TA31 black-scope issue remains unresolved.
