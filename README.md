# Visor TaCZ Compatibility Patch

Unpackaged **alpha.8 source** adds M700 re-grip fixes, displacement-based cylinder gestures and an experimental TOML profile switch with SCAR-L/H. [Runtime profile setup](docs/RUNTIME-PROFILES.md) · [Pistol calibration audit](docs/CALIBRATION-AUDIT.md). Source protocol is **24**; the last packaged alpha.7 uses **23**.


Alpha.7 handling update: [changes and calibration import](docs/RELEASE-NEXT-HANDLING.md), [pending tests and detailed inspection procedure](docs/TESTING-CURRENT.md).

Experimental Minecraft **1.20.1 Forge** mod that registers as a **Visor addon** and gives TaCZ guns controller aiming and physical VR handling. VR and flatscreen players can share a server.

Current release: **0.12.0-alpha.7** | network protocol **23** | Java **17** | MIT license. Stable checkpoint: **0.11.0**.

**New in alpha.6:** [inspection calibration, handling fixes, crouching holsters and cylinder instructions](docs/PENDING-HANDLING-CHANGES.md). These changes are included in alpha.6.

Alpha.5 adds upward inspection grip orientation, falling/flicked cosmetic magazines, M4/M870 support-hand transfer, experimental one-hand inertia pumping, wider support retention and forgiving pump return. See [controls and pending tests](docs/RELEASE-0.12.0-alpha.5.md).

Alpha.4 adds fine/coarse holster adjustment and Save-without-closing in both calibration editors. After saving, the button becomes Close until values change. See [calibration controls](docs/RELEASE-0.12.0-alpha.4.md).

Alpha.3 fixes holsters rendered above the player and out of alignment with Grip detection. If you lowered holster Height to compensate in alpha.2, restore holster defaults after updating. See [fix and retests](docs/RELEASE-0.12.0-alpha.3.md).

Alpha.2 adds per-gun holster calibration, 30-degree forward pistol holsters, 45-degree downward chest holsters, 75% bundled gun defaults, smoother tossed props, increased inspection travel and session input cleanup. Saved gun profiles still override bundled defaults. See [update notes and focused retests](docs/RELEASE-0.12.0-alpha.2.md).

New experiment: opt-in **Advanced Handling** adds holster/draw, inventory-backed toss/catch with automatic recovery, contextual Grip, and timed cosmetic pistol inspection. This alpha keeps the gun in its original slot; offhand catches are temporary support holds, not offhand firing or hotbar transfer. See [controls, limitations and pending tests](docs/RELEASE-0.12.0-alpha.1.md). The external gun-profile architecture remains an offline prototype.

New in 0.10.0: all 14 pinned default-pack pistols now have profiles. M1911, P320, M9A4, Desert Eagle/Golden, Timeless 50, B93R, CZ75 and MK23 reuse physical magazine/slide handling with per-gun assets. Rhino 357, Taurus 500/943 and Lonetrail add **experimental aggregate cylinder/breech loading**: open, eject all, insert individual rounds, close. This is not individual chamber selection or speedloader support. See [pistol testing](docs/TESTING-PISTOLS-0.10.0.md).

Current alpha.9 source: **all 54 base-pack guns have button support**; 20 additionally have physical handling. The other 34 automatically fall back to buttons. Gun definitions now come exclusively from [runtime TOML profiles](docs/RUNTIME-PROFILES.md). See [the complete support list](docs/SUPPORTED-GUNS.md), [calibration import](docs/CALIBRATION-IMPORT-0.11.0.md), and [future data-driven/gun-pack support design](docs/DATA-DRIVEN-GUN-SUPPORT.md). External packs are not yet automatically registered.

## Requirements and installation

| Component | Required version |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.0 or newer in the 47.x series |
| Visor | 0.5.0 |
| Timeless and Classics Zero (TaCZ) | 1.1.8-hotfix |
| Gun Durability (`gundb`) | Optional; integration targets 2.2.2 only |

Build the JAR below and install it, Visor, and TaCZ on **all clients and the server**. Single-player includes the server component internally. Keep addon versions consistent across players/server. No Prehistoric World 2 modpack or custom gun pack is required.

The addon follows Visor's VR state automatically. Flatscreen players keep normal TaCZ controls; some rendering compatibility fixes also apply in flatscreen. Physical handling is opt-in through `/visor_tacz menu`.

## Features

New in 0.9.3: repeat held racks; MP5 held forward return; M700 downward bolt slap and automatic re-grip when the hand returns; touch-only pistol support; immediate native ammo transfer on physical magazine insertion; detached magazines turned sideways 30 degrees; reversed jam casing ends. M870/M700 still use generic durability jams, not the three visual jam simulations.

Implemented in **0.11.0**; newer gestures still need headset and multiplayer testing.

### Gun-specific physical handling

| Default TaCZ gun | Implemented handling |
| --- | --- |
| **Glock 17** | Magazine removal/replacement, slide racking, empty-shot slide lock, main-hand Use or calibrated slide-release region, support-hand region for two-hand ADS |
| **M4A1** | Magazine removal/replacement, charging handle, empty-shot bolt lock, calibrated bolt-release region, foregrip support aiming and fire selector |
| **M870** | Inventory-backed individual shells, closed-pump underside tube loading, open-pump side-port chamber loading, manual pump and fore-end support; spent shell ejects during pumping |
| **M700** | Detachable magazine, lift-back-forward-lower bolt cycle, temporary support-hand carry so the main hand can operate the bolt; transfer policy **BOLT NEEDED** (default) or **ANYTIME** |
| **New magazine pistols** | M1911, P320, M9A4, Deagle/Golden, Timeless 50, B93R, CZ75, MK23: profile-specific physical magazine/slide handling; VR tests pending |
| **Experimental cylinder/breech pistols** | Rhino 357, Taurus 500/943, Lonetrail: open, eject all, load individual rounds, close; aggregate ammo state, no individual chamber selection |
| **HK MP5A5** | Magazine replacement, rearward charging-handle pull to latch, button-free forward sweep or downward slap through release region, button release fallback, support aiming and fire selector |

Magazine replacement draws compatible ammunition from inventory through TaCZ’s native ammo API when the physical magazine is inserted. Magazines are not separate persistent inventory items. Hand transfer is currently M700-specific and requires the support grip to remain held.

### Shared VR integration

- Registers as a Visor addon and follows VR state automatically; per-player OFF/AUTO and Physical/Buttons controls. VR and flatscreen players can share a server.
- Controller-aligned gun models and server-authoritative shots from the calibrated muzzle, including supported aim. TaCZ retains damage, spread, rate of fire and native ammunition rules.
- Remote calibrated weapons and moving parts, including M700 support-hand carry. Body/hand IK remains Visor-owned.
- Sight-aligned automatic ADS, with a **two-hand support requirement enabled by default** and a menu toggle. Glock support enables ADS while aim direction stays with the firing controller.
- Configurable contextual offhand **Use or Trigger** grabs, haptics, action sounds, muzzle flashes, and live/spent casing visuals.
- Experimental per-eye red dots and scope magnification with shader packs **off**.
- Ammo/ADS panel on gun, wrist, ordinary GUI HUD, or off. Standard HUD placement can use Visor overlay presets.
- Interrupted-magazine recovery and saved open-action/charging requirements. Recovery retains committed ammunition changes rather than rewinding inventory; held gestures are transient.

### Calibration and settings

- Per-gun saved grip position/rotation, muzzle origin, support, magazine/loading port, rack/pump, selector, pouch, sight, ejection port and applicable action-release regions.
- Uniform **gun scale 50-150%**, independent XYZ interaction-box dimensions, and a default-on scale link for mounted boxes. The pouch remains independent.
- **Casing scale 10-300%**, multiplying gun scale, with an in-menu preview. Applies to jam obstructions, ejections and M870 held-shell visuals.
- Bundled starting profiles retain explicit offsets; personal JSON overrides them. Reload calibration through the menu or command without restarting.
- Per-gun author-editable bundled TOML hints. Hide debug regions during play while keeping calibration guides visible in the calibration screen.
- Persistent per-player M700 transfer preference: **BOLT NEEDED** permits transfer after a shot, with an empty chamber, open/lifted bolt or jam; **ANYTIME** also permits it when ready to fire. Both automatic movement and main-hand Use follow the selected rule.

### Optional durability jams

With **gundb 2.2.2**, Glock/M4/MP5 and the new conventional magazine pistols support stovepipe, double feed and dud states. Stovepipes can be plucked or racked; double feeds require magazine removal and two obstruction ejections; duds are cleared by racking. M870/M700 and the experimental cylinder/breech pistols support generic jam clearing through their manual action cycle.

Stovepipe visuals lie across the bore with an upward tilt. Double-feed visuals reuse casing geometry. A dud has no protruding obstruction: the blocked trigger clicks and HUD identifies it. No magazine-tap requirement is implemented. Normal gun handling works without Durability; forced jam tests require the optional integration.

See the [0.9.1 handling guide](docs/RELEASE-0.9.1.md), [0.9.3 transfer controls](docs/RELEASE-0.11.0.md), and [consolidated test checklist](docs/TESTING-CURRENT.md) for sequences, prior results and outstanding tests.

## Controls and setup

Start with `/visor_tacz menu`. Bindings follow Visor's logical main/offhand, including its handedness setting. In two-handed mode, the offhand's **assigned hotbar item** must be empty for physical grabs, not just the vanilla offhand slot.

| Action | Button handling | Physical handling |
| --- | --- | --- |
| Main-hand attack / trigger | Fire | Fire when ready; grab bolt during M700 support-hand carry |
| Main-hand use | Reload | Glock slide release; M700 hand transfer/return; other guns change mode when offhand is at selector |
| Offhand use | Cycle fire mode with empty offhand | Contextual grab by default; also presses an applicable release region |
| Offhand trigger | Normal Visor behavior | Optional contextual grab via menu |

Physical grabbing claims valid interaction zones; other actions pass through. Losing tracking/focus, changing items, or opening menus releases owned input. Press again to resume firing.

**Magazine guns:** grab the magazine, pull down, release, take a replacement from the waist pouch, move it to the magwell, and release. After an empty reload, chamber using the gun's action or supported release control.

**M700:** hold the fore-end, then move the main hand more than 16 cm away or press main-hand Use to transfer, subject to the selected policy. Main trigger grabs the bolt. Return automatically by moving the main hand near the grip after closing the bolt, or use main-hand Use near the grip or release support. A downward slap can lower the closed, raised bolt.

**MP5:** pull back and push forward while holding Grab to rack. To park the handle at the rear, release Grab, reload, then sweep the empty hand forward or downward through the cyan release region without pressing a button. Empty firing alone does not latch it.

**M870:** with pump closed, release a pouch shell at the green underside port to fill the tube. With pump open, release at the pink side port to load the chamber, then close the pump. Valid insertion consumes one compatible inventory round. The fully forward held pump also provides support aiming.

**Calibration:** hold the gun, choose Calibrate gun, adjust, then Save & close. Translation readouts are millimetres; JSON stores metres. Rotation is in degrees. The purple sight marker controls automatic ADS alignment, not the rendered scope lens.

## Commands and files

| Command | Purpose |
| --- | --- |
| `/visor_tacz menu` | Settings, calibration, reload button, debug cubes |
| `/visor_tacz calibrate` | Calibrate the held supported gun |
| `/visor_tacz reload_calibration` | Reload the JSON and sync the held profile |
| `/visor_tacz auto` | Follow Visor VR state (default); `on` is an alias |
| `/visor_tacz off` | Disable per-player VR gun integration |
| `/visor_tacz status` | Show current activation state in chat |
| `/visor_tacz handling physical` / `buttons` | Choose handling mode |
| `/visor_tacz grab use` / `trigger` | Choose offhand grab input |
| `/visor_tacz display gun` / `wrist` / `hud` / `off` | Choose ammo/ADS display |
| `/visor_tacz ads auto` / `off` | Automatic ADS |
| `/visor_tacz optics on` / `off` | Experimental VR optic rendering |
| `/visor_tacz_test jam [stovepipe\|double_feed\|dud\|misfire]` | Operator-only test with compatible Gun Durability; M870/M700 only accept generic/stovepipe test |

Settings: `config/visor_tacz-client.toml`. Calibration: `config/visor_tacz-calibration.json`, on **each player's client**. See [sharing and installing calibration](docs/CALIBRATION-SHARING.md).

`off` **does not unload the mod**: render stability/startup patches and remote-player rendering remain. For a clean isolation test, remove this addon's JAR and restart. If Visor was disabled at login, enabling VR may require reconnecting because Visor 0.5.0 establishes server VR identity at login.

## Known issues and not implemented

| Area | Current limitation |
| --- | --- |
| Optics | Later user report says scopes work in old/new worlds; PK06 magnification remains unresolved. Individual optic, decal and shader cases still require targeted tests. |
| Shader-pack optics | Custom magnification is disabled; shader framebuffer/compositing integration is not implemented. |
| Scope quality | Uses the current eye image, not an independent high-resolution scope camera; generic reticles replace pack artwork. |
| Other guns/packs | No universal profile generator or external profile-pack loader. Replaced/custom geometry requires implementation and calibration. |
| Two guns | No dual wield or addon-specific main-hand selector. |
| Attachments | Native TaCZ attachment GUI; no physical attachment insertion/removal. |
| Recoil | No added controller-driven physical recoil simulation. |
| Physical inventory | No independently counted magazine items, recoverable dropped rounds/magazines, general item pouch, holsters, or free weapon pickup. |
| Other mechanisms | No individual cylinder-slot selection, speedloaders, direct M700 chamber loading, physical safety controls, or universal support beyond the registered default-pack profiles. M870 side-port loading and gun-specific action releases are implemented. |
| Jams | Detailed three-type handling covers supported conventional magazine pistols plus M4/MP5 and requires the optional integration. Model-specific frozen-action polish, dedicated cues and a magazine-tap requirement remain unfinished. Heat/cook-off simulation is not added. |
| Networking | Latest Visor pose is used, not latency-rewound poses. Stale tracking or obstructed muzzle positions can reject shots. |
| Validation | M700 transfer, MP5 swept release, new Glock controls, casing calibration and two-hand ADS still need 0.9.3 headset confirmation. Broader shader, handedness and multiplayer tests remain open. |

World scale is restricted to 0.
. Third-party gun scripts that replace TaCZ's shooting/reload behavior are not guaranteed compatible. The addon does not repair weapon durability or bypass ordinary cooldowns.

See [0.7.1 changes and focused retests](docs/RELEASE-0.7.1.md) for scale lock, ammo visuals, input changes, and optics candidates.

## Verification status

Use the [rolling manual test tracker](docs/TESTING-CURRENT.md) for remaining tests,
previous user results, and stable IDs to report pass/fail across releases.

- Latest build: **157 unit tests passed**. Build success is not proof of in-headset correctness.
- Development feedback uses **Pico 4 Ultra + Virtual Desktop + SteamVR**. Controller aiming and M4 support aiming have received successful user feedback; many newer interactions remain under testing.
- 0.6.1 prepares stencil before world rendering to address scope/depth-format crashes. A subsequent user session reported no crash or blinking. This is not a guarantee for every shader/mod combination.
- Blinking was also reproduced without this addon, with Visor + base TaCZ, and stopped after a PC restart in that test session; its root cause is not established.
- Graphics diagnostic tools reproduce/check specific OpenGL behavior. Dedicated development server startup was checked to the EULA gate, not a full multiplayer playthrough.

## Build from source

Use JDK 17. The Gradle wrapper downloads dependencies; local upstream source clones are not needed.

```powershell
Set-Location 'E:\visor-compat\visor-compat-tacz'
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat build --console=plain
```

Adjust the repository and JDK paths if installed elsewhere. Wait for `BUILD SUCCESSFUL`; the first build needs internet and takes longer to download dependencies. Replace the old addon JAR in each instance/server `mods` folder with the new one while Minecraft/server is stopped. Keep only one version of this addon installed.

Linux/macOS: `sh gradlew build`.

Current source output: `build/libs/visor-compat-tacz-1.20.1-0.12.0-alpha.8.jar`.

`build` includes tests. Development tasks: `runClient` and `runServer`; runtime files stay under `run/`. Accept Minecraft's EULA yourself before using the development server. Dependencies and downloaded upstream sources are not bundled into this repository or output JAR.

## Contributing and bug reports

Use this repository's Issues for reports. Include addon/Visor/TaCZ/Forge versions, gun and attachment IDs, gun pack, VR/flatscreen mode, shader name/on-off state, reproduction steps, and relevant crash report/log. Share calibration JSON when alignment is involved. Review logs for personal information before posting.

Source areas: `client` for rendering/input/settings, `server` for authoritative poses and physical state, `physical` for shared interaction logic, `network` for synchronization, and `mixin` for integration hooks. Bundled calibration is `src/main/resources/calibration-defaults.json`.

See [current release notes](docs/RELEASE-0.11.0.md), [physical handling details](docs/RELEASE-0.9.1.md), and [calibration sharing](docs/CALIBRATION-SHARING.md). Older versioned notes are historical and may describe superseded behavior; this README describes 0.11.0.

Built against published Visor `gqaBzrB7` and TaCZ `yOVIzIJR` Modrinth versions. Reference source pins: Visor `3d56cd0a75c0e1ba5e73e3b10d64165b1b3e0bef`, TaCZ `b43eb84c38e9768d8e73c8b14f0b845669704b38`. This is a community compatibility addon, not an official Visor or TaCZ release. MIT applies to this repository's original code; upstream projects retain their own licenses.

## Adding another gun

Follow the [UMP45 walkthrough and Kar98k exercise](docs/ADDING-GUN-SUPPORT.md).
These are historical physical-handling exercises. UMP45 and Kar98 now have button-only support; their physical workflows remain unimplemented.

Historical pistol walkthrough: [M1911 and batch checklist](docs/ADDING-PISTOL-SUPPORT.md). The default-pack pistol batch is now implemented; do not duplicate its registry entries.
