# Small VR gun ownership API: discussion draft

This is a proposed agreement between addon authors, not an existing Visor API or an implementation already available in this addon. It does not prescribe either developer's reload engine.

## Goal

For any gun action, exactly one participating addon decides what happens. For any controller input, one accepted handler receives the press and its matching release. When no addon accepts an action, normal Visor behavior continues.

Three things are separate:

- **Mechanics provider:** the addon responsible for firing, reloading, ADS, recoil and native gun state for this gun.
- **Hand claim:** temporary permission to use a physical controller for holding, support, manipulation, a magazine or a menu.
- **Holster assignment:** an inventory reference and body mount. A holstered gun is not actively held, even if its hotbar slot remains selected.

## 1. Provider registration and selection

Each participating addon registers a namespaced provider ID, display name, API version, supported-gun predicate and capabilities. Example IDs in this document are illustrative, not agreed identifiers.

```text
provider: visor_tacz:physical
supports: recognized TaCZ gun stacks
capabilities: physical reload, gun pose, holster rendering, independent offhand
```

The other developer registers their own provider. They can support TaCZ and Point Blank without our holster module importing either mod's classes.

Start with one mechanics provider per gun instance. Settings can choose a default per gun mod/pack with exact gun-ID overrides. Do not select by translated display name. An unmatched gun retains its native behavior. If a configured provider is absent, report that and use an explicit fallback policy rather than silently activating multiple providers.

Provider selection must gate input handlers, background ticks, render hooks and server actions. Disabling only a keybind is insufficient. Each mixin still has to be safe to load alongside the other addon; runtime ownership alone cannot repair incompatible injection targets.

Our addon also needs changes to honor this contract: several handling-mode and server checks are currently player-wide, and the holster code depends directly on TaCZ and our dual client. This is not a request for the other author to do all the integration. The cheapest first milestone is a single TaCZ provider choice for a player/session and a mutual opt-out test; more granular per-gun choices can come after the underlying state supports them.

For version 1, do not split firing, reload, recoil and ADS among several providers. Those states are interdependent. Read-only HUD integration can be independent. An attachment menu can delegate edits to the chosen provider.

## 2. Identify the actual gun

A gun type such as `tacz:glock_17` identifies a model, not one specific Glock. A context needs:

```text
player
actual inventory slot/reference
replacement generation
gun type ID
chosen mechanics provider
physical controller, if held
state: stowed / held / transferring / temporarily released
handling mode and current supported interactions
```

The generation changes when the referenced item is replaced, deleted, or reassigned. Ordinary ammo/component updates do not create a new gun session. The server validates the reference again before changing ammo or inventory. A stale request must fail; it must never restore an old copied stack.

Do not require permanent custom item IDs for the first version unless the agreed design needs them. Ephemeral references and lifecycle cleanup avoid imposing unnecessary persistent data on the gun.

## 3. Claim the relevant hand and inputs

Use physical LEFT/RIGHT controllers in the contract, with an explicit mapping to Visor MAIN/OFFHAND. This prevents left-handed settings from reversing the agreement.

A claim names its owner, gun context, hand, role and consumed actions. Claiming Trigger must not automatically consume Use or Grab. A held-gun role may prevent a competing grip action while still allowing an explicitly unclaimed input to fall through.

| Input/action | Example owner | Rule |
| --- | --- | --- |
| Grab near a stowed gun | Holster service | Accept a fresh press, validate slot, draw through the provider |
| Trigger while holding a gun | Mechanics provider | Fire or perform the provider's contextual action |
| Use while holding a gun | Mechanics provider, only if applicable | Magazine release, slide release or another agreed action |
| Grab near another held gun's slide | That gun's provider | Only if this hand is available for manipulation |
| Trigger while holding a magazine | Magazine interaction owner | Do not accidentally fire a different gun |
| No accepted contextual action | Visor/native behavior | Do not swallow ordinary interaction |

The API returns a token for an accepted claim. The token lasts until completion, release, or cancellation; it is not just a boolean checked anew each frame.

- A fresh press after entering an action zone can start a grip interaction.
- Entering a zone with Grab already held does not steal the input from another mod.
- Once accepted, route the matching release to the same owner even if the hand leaves the zone.
- A claim cannot preempt another active claim without an explicit transfer/cancel agreement.
- Never reverse cancellation performed by another mod outside the agreement.
- If several zones are eligible on one fresh press, a shared resolver chooses one deterministically. Do not depend on whichever addon happens to tick first.
- Passive touch-based support aiming is a separate, explicitly advertised behavior. Reading a nearby free hand's pose must not silently seize its buttons. This preserves our optional pistol support-by-proximity behavior.

Every owned interaction needs an explicit cancel path so tracking loss or an absent release event cannot leave firing or grabbing stuck.

## 4. Selected is not held

This distinction is required for body holsters:

```text
Hotbar slot 3 contains a Glock.
Visor right-hand selection points at slot 3.
The Glock is still in its holster.
```

The hand is available for interaction. Neither provider should fire or auto-reload the Glock merely because slot 3 is selected. The holster service accepts a new Grab near the grip, validates the item and requests draw. Only then does the gun become held and its provider activate gun controls.

Both addons must check this held state. A middle mod cannot enforce it against a provider that independently reacts to the selected stack.

## 5. What the separate holster module owns

Own: body mount assignment, mount calibration, visible stowed poses, selection/draw/stow requests, slot reservations and the hand transfer surrounding a draw.

Delegate: gun classification, native model rendering, firing, magazine/chamber logic, jam clearing, ammo consumption, recoil and attachments.

The holster module stores references to real inventory items. It must work with either provider installed alone. If a provider disappears or the player deletes the gun, clear temporary ownership and visuals; do not recreate the item.

A draw should be a validated transition: prepare/select the actual item, confirm acceptance, then commit the held context. Failed or stale requests leave the inventory as it actually is. Transfer a hand claim atomically from holster interaction to the gun provider rather than briefly allowing both to control it.

## 6. Shared rendering and attachment UI

The mechanics provider supplies the canonical gun pose, muzzle pose and held/holstered status. A crosshair or optic uses that pose rather than independently choosing the last active controller.

Only one provider should apply a gun's scope rendering, recoil transform or aim animation. Camera-wide effects such as FOV also need a policy when two guns are held; per-hand input ownership alone does not resolve global rendering state.

An external attachment menu asks the mechanics owner for supported attachment operations and submits an edit for the validated gun context. It must not assume the target is still the current main-hand item after a hotbar switch. The server rechecks item identity, ownership and attachment rules before applying the edit.

An empty-trigger auto-reload feature needs permission from the mechanics owner. In physical handling, an empty chamber may require a bolt action or jam clearing rather than a fresh magazine reload.

## 7. Lifecycle and server responsibilities

Cancel claims and stop sustained actions on VR exit, tracking loss, disconnect, death, world change, item deletion/replacement or provider disablement. Provider switching occurs at a safe idle/stowed boundary, not halfway through firing or reload.

Cancellation stops transient actions; it does not automatically reset ammo, refund cartridges or undo persistent weapon state. That policy belongs to the mechanics provider and its server logic.

The client requests actions. The server validates gun reference, allowed state, cooldown, provider permission and ammo. A client-supplied ownership token is not proof that the action is valid. Both developers should agree which parts are client-only and which need matching server support.

## 8. Mixed-provider dual wield is a capability, not a default promise

Example:

1. Right hand holds our TaCZ pistol; left hand holds the other provider's gun.
2. Each trigger routes to its own provider.
3. The left hand is occupied, so it cannot also grab the TaCZ slide.
4. Stow the left gun; the left hand becomes available.
5. A fresh left Grab on the TaCZ slide grants a manipulation claim to our provider.
6. Releasing ends manipulation; ordinary left-hand behavior resumes.

This requires both native backends to support independent offhand operation. Some gun mods use shared player-wide reload/shoot state. Register that capability only when implemented and tested. Until then, make unsupported combinations explicit rather than claiming compatibility based only on separate controller callbacks.

## Minimal conceptual API

These names are discussion pseudocode, not a final Java ABI:

```text
registerProvider(provider)
resolveProvider(gunReference)
getGunContext(gunReference)
getHandContext(physicalHand)
tryClaim(request) -> token or denied
release(token, reason)

provider callbacks:
  onInput(context, inputEvent)
  onDraw / onStow / onCancel(context, reason)
  getGunPose / renderHolstered(context)
```

The runtime routes accepted input to the token owner. Providers honor the resolver and expose native capabilities. Begin with this small contract; add specialized attachment and optic interfaces only when both authors need them.

## First compatibility tests to agree on

1. Both addons installed, our provider selected for a TaCZ gun: one shot, one ammo deduction, one recoil and one reload action.
2. Other provider selected: ours leaves its input/rendering/native actions alone.
3. Selected gun stays holstered: Use and Trigger retain normal non-gun behavior where applicable.
4. Hold Grab elsewhere, then move into a holster/pouch/slide zone: no takeover.
5. Start a valid grab, move out of the zone, then release: no stuck ownership.
6. Delete or inventory-drop a held/stowed gun: no restoration and no duplicate model/item.
7. Leave VR or lose tracking while firing/manipulating: sustained actions stop; inventory and ammo remain valid.
8. Switch left-handed setting: physical controller routing stays correct.
9. Enable mixed-provider dual wield only after both backends pass independent firing/reload and cross-hand exclusion tests.

## Message to send

“Could we agree on a small per-gun/per-hand ownership contract first? Each gun would have one chosen mechanics provider. Both mods would gate their input, rendering, ticks and server actions on that selection. The shared holster module would own mount/slot references and draw/stow, while your or my provider handles native gun mechanics. Selected-but-holstered guns must be inactive. Contextual grabbing would use fresh press claims, with release/cancel returned to the same owner. We can initially test one TaCZ gun with both addons installed; mixed-mod dual wield can follow only when both backends support independent offhand state. Does that fit your architecture, and can your TaCZ hooks yield when another provider owns the gun?”
