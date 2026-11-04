# Build all targets (Windows)

From repository root: `./gradlew.bat -p all build --console=plain`.
Alternatively: `./scripts/Build-All.ps1` (automatically finds each required JDK).

Builds four production targets and checks Fabric26.2; only four production JARs are collected.
Output: `all/build/libs`; `latest-build.json` identifies this run's exact artifacts.
See [build instructions](../docs/BUILDING.md) for overrides and prerequisites.
