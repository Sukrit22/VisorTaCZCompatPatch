# Gun packs and physical profiles without recompiling

## Implemented in 0.11.0

Basic geometry and physical capability are now separate. All 54 pinned default-pack guns can use button controls; 36 new basic profiles come from bundled `button-profiles.json`. The physical engine only runs for explicitly physical-capable profiles. Client controls, server reload rules, remote state and calibration pages respect that distinction.

The bundled JSON is currently compiled into the JAR. It is NOT yet an external-file loader or automatic pack discovery feature. Unknown gun IDs and alternate displays remain unregistered. This document specifies the next architecture; the example below is not a working config file yet.

## Physical handling without recompiling

Move profile identity and capability out of the per-gun enum. Keep tested mechanism implementations in Java, but load their parameters from versioned JSON descriptors:

- Gun ID + display ID (different skins/models may require different pivots).
- Geometry: grip/muzzle/sight/ejection transforms and model scale.
- Mechanism template: buttons, magazine-slide, magazine-charging-handle, pump, lifted bolt, or aggregate cylinder/breech.
- Named moving parts, parent/child relationships, motion axes/travel/angles, magazine and cartridge nodes, sound resources.
- Enabled interactions and their positions/box dimensions; optional selector, release catch, support transfer and jams.
- Calibration hints and defaults, preserving user overrides as a separate layer.

Illustrative descriptor (not accepted by this release):

```json
{
  "schema": 1,
  "gun": "example:service_pistol",
  "display": "tacz:default",
  "mechanism": "magazine_slide",
  "parts": {"slide": "upper", "magazine": "magazine"},
  "capabilities": {"support": "touch", "slide_release": true},
  "geometry": {"source": "pack_nodes"}
}
```

The finished schema must require explicit geometry where node inference fails, validate every field and reject incompatible combinations. It cannot infer a double-feed procedure from a model name.

Proposed sources: bundled defaults, a pack-provided VR descriptor, then administrator overrides. Personal calibration remains separate. Load candidate profiles atomically: validate all references before activating them; retain the old set if reload fails. A reload command/menu should cancel held previews safely, settle existing reservations, replace the set at a safe tick and resync clients. Never replace a live mechanism halfway through an uncommitted ammo transaction.

Server chooses gameplay capability and ammo transitions. Send a version/hash and required profile geometry to clients/observers; mismatches fall back to buttons only when both sides agree on basic geometry. Dedicated servers must not load client model/render classes. User files must not execute arbitrary Java/Lua through this profile system.

This enables adding another gun with an EXISTING mechanism without recompilation. A genuinely new action sequence still requires an engine change. Automatic detection must never select a physical mechanism just because a gun's pack category says pistol or SMG.

## Automatic button registration for gun packs

Use TaCZ's loaded registry rather than hard-coded pack names or scanning arbitrary ZIPs during play. The pinned API exposes `TimelessAPI.getAllClientGunIndex()`, `getAllCommonGunIndex()`, and actual display resolution through `getGunDisplay()`. Bedrock models expose third-person hand and muzzle paths. These are useful inputs, not guarantees every pack supplies every marker.

Suggested pipeline:

1. After TaCZ pack/resource load completes, enumerate gun IDs and resolve their actual display/model.
2. Derive bind-pose grip and muzzle transforms from the full node hierarchy and display scale. Do not use animated frame transforms or the LOD model. Include rotations/scales from ancestors.
3. Missing grip/muzzle markers produce a clearly marked needs-calibration entry; do not silently call the guessed position correct. Keep administrator geometry overrides available.
4. Register basic button capability only. Retain TaCZ's own ammo, capacities, timing, fire modes and reload scripts.
5. Validate matching gun IDs and bounded geometry on the server; provide a server-owned descriptor or an explicitly governed client-derived geometry exchange. The current server has common gun data, not necessarily client display assets. Never trust client-supplied damage, ammo or action-state transitions.
6. Key caches by gun AND display, invalidate after pack reload, synchronize observers, and handle missing/removed packs without stale rendering state.
7. Test ordinary magazines, manual-bolt scripts, charge/spin-up weapons, launchers, built-in optics, attachment variants and custom projectile APIs. API discovery is not proof of gameplay compatibility.

The recommended next step is basic pack discovery plus explicit geometry overrides, then external physical descriptors. This keeps new pack support useful while avoiding accidental physical reload behavior on unknown mechanisms.

## Immersive Armorer example

Its [CurseForge page](https://www.curseforge.com/minecraft/customization/tacz-immersive-armorer-koei) describes a TaCZ gun pack with ten guns, installed by placing its ZIP in the instance's `tacz` folder. That makes TaCZ's loaded gun registry the appropriate discovery point. The page currently lists a 1.20.1 release named `Immersive Armorer-v1.2.1-for117.zip`.

The page was inspected; this pack's ZIP/models/scripts were not installed or validated in the current workspace. 0.11.0 does NOT automatically support this pack. Its custom mechanisms and renderer/script assumptions need inspection and tests once discovery is implemented; the filename alone does not prove compatibility with our pinned TaCZ version. Do not bundle the pack's models/textures into this addon to achieve registration.
