# Dual-hand guns: experimental branch design

Checkpoint: `641cb3a` on `codex/data-driven-gun-profiles`.
Branch: `codex/dual-hand-holsters`.
Status: architecture review only; independent dual-gun gameplay is not implemented.

## User-facing behavior

Each Visor hand assignment may identify a different inventory-backed gun. Assignment, current gripping hand, and holster mount are separate concepts. Two equal gun IDs in two slots must remain distinct weapons. No duplicated inventory items.

| Assignment | Pistol | Long gun |
|---|---|---|
| Main | Dominant-side waist | Center chest |
| Offhand | Opposite-side waist | Back, handle reachable over dominant-side shoulder |

Left-handed mode mirrors the dominant side. Existing main-gun calibration stays the fallback. Store mount overrides separately so two identical pistols can have different holster locations. Both mounts share the existing body-heading and crouch-follow rules; they must not independently drift/recenter.

A holstered gun does not occupy its owner's controller. Drawing it does. An occupied hand cannot simultaneously support, rack, reload, or grab the other gun. A handoff changes the current holder explicitly; merely reaching across must not silently swap inventory slots or holster ownership. Preserve Button and Physical behavior; first introduce this as an Advanced experiment.

Provisional long-gun rule: two rifles can be equipped/stowed, but only one drawn at a time, preserving the earlier request against dual-wielding long guns. Holster the first before drawing the second. Separate dual-pistol firing is the eventual target.

## Why this needs more than a second model

`AdvancedClient` owns one static hold, selected slot, profile key and pose. `ServerAdvanced` and `AdvancedRemote` store one entry per player UUID. Physical, cylinder, pump and recoil state also assume the selected main-hand stack. Existing offhand carry moves that same gun; it is not a second weapon.

Visor has a separate hotbar assignment (`TaskHotBar.getSlotOffhand()`), already referenced by `HeldHotbarMixin`. Do not assume vanilla offhand inventory numbering identifies the assigned gun.

TaCZ `LivingEntityShoot` reads `ShooterDataHolder.currentGunItem` and shared shoot timestamps/reload state. Changing the inventory selection temporarily around shooting is not a safe implementation: delayed reloads/bursts and server validation can resolve another weapon.

## Implementation gates

1. Introduce weapon references (slot plus validated stack identity and assignment generation), hand occupancy and four mount identities. Reject duplicate slot ownership and stale commands. Profile ID is not weapon identity.
2. Add two inventory-backed props and holsters, server validation and remote rendering. Keep firing on the existing path until isolated native gun contexts exist. Never present this stage as working dual wield.
3. Move physical handling, cylinder/pump, recoil and input ownership to per-weapon contexts. Packets carry weapon reference, acting hand and fresh sequence. Keep distance, tracking freshness, collision and ammo checks.
4. Integrate independent native shooting/reload state on client and server; handle native burst callbacks and animations. Verify matching Forge/Fabric APIs. Avoid inventory swapping as an adapter.
5. Add simple cuboid pistol holster and strip sling meshes. Render them from the same mount transforms used for grabbing; optional visibility and calibration. A textured bespoke asset can replace the prototype later.

## Required tests

- Two identical pistols in different slots never share ammo, recoil, jam or reload state.
- Simultaneous triggers and native burst callbacks address the correct weapon.
- Slot replacement, hotbar reassignment and old network messages cannot operate a replacement stack.
- Free hand can interact with doors, support/reload the other gun and draw its own holstered gun.
- Occupied hand cannot manipulate a second gun; no duplicate slot assignment.
- Handedness, crouching and recentering preserve mount relationships.
- Disconnect, death, focus loss and switching to flatscreen clear held input; inventory items remain in their actual slots.
- Remote observers see both guns and shot origins correctly.

## External model evaluation

TokenMe connectivity and model listing succeeded. Exact available Qwen IDs: `qwen/qwen3.8-27b` and `qwen/qwen3.8-27b-nvfp4`. Used the former for a bounded review of three source files. No credential values were printed or committed; `.env` remains ignored.

It correctly identified UUID-only state, main-hand validation and singleton remote rendering. It incorrectly treated inventory slot 45 as a universal offhand reference, conflating menu slot numbering with the Visor assignment model. The response hit the requested output budget and was incomplete. This is one small review, not a general benchmark: use it for scoped drafts/reviews with source verification, not unreviewed ownership or ammunition changes. Sol independently identified the Visor hotbar assignment and the broader per-weapon state boundary.
