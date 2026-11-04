# Shared source ownership

`core/src/main/java` is authored, platform-independent Java. Existing Forge/Fabric 1.20.1 and Fabric/NeoForge 1.21.1 compile it directly with its existing packages; it is not copied through source rewriting. Minecraft 26.2 compiles the same files on Java 25. No additional core mod is installed.

`26.2/src/main/java` contains the first Minecraft/TaCZ-specific adapter. It is compiled only by `fabric/26.2`. It is a migration foundation, not complete 26.2 gameplay integration.

Remaining authored integration code stays in root `src/main/java` for now. Its client, server, mixin and network packages still depend on Minecraft/TaCZ and some Forge APIs. Existing generated platform copies remain transitional. Do not edit `build/generated/shared`.

Boundary: core may not import Minecraft, Fabric/Forge/NeoForge, TaCZ, Visor or addon client/server/network classes. `common/core` has a standalone `check` task enforcing this. Move a class only after its dependency closure fits that boundary; folder relocation alone does not make it shared.

From repository root, JDK17: `./gradlew.bat -p common/core check`. Full existing target suites retain integration coverage; the standalone suite deliberately uses only core-compatible test classes.
