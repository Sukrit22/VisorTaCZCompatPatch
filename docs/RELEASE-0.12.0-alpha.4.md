# 0.12.0-alpha.4 — calibration controls

Protocol 20 unchanged. Includes the alpha.3 holster floor/grab fix.

- Holster calibration now has **Fine: 1 cm / 1 degree** and **Coarse: 10 cm / 10 degrees** steps.
- Both gun and holster calibration start with **Save** and **Cancel**. Save writes to the corresponding JSON file immediately and leaves the screen open.
- After a successful save, Save becomes **Close**. Changing calibration values changes it back to Save. Switching pages or step size alone does not make saved data dirty.
- Cancel closes and discards changes made since the last successful save. It does not undo already saved data.
- Failed saves remain in the editor with an error; they do not mark the preview as saved.

Files: `config/visor_tacz-calibration.json` for gun calibration; `config/visor_tacz-holsters.json` for holsters.

## UI retest

1. Save a changed value. Confirm the screen stays open and the button reads Close; inspect/reload the file to confirm persistence.
2. Switch pages and Fine/Coarse. Close should remain Close.
3. Edit a value (including scale lock or a reset/default action). Save should return.
4. Cancel after another edit. Reopen and confirm the last saved value remains.
5. In holster calibration, compare one Fine and one Coarse click on position and rotation.

Existing headset checks in TESTING-CURRENT.md remain pending; this update does not imply new in-game passes.
