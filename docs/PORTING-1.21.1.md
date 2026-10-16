# Minecraft 1.21.1 ports

These targets are under development. A configured build or successful compilation alone does not establish VR compatibility. The 1.20.1 targets remain separate.

| Target | Project directory | Java | Visor dependency | TaCZ dependency |
| --- | --- | --- | --- | --- |
| Fabric 1.21.1 | `fabric-1.21.1` | JDK 21 | `7i7UZxdB` (0.6.0-beta) | `veM4XOhW` (Refabricated 0.7.1 / 1.1.8-hotfix2) |
| NeoForge 1.21.1 | `neoforge-1.21.1` | JDK 21 | `TfyC5Jd9` (0.6.0-beta) | `62q6mB5Q` (1.1.8-hotfix-r7) |

IDs refer to Modrinth releases. These are independent upstream TaCZ ports; matching Minecraft versions do not guarantee identical internal APIs.

## Source ownership

Gameplay and bundled calibration remain authored in `src/main/java` and `src/main/resources`. Each target generates its own source copy under its `build` directory. Never edit generated copies.

`ports/minecraft-1.21.1.gradle` contains the shared Minecraft API migration. Loader-specific adapters and transformations belong in their target directory. Changes needed by all versions should still be authored in the root source.

In 1.21.1, addon item state uses the `CUSTOM_DATA` component. Reads return snapshots; mutations must use the `StackData` helper so changes persist. The generator rejects legacy NBT calls left behind by migration. This preserves unrelated item components and custom data.

Networking uses Minecraft 1.21 custom payloads, with loader-specific registration. Packet handlers still perform shared protocol and direction checks. Only use matching addon builds on clients and servers.

## Build commands

From the repository root, with a **JDK**, not just a JRE:

```powershell
.\scripts\Build-Minecraft121.ps1 -Loader Both -JavaHome 'C:\path\to\jdk-21'
```

Use `-Loader Fabric` or `-Loader NeoForge` for one target. Add `-Offline` only after dependencies have downloaded successfully. Tests remain enabled.

Equivalent direct commands:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
.\gradlew.bat -p fabric-1.21.1 build --console=plain
.\gradlew.bat -p neoforge-1.21.1 build --console=plain
```

Outputs belong in each target's `build/libs` directory. Do not install a Fabric JAR on NeoForge, or use either 1.21.1 JAR in a 1.20.1 instance.

Calibration import remains a separate step: update the root bundled defaults with the existing import procedure before building. Both targets read the same root resources. Building does not import instance calibration automatically.

## Validation gates

- Compile both targets against the exact upstream dependencies.
- Run shared tests and the actual-Minecraft component persistence regression test.
- Start an isolated server and load a TaCZ gun to exercise lazy script mixins.
- Start a client, enter a world, equip a gun and attachment, then switch flat/VR.
- Verify saved ammo, jam and reload state survives world exit/rejoin.
- Verify dual slots, hotbar ownership, holsters and ordinary-tool input.
- Verify optics, per-eye HUD, hands, sound and recoil in a headset.

Use a separate test instance and a backup of its world. Do not use a 1.21.1 world to test a downgrade to 1.20.1. Headset results from 1.20.1 do not count as 1.21.1 passes.
