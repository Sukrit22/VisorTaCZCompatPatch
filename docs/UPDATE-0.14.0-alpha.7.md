# Alpha.7: alpha.6 feedback follow-up

Source candidate, protocol 33 unchanged. No JAR packaging or installation requested in this update. Headset checks remain pending.

## Behavior changes

- Dual native charge support for Rhino/Taurus revolvers, separate per-hand charge clocks and cancellation.
- Both dual hands use the same server-owned native automatic Button bolt cycle; suppress competing selected-main native bolt calls.
- Native inaccuracy lookup resolves the active dual operator, so ADS is not only a HUD label. Passive free-hand support hides that hand's world cursor without taking its buttons.
- Shotgun reload audio reads segmented intro/loop/end animation clips, including alternate SPAS and two-shell loops.
- Unmagnified optics preserve barrel/muzzle geometry through the lens; magnifying optics retain the captured scene. A sight-only attachment's desktop ADS camera zoom (e.g. 1.5) does not automatically become optical zoom.
- Optional config/visor_tacz-optics.toml supports explicit per-attachment optical power. Empty defaults, automatic refresh approximately once per second. Example below; exact advertised2x pistol optic is still unidentified.
- M700 on-gun HUD moves 8cm farther right; wrist position unchanged.
- Cylinder close retains the completed8cm stroke through endpoint jitter and accepts clear slowing/reversal. Context grab uses2cm left open /8cm right close. Existing debug/calibration guides show orange distance line and endpoint.
- Advanced M700 allows continuing the existing bolt Grab back to the handle after the bolt is closed. Requires entering the existing tight handle zone; no remote regrip. Physical's explicit Use transfer remains intact.
- Single Physical/Advanced magazine pistols: while holding the slide fully rearward, main Use locks it back. No ammo feed/refund occurs until later release. Dual physical slide locking awaits per-gun physical sessions; the future binding is Use on the holding hand.
- Four Holsters defaults for manual bindings: chest2, right5, back6, left7. Existing saved bindings are preserved; use the menu preset button to apply this mapping.

```toml
[magnification]
"pack:attachment_id" = 2.0
```

Use real attachment IDs, not gun IDs or translated names. Values1..16;1 forces unmagnified. A hybrid optic's secondary backup aperture remains1x. No guesses preseeded for the unidentified pistol attachment.

## Test record

Preserved the reported alpha.6 passes in TESTING-CURRENT.md. DW22 had a stale retest label; corrected. DW24/dual ADS remains a focused retest. AH59 had no supplied result and remains pending. A6-POSE awaits multiplayer observer confirmation.

AH05 means catch with Grab while Trigger is already held; Trigger is not the catch button. AH58 tests empty locked-slide Use order (remove magazine, then release slide without spawning ammunition). H4-06 now separates calibration persistence, Cancel, assignment while held, and real-item deletion tests.

## Cylinder source and handedness

physical/CylinderMotion.java handles held-gun flick recognition; server/ServerCylinder.java projects fresh controller poses onto a frozen head-right axis and applies the action. physical/CylinderCycle.java handles contextual cylinder strokes. The debug guide uses real metre distances, not visual gun scale.

Current single-gun offhand carry keeps the same gesture directions and does not mirror the mechanical model. Independent dual physical cylinder manipulation is not implemented. The cylinder's physical hinge/swing should remain gun-defined when it is implemented; hand-specific input mapping must not mirror the firearm mesh.

## Before Physical/Advanced independent dual

1. Verify Button foundations: charged revolvers, ADS/inaccuracy, per-hand cycle and segmented reload sounds; retain earlier passing hotbar/jam/HUD tests.
2. Convert physical sessions from player/main-slot ownership to validated gun-slot sessions. Every action, calibration, magazine reservation, jam and network update must address the actual gun.
3. Add hand claims: held gun, support, action part and magazine. A holstered selection leaves that physical hand free. Acquiring a slide cannot also acquire the other gun or Visor hotbar.
4. Start with one physical gun plus one stowed gun. Validate ammo conservation, deletion, disconnect, tracking loss, handoff and returning to flat. Then permit two held guns; stow one to free a manipulating hand unless a deliberate one-hand action exists.
5. Add Advanced toss/catch/holster transfer only after Physical sessions are isolated. Track each prop and its originating real slot; never restore inventory copies.

Do not enable both old singleton Physical and independent dual simultaneously. The Bridge contract can supply shared hand/gun ownership, but each native backend still needs this per-gun isolation. No need to finish arm IK before beginning that work; multiplayer pose verification can proceed independently.

## Automated validation (2026-10-18)

All four targets compiled. Forge 1.20.1, Fabric 1.20.1 and Fabric 1.21.1 each passed 285 tests; NeoForge1.21.1 passed 286. Optic shader checks compiled/linked the actual shader and compared 1x preservation versus 2x scene sampling in OpenGL. These checks do not certify headset appearance, reload-audio timing or multiplayer behavior.

Forge1.20.1 development-server smoke reached ready with a gun-equipped zombie. JVM inspection confirmed InaccuracyType and LivingEntityBolt loaded with the changed mixins; no mixin failure. This used the default Visor0.5 development dependency, not a headset client or mixed-loader multiplayer session.
