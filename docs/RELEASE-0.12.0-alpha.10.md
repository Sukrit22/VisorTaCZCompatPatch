# 0.12.0-alpha.10

- Fix M700 Physical handling support-anchor drift after returning from the bolt: reset to the calibrated two-hand pose and capture the next transfer from that same restored pose. Advanced keeps pose-preserving regrip.
- Bundle the current imported gun, holster and inspection calibration without changing the user's numbers.
- Fix the build tests: bundled calibration must be valid and cover supported guns, but different guns may have different personal fits. Historical exact offsets and cross-gun equality are not release requirements.

This is a distinct release from the already-built alpha.9. Protocol remains 25. Use alpha.10 on both client and server to receive the server-side M700 fix.

Test T09-13 in TESTING-CURRENT.md: repeat ten Physical bolt/return cycles while holding support; verify no accumulating vertical shift and continued offhand aiming. Also check Advanced retains its stable return.
