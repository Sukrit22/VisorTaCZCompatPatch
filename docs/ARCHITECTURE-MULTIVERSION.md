# Multiversion architecture and Fabric 26.2 migration

Updated 2026-11-04. Work branch: `codex/fabric-26-architecture`. This branch preserves the pre-existing uncommitted gameplay/calibration work. The 26.2 target is **not yet a playable addon**.

## Decision

Use explicit common source and loader/version adapters, introduced incrementally. Prefer `common/core` over `common/common`: the name states that these classes cannot depend on Minecraft, Visor, TaCZ or a loader. Keep client/server/mixin packages within the shared integration source only when their actual dependencies and target signatures are shared.

The user's proposed layout is a good direction. Creating directories alone does not remove Forge imports or version-specific APIs. Shared source is compiled into each artifact, not installed as an additional mod.

Desired ownership:

```text
common/
  core/src/main/java/                  # version-independent mechanisms/contracts
  integration/src/main/java/          # future shared client/server/network logic behind adapters
  integration/src/main/resources/     # future shared gun TOML/calibration assets
  1.20.1/src/main/java/                # future Minecraft/TaCZ adapter implementations
  1.21.1/src/main/java/                # future components/rendering adapters
  26.2/src/main/java/                  # first native gun-data adapter implemented
fabric/
  shared/src/main/java/               # future genuinely common Fabric hooks
  1.20.1/, 1.21.1/, 26.2/              # isolated loader + Minecraft target builds
forge/1.20.1/
neoforge/1.21.1/
```

The Fabric targets now live at `fabric/1.20.1`, `fabric/1.21.1` and `fabric/26.2`; NeoForge lives at `neoforge/1.21.1`. Forge now lives at `forge/1.20.1`, with root commands forwarding there and shared integration source still in root `src`. The future `common/integration`, common1.20.1/1.21.1 adapters, Fabric shared hooks are not implemented yet. Build commands use the new target paths; see [BUILDING.md](BUILDING.md).

## What changed now

21 pure mechanism classes moved from root `src/main/java/.../physical` into `common/core`, retaining packages and behavior. Root Forge1.20.1, Fabric1.20.1, Fabric1.21.1 and NeoForge1.21.1 compile those authored files directly. They do not pass through the source-rewriting scripts. A platform-neutral `GunSnapshot` record forms the first data boundary.

`common/core` can compile/test independently on Java17. Its `verifyCoreBoundary` check rejects platform imports. Its selected 21 tests are a subset of the full integration suites; excluded integration tests remain in the existing targets. `GunStatus`, for example, stays outside core because it depends on `Handling.Phase` and that dependency closure is not yet pure.

`common/26.2/NativeGunData` reads the pinned TaCZ gun ID/display/ammo/chamber/fire mode into `GunSnapshot`. `fabric/26.2/PlatformPaths` owns Fabric config-path lookup. A separate compile-only probe verifies Visor player lookup and basic BedrockPart rendering signatures. These are real dependency checks, not replacement/stub APIs, but no gameplay entrypoint calls them yet.

## How this compares with Visor's template

Reviewed the current template at commit `559576bc031369c4da17aa0a8515de5a0321800a`, unchanged from the prior review. It publishes common Java/resources from `mod-core` into loader projects and uses version properties to select dependencies/toolchains. Visor's own Stonecutter architecture and this template's property-based selection are distinct. See [the detailed template review](VISOR-ADDON-MULTIPLATFORM-REVIEW.md).

| Approach | Benefit | Cost / decision |
| --- | --- | --- |
| Keep root source + generated replacements | Existing four targets already work | Replacement rules multiply with API changes; retain temporarily, retire by subsystem |
| Explicit common core + version/loader adapters | Clear ownership, parallel target builds, independent tests | Requires actual API boundaries, not just file moves; selected approach |
| Switch wholesale to template/Stonecutter | Uniform matrix/tooling | Does not solve TaCZ/render/API differences; risks current builds and older Visor support; defer |

Prefer isolated target projects over several jobs rewriting the same active version properties. Each Minecraft/loader target produces a separate artifact. Do not claim one universal 26.x JAR.

## Concrete 26.2 target

- Minecraft26.2, JDK25, Gradle9.7.1, Fabric Loom1.17.21 (unobfuscated, no mappings/remap path).
- Fabric Loader0.19.5 satisfies both selected upstream mods; TaCZ alone permits0.19.3, but Visor0.6.1 requires0.19.5.
- Fabric API0.155.2+26.2, Visor0.6.1 (`egOqPxop`).
- Unofficial TaCZ Refabricated `1.1.8+fabric.26.2.R3-hotfix4`; SHA-256 pinned in `fabric/26.2/dependencies.lock.json`. Other TaCZ26 forks are not certified by this target.
- Confirmed native ID API is `net.minecraft.resources.Identifier`, replacing the older `ResourceLocation`. BedrockPart still exposes the probed pose/render methods; that alone does not certify the surrounding render pipeline.

Use [the target instructions](../fabric/26.2/README.md). Packaging is disabled; there is deliberately no misleading partial addon JAR.

## Remaining port work / acceptance gates

1. Extract platform services for event/lifecycle registration, config, packets and item components. Preserve server authority and per-hand item identity. Keep old target adapters working.
2. Move shared integration behind those services; replace each generator rule only after corresponding tests pass. Keep version-specific mixin descriptors/targets outside falsely shared classes.
3. Port26.2 client rendering/UI/input, shader/optic targets, native gun operators/reload events/Lua hooks and remote poses. Audit actual APIs and semantics, not just renamed methods. Extend the existing26.2 native component/ItemStack codec tests to full tactical state, gameplay packets and lifecycle transitions.
4. Compile full feature code, verify packaged resources/dependencies, then launch real client and dedicated server. Add headset tests for input ownership, stereo optics and inventory transitions. Only then enable packaging/release claims.
5. Add26.1.2/26.3 as distinct verified targets if wanted; reuse adapters only after compatibility checks.

Existing magazine top-round visibility and automatic-shotgun motion reports remain separate open runtime issues; this architecture change does not claim to fix them.

## Evidence

After extraction: Forge/Fabric1.20.1 and Fabric1.21.1 suites each335 tests, NeoForge1.21.1 suite336 tests; all pass. Existing1.21.1 component persistence checks pass. The standalone core and Fabric26.2 development target each run21 selected tests; real26.2 native gun/Visor/model API compilation passes. These counts do not imply full26.2 gameplay or headset compatibility.

Upstreams: [template](https://github.com/VisorModStudio/VisorAddonMultiplatform), [Visor releases](https://github.com/VisorModStudio/Visor/releases), [selected TaCZ26 fork](https://github.com/q14433686-arch/TaCZ_Refabricated_Unofficial).

### 2026-11-04 follow-up

Added native26.2 CUSTOM_DATA access in `common/26.2`, with typed default reads and copy-safe writes/removal. Three tests exercise real Minecraft component and ItemStack wire codecs using a stone fixture with explicitly bound common defaults; this does not certify resource loading, full tactical state, or game startup.

Revolver Use-gate settings pass all four existing targets: 338 tests on Forge1.20.1, Fabric1.20.1 and Fabric1.21.1; 339 on NeoForge1.21.1. Both1.21.1 native component checks pass. Standalone core:34 tests; Fabric26.2 harness:37 tests plus API probes. No new packaged addon or headset validation.
