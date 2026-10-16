# Visor 0.6.0-beta compatibility — Minecraft 1.20.1

Checked 2026-10-14 against official Modrinth builds i4ztAonN (Forge) and aw1aV4jW (Fabric). Downloaded copies only into build/visor-beta-audit; no installed instance changed.

alpha.5 Forge explicitly listed exact accepted Visor versions and omitted 0.6.0-beta. alpha.5.1 adds that exact version; no broad wildcard. Protocol stays 32 and feature behavior stays alpha.5. Fabric already allows >=0.4.0, so no Fabric dependency-range change was needed.

Binary audit: all 99 unique Visor method/field references in each alpha.5 packaged JAR resolved against its corresponding beta JAR, following superclass/interface inheritance. This is a linkage check, not proof of behavior. Hotbar field names and onRun/setOffhandSlot/handleSlotCollision signatures remain; server receivedPosePacket, render startVRGuiPhase, and crosshair isVisible targets were inspected too.

Forge alpha.5.1 compiled and built directly against beta using -PvisorVersion=i4ztAonN; 257 tests passed. Fabric alpha.5.1 built with its existing dependency and production-JAR verification; 257 tests passed. Both archives passed ZIP validation. Default Forge compile dependency stays 0.5.0 unless the property is supplied.

Headset/client runtime validation remains pending: startup, VR toggle, GUI/wrist cursor, holster draw, radial collision, shooting/reload/ADS, reconnect and flat transition. This does not certify VisorEssentialsPlus, backpack integration, desktop/notetaker/overlay addons or arbitrary packs. Existing alpha.5 limitations and INV01 remain unresolved.

Official files:
- https://modrinth.com/mod/visor/version/i4ztAonN
- https://modrinth.com/mod/visor/version/aw1aV4jW

Forge beta development-server smoke: reached world with M870-equipped entity; no mixin failure observed. This covers server bootstrap/lazy gun code, not client/headset initialization.
