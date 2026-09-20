# Adding a gun: UMP45 walkthrough, Kar98k exercise

Reviewed against this workspace's **0.9.3** source and pinned TaCZ 1.1.8-hotfix
default pack. These are instructions, **not installed gun support**. UMP45 and
Kar98k have deliberately not been added to the running mod for you.

## What a profile actually does

A registry entry enables aiming/rendering, but full physical support also needs:

| Layer | Current source | What you provide |
| --- | --- | --- |
| Gun ID and model pivots | `src/main/java/dev/visorcompat/tacz/Profiles.java` | Gun ID, grip, muzzle, model scale, support distance, profile family |
| Mechanism/model parts | `WeaponProfile.java` in that directory | Correct moving-part names and capabilities |
| Grab positions | `physical/Handling.java` | Magazine, action grip, support, selector |
| Ejection port | `physical/JamProfile.java` | Converted shell pivot |
| Text | `src/main/resources/calibration-hints.toml` | Each supported page's title/color/hint |
| Physical state | `server/ServerPhysical.java`, `server/ServerPump.java`, `physical/BoltCycle.java` | New logic only when existing handling does not fit |
| Sound | `client/HandlingEffects.java` | Real available sound resources |

Paths in the Java rows are relative to `src/main/java/dev/visorcompat/tacz/` unless
specified otherwise. TOML changes labels, not capabilities or server rules.

Our `SMG` enum currently identifies the MP5 profile family, including its part
names. It is not a universal "every SMG works" category. This is technical debt
in the prototype. For the exercise, an explicit UMP family avoids changing MP5
behavior. A later refactor can separate mechanism from per-gun geometry/parts.

## Exercise 1: UMP45 — guided implementation

UMP45 uses a closed bolt and detachable magazine, so it can use the existing
server reload state machine. You should not need to edit ammo extraction or add
another firing packet. In 0.9.3 physical magazine insertion transfers native
ammo immediately; do not also start TaCZ's timed reload, or you risk a second
feed path. Held racks can cycle repeatedly after a full forward return.
Its default-pack ammo is `tacz:45acp`, capacity 25, and
native modes are auto/burst. Those properties remain TaCZ-owned.

### 1. Inspect the source assets

Base folder:

`.upstream/TACZ/src/main/resources/assets/tacz/custom/tacz_default_gun/`

Open these files beneath it:

- `data/tacz/data/guns/ump45_data.json` — ammo, bolt type, reload and modes.
- `assets/tacz/display/guns/ump45_display.json` — model path and third-person scale.
- `assets/tacz/geo_models/gun/ump45_geo.json` — named bones and pivots.
- `assets/tacz/animations/ump45.animation.json` — how native parts move.
- `assets/tacz/tacz_sounds/ump45/` — sounds that actually exist.

Do not edit `.upstream` to change our addon. It is reference material and is ignored
by Git. Some TaCZ data files contain comments, so a strict JSON parser may reject
them; reading them in an editor is sufficient for this exercise.

Verified UMP45 values:

| Field | Value |
| --- | --- |
| ID | `tacz:ump45` |
| Third-person scale | `0.6` |
| `thirdperson_hand` pivot | `[0, 8.175, 5.75]` |
| `muzzle_flash` pivot | `[0, 10.5, -11.0625]` |
| `shell` pivot | `[-0.15, 10.39375, 0.7]` |
| Charging-handle bone | `ump45_charge_handle` |
| Bolt bone | `ump45_bolt` |
| Magazine / visible cartridge bones | `magazine` / `bullet_in_mag` |

### 2. Add the profile family

In `WeaponProfile.java`, add `UMP` to the enum:

```java
public enum Mechanism { MAGAZINE, PUMP, BOLT, SMG, UMP }
```

Add this helper:

```java
public boolean ump(){return mechanism==Mechanism.UMP;}
```

At the beginning of each existing part-name method, add an early return:

```java
// First line inside rackNode():
if(ump())return "ump45_charge_handle";

// First line inside boltNode():
if(ump())return "ump45_bolt";
```

Expand the existing one-line methods to multiple lines if easier. Keep their
existing return expressions afterward. Do not replace MP5's names globally.
Leave `smg()` as-is; its true branch currently selects MP5-specific geometry.
`manualAction()` stays false for UMP, and the existing selector rule already fits.

### 3. Register the gun

In the `PROFILES` map in `Profiles.java`, add a comma after the MP5 entry and add:

```java
"tacz:ump45", new WeaponProfile(
    .6f,
    0, 8.175f, 5.75f,
    0, 10.5f, -11.0625f,
    .25f, WeaponProfile.Mechanism.UMP)
```

The constructor order is model scale, grip XYZ, muzzle XYZ, support distance,
then profile family. Grip/muzzle are raw model pixels, **not metres**. The support
distance `.25f` is an initial estimate in metres and should be calibrated.

`Profiles.get()` currently accepts `tacz:default` display only. Alternate gun-pack
display IDs need their own geometry review; do not remove that guard blindly.

### 4. Set initial grab points

In `Handling.magazine(WeaponProfile p)`, before the fallback return, add:

```java
if(p.ump())return new Vector3f(0,-.08f,-.17f);
```

In `Handling.rack(WeaponProfile p)`, before the fallback return, add:

```java
if(p.ump())return new Vector3f(-.0283f,.1464f,-.4696f);
```

These are gun-local **metres**, unlike the profile pivots. The magazine point is
an estimate; the rack point comes from the charge-handle pivot relative to grip.
A bone pivot need not be the exact controller grab point. Use calibration to
place it comfortably on the handle, not to force the user's hand onto a pivot.

The conversion for these unrotated default reference nodes is:

```text
x = -(nodeX - gripX) * modelScale / 16
y =  (nodeY - gripY) * modelScale / 16
z =  (nodeZ - gripZ) * modelScale / 16
```

Negative Z is forward. Do not multiply by the user's 80% gun scale here; calibrated
helpers already apply that. For another model with transformed ancestors, first
resolve its full bone hierarchy; copying this subtraction is not universally valid.

### 5. Set the ejection-port reference

In `JamProfile.of()`, prepend this case to the existing `shell` expression:

```java
Vector3f shell = profile.ump()
    ? new Vector3f(.15f,10.39375f,.7f)
    : /* keep the existing profile.bolt() ? ... expression here */;
```

The comment is a placeholder for the existing expression, not code to compile
unchanged. Shell X is already sign-flipped in this method; the later code applies
scale, subtracts grip, and adds saved port offsets. Do not convert it twice.

### 6. Add hints, with no state-machine changes

Copy all `["tacz:hk_mp5a5".…]` sections in `calibration-hints.toml`, append them,
and change the copied section keys to `tacz:ump45`. Edit the text for UMP's handle.
Keep grip, rotation, scale, muzzle, support, magazine, rack, selector, pouch, sight,
port, release, and casing. The release/casing sections are farther down the file,
not next to the main MP5 block. Each section must occur only once.

Rewrite the copied release hint: this UMP family uses the ordinary locked-bolt
release control, not MP5's raised-handle latch or hands-free slap. Its release
region initially uses the rifle fallback in `Handling.release()`. Calibrate it
onto the intended control and save; add a UMP-specific default there if needed.
The casing page controls loose-round/casing size, independently of uniform gun
scale. Do not copy MP5-specific motion instructions unchanged.

`CalibrationLayout` now reads profile capabilities, so UMP gets support/selector
pages automatically. The renderer uses `rackNode()` and `boltNode()`; the magazine
and `bullet_in_mag` names already match. Leave `ServerPhysical` unchanged for this
exercise. Copying it would create a second reload implementation unnecessarily.

This is a first working profile, not a guarantee of complete moving-part fidelity.
`PhysicalModel` currently couples the separate bolt to handle travel only for
`smg()` (MP5). Merely returning an UMP bolt name does not add that coupling.
Inspect the UMP animation and bone hierarchy before adding a UMP-specific
rendering case; verify both handle and bolt motion, locked-open pose, and jam
pose. Do not change `smg()` to include UMP just to animate the bolt: that would
also enable MP5 geometry and server latch/slap rules.

### 7. Choose explicit temporary action sounds

UMP's shipped files contain combined reload/inspect tracks, not separate files
for every animation event label. `ump45_reload_magout` appears as an animation
label but is **not** an available `.ogg` in the pinned pack. Do not invent its URL.

For this first exercise, reuse MP5's verified short action sounds. In
`HandlingEffects.receive()`, immediately after:

```java
String folder=m.gun().getPath();
```

add:

```java
if(folder.equals("ump45"))folder="hk_mp5a5";
```

This only selects physical action sounds; TaCZ's normal UMP firing audio and ammo
ID remain unchanged. Document it as a temporary borrowed sound set.

### 8. Add checks and compile

Add `"tacz:ump45"` to the gun list in `CalibrationHintsTest`. Add a test similar
to `BoltCycleTest.newProfilesExposeCorrectMechanismsAndParts` that checks:

```java
var p=Profiles.byId("tacz:ump45");
assertNotNull(p);
assertFalse(p.manualAction());
assertTrue(p.selector());
assertEquals("ump45_charge_handle",p.rackNode());
assertEquals("ump45_bolt",p.boltNode());
```

Use a distinct local version, e.g. `0.9.3-ump-learning`, in `build.gradle` so you
can distinguish the JAR. Change the three channel-version strings in
`CompatNetwork` from `17` to `17-ump-learning`, and install that build on both
clients/server. A stock 0.9.3 server does not have your new profile even though
the packet layout looks the same.

From the project folder in PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat build --console=plain
```

The JAR is in `build/libs`. Close Minecraft before replacing its addon JAR, keep
one addon version in `mods`, and preserve your config files. Compile success
does not validate a physical grab or model alignment.

### 9. Test in this order

1. Button mode: model, muzzle origin, aim, normal TaCZ firing/reloading.
2. Calibrate grip, muzzle, rack, magazine, support, port, release and casing size; Save & close before
   testing server interactions. Save a copy of your calibration JSON.
3. Physical tactical and empty reloads: immediate ammo transfer, correct mag/handle motion, locked-bolt release, repeated held racks, and exact ammo counts.
4. Native auto/burst modes, trigger release, support aim, selector, no spare ammo.
5. If using gundb: existing three jam types, checking port/bolt visuals carefully.
6. Reconnect mid-reload; flatscreen friend checks ordinary play and remote visuals.

Start with iron sights and muzzle/impact alignment to isolate the new profile,
then test optics separately. The earlier blanket warning that optics are broken
is outdated; working optics have since been reported.

## Exercise 2: Kar98k — investigate and propose your design

Gun ID: `tacz:kar98`. Inspect the corresponding `kar98_data.json`, display/model,
`kar98_gun_logic.lua`, and `kar98_state_machine.lua` in the same default-pack tree.
Use M700 as a reference for cycling and M870 as a reference for individual loading.

The challenge: TaCZ labels its reload type `magazine`, but its **Lua script** has
an empty/no-scope clip-loading path and a different loop-loading path. The model
also has a bone named `magazine` associated with loading geometry. Neither fact
means it should receive M4-style detachable-magazine removal.

Before writing code, try answering:

1. What does the model's `magazine` actually depict? Which parts move during each
   native reload animation? What is the role of `clip`?
2. Which parts of M700's lift/back/forward/lower cycle, downward slap and
   main-hand re-grip can you reuse, and which model pivots differ?
3. When may ammunition be inserted: action closed, action open, or both? How will
   you distinguish reserve count from chamber count?
4. How does an attached scope change the native script's loading choice? Would
   your first physical demo implement single cartridges only, or clips too?
5. Which calibration pages should exist? Which M700/M4 actions must be absent?
6. At what exact gesture does one server-side ammo transfer commit? What happens
   on cancellation, a full gun, reconnect, or a repeated packet?
7. How will you avoid running both your physical feed and TaCZ's timed Lua feed
   for the same cartridges?

Send your proposed file changes and state transitions first. This guide leaves
the Kar98k implementation for you; registering it as `BOLT` alone would give it
M700's detachable-magazine workflow and is not full support.

Longer-term, profile geometry/part names should become data fields rather than
new enum values per gun. Different mechanisms will still need tested server
logic; moving names into TOML cannot implement a new reload sequence by itself.
