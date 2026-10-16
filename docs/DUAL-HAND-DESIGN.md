# Dual-hand guns: experimental branch design

## Agreed next design — four mounts and input ownership (2026-10-14)

Design target; alpha.5 source implements the four local mounts, rule UI, calibration, fresh-press draw and symmetric slot protection. Physical hand-independent operation and all-four-mount remote sync remain pending. Not implemented by alpha.4. This section supersedes the earlier hand-assignment mount table for the next architecture. alpha.4 still uses the current two selected-hand mounts and button-based dual handling.

- Four physical mounts: right waist, left waist, chest, back. Auto scans hotbar 1→9 or 9→1: first two handguns go right/left, first two long guns chest/back. Manual mode binds each mount to one slot or disables it. Wrong gun class leaves mount empty and emits one chat notification per invalid assignment/item transition. Reject duplicate bindings. Use gun type/profile with pack overrides, not support-zone distance, to classify guns.
- Mount assignment is independent of selected/holding hand. Gun stays in its inventory slot, is hidden from mount while held, and reserves that mount until returned. Each mount needs independent calibration, with old values preserved during migration. Either hand can draw any reachable mount.
- Optional **Lock other hand's selected gun slot**, default ON: neither Visor radial may choose a TaCZ slot currently selected by the other hand, even if holstered. Lock OFF restores native selection collision behavior only for unheld guns. A physically held gun is always protected from selection theft in both directions. Non-TaCZ slots keep native Visor behavior. Do not reserve every mounted gun against both hands; the other hand's selection and actual held ownership are the relevant locks.
- **Require empty hand to draw**, default ON: empty hand or selected-but-holstered gun counts free. OFF permits replacing a selected ordinary item without dropping/moving it in inventory. A physically held gun still requires explicit holster/transfer first. Selecting a holstered gun through the radial must not itself draw it.
- Normal acquisition is a fresh Grab press while already inside an eligible zone. Entering while Grab is held must never acquire a gun, support, magazine or action. Once acquired, that owner retains the matching release even if hand leaves the zone. No transfer to a second target during the same press. Focus/tracking/menu loss cancels ownership; require release before a new press can acquire again. Respect already-consumed events where the API exposes them. There is no universal ownership registry for every third-party addon, so do not claim complete inter-mod exclusion solely from a down-state check.
- The existing toss-catch grace is an explicit exception to fresh-inside-zone acquisition. For strict interoperability, disable it by default in this new mode; an optional catch-assist setting may arm only for this addon's tossed gun after an eligible fresh unclaimed press. Never use its grace to draw holstered guns or grab reload parts.
- Drawing one physically supported gun should automatically enable its physical actions, from either hand, even when other guns are mounted. This is effective per-weapon capability/state, not a rewrite of the saved global Button/Physical/Advanced preference. Explicit Button-only remains available; unsupported physical profiles use clearly indicated button fallback.
- Drawing a second gun must not reset/recreate the first gun's chamber, magazine, reload, jam, action or input owner. Holstering one frees that hand to operate the other. Do not switch all weapons into button mode when two guns are held, or auto-complete a physical reload. Until per-weapon physical logic is available, keep that configuration explicitly experimental/unavailable rather than pretending a mode switch makes it safe.
- Server actions identify inventory slot + validated weapon identity/generation + acting hand; stale callbacks/packets cannot act on replacements. World exit/flat transition cancel ownership and preserve actual inventory, using existing authoritative ammo recovery. Never swap inventory stacks to fake native main-hand APIs.

Implementation order: symmetric held/selected-slot protection and input press ownership; four-mount resolver/render/calibration/draw binding; single drawn weapon physical handling from either hand using per-weapon state; then two-held physical actions. No headset pass is implied by this design.

Required focused checks: both directions of selected-slot lock ON/OFF; non-TaCZ native collision; held gun always protected; hold Grab outside then enter each zone (no pickup); release/repress inside (one pickup); occupied external interaction cannot be stolen merely by crossing a zone; one-hand draw with three mounted guns; second draw/holster during magazine removal, open bolt and jam; exact ammo totals and no stale input on flat/focus/reconnect.


Checkpoint before implementation: `5331c0d` on `codex/dual-hand-holsters`.
Implementation branch: `codex/independent-dual-wield`.
Status: `0.14.0-alpha.3` experimental independent native shooting/button reload implementation. Headset validation is pending. The design below includes future work, not a claim that all gates are finished.

## Try this prototype

Enable **Independent dual wield** in the Advanced Handling menu, then assign different gun slots to Visor's main hand and offhand. This opt-in experiment overrides single-gun handling while both assignments contain guns. Disable it to return to the existing handling modes.

- Grip near a gun's holster handle to draw it; keep Grip held. Releasing Grip immediately holsters that gun.
- Trigger fires that hand's gun. Release Trigger before shooting after drawing or resuming focus.
- Use starts that gun's native button reload; for a loaded manual-action gun needing its bolt, Use cycles the bolt instead.
- Pistols have opposite waist mounts. The main long gun uses the chest and offhand long gun uses a preliminary back/shoulder mount. Both long guns can remain drawn (alpha.3).
- With a gun holstered, that controller's Trigger/Use is returned to Visor/world interaction.
- Items remain in their actual hotbar slots. Guns of the same ID in different slots have separate native operations, ammo, recoil, reload timers and delayed burst context.

**Not yet implemented in dual mode:** physical magazine/slide/cylinder handling, two-hand support aiming, independent ADS/zoom optics, charged-trigger guns, toss/catch or hand transfer, visible holster/sling meshes, and complete per-hand native model animations. The current held models use calibrated transforms with recoil; gunpack-specific scripts and remote observer behavior require testing. Charging is explicitly blocked rather than bypassing the native charge requirement.

Client and server must both use the new build (network protocol 30). No changes to installed instances are made by building it.

See [the dual-wield test checklist](TESTING-DUAL-WIELD.md).

## User-facing behavior

Each Visor hand assignment may identify a different inventory-backed gun. Assignment, current gripping hand, and holster mount are separate concepts. Two equal gun IDs in two slots must remain distinct weapons. No duplicated inventory items.

| Assignment | Pistol | Long gun |
|---|---|---|
| Main | Dominant-side waist | Center chest |
| Offhand | Opposite-side waist | Back, handle reachable over dominant-side shoulder |

Left-handed mode mirrors the dominant side. Existing main-gun calibration stays the fallback. Mount overrides are stored separately in visor_tacz-dual-holsters.json so two identical pistols can have different holster locations. Both mounts share the existing body-heading and crouch-follow rules; they must not independently drift/recenter.

A holstered gun does not occupy its owner's controller. Drawing it does. An occupied hand cannot simultaneously support, rack, reload, or grab the other gun. A handoff changes the current holder explicitly; merely reaching across must not silently swap inventory slots or holster ownership. Preserve Button and Physical behavior; first introduce this as an Advanced experiment.

Provisional long-gun rule: two rifles can be equipped/stowed, but both may now be drawn at once (alpha.3, per revised user request). Separate dual-pistol firing is the eventual target.

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

## Deferred visual request — 2026-10-14

User/player request: visible 3D muzzle flash in VR. Current DualGunRenderer explicitly suppresses the native self muzzle flash while rendering gun models; this is not an implemented dual muzzle-flash effect. Design a per-weapon shot-driven effect at the calibrated muzzle, correct in both eyes and at gun scale, with independent simultaneous flashes, gunpack/suppressor compatibility and no extra ammo/shots. Check nearby geometry and remote observers. Deferred until hand/weapon lifecycle work is stable; not a claimed fix in alpha.5.
