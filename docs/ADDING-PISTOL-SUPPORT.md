# Adding pistol support: M1911 first, then the remaining default-pack pistols

> Historical walkthrough: the default-pack pistol batch was implemented in 0.10.0. Do not repeat its registry/enum additions on the current branch. Use `PistolProfiles.java` for current mappings and `TESTING-PISTOLS-0.10.0.md` for testing; these examples describe the earlier learning baseline.

Checked against addon 0.9.3 / protocol 17 and the pinned TaCZ 1.1.8-hotfix default pack. This is a development exercise; this document does not register any additional guns. Keep your changes for review before calling the entire batch supported.

## Choose the batch

These are the 14 pistol-category entries in the pinned pack, including the existing Glock. Classification below describes the pack, not a promise about every real-world variant or newer gun pack.

| Gun ID (`tacz:` prefix) | Suggested order | Work beyond calibration |
| --- | --- | --- |
| glock_17 | Existing regression reference | Keep existing behavior unchanged |
| m1911 | First complete implementation | Walkthrough below |
| p320, m9a4 | Next | Verify each slide, magazine, shell pivot and sound |
| deagle, deagle_golden, timeless50 | Next | Inspect compound slide assemblies and parent transforms; variants need their own entries |
| b93r, cz75, hk_mk23 | After conventional pistols | Pack uses burst/semi, auto, and semi/burst respectively; test selector and delayed-fire interruption |
| rhino357, taurus500, taurus943, lonetrail | Separate mechanism exercises | Pack uses `open_bolt`; inspect cylinder/loading geometry and scripts. Do not give these Glock magazine/slide handling |

You can investigate every pistol now. Implement and validate one conventional pistol end to end before duplicating it. The last group needs new handling design; registering it as `MAGAZINE` is not full support.

## Files and units

Java paths below are relative to `src/main/java/dev/visorcompat/tacz/`.
Reference assets are under:

`.upstream/TACZ/src/main/resources/assets/tacz/custom/tacz_default_gun/`

For each gun read:

- `data/tacz/data/guns/<id>_data.json`: ammo, capacity, bolt, reload type, fire modes and script references.
- `assets/tacz/display/guns/<id>_display.json`: actual model/animation IDs, `transform.scale.thirdperson`, shell and sound references.
- The referenced `assets/tacz/geo_models/...` model: bone names, parents, pivots and rotations.
- The referenced animations and any Lua scripts: which parts actually move and when ammo feeds.
- `assets/tacz/tacz_sounds/<id>/`: actual available sound files. An animation event name is not necessarily an OGG filename.

Use the full gun model, not its LOD model. Do not edit `.upstream`; it is ignored reference material. Some files have comments; read them as text if a strict JSON parser fails.

Profile grip/muzzle pivots are raw model pixels. Handling interaction coordinates are gun-local metres. Never bake the user's 80% size into either: calibration applies that separately.

## M1911 walkthrough

### 1. Give it an identity without changing Glock

In `WeaponProfile.java`, append `M1911` to `Mechanism` and add:

```java
public boolean m1911(){return mechanism==Mechanism.M1911;}
```

Keep `pump()`, `bolt()`, `smg()` and `manualAction()` unchanged. The existing pistol part fallback uses `slide`, which the pinned M1911 actually has; `boltNode()` remains null for this pistol.

Keep **supportDistance = 0**. In this prototype that value selects pistol behavior: touch-only support, main-hand aiming and main-hand Use slide release. It does NOT remove the support zone. Setting it to a positive value just to enable a selector would also change aiming and handling.

The family enum is a temporary per-gun identity, not an invitation to duplicate the reload state machine. You need an identity because ejection pivots and default grab points differ even when the mechanism is shared.

### 2. Register verified profile values

Add this pair to `Profiles.PROFILES`, preserving all current entries:

```java
"tacz:m1911", new WeaponProfile(
    .6f,
    0, 4.075f, -.15f,
    0, 5.475f, -7.875f,
    0, WeaponProfile.Mechanism.M1911)
```

These values are the pinned display scale, `thirdperson_hand` pivot and `muzzle_flash` pivot. M1911's ammo is `tacz:45acp`, base magazine capacity 7; TaCZ owns these values, so do not hard-code ammo or capacity into the addon.

**Batch-size trap:** `Map.of(...)` accepts at most 10 key/value pairs. There are already five profiles. Before the sixth new gun, convert the whole registry to:

```java
private static final Map<String, WeaponProfile> PROFILES = Map.ofEntries(
    Map.entry("tacz:glock_17", /* existing constructor unchanged */),
    // Each existing/new pair becomes one Map.entry(id, profile).
    Map.entry("tacz:m1911", new WeaponProfile(
        .6f, 0, 4.075f, -.15f, 0, 5.475f, -7.875f,
        0, WeaponProfile.Mechanism.M1911))
);
```

The comment is a placeholder, not compilable code. Duplicate IDs fail at startup. Preserve the `tacz:default` display guard in `Profiles.get()`; custom displays need separate geometry review.

### 3. Set the ejection port

In `physical/JamProfile.java`, prepend a M1911 case to the existing `shell` expression:

```java
Vector3f shell = profile.m1911()
    ? new Vector3f(0, 5.55f, .625f)
    : /* existing expression, unchanged */;
```

The pinned raw shell pivot is `[0, 5.55, 0.625]`. This method expects X already negated; zero is unchanged here. The existing tail applies model scale, subtracts grip, adds calibration and applies gun scale. Do not convert to metres twice.

For other guns inspect the actual shell pivot. Do not replace the shared Glock fallback with another pistol's values.

### 4. Calibrate interaction points, then optionally bundle defaults

Initially M1911 will inherit the Glock-shaped fallback points. They are starting guesses, not verified M1911 positions. In game calibrate and save:

- Grip/rotation, uniform gun size and muzzle.
- Blue support: comfortable two-hand pistol hold; touching is enough, no Grab required.
- Orange rack: where the offhand grips the slide.
- Green magazine: removal/insertion point.
- Cyan release: intended slide-release control.
- Pink port: case/obstruction location; casing size is a separate page.
- Purple sight: eye-alignment region for auto ADS; inspect the existing sight guide instructions.

For new bundled defaults add per-family cases in `Handling.magazine()`, `rack()`, `release()` and `support()` as needed. Preserve the calibrated wrappers and do not multiply gun scale twice. `release()` and `support()` already apply offsets/scale inside their bodies; insert your case without skipping those operations.

For simple unrotated model coordinates:

```text
x = -(nodeX - gripX) * modelScale / 16
y =  (nodeY - gripY) * modelScale / 16
z =  (nodeZ - gripZ) * modelScale / 16
```

Negative Z points forward. A bone pivot is not necessarily a comfortable grab point. Transformed parents require evaluating the hierarchy first. Keep calibration numbers nonzero when sharing; do not erase offsets to make a profile appear correct. If you later bake offsets into defaults, account for the saved offsets to avoid applying them twice.

### 5. Check moving and detached parts

Pinned M1911 has `slide`, `magazine`, and `bullet_in_mag`; the magazine is under `magazine_and_bullet`. Existing detached rendering walks the parent chain and applies the shared 30-degree sideways turn.

For every other pistol inspect these names rather than assuming they match:

- `rackNode()` must identify the moving slide assembly. Add a family-specific return if needed.
- `PhysicalModel` currently hard-codes `magazine` and `bullet_in_mag` in both mounted and detached rendering. A different name requires updating both paths, preferably through profile accessors with defaults that preserve existing guns.
- A separate bolt or compound slide needs a model-specific review. Avoid moving both a parent and its child by the full distance, which doubles travel.
- Test last-round slide lock, release, normal racking, jam pose and restoration of the original model transforms after rendering.

Do not return a rifle bolt name simply to make a pistol's animation move.

### 6. Add actual action sounds

`HandlingEffects.receive()` currently falls back to M4 clip names for an unknown gun. Registering M1911 alone therefore does not supply correct sounds.

After existing gun-specific sound cases, before playback, add:

```java
if(folder.equals("m1911"))clip=switch(m.kind()) {
    case 0 -> "m1911_reload_magout";
    case 1 -> "m1911_reload_magin";
    case 5,6 -> "m1911_reload_empty_chamber";
    default -> "m1911_inspect_slide_pull";
};
```

These files exist in the pinned pack. This is a starting selection: listen in VR for a suitable short action sound; existence does not establish the best timing or tone. This does not alter firing audio. Repeat file verification for every pistol, or explicitly document borrowed sounds.

### 7. Add all calibration hints

In `src/main/resources/calibration-hints.toml`, copy every Glock section with a new `tacz:m1911` key. Release and casing sections are near the end of the file, separate from the main Glock block. Use unique keys and rewrite gun-specific wording.

Include grip, rotation, scale, casing, muzzle, support, magazine, rack, pouch, sight, port and release, as exposed by `CalibrationLayout.pages()`. For support say touch the blue region; for rack describe full back/forward travel and repeat cycles while holding Grab. Hints only describe behavior; they cannot enable new mechanics.

### 8. Preserve shared ammo and jam rules

Do not copy `ServerPhysical` or start a second native reload after insertion. In 0.9.3 insertion restores reserved magazine rounds and transfers compatible inventory ammo through TaCZ's API immediately. Empty reload still needs chambering; tactical reload preserves the chamber. Detached magazines remain previews, not persistent magazine items.

The three jam simulations can be tested on conventional closed-bolt magazine pistols with supported gundb. New ammo models still need visual scale/orientation checks. Revolver and unusual open-bolt profiles must not inherit this system merely because their item category says pistol.

### 9. Tests, local version and build

Add each new gun to `CalibrationHintsTest`'s explicit list. Add profile tests checking identity, `supportDistance()==0`, `manualAction()==false`, expected slide/bolt names, muzzle and port geometry. Verify another gun's defaults remain unchanged. For renamed parts, check the referenced asset actually contains the node; compiling a string literal cannot catch a wrong bone name.

Use a local version such as `0.9.3-pistols-learning` in `build.gradle`. Change all three channel version strings in `CompatNetwork` from `17` to `17-pistols-learning`. Install your matching build on client and server. Do not mix it with stock 0.9.3 merely because packet layouts match: server profile registration matters.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat build --console=plain
```

Output is in `build/libs`. Keep only one addon JAR installed, preserve config/calibration and use a test world. Build success is not a VR pass.

## Extra work for burst/automatic pistols

Keep supportDistance zero. If you want a physical selector zone, explicitly extend `selector()` for the appropriate new families and define/calibrate its position. Check the existing bound fire-select action as well. Native fire modes come from TaCZ; never invent an auto mode for a gun whose pack lacks it.

For B93R/CZ75/MK23 test every native mode, trigger release, switching guns during bursts, reload/jam during delayed rounds, menu/focus loss and controller tracking loss. No delayed round should bypass the server's current action restrictions. Do not claim the full group passes because semi-auto worked.

## Per-gun record to send for review

Copy this for each gun, including failures and untested items:

```text
Gun ID / addon build:
Profile family / display ID:
Asset model + animation + scripts inspected:
Grip / muzzle / shell pivots and scale:
Slide / bolt / magazine / visible-round bone names:
Sound filenames (or documented borrowed set):
Changes beyond registration/calibration:
[ ] Model, grip, muzzle/impact alignment; both hands
[ ] Touch support and two-hand ADS enter/exit
[ ] Empty reload and both release controls
[ ] Tactical reload; partial/no spare inventory ammo; capacity attachment
[ ] Five full racks without releasing Grab; exact round totals
[ ] Partial rack and held-rear action cause no duplicate ejection
[ ] Last-round slide lock and model restoration
[ ] Each native firing mode and interruptions
[ ] All three applicable jams: visuals, clearing, exact ammo totals
[ ] Calibration save/reload/restart, gun scale and casing scale
[ ] Disconnect mid-reload, rejoin twice; exact ammo totals
[ ] Flat friend unchanged; remote held gun and detached magazine
Known failures / not tested:
```

Share your code diff, build/test result, this record and `config/visor_tacz-calibration.json`. For a visual issue include the gun ID, attachment IDs and a screenshot/video if available. Do not remove unrelated calibration entries. The review should check shared regressions as well as each new profile.

## Revolvers and other distinct mechanisms

For rhino357, taurus500, taurus943 and lonetrail, first write a mechanism proposal: opening/closing, loading access, cylinder or other ammo storage, live/spent cartridge states, extraction gesture, shot consumption, reconnect recovery and addon removal. Inspect native Lua and model animation; `open_bolt` is a TaCZ data choice, not a full description of real mechanics.

Keep their registration out of the playable registry until an appropriate state machine exists, or clearly separate an intentionally limited aiming/button-mode prototype from physical support. Use the Kar98k exercise in ADDING-GUN-SUPPORT.md as a model for your proposal. There is no safe universal one-line Glock conversion for this group.
