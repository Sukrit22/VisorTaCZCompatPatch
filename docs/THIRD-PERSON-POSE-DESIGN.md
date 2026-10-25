# Third-person weapon and arm pose options

Source audit, 2026-10-17. This is a design note, not a claim of headset or multiplayer validation. Visor evidence below comes from the local `sources/Visor` checkout; an installed Visor build may differ.

## Why the gun and arm separate

The addon and Visor calculate different transforms. `GunPose.resolve()` and `DualPose.resolve()` use controller **position and rotation**, calibration, and (for single guns) support-hand alignment. `RemoteGuns.render()` and `DualRemote.render()` draw the gun directly in world space after entities, independently of the avatar skeleton. `RemoteGunMixin` suppresses the corresponding ordinary held-item model.

The inspected Visor third-person models retain fixed shoulder pivots and rigid 12-pixel arm cubes. `VRPlayerModelFull.applyYawPitchToArm()` and the simple model rotate those cubes using `ArmPoseClamp.solveArmFrame()`. That solver reads controller **direction**, clamps yaw/pitch, and does not solve shoulder-to-controller position. Arm roll is initially zero; vanilla attack swing can subsequently modify the arm. Consequently a real controller may be closer to, farther from, or sideways from the cube's hand endpoint. Matching gun rotation alone cannot close that positional gap.

Visor's ordinary held-item layer also adds controller roll independently through `ItemInHandLayerMixin.visor$applyItemHandPose()`. Allowing the ordinary item path therefore needs careful definition of whether “follows native hand” means matching the arm endpoint or inheriting the full ordinary item transform, including its roll and item-specific adjustments.

## Three selectable presentation modes

| Mode | Visible result | Implementation boundary |
| --- | --- | --- |
| Independent weapon / native arms | Current controller-positioned gun with Visor's existing avatar arms. Accurate controller placement can expose a gap. | Keep world-space gun rendering and native arms. Safe baseline; no skeleton change. |
| Weapon follows native hand | Attach the third-person visual gun to the rendered avatar hand endpoint. First-person gun, muzzle authority, and controller interaction remain controller-based. | Add a player-model/held-item-layer render path for supported guns, with the profile grip/model transform. Suppress the independent third-person draw only when that path will draw successfully. Reconcile Visor's roll adjustment explicitly. Ordinary inventory items cannot represent holstered or extra-slot guns by themselves. |
| Arms reach controller / gun, independent wrist | Hand reaches the controller or calibrated grip target; wrist rotation follows the weapon without forcing the entire arm to share wrist orientation. | Requires a positional arm solver and an articulated upper-arm/forearm/hand representation, or a documented visual approximation. Define elbow pole, reach limits, body scale, skin/sleeve/armor behavior, and interpolation. Existing rigid arm cubes and yaw/pitch clamps cannot provide an independent wrist joint. |

Mode two is a feasible contained rendering feature after defining grip offsets, handedness, and holstered-gun behavior. Mode three needs an IK/model design and validation across Visor body types; rotating a whole cube toward controller aim is insufficient. A straight shoulder-to-target cube that stretches or floats can be prototyped, but should not be represented as articulated arms with independent wrists.

## Local view versus multiplayer

`DualRemote.state()` explicitly rejects the local UUID; `RemoteGuns.state()` rejects `p == mc.player`. These renderers serve other players and cannot repair the local mirror by themselves. Local hand effects (`GunRenderer`, `DualGunRenderer`) and Visor's `VRRenderPass.THIRD_PERSON` / self-model visibility branches must be inspected separately. In particular, Visor full-model `setupAnim()` treats self-model rendering differently from ordinary third-person animation, and `PlayerItemInHandLayerMixin` can cancel self-model held items based on hand state.

Single-gun remote rendering resolves the remote Visor `RENDER` pose plus addon calibration/handling state. Dual rendering consumes server-supplied `DualNetwork.Visual` gun poses. Both expire snapshots after 2.5 seconds; the single route additionally requires a tracked `VRClientPlayer`. These are different synchronization paths. A pose mode must use the same per-player mode decision for replacement suppression and actual drawing, and test both local mirrors and a second client. A viewer-only visual preference can stay client-side; an owner-selected appearance that all viewers must agree on needs synchronized selection.

Safe immediate work is fixing duplicate or missing local/remote render ownership and preserving the current independent presentation. Choosing the native-hand mode must not silently move authoritative bullets, reload zones, controller calibration, or first-person aim. It also must not enable vanilla held-item rendering for every gun indiscriminately.

## Evidence paths

Addon:

- `src/main/java/dev/visorcompat/tacz/GunPose.java`: `resolve()`.
- `src/main/java/dev/visorcompat/tacz/DualPose.java`: `resolve()`.
- `src/main/java/dev/visorcompat/tacz/client/RemoteGuns.java`: `state()`, `replaces()`, `render()`.
- `src/main/java/dev/visorcompat/tacz/client/DualRemote.java`: `state()`, `replaces()`, `render()`.
- `src/main/java/dev/visorcompat/tacz/mixin/RemoteGunMixin.java`: held-item replacement suppression.
- `src/main/java/dev/visorcompat/tacz/client/GunRenderer.java` and `DualGunRenderer.java`: local hand-effect transforms.

Visor base: `E:/visor-compat/visor-0.5.0-questcraft/sources/Visor/`

- `visor-core/src/main/java/org/vmstudio/visor/core/client/render/player/model/full/VRPlayerModelFull.java`: `setupAnim()`, `animateThirdPersonVRModel()`, `applyYawPitchToArm()`.
- `visor-core/src/main/java/org/vmstudio/visor/core/client/render/player/model/simple/VRPlayerModelSimple.java`: arm animation and `translateToHand()`.
- `visor-core/src/main/java/org/vmstudio/visor/core/client/render/player/model/ArmPoseClamp.java`: `solveArmFrame()` and yaw/pitch limits.
- `visor-core/src/main/java/org/vmstudio/visor/core/client/render/player/model/CenteredArmsPlayerMesh.java`: rigid arm geometry and shoulder pivots.
- `visor-core/src/main/java/org/vmstudio/visor/core/client/render/player/VRPlayerRendererFull.java`: self-model visibility and spectated-hand paths.
- `visor-core/src/main/java/org/vmstudio/visor/mixin/client/renderer/entity/player/layers/ItemInHandLayerMixin.java`: controller roll and item-hand pose.
- `visor-core/src/main/java/org/vmstudio/visor/mixin/client/renderer/entity/player/layers/PlayerItemInHandLayerMixin.java`: self-model and spectated item suppression.
- `visor-api/src/main/java/org/vmstudio/visor/api/client/render/VRRenderPass.java`: explicit third-person pass.

## Alpha.6 limited local correction

`DualRemote.replaces` now also checks local active dual bindings in third-person item contexts. When the local custom gun has a valid drawable pose, it suppresses the ordinary native held-item duplicate. This does not feed self through the remote snapshot renderer or add an arm solver. Verify it in Visor's observer pass; it does not certify offhand avatar rendering or multiplayer pose alignment.
