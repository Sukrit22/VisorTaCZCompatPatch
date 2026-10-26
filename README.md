# Visor TaCZ Compatibility Patch

A Visor VR addon for TaCZ: controller aiming, muzzle-origin shots, physical weapon handling, calibration, and experimental dual wield with body holsters. VR and flatscreen players can share a server.

**[Download the 0.14.0 release](https://github.com/Sukrit22/VisorTaCZCompatPatch/releases/tag/0.14.0)** · [Build instructions](docs/BUILDING.md) · [Controls and test tracker](docs/TESTING-CURRENT.md) · [Developer tour](docs/PROJECT-TOUR.md)

## Release versus current source

The **0.14.0** GitHub release is a download collection. Its uploaded binaries are different alpha builds; the release label does not change their embedded version or protocol:

| Minecraft / loader | Uploaded binary version |
| --- | --- |
| 1.20.1 Forge | 0.14.0-alpha.6 |
| 1.20.1 Fabric | 0.14.0-alpha.5.1 |
| 1.21.1 Fabric | 0.14.0-alpha.5.1+1.21.1 |
| 1.21.1 NeoForge | 0.14.0-alpha.5.1+1.21.1 |

**Current source on `main`: 0.14.0-alpha.9, protocol 34** (1.21.1 adds `+1.21.1`). The release tag points to this source history, but the attached older JARs do not contain every source feature below. In particular, the Physical dual Glock milestone and alpha.9 feedback/ADS fixes require a newer build. Use matching addon builds on client and server; do not mix artifacts merely because they share the GitHub release page. See [alpha.8](docs/UPDATE-0.14.0-alpha.8.md) and [alpha.9 changes](docs/UPDATE-0.14.0-alpha.9.md).

## Installation

Choose the JAR for **both** your Minecraft version and loader. Install the addon and compatible Visor/TaCZ dependencies on clients and the server. Single-player includes the server internally. No particular modpack or custom gun pack is required.

| Target | Java | Dependencies |
| --- | --- | --- |
| Forge 1.20.1 | 17 | Forge 47.4.x; Visor; TaCZ 1.1.8-hotfix/hotfix2 |
| Fabric 1.20.1 | 17 | Fabric Loader 0.17.2+; Fabric API; Visor; TaCZ: Refabricated; Forge Config API Port |
| Fabric 1.21.1 | 21 | Fabric Loader 0.17.2+; Fabric API; Visor 0.6.0-beta or compatible later build; TaCZ: Refabricated; Forge Config API Port |
| NeoForge 1.21.1 | 21 | NeoForge 21.1; Visor 0.6.0-beta or compatible later build; the targeted NeoForge TaCZ port |

The [build guide](docs/BUILDING.md) identifies the pinned development dependencies and metadata. A permissive dependency range is not a claim that every upstream version has been tested. **Gun Durability (`gundb`) is optional**; the detailed jam adapter targets 2.2.2.

Open **`/visor_tacz menu`**. Activation follows Visor's VR state automatically. Flatscreen users retain native TaCZ controls; some compatibility fixes still apply outside VR. `/visor_tacz off` disables per-player VR integration, not the loaded mod or all mixins. Remove the JAR and restart for a complete isolation test.

## Handling and support

| Mode | Behavior |
| --- | --- |
| **Button** | Controller aiming and native ammunition/reload behavior. Broad base-gun support and conservative fallback for loaded custom-pack guns. |
| **Physical** | Gun-specific magazine, slide/bolt/pump/cylinder and loading interactions. Uses TaCZ inventory ammunition, not separate persistent magazine items. |
| **Advanced** | Experimental holster/draw, free-hand transfer, toss/catch and inspection interactions built on supported mechanisms. |

The bundled TOML catalog contains **54 base guns: 27 physical profiles and 27 button-only profiles**. A profile's existence is not a calibration or headset-test certification. Custom gun packs receive generic Button fallback where possible; unusual scripts and replacement models still need checking.

Physical profiles cover magazine pistols, Rhino/Taurus revolvers and Lonetrail, M4A1, SCAR-L/H, AK47, MP5, UMP45, M700, M870, SPAS-12, M1014, AA12 and DB-2/DB-4. Mechanism completeness varies. See [support list](docs/SUPPORTED-GUNS.md), [runtime TOML profiles](docs/RUNTIME-PROFILES.md), [adding guns](docs/ADDING-GUN-SUPPORT.md), and [pistol authoring](docs/ADDING-PISTOL-SUPPORT.md).

### Dual wield and four holsters

- Independent per-hand Button shooting/reloading; per-gun HUD and audio.
- Configurable right/left handgun holsters and chest/back long-gun mounts. Automatic hotbar rules or explicit bindings; contextual fresh Grab draws the gun.
- Experimental **Physical Glock 17 in either hand** in current source. Stow the other gun to free a controller for manipulation. Other dual guns still use Button handling.
- Full dual Physical/Advanced parity is unfinished. Single-gun tests do not certify dual mechanics.

### Shared features

- Calibrated controller models and server-authoritative muzzle-origin shooting; TaCZ retains native damage, ammunition, spread and cooldown rules.
- Two-hand supported aiming and eye-aligned automatic ADS; configurable two-hand ADS requirement and recoil.
- Red dots and experimental per-eye scope magnification. Shader-pack magnification remains unsupported.
- Gun, wrist or ordinary GUI ammo/ADS HUD; configurable contextual Use/Trigger grabs and debug regions.
- Gun-specific action sounds, moving parts and live/spent round visuals. Latest dual feedback is part of alpha.9 source.
- Optional durability jams, including supported stovepipe, double-feed and dud interactions; other mechanisms use generic action clearing.

## Calibration and authoring

Calibrate grip position/rotation, uniform gun size, muzzle, supported interaction regions and XYZ box sizes, casing size, holsters and inspection grip. Desktop calibration includes mouse rotation, axis views and a hand placeholder. **Save** writes without closing; it becomes **Close** until you edit again.

Personal calibration overrides bundled defaults. `/visor_tacz reload_calibration` reloads supported calibration JSON; it does not reload runtime gun TOML. [Share/import calibration](docs/CALIBRATION-SHARING.md) · [ADS baseline and attachment/view corrections](docs/ADS-CALIBRATION.md) · [Gun-pack support/review markers](docs/GUN-REVIEW-MARKERS.md)

Runtime TOML binds models and zones to existing Java mechanisms without recompiling. It is not an arbitrary state-machine or animation scripting language. [Authoring map and limits](docs/GUN-AUTHORING-MAP.md)

## Known limits and validation

- Calibration and physical fit vary by gun and pack. Shotguns, newer profiles, handedness and remote player poses still need targeted testing.
- Magnified scopes use the current eye image, not a separate high-resolution camera. Shader integration remains unfinished.
- Native attachment GUI remains; physical attachment insertion/removal is not implemented.
- No independent persistent magazine inventory, general item pouch, finger-spinning implementation, or complete dual Physical/Advanced handling.
- Multiplayer arm placement remains partly Visor-owned. The proposed shared gun-provider/holster ownership API is a design, not a shipped interoperability layer.
- Latest source passed 314 tests per Forge/Fabric 1.20.1 and Fabric 1.21.1 target, 315 on NeoForge 1.21.1. This does not certify headset rendering or live multiplayer inventory transitions. The 1.21.1 ports remain experimental.

Use the [master checklist](docs/TESTING-CURRENT.md): it retains reported passes and identifies narrow retests. Historical release notes describe their own versions, not necessarily current behavior.

## Build and contribute

See [all four build commands and artifact paths](docs/BUILDING.md). Shared authored code lives under `src/main`; do not edit generated files under a target's `build/generated/shared` directory.

Development now proceeds on **`main`**. Earlier experiment branches are retained as checkpoints; their commits are included in `main`. The [Visor multiplatform template review](docs/VISOR-ADDON-MULTIPLATFORM-REVIEW.md) describes a staged route toward clearer common/platform modules without silently raising minimum Visor versions or dropping TaCZ port-specific behavior.

MIT license. Visor, TaCZ and gun-pack assets remain owned by their respective projects.
