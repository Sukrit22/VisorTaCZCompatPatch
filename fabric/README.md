# Fabric targets

Run commands from the repository root.

| Minecraft | Project | JDK | Status |
| --- | --- | --- | --- |
| 1.20.1 | `fabric/1.20.1` | 17 | Existing addon |
| 1.21.1 | `fabric/1.21.1` | 21 | Existing port; runtime acceptance remains separate |
| 26.2 | `fabric/26.2` | 25 | Development checks only; no playable JAR |

Use `./gradlew.bat -p fabric/1.20.1 build` or `./gradlew.bat -p fabric/1.21.1 build`.
The 26.2 target has its own wrapper: `./fabric/26.2/gradlew.bat -p fabric/26.2 check`.

The old `-p fabric` command no longer selects a build. See [build instructions](../docs/BUILDING.md).
