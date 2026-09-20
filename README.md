# Visor TaCZ Compatibility Patch

Experimental Minecraft **1.20.1 Forge** mod that registers as a **Visor addon** and gives TaCZ guns controller aiming and physical VR handling. VR and flatscreen players can share a server.

Current version: **0.9.2** · network protocol **16** · Java **17** · MIT license.

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

- Supported default TaCZ models: **Glock 17, M4A1, M870, M700, HK MP5A5**.
- Gun models follow the controller; server bullets originate at the calibrated muzzle and follow gun direction, including M4 support-hand aiming. TaCZ retains damage, spread, rate of fire, and ammunition rules.
- Remote calibrated gun and physical-part rendering so other players can see the weapon state. Body/hand IK remains Visor-owned.
- Glock/M4 magazine removal, inventory-backed waist-pouch replacement, insertion, and slide/charging-handle gestures. Native reload timing still supplies ammunition after insertion.
- M870 individual shell insertion, manual back-and-forward pumping, and fore-end support. Spent shells eject during pumping instead of per shotgun pellet.
- Optional Gun Durability integration: Glock/M4/MP5 stovepipe, double-feed, and dud handling states. M870 and M700 support generic jam clearing through a completed pump/bolt cycle. No durability mod is required for normal operation.
- Two-hand ADS requirement (default on; menu toggle), pistol support region, and per-gun casing-size calibration.
- Physical ADS detection, VR comfort changes, haptics, action sounds, muzzle flashes, and shell/cartridge visuals.
- Experimental per-eye red dots and scope magnification with shader packs **off**.
- Ammo/ADS display on the gun, wrist, ordinary GUI HUD, or off. The standard HUD can be arranged with Visor overlay presets; no separate overlay addon is required.
- Saved grip position/rotation, muzzle offsets, support point, magazine/loading port, action grip, selector, pouch, sight, and ejection-port calibration.
- Uniform gun scale (50–150%), independent X/Y/Z interaction-box sizes, and bundled per-gun TOML hints. See [0.7.0 setup and tests](docs/RELEASE-0.7.0.md).
- Bundled starting calibrations keep their explicit offset values. Local profiles override them; reload an edited file without restarting.
- Menu toggle for colored debug cubes. Hiding them does not disable interactions.
- Removed-magazine ammo recovery on reconnect, and saved empty-reload charging requirements. Recovery keeps committed ammo changes rather than rewinding the inventory.

The [0.9.1 release guide and focused tests](docs/RELEASE-0.9.1.md) cover action releases, M700 hand transfer, MP5 forward-sweep release, M870 side-port loading, casing-size calibration and two-hand ADS. These changes require headset retesting.

M700 hand transfer defaults to **BOLT NEEDED**, with an **ANYTIME** menu option. See the [0.9.2 controls and tests](docs/RELEASE-0.9.2.md).

The [consolidated 0.9.2 checklist](docs/TESTING-CURRENT.md) includes prior results, remaining tests, a short Glock-first session, and the proposed pistol rollout batches.

## Controls and setup

Start with `/visor_tacz menu`. Bindings follow Visor's logical main/offhand, including its handedness setting. In two-handed mode, the offhand's **assigned hotbar item** must be empty for physical grabs, not just the vanilla offhand slot.

| Action | Button handling | Physical handling |
| --- | --- | --- |
| Main-hand attack / trigger | Fire | Fire when the action is ready |
| Main-hand use | Reload | Glock slide release; M700 hand transfer/return; other guns change mode when offhand is at selector |
| Offhand use | Cycle fire mode with empty offhand | Contextual grab by default |
| Offhand trigger | Normal Visor behavior | Optional contextual grab via menu |

Physical grabbing claims valid interaction zones; other actions pass through. Losing tracking/focus, changing items, or opening menus releases owned input. Press again to resume firing.

**Glock/M4:** grab the magazine, pull down, release, grab a replacement from the waist pouch, move to the magwell, and release. Charge after an empty reload. Hold the M4 foregrip to support aiming.

**M870:** grab a shell preview at the cyan pouch, release at the green loading port, then pull the orange pump grip back and forward. Compatible inventory ammunition is consumed only on a valid insertion. Hold the pump forward for support; support and pump share the same calibrated point and box.

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
| `/visor_tacz_test jam [stovepipe\|double_feed\|dud]` | Operator-only test with compatible Gun Durability; M870/M700 only accept generic/stovepipe test |

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
| Physical inventory | No independently counted magazine items, recoverable dropped rounds/magazines, general item pouch, holsters, or free weapon pickup. |
| Other mechanisms | No direct M870 breech loading, bolt-release/safety gestures, or additional gun mechanisms beyond these five profiles. |
| Jams | Detailed three-type handling is Glock/M4/MP5 only and requires verified optional integration. Final frozen-action animations and dedicated sound cues need more work. Heat/cook-off simulation is not added. |
| Networking | Latest Visor pose is used, not latency-rewound poses. Stale tracking or obstructed muzzle positions can reject shots. |
| Validation | Headset/model alignment, shader combinations, controller handedness, and multiplayer interactions still need broader testing. |

World scale is restricted to 0.25–4. Third-party gun scripts that replace TaCZ's shooting/reload behavior are not guaranteed compatible. The addon does not repair weapon durability or bypass ordinary cooldowns.

See [0.7.1 changes and focused retests](docs/RELEASE-0.7.1.md) for scale lock, ammo visuals, input changes, and optics candidates.

## Verification status

Use the [rolling manual test tracker](docs/TESTING-CURRENT.md) for remaining tests,
previous user results, and stable IDs to report pass/fail across releases.

- Latest build: **100 unit tests passed**. Build success is not proof of in-headset correctness.
- Development feedback uses **Pico 4 Ultra + Virtual Desktop + SteamVR**. Controller aiming and M4 support aiming have received successful user feedback; many newer interactions remain under testing.
- 0.6.1 prepares stencil before world rendering to address scope/depth-format crashes. A subsequent user session reported no crash or blinking. This is not a guarantee for every shader/mod combination.
- Blinking was also reproduced without this addon, with Visor + base TaCZ, and stopped after a PC restart in that test session; its root cause is not established.
- Graphics diagnostic tools reproduce/check specific OpenGL behavior. Dedicated development server startup was checked to the EULA gate, not a full multiplayer playthrough.

## Build from source

Use JDK 17. The Gradle wrapper downloads dependencies; local upstream source clones are not needed.

```powershell
.\gradlew.bat build
```

Linux/macOS: `sh gradlew build`.

Output: `build/libs/visor-compat-tacz-1.20.1-0.6.5.jar`.

`build` includes tests. Development tasks: `runClient` and `runServer`; runtime files stay under `run/`. Accept Minecraft's EULA yourself before using the development server. Dependencies and downloaded upstream sources are not bundled into this repository or output JAR.

## Contributing and bug reports

Use this repository's Issues for reports. Include addon/Visor/TaCZ/Forge versions, gun and attachment IDs, gun pack, VR/flatscreen mode, shader name/on-off state, reproduction steps, and relevant crash report/log. Share calibration JSON when alignment is involved. Review logs for personal information before posting.

Source areas: `client` for rendering/input/settings, `server` for authoritative poses and physical state, `physical` for shared interaction logic, `network` for synchronization, and `mixin` for integration hooks. Bundled calibration is `src/main/resources/calibration-defaults.json`.

See [release notes](docs/RELEASE-0.6.5.md), [M870 handling](docs/RELEASE-0.6.0.md), [Glock/M4 jams](docs/RELEASE-0.5.0.md), and [calibration sharing](docs/CALIBRATION-SHARING.md). Older versioned notes are historical and may describe superseded behavior; this README describes 0.6.5.

Built against published Visor `gqaBzrB7` and TaCZ `yOVIzIJR` Modrinth versions. Reference source pins: Visor `3d56cd0a75c0e1ba5e73e3b10d64165b1b3e0bef`, TaCZ `b43eb84c38e9768d8e73c8b14f0b845669704b38`. This is a community compatibility addon, not an official Visor or TaCZ release. MIT applies to this repository's original code; upstream projects retain their own licenses.

## Adding another gun

Follow the [UMP45 walkthrough and Kar98k exercise](docs/ADDING-GUN-SUPPORT.md).
These are learning exercises, not already registered gun support.
