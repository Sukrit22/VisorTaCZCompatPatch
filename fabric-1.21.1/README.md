# Fabric 1.21.1 target

This independent Gradle project targets Minecraft 1.21.1, Java 21 and Fabric Loader 0.17.2. It generates shared code from authored `../src/main/java` through `shared-sources.gradle` and `../ports/minecraft-1.21.1.gradle`. Edit authored sources or generation rules, never generated files. Existing 1.20.1 projects are independent.

Build from the repository root with a full JDK 21:

```powershell
./gradlew.bat -p fabric-1.21.1 build testStackData --console=plain
```

`dependency-lock.json` records the exact upstream Modrinth releases and published hashes. The Gradle build resolves those version IDs, exposes nested upstream libraries for development and does not bundle the upstream mods in this addon. `verifyProductionJar` checks target metadata, resource pack format 34 and the Bullet constructor intermediary refmap.

`productionServer` and `productionClient` launch the packaged, remapped release with published upstream jars. Their isolated game directory is `build/production-smoke/<version>`. The client requires asset downloads and a graphical environment. A server reaching the world does not cover lazy gun scripts: equip a loaded entity with a gun and let it tick.

Minecraft 1.21.1 gun-equipped server smoke command:

```mcfunction
forceload add 0 0
summon minecraft:zombie 0 80 0 {Tags:["visor_smoke"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b,HandItems:[{id:"tacz:modern_kinetic_gun",count:1,components:{"minecraft:custom_data":{GunId:"tacz:m870"}}},{}]}
data get entity @e[tag=visor_smoke,limit=1] HandItems
```

Wait at least 15 seconds after summoning, then use `stop` to save and shut down. Headset input, stereo rendering, audio and multiplayer gameplay require separate validation.
