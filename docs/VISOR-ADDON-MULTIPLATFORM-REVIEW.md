# VisorAddonMultiplatform review and adoption plan

Reviewed 2026-10-26 at upstream commit [`559576bc031369c4da17aa0a8515de5a0321800a`](https://github.com/VisorModStudio/VisorAddonMultiplatform/tree/559576bc031369c4da17aa0a8515de5a0321800a). This is a source review, not a claim that every template target was built or headset-tested.

## Recommendation

Adopt the shared-core/platform-module architecture incrementally. Keep the four current build targets working while replacing fragile source rewriting with explicit compatibility adapters. Do not replace the released project's build wholesale or raise the Visor baseline as part of documentation/branch cleanup.

The template is an addon example, not the complete implementation of Visor's own version-management system. Its `gradle.properties` presets cite Visor's Stonecutter matrix, but this template itself uses Gradle properties and target switching, not Stonecutter source preprocessing. Visor's public Minecraft/loader adapters are a separate reusable facility; see [the earlier source/API review](VISOR-MULTIVERSION-AND-INTEROP.md).

## What the template actually does

- `mod-core` publishes shared Java source and resources. Fabric/Forge projects compile that same source into their own artifacts; `mod-core` is not another mod users install.
- Thin loader entrypoints register the client or server Visor addon through `VisorAPI`. The common project compiles against the Visor API classifier; loader projects use full Visor at development runtime.
- `versions/*.properties` holds Minecraft/Java/loader/Visor versions. `gradlew useMinecraft-1.20.1` or `useMinecraft-1.21.1` rewrites a marked block in `gradle.properties` and adjusts access-widener namespace. Those are **template commands**, not commands in our current repository.
- It uses Architectury Loom for the included Fabric and Forge projects, central resource placeholders and toolchains. Newer unobfuscated Minecraft versions use a no-remap path. The wrapper currently selects Gradle 9.7.1 and daemon JDK21.
- It extracts Visor's nested runtime libraries for development because Loom strips nested jars from remapped dependencies. Our Fabric builds already solve the corresponding issue for Visor **and TaCZ**.

Relevant upstream files: [root build](https://github.com/VisorModStudio/VisorAddonMultiplatform/blob/559576bc031369c4da17aa0a8515de5a0321800a/build.gradle), [target switching](https://github.com/VisorModStudio/VisorAddonMultiplatform/blob/559576bc031369c4da17aa0a8515de5a0321800a/gradle/minecraft.gradle), [common source publication](https://github.com/VisorModStudio/VisorAddonMultiplatform/blob/559576bc031369c4da17aa0a8515de5a0321800a/mod-core/build.gradle), [1.20.1 preset](https://github.com/VisorModStudio/VisorAddonMultiplatform/blob/559576bc031369c4da17aa0a8515de5a0321800a/versions/1.20.1.properties).

## Comparison

| Concern | Our current project | Template / implication |
| --- | --- | --- |
| Shared gameplay | Root Forge-shaped source; generated copies for other targets | Common source compiled by loader projects; cleaner ownership, but moving files alone does not remove Forge imports |
| Loader APIs | Local event/network/lifecycle bridges and transformations | Thin entrypoints plus Visor services; can replace some bridges after API availability checks |
| Minecraft APIs | Shared 1.21 conversion script and StackData helper | Version presets choose dependencies; API-semantic differences still need code adapters |
| Build targets | Four independent projects, Gradle8.12 | One selected MC version with enabled loaders; mutable target file is less convenient for parallel CI |
| NeoForge | Existing 1.21.1 target | No NeoForge module in inspected template; Forge is not NeoForge |
| Minimum Visor | Some 1.20.1 metadata permits older builds, including Fabric0.4.0 | Template presets require0.6.0; direct adoption could break QuestCraft/older users |
| Native gun APIs | TaCZ, Refabricated and NeoForge port; script/model/mixin differences | Template has no TaCZ integration and cannot resolve those differences |

## Constraints we must preserve

1. Separate artifacts per loader and Minecraft version. Neither approach yields a universal JAR.
2. TaCZ native shot/reload hooks, Lua operators, model rendering and mixin descriptors remain version-sensitive.
3. Minecraft1.21 item components require persisted writes, not mutations of copied NBT. Keep the actual-Minecraft persistence tests and packet-codec checks.
4. Shader/render pipeline changes and dedicated-server classloading need runtime checks in addition to compilation.
5. Do not adopt Visor0.6-only classes into a target that still claims older Visor support. Either keep an adapter/fallback or explicitly create a new support baseline.
6. Do not transplant the template's newer wrapper/plugin versions into our ForgeGradle6 project without a separate compatibility exercise.

## Practical migration sequence

1. Centralize release version and the four-target dependency matrix first; retain current artifact naming and build commands. This removes metadata drift without moving gameplay code.
2. Define small explicit platform interfaces for event registration, networking, config paths and item custom-data persistence. Use public Visor APIs where every supported target actually provides them.
3. Move pure mechanism/calibration/ownership logic to a common module. Move client/server integration only once its loader imports are behind those adapters. Keep authored gun TOML and calibration resources shared.
4. Add loader modules for Fabric, Forge and NeoForge, with explicit Minecraft-version adapters. Choose either isolated target projects or parameterized CI builds; avoid several jobs rewriting the same `gradle.properties` checkout.
5. Retire each text replacement only after equivalent behavior passes all four test suites, production packaging/resource checks, dedicated-server startup, and targeted headset tests. Keep the current generator as the working path during transition.

This review recommends the direction; the migration is not implemented by the README/main-branch update. Our existing commands are in [BUILDING.md](BUILDING.md).
