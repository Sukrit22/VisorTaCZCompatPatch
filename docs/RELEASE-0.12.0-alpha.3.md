# 0.12.0-alpha.3 — holster height and grab alignment fix

Protocol 20 unchanged. This fixes a bug introduced in alpha.2's holster rendering; no gun calibration or inventory data is rewritten.

## Cause and fix

Visor's `GameRendererMixin.visor$setupCameraEntity` temporarily puts the camera entity at the current eye pose. Alpha.2 rebuilt the holster during rendering using `Minecraft.player.getY()` as its floor height. Rendering could therefore put the gun above the headset, while Grip checked the different pose computed during the game tick.

Holster height now uses `VRPlayerPose.getOrigin().y()` (the VR room floor), and rendering uses the exact holster snapshot that Grip detection and remote synchronization use. Holster offsets are VR metres multiplied by world scale once; player bounding-box height is no longer an extra multiplier. The menu now labels Y as **Height above VR floor**.

## Updating an instance already adjusted to compensate

Restart Minecraft with the new JAR. Under Advanced Handling → Calibrate holster, select **Defaults**, then **Save**, for affected guns. The user's inspected Glock and M4 entries were both approximately zero metres; those entries are preserved, so leaving them unchanged would now put the handle near the floor.

Default heights: Glock/pistol **0.94 m**, M4/long gun **1.04 m**. Set just Height if you prefer to keep other custom holster offsets. This does not require resetting the separate grip/muzzle/gun calibration.

## Retest

- **AH28:** Reset Glock holster defaults; close the menu. Confirm it appears at waist height, and Grip draws it at its visible handle.
- **AH29:** Repeat with M4 or P90 at the chest; holster and draw repeatedly.
- **AH30:** Change holster Height, Save, and re-grab at the new visible position. Rendering and interaction should remain aligned.
- **AH31:** If using non-default VR world scale, confirm height scales once rather than growing with an extra player-height multiplier.

Headset verification remains pending. Previous alpha.2 tests and advanced-handling limitations still apply.
