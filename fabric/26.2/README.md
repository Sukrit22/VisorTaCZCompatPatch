# Fabric Minecraft 26.2 migration target

**Not an installable/playable addon yet.** `check` compiles shared mechanics, a native gun data adapter, Fabric path lookup, and basic Visor/model API probes against real published dependencies. It runs selected pure mechanism tests and native ItemStack component/packet round-trip tests on Java25. No addon entrypoint/metadata or complete feature integration exists. JAR packaging is disabled intentionally.

Preparation from repository root:

```powershell
.\scripts\Prepare-Fabric26.ps1
$env:JAVA_HOME = 'C:\path\to\jdk-25'
.\fabric\26.2\gradlew.bat -p fabric/26.2 check --console=plain
```

Use this target's Gradle9.7.1 wrapper; the root Gradle8.12 wrapper remains for existing targets. Dependency identities and SHA-256 checksums are pinned in `dependencies.lock.json`. The upstream TaCZ fork also requires Forge Config API Port26.2.1 for a future game launch; this compile-only harness is not a runtime dependency installer.

Verified here: Minecraft26.2, Fabric Loader0.19.5, Fabric API0.155.2+26.2, Visor0.6.1, unofficial TaCZ Refabricated1.1.8+fabric.26.2.R3-hotfix4. This does not assert support for 26.1.2, 26.3, other TaCZ forks or all 26.x versions.

Pending: loader lifecycle/config/event bridge, complete gameplay packet codecs and inventory lifecycle integration, native shooting/reload/dual contexts, input ownership, UI and stereo rendering, optics/shaders, mixin target audit, complete tests and client/dedicated-server/headset acceptance. See [architecture plan](../../docs/ARCHITECTURE-MULTIVERSION.md) for the staged migration.

Validation on 2026-11-04: 37 tests pass, including three native component tests. The stone test fixture binds common vanilla defaults explicitly; this verifies component mutation/copy and ItemStack wire encoding, not a complete resource-loading or game lifecycle. Full TaCZ tactical state and custom gameplay packets are not yet integrated.
