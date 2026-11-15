# TaCZ 1.2 upgrade — 0.14.2-alpha.1

This branch targets the November 2026 TaCZ API. Use the matching dependency below; this release intentionally rejects older TaCZ binaries.

| Minecraft / loader | Required TaCZ | Modrinth version ID |
| --- | --- | --- |
| 1.20.1 Forge | 1.2.0 | QGCbNkTy |
| 1.20.1 Fabric | Refabricated 1.0.0-forge1.2.0+mc1.20.1 | wbgtQwxp |
| 1.21.1 NeoForge | Official TaCZ 1.2.0 | O2Pfsh6w |
| 1.21.1 Fabric | Refabricated 1.0.0-neo1.2.0+mc1.21.1 | eoaP2iB9 |

The 26.2 experimental harness is unchanged. No instance mods or personal calibration files were replaced.

## Changes

- Use native SoundData volume/pitch settings and the new positional sound constructor for independent guns.
- Implement the new unloadAmmo operator method within the weapon's existing scoped context, including native main-hand reads.
- Remove the obsolete muzzle-flash shadow: TaCZ now refreshes the matrices itself.
- Hook the new full gun-render overload for stencil preservation. Keep named and intermediary selectors for Fabric development and release environments.
- Use the model library bundled with TaCZ in Fabric. Forge's older model API is compile-only; it no longer overrides TaCZ's newer runtime library.
- Update Fabric 1.21.1's render-frame callback.
- Refresh the model-parts catalogue to 70 guns. This does not promise physical handling for the 16 new guns; they use automatic button support until explicitly profiled. Existing personal calibration remains untouched. No existing profile part reference was found to have been removed by this pack update.

## Optional TaCZ: Durability (`gundb`)

The latest published version checked was still 2.2.2. Its inspection/jam code calls the old `GunDisplayInstance.getSounds(String): ResourceLocation` descriptor. A client-only binary bridge preserves that descriptor alongside TaCZ's new SoundData return type. The addon does not require gundb and does not modify its JAR.

The exported, transformed Fabric runtime class was inspected and contains both return descriptors. This validates the bridge transformation, **not** an end-to-end gundb jam-clear test. Test gundb on its supported Forge/NeoForge loader; no Fabric gundb port is supplied here.

## Validation and remaining tests

All four targets compile and build. The suite passes 347 tests on each Forge/Fabric target and 348 on NeoForge. Automated tests cover the new stencil location and sound bridge call descriptor as well as the existing suite.

Production Fabric 1.20.1 and 1.21.1 launches reached addon registration and resource/atlas initialization without the earlier stencil mixin crash. No headset was connected. The 1.21.1 launch skipped the asset-download task after its download failed, using available cached assets. Forge's development client launch was blocked by Visor's mapped BlockAccessor; it is not counted as a successful runtime check. Forge/NeoForge installed runtime validation remains pending.

Before release, test:

1. Start and join a world on each loader, with and without optional gundb where available.
2. Single/dual firing, reload sounds, unload, switch slots, disconnect/rejoin. Confirm ammunition counts and that the other hand's gun is unchanged.
3. Durability inspection and jam clearing, both ordinary button handling and VR physical handling.
4. Loose and attached optics, attachment screen, third-person gun rendering and shader/no-shader stencil behavior.
5. M4/Glock/M700/M870/revolver model movement, muzzle/ejection origins and existing calibration against the updated pack.
6. A new TaCZ 1.2 gun in button mode. Do not mark its physical/advanced handling as supported without a profile and in-game test.

Build commands and JDK selection remain in [BUILDING.md](BUILDING.md).
