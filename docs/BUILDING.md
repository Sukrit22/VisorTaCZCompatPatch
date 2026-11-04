# Building every supported target

Run these commands from the repository root. Use a **JDK**, not a JRE. The 1.20.1 targets currently use `0.14.1`; the 1.21.1 targets use `0.14.0-alpha.10+1.21.1`. These existing labels have not been unified by the folder cleanup. Build version strings are in each target's `build.gradle`; the build-all manifest reports the actual versions.

The `0.14.0` GitHub tag and download collection contain older uploaded alpha binaries; rebuilding its tagged source does not reproduce those older assets. See the README release table. This guide builds the current checkout, leaving release assets and existing game instances unchanged.

## Windows PowerShell

```powershell
Set-Location 'E:\visor-compat\visor-compat-tacz'

# Minecraft 1.20.1: JDK 17
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat build --console=plain                       # Forge (root forwards to forge/1.20.1)
.\gradlew.bat -p fabric/1.20.1 build --console=plain             # Fabric

# Minecraft 1.21.1: replace this with your installed JDK 21 path
$env:JAVA_HOME = 'C:\path\to\jdk-21'
.\gradlew.bat -p fabric/1.21.1 build --console=plain       # Fabric
.\gradlew.bat -p neoforge/1.21.1 build --console=plain     # NeoForge
```

Run the desired commands individually and stop if one fails. `build` includes the configured tests and packaging; do not routinely use `-x test`. First builds need internet for dependencies. `--offline` only works after the required artifacts are cached. No `clean` is needed for an ordinary rebuild; it removes that target's historical build outputs.

A convenience script already builds both 1.21.1 loaders, validates JDK 21, and stops on failure:

```powershell
.\scripts\Build-Minecraft121.ps1 -Loader Both -JavaHome 'C:\path\to\jdk-21'
# -Loader Fabric or -Loader NeoForge builds just that target.
```

On Linux/macOS use `./gradlew` and the corresponding JDK installation for `JAVA_HOME`.

| Target | Output folder | Current installable filename |
| --- | --- | --- |
| Forge 1.20.1 | `forge/1.20.1/build/libs` | `visor-compat-tacz-1.20.1-forge-0.14.1.jar` |
| Fabric 1.20.1 | `fabric/1.20.1/build/libs` | `visor-compat-tacz-1.20.1-fabric-0.14.1.jar` |
| Fabric 1.21.1 | `fabric/1.21.1/build/libs` | `visor-compat-tacz-1.21.1-fabric-0.14.0-alpha.10+1.21.1.jar` |
| NeoForge 1.21.1 | `neoforge/1.21.1/build/libs` | `visor-compat-tacz-1.21.1-neoforge-0.14.0-alpha.10+1.21.1.jar` |

Select the normal production JAR, not a sources/dev JAR. Do not rename a JAR to claim a different loader or Minecraft version. Install matching addon versions on server and clients. Nothing in these commands installs to CurseForge automatically.

## Dependencies and version boundaries

Each project pins its own Minecraft, loader, Visor and TaCZ compile dependencies:

| Project | Loader development target | Visor Modrinth version ID | TaCZ Modrinth version ID |
| --- | --- | --- | --- |
| `forge/1.20.1` | Forge 47.4.0 / MC1.20.1 | `gqaBzrB7` by default; `-PvisorVersion=<id>` override | `yOVIzIJR` (TaCZ) |
| `fabric/1.20.1` | Loader 0.17.2 / MC1.20.1 | `ehts00JW` | `c6sOTqZb` (Refabricated) |
| `fabric/1.21.1` | Loader 0.17.2 / MC1.21.1 | `7i7UZxdB` | `veM4XOhW` (Refabricated) |
| `neoforge/1.21.1` | NeoForge 21.1.219 / MC1.21.1 | `TfyC5Jd9` | `62q6mB5Q` (NeoForge TaCZ port) |

Dependency declarations are build inputs; accepted installed versions are separately declared in `mods.toml`, `fabric.mod.json`, or `neoforge.mods.toml`. Widening metadata alone does not establish API compatibility. Fabric also needs Fabric API and Forge Config API Port. Java targets are 17 for 1.20.1 and 21 for 1.21.1.

There is currently **no Forge 1.21.1 or NeoForge 1.20.1 target**, and no general arbitrary-version switch. Another Minecraft version requires matching Visor/TaCZ artifacts plus verified source adapters and mixins. Do not copy the Visor template's target ranges into this mod without those checks.

## Source and calibration ownership

- Edit gameplay in `src/main/java` and bundled data in `src/main/resources`.
- `fabric/1.20.1/shared-sources.gradle` and each 1.21.1 target generate build-local copies. Never edit those generated files.
- `ports/minecraft-1.21.1.gradle` handles common 1.21 API conversions. Loader-specific event, network and lifecycle adapters live in their target source directories.
- Builds use already bundled defaults; they do **not** copy your instance calibration automatically. Import your personal files into the root defaults first using [the calibration sharing/import instructions](CALIBRATION-SHARING.md). All four targets then consume those root resources. Preserve existing entries and keep an instance backup.
- A successful compile/test is not headset validation. Use [the test tracker](TESTING-CURRENT.md), especially after changing native TaCZ dependencies or mixins.

## Linear development workflow

All existing experimental branch tips were ancestors of `codex/independent-dual-wield`; `main` was fast-forwarded to that history. Continue new work on `main`, or use short-lived branches for risky changes and merge them when ready. Old branch names and the published `0.14.0` tag are retained; no history rewrite is needed.


## Fabric26.2 development target and extracted core

This is a migration/API-check target, **not a playable addon build**. See [architecture/status](ARCHITECTURE-MULTIVERSION.md) and [target instructions](../fabric/26.2/README.md). Use JDK25 and the target's wrapper:

```powershell
.\scripts\Prepare-Fabric26.ps1
$env:JAVA_HOME = 'C:\path\to\jdk-25'
.\fabric\26.2\gradlew.bat -p fabric/26.2 check --console=plain
```

Standalone shared mechanics, using JDK17 and the existing root wrapper:

```powershell
.\gradlew.bat -p common/core check --console=plain
```

Existing four target commands remain valid. `common/core` source is compiled directly by every target; edit it there rather than recreating files in root source or generated folders.

## Folder cleanup (2026-11-04)

`fabric` is now a container for version targets: use `-p fabric/1.20.1` instead of `-p fabric`. The previous `fabric-1.21.1` and `neoforge-1.21.1` projects moved to `fabric/1.21.1` and `neoforge/1.21.1`. Their local build outputs, logs and run directories moved with them. Existing root Forge commands and calibration resource locations are unchanged. This cleanup changes paths, not the remaining integration architecture; root `src` is still authoritative for shared Minecraft code.

## Build all targets and collect releases

From repository root on Windows:

```powershell
.\gradlew.bat -p all build --console=plain
```

This selects JDK17/21/25 for the individual builds, builds and tests all four production targets, runs the development-only26.2 checks, then copies the exact production JARs to `all/build/libs`. It does not package26.2. `latest-build.json` lists this successful run's target, version, filename and SHA-256. Historical JARs remain in the folder; consult the manifest rather than choosing by timestamp. If a target fails, collection does not start. No calibration import or instance installation is performed.

The script can also run directly, without needing Java on PATH for the outer wrapper:

```powershell
.\scripts\Build-All.ps1
# Optional explicit paths, otherwise detected automatically:
.\scripts\Build-All.ps1 -Java17Home 'C:\path\jdk-17' -Java21Home 'C:\path\jdk-21' -Java25Home 'C:\path\jdk-25'
# Only the four playable targets; skip26.2 development checks:
.\scripts\Build-All.ps1 -Skip26Checks
```

`JAVA17_HOME`, `JAVA21_HOME`, `JAVA25_HOME`, `JAVA_HOME`, repo `build/tools`, user `.gradle/jdks`, and standard Windows Java directories are searched, with `javac -version` verified. `-Offline` skips downloading26.2 dependencies and requires all dependencies already cached. The Gradle convenience command requires Windows PowerShell; other platforms can run each target directly.

Forge now lives at `forge/1.20.1`. Root wrapper commands still forward there, so `./gradlew.bat build` remains valid. New outputs and development runs live inside that target. Previous root `build` and `run` directories are retained as historical data; they are not migrated worlds. Shared `src/main/resources` stays in place, so existing calibration import scripts remain valid. The existing Gradle8 wrapper serves1.20.1/1.21.1;26.2 uses its own Gradle9 wrapper.
