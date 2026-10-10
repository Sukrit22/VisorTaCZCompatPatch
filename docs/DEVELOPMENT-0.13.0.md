# Current development changes (0.13.0 source)

## Desktop calibration

Open gun calibration while playing flatscreen. Drag the preview to orbit and scroll to zoom. Click X/-X, Y/-Y, or Z/-Z for axis views; keyboard X/Y/Z selects the positive view and Shift selects the opposite view. The camera rotates smoothly and keeps its zoom. Guides draw over the gun; the selected zone has a thicker white outline. Tan outlines mark an approximate controller/grip hand, not an anatomical hand model. The pouch is head-relative and still needs VR checking.

Magazine grip calibration has its own six position/rotation controls and preview. It saves to `config/visor_tacz-magazine-grips.json`. The existing three-file calibration import script does not import this new file. Magazine grip offsets are currently local; remote players use the default loose-magazine grip.

## Experimental mechanisms to test

- Lone Eagle: rotate the breech; opening ejects spent ammunition.
- DB-2 / DB-4: hinged opening, Use plus flick, spent-round ejection; Grip retrieves up to two shells, Trigger one. Native ammunition counts remain authoritative; individual left/right barrel identity is not tracked.
- DB-2: with an active analog trigger binding, 33% requests one shell and 90% the second; release below 10% to rearm. Uses native single-shell mode and cooldown. Digital bindings retain their existing behavior. Test focus loss, holstering, changing slots, opening the gun, and releasing before the second shot: none should cause a delayed unintended shot.
- SPAS-12: pump and native semi-auto selector modes. M1014: semi-auto with manual action handling. AA12: magazine loading with native open-bolt ammunition behavior.
- CZ75: while holding a loose magazine, offhand Use flips it 180 degrees. Release a flipped magazine at the support zone to show the front magazine. This is cosmetic, not a separately tracked ammunition reserve. Physical mode starts without that front magazine.

These mechanisms need in-game testing. Client and server must use matching builds (network protocol 28).

## Calibration and build history

The user's import script preserves bundled gun entries missing from the saved file, while saved entries replace matching bundled entries. It is a top-level merge, not a recursive merge of fields within a gun. Imported resources are preserved in this update.

`build/libs` is generated output, not a release archive. Missing old JARs do not affect saved calibration or worlds, but remove convenient rollback/comparison copies. Keep important tested JARs outside `build`, such as in a separate release archive. Git can recreate committed source states; it cannot reproduce an uncommitted historical build exactly.

Run the normal build with tests enabled after importing calibration:

```powershell
Set-Location 'E:\visor-compat\visor-compat-tacz'
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat build --offline --console=plain
```

Do not use `-x test` for release validation. The profile inventory is now 25 physical and 29 button-only bundled guns; tests check the five newly migrated guns explicitly.
