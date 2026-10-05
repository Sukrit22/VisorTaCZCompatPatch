# Fabric 1.20.1 port

This project builds the Fabric variant from the shared gun-handling sources in
`../src/main/java`. Loader adapters live in `src/main/java`; `shared-sources.gradle`
generates the adapted sources under `build/generated/shared`. Edit the originals
or the transformation, never the generated files. Calibration defaults and gun
TOML files come from the same resources as the Forge build.

## Build

From the repository root, with Java 17 installed:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat -p fabric build --console=plain
```

The installable JAR is under `fabric/build/libs/`. Use the remapped JAR, not a
development JAR. The root `build` task continues to build Forge separately.
The Fabric build runs the same unit tests as Forge. Development launch commands
are `./gradlew.bat -p fabric runClient` and `./gradlew.bat -p fabric runServer`;
their files stay under `fabric/run/`. The build exposes libraries embedded in
upstream mods on the development classpath; it does not copy those libraries
into the addon JAR. Accept Minecraft's EULA yourself before running a server.

## Dependencies

Use Minecraft 1.20.1, Fabric Loader 0.17.2 or later, Fabric API, Fabric Visor,
TaCZ Refabricated, and Forge Config API Port. Despite its name, the latter has a
Fabric build. Install the Fabric versions of dependencies, not the Forge JARs.

Compile targets are Visor 0.6.0-alpha.2 and TaCZ Refabricated
0.7.0-forge1.1.8-hotfix. Runtime metadata does not require those exact builds;
other versions still need compatible APIs. Visor and TaCZ are required mods.
SimpleBedrockModel is provided by TaCZ Refabricated at runtime.

## Validation before a public release

Alpha.13 fixes the bullet constructor injection's runtime descriptor remapping.
The build now checks the packaged constructor refmap and version metadata.
On 2026-10-04, all 200 shared tests and the packaged checks passed. The release
JAR initialized successfully in production mode with TaCZ Refabricated 0.7.1 and
desktop Visor 0.6.0-alpha.2, including addon/render-effect registration. VR fell
back to flat because no HMD was available. Pico/Android and gameplay remain untested.
To test the packaged JAR rather than development classes, run:

```powershell
.\gradlew.bat -p fabric build productionClient --console=plain
```

This uses official Minecraft, published dependency JARs and `fabric.development=false`
in `fabric/build/production-smoke/<version>/`. It defaults to TaCZ Refabricated
0.7.1 for 1.20.1, matching the reported Pico failure. The compile target remains
0.7.0. Use `-PsmokeTacz=c6sOTqZb` to test that older target. It opens a separate
desktop client; it does not test Android native libraries or headset behavior.

2026-10-03, experimental 0.12.0-alpha.12:

- Compilation and production JAR remapping passed; all 200 shared unit tests passed.
- Development server initialized the mods and reached the EULA gate. No world or
  multiplayer session was started.
- Development client initialized rendering and registered the `visor_tacz` addon,
  `tacz_gun`, `tacz_status`, and `tacz_late_gun` effects without crashing. Visor
  fell back to flat mode because no HMD was available (`ERROR_FORM_FACTOR_UNAVAILABLE`).
- Headset gameplay, production-launcher startup, multiplayer, and shader optics
  remain unverified. This is a testing build, not a verified public release.

Compilation is only the first check. Test client startup and a dedicated server,
world join/rejoin, flat/VR switching, gun selection, button firing/reloading,
physical and advanced handling, optics with and without shaders, calibration
save/reload, recoil, magazine ejection, and multiplayer poses. Optional Forge-only
addons are not automatically available on Fabric. Do not promise interoperability
between Fabric and Forge servers based on this port alone.
