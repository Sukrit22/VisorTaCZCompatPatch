# 0.14.0-alpha.6 source update

Protocol 33: use matching client/server builds. Alpha.5.1 uses protocol 32. No calibration format changes.

## Changes

- Independent dual Button mode: hold Use on a jammed gun for two seconds to clear supported durability jams, with per-hand progress. Release, tracking/input loss, slot replacement or stowing interrupts the hold. The other gun retains its own state. Offhand synchronization targets the actual inventory slot.
- With dual disabled, only main-hand gun selection reserves its slot. With dual enabled, both hands reserve selected guns when selection locking is enabled.
- Fresh Grab in a holster can draw a stowed gun even if the other hand still selects it. Held guns remain protected; no inventory copies or swaps are used.
- Dual optics now use the existing full attachment/lens path. Both hands survive late rendering; status panels render after world translucency on the non-shader path.
- Hide native TaCZ center crosshair while an addon-held gun owns aiming. Hit markers remain.
- Advanced revolver caught live rounds remain visible until descending to the validated catching-hand height. Refunds remain server-owned and occur once; Physical automatic recovery is unchanged.
- Local dual third-person native item suppression now covers self when a custom gun has a valid drawable pose, avoiding the extra ordinary gun model. This does not change arm geometry.

## Design work

See VISOR-GUN-BRIDGE-DESIGN.md for the proposed separate middle mod: exact per-gun provider preference, shared hand/input claims, server session validation, optional independent holsters and capability-gated mixed-provider dual wield. This is not yet a released Bridge implementation or a claim of compatibility with the other addon.

See THIRD-PERSON-POSE-DESIGN.md for the three requested observer pose modes. Menu toggles and an articulated arm solver remain pending. Local Visor mirror results do not certify multiplayer observer behavior.

## Validation

267 tests passed on Forge 1.20.1, Fabric 1.20.1 and Fabric 1.21.1; 268 on NeoForge 1.21.1. All four compiled. These are automated checks, not headset tests. New rendering and holster handoff require runtime checks. See TESTING-CURRENT.md, A6 checks. No JAR was packaged or installed during this update.

Preserved alpha.5.1 reported passes: FIX-N01, G02, DW20, DW21 (reported local scope), plus stereo HUD alignment. Water ordering and third-person observations remain separate checks.
