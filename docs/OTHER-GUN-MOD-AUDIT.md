# Visor Gun Support: local JAR compatibility review

Reviewed 2026-10-17. Static inspection only; the two addons have not been run together. Findings distinguish definite overlapping hooks from predicted gameplay effects. Decompiled code is local under ignored `build/other-gun-review/source`; method names below are evidence pointers, not original source line numbers.

## Inspected artifact

- User file: `other-gun-mods/vic point blank x tacz 1.21.1 beta.jar`
- SHA256: `2fb7eaa78c6298b2add73e1a377db387bd903f2bb08845d019bcd699fc7873c5`
- Mod ID: `visor_gun_support`; internal version: `1.0.0`.
- Loader: Fabric; Minecraft exactly 1.21.1; Java >=21; Fabric Loader >=0.19.5.
- Metadata names it “Vic's Point Blank and TACZ Support for Visor”. No NeoForge entrypoint/metadata in this file.

Our Fabric target permits Loader >=0.17.2, so a shared test instance must satisfy this JAR's higher >=0.19.5 minimum. Do not load this particular artifact into the NeoForge target.

## Finding 1: independent TaCZ trigger handling bypasses a shared ownership decision

`VRTriggerControls.shouldHandle` checks player/world/active VR, rather than this addon's ownership of a particular gun. `VRHandControls.captureTriggerAction` captures Visor trigger-mouse actions when gun interactions are allowed. `VisorGunSpinActionMixin` injects at `VRActionButton.press` HEAD and cancels accepted actions. `VisorControllerMiningMixin` separately wraps Minecraft `handleKeybinds` and runs controller processing.

`VRHandControls.Hand.update` reads the selected main/offhand stack, resets native TaCZ pressed state for MAIN, and calls `TaczControllerFiring.setPressed(true)` when an eligible main-hand TaCZ trigger is held. That bridge invokes TaCZ `ShootKey.shootControllerTick` directly. It is not governed by our input cancellation or server dual-hand context.

Expected risk: competing native firing state, swallowed contextual inputs, or inconsistent press/release behavior when both addons act. Duplicate shots are a possibility to test, not an observed result or guaranteed consequence.

Required contract: gate direct trigger sampling/native firing by mechanics owner and actual held state; coordinate trigger/Use/Grab claims and their release. Do not rely only on cancelling one Visor event.

## Finding 2: holstered-selected guns are still classified as guns

`VisorInputMode.forStack` identifies TaCZ by its `IGun` interface. `VRHandControls.Hand.update`, `EmptyMagazineReload.manualHand`, and crosshair eligibility use the selected main/offhand stacks. No integration with our holster state or provider registry was found in the inspected classes; a whole-decompiled-tree search found no `visor_tacz` or holster reference.

Expected risk: a gun we consider holstered can still participate in firing/reload/crosshair routing, and the hand may not fall through to ordinary interaction.

Required contract: distinguish selected from held. Both addons must consult the same draw/stow state before activating gun behavior.

## Finding 3: two systems alter TaCZ bullet direction

Their `TaczShotRayMixin` wraps `EntityKineticBullet.shootFromRotation` with Vector2d spread. It samples `VisorShotAim.sample(player, false)`—explicitly MAIN—and replaces pitch/yaw using controller convergence plus incoming head-relative offsets. The common mixin is applicable beyond client-only rendering.

Our `BulletSpreadMixin` changes spread pitch/yaw from the validated gun pose; `BulletMixin` establishes muzzle origin and routes recoil/dual contexts. Thus both can alter the same shot pipeline. Main-hand sampling in the other addon cannot represent our independent offhand TaCZ shot correctly without an ownership/context bridge.

Expected risk: re-aimed bullets, disagreement with calibrated/support-hand/recoil poses, especially offhand firing. Exact composition depends on the runtime call/mixin order and needs a joint test.

Required contract: one authoritative shot-pose provider for each gun operation. The non-owner must yield on the common/server path too, not just disable its crosshair.

## Finding 4: native reload can compete with physical reload

`EmptyMagazineReload.onHandTriggerPull` invokes TaCZ empty-trigger reload for MAIN. Its native TaCZ adapter checks ammo/chamber and native eligibility; it does not consult our magazine-out, manual bolt, jam or holstered state. `VisorReloadControls` adds manual controller reload handling.

Expected risk: native button reload begins while our physical state machine expects a magazine/slide/pump action.

Required contract: automatic and manual reload requests go to, or obtain permission from, the mechanics owner. Keep both native and addon state validation; do not merely remove the native checks.

## Finding 5: render and crosshair policies overlap

`VisorTaczCrosshair` registers a separate `WorldRenderEvents.LAST` renderer and checks eye passes. `VisorTaczCrosshairMixin` cancels Visor's default hand reticle rendering. The separate world reticle is not automatically disabled by our own `HandEffectCrosshair` visibility policy.

`VisorHeldItemRendererMixin` wraps held-item rendering, applies `VisorGunSpin`, and changes a TaCZ display-context path. `VisorGunPose` and `GunPoseProfiles` provide additional pose/calibration behavior.

Expected risk: unwanted reticles, conflicting pose transforms and visual/shot disagreement. No flashing, black optics or doubled model has been reproduced with the combined mods.

Required contract: the chosen gun provider supplies the canonical gun/muzzle pose and decides which render/crosshair capabilities are delegated. Do not let both independently transform the same gun.

## Existing work worth coordinating around

- `ButtonPressLease` tracks action identity and USE/SPIN/RELOAD ownership until release. It is an internal helper, not yet a cross-addon arbitration API.
- `HandTriggerState` and `HeldGunIdentity` handle held-input/weapon identity transitions.
- `VRTriggerControls.cancel` and menu hooks clean up input on lifecycle transitions.
- Point Blank has explicit hand-aware native bridges. This does not prove mixed-provider dual wield, but is a useful foundation.
- TaCZ OFFHAND firing is deliberately rejected with a warning; manual offhand TaCZ reload is also reported unsupported. Our independent TaCZ offhand operator is therefore complementary work, not an interchangeable native API this addon already uses.
- Attachment integration exists. `GunAttachmentBridge.openCustomization` resolves the current main-hand gun and opens native TaCZ `GunRefitScreen` or Point Blank's native attachment screen request. A shared UI for a holstered/offhand gun still needs an explicit validated gun reference.

## Recommended first agreement

1. Initially choose one TaCZ mechanics provider per player/session. Both authors implement an explicit opt-out for the other's ownership.
2. Cover direct tick polling, native firing/reload, common shot-direction mixins, pose and world-reticle rendering—not only keybindings.
3. Share selected-versus-held and hand-availability queries before enabling body holsters with both addons.
4. Generalize the existing press lease into an agreed cross-addon contract after a simple single-gun test works.
5. Keep Point Blank support available, but audit generic Visor trigger/mining/face-button hooks too. Disabling only TaCZ-specific mixins will not isolate all input behavior.
6. Add mixed-provider dual wield and offhand attachment editing as later capability-gated tests.

Our addon needs ownership guards and holster/provider separation too. No JAR was modified, installed, or rebuilt during this audit, and no message was sent to the other developer.

## Suggested message

“I inspected the beta you shared. Your ButtonPressLease and hand-aware Point Blank bridges look useful for a shared contract. The first overlap to resolve is TaCZ ownership: both addons drive trigger/reload and modify shot direction, and your handlers currently identify guns from selected stacks while ours can keep a selected gun holstered. Could we add a shared owner/held-state check to the tick/input handlers, common shot-ray hook, reload and render paths? We also need generic trigger/mining handling to yield for a hand claimed by the other addon. Let's first test one TaCZ owner at a time; your Point Blank integration and our independent TaCZ offhand operator can remain separate backends.”
