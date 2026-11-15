# 0.14.2-alpha.2 — TaCZ 1.2 compatibility and gun-profile review

**Experimental pre-release.** Includes the TaCZ 1.2 migration from alpha.1 and the profile/model corrections in alpha.2. Choose exactly one addon JAR matching your Minecraft version and loader. Use this addon version on both client and server (protocol 37); replace the previous addon JAR rather than keeping both.

## Required mods and Java

| Addon target | Java / loader | Required TaCZ build | Other required mods |
| --- | --- | --- | --- |
| Forge 1.20.1 | Java 17; Forge 47.4.x (built with 47.4.0) | [TaCZ 1.2.0](https://modrinth.com/mod/timeless-and-classics-zero/version/QGCbNkTy) | Visor for Forge 1.20.1 |
| Fabric 1.20.1 | Java 17; Fabric Loader 0.17.2+ | [Refabricated 1.0.0-forge1.2.0+mc1.20.1](https://modrinth.com/mod/tacz-refabricated/version/wbgtQwxp) | Visor; Fabric API (built with 0.92.6+1.20.1); Forge Config API Port (built with 8.0.3) |
| Fabric 1.21.1 | Java 21; Fabric Loader 0.17.2+ | [Refabricated 1.0.0-neo1.2.0+mc1.21.1](https://modrinth.com/mod/tacz-refabricated/version/eoaP2iB9) | Visor 0.6.0-beta or compatible later build; Fabric API (built with 0.116.17+1.21.1); [Forge Config API Port 21.1.6](https://modrinth.com/mod/forge-config-api-port/version/N5qzq0XV) |
| NeoForge 1.21.1 | Java 21; NeoForge 21.1.x (built with 21.1.219) | [Official TaCZ 1.2.0](https://modrinth.com/mod/timeless-and-classics-zero/version/O2Pfsh6w) | Visor 0.6.0-beta or compatible later build for NeoForge 1.21.1 |

Older TaCZ binaries are intentionally rejected. Use each dependency's matching Minecraft/loader file and install its own required dependencies as well. Fabric API and Forge Config API Port versions above identify the build baseline, not newly imposed exact-version requirements.

### Visor version details

- Forge 1.20.1 was compiled against [Visor 0.5.0](https://modrinth.com/mod/visor/version/gqaBzrB7). Metadata accepts 0.5.0, 0.6.0-alpha.1/.2/.3, 0.6.0-beta and 0.6.0.
- Fabric 1.20.1 was compiled against [Visor 0.6.0-alpha.2](https://modrinth.com/mod/visor/version/ehts00JW). Metadata accepts 0.4.0 or later; that range does not establish compatibility with every QuestCraft/custom build.
- Fabric 1.21.1 uses [Visor 0.6.0-beta](https://modrinth.com/mod/visor/version/7i7UZxdB); NeoForge 1.21.1 uses [Visor 0.6.0-beta](https://modrinth.com/mod/visor/version/TfyC5Jd9). Metadata accepts beta or later; later versions still need runtime verification.

TaCZ: Durability (`gundb`) is **optional**. The compatibility bridge targets its 2.2.2 legacy sound API where that mod supports the loader. No Fabric durability port is included. No custom gun pack is required. No Minecraft 26.x artifact is included in this release.

## Changes

- Migrate all four targets to TaCZ 1.2 sound, operator and rendering APIs; preserve the legacy sound descriptor used by optional durability support.
- Correct AA-12 physical charging to move the top `charge_handle`, with the separate bolt indicator, corrected grab zone and native shot direction/travel.
- Correct M1014 shot travel, SPAS-12 pump versus self-loading shot animation, and M700's bolt/chamber-round group.
- Prevent child bolts from receiving their parent's movement twice. Match configured shot excursions to native animation peaks.
- Audit all 70 bundled profiles against real model parts, hierarchy, animation channels and sound resources. Add TOML fields for signed bolt coupling, shot followers, fire-mode filtering and action sounds.
- Add 12 experimental physical profiles: **AK-12, AK-74M, HCAR, MP7A1, MCX Spear LT, MK18, QR-24, SCAR Mk20, SVD, URGI, M1897 and TEC-9**.
- Add explicit button profiles for HM50B, MRAD, M1866 and PKM. Their straight-pull, independent-joint, lever and belt mechanisms respectively need further work.

There are now **39 physical-template profiles and 31 button-only profiles**. New physical profiles are calibration/test candidates, not headset-certified support.

## Upgrading and calibration

Back up your world/config before testing. Existing bundled calibration, holster and inspection values are preserved; new guns receive initial defaults and are not marked calibrated. Personal instance files are not replaced by this release.

Custom `config/visor_tacz/profiles/*.toml` entries replace the entire bundled gun entry. An old AA-12 override can therefore hide this fix: merge the corrected parts/visual/zone fields and restart. Personal calibration JSON is a separate overlay; an old rack offset may still move the orange zone. Do not reset all calibration to obtain the fix.

## Validation and known limitations

- All four builds passed: 356 automated tests each on Forge 1.20.1, Fabric 1.20.1 and Fabric 1.21.1; 357 on NeoForge 1.21.1. No tests skipped.
- Static model/profile audit: 70 profiles, zero errors. Packaged profiles and SHA-256 hashes verified; `manifest.json` accompanies the JARs.
- Alpha.2 has **not** received headset or installed multiplayer acceptance testing. Earlier alpha.1 Fabric launches reached registration/resource initialization, which is not alpha.2 runtime certification. Forge/NeoForge installed startup remains pending.
- SPAS-12 still uses the pump template; an independent grab zone for its small side charging handle is not implemented.
- Shot timing uses the addon's animation envelope rather than replaying every native keyframe. AA-12/UMP use documented borrowed short action sounds where native isolated clips are unavailable.
- Test ammunition counts, reload interruptions, jam clearing, optics, dual wield and calibration with the updated pack before using important worlds.

See [profile review and VR test list](PROFILE-REVIEW-1.2.md), [full audit](generated/PROFILE-AUDIT.md), [TaCZ migration notes](TACZ-1.2-UPGRADE.md), and [build instructions](BUILDING.md).
