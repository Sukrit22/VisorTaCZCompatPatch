# 0.12.0-alpha.1 — Advanced Handling experiment

Branch: `codex/data-driven-gun-profiles`. Protocol **20**: use this build on both clients and server. No instance files are installed automatically. Advanced Handling is OFF by default. Existing calibration files and bundled numeric offsets are unchanged.

This is a first playable ownership experiment, not the completed dual-hand inventory architecture. The profile TOML work remains an offline prototype; Glock has not yet been migrated into a live external profile.

## Setup and controls

1. Open `/visor_tacz menu` → **Advanced Handling** → turn ON.
2. Enable **Physical** in the parent menu to use physical parts. Button-only guns retain their native button reload.
3. Select a supported gun through Visor. It appears holstered: pistol at the main-hand waist, long gun at the central chest. Holsters follow body yaw and character height. Left-handed Visor mirrors the pistol side.
4. Press and hold the normal Visor hotbar/Grip action close to the handle (18 cm grab radius) to draw. An unrelated Grip press passes to Visor. A consumed Grip stays consumed through its release.
5. Keep Grip held while shooting. Trigger fires; Use removes a magazine, or releases a locked action after insertion of a non-empty replacement magazine. This reuses the existing ammunition reservation/reload system. It does not spawn an inventory magazine. Manual-action guns retain their physical action controls instead of a universal Use action.
6. Offhand Grip operates supported parts/pouch. Offhand Trigger can press a detected release or selector region. Outside these regions normal Visor input is retained. The old Use/Trigger grab preference applies when Advanced Handling is OFF.
7. Return to the holster and release Grip to stow. Releasing elsewhere follows the menu release policy. **Auto-holster** waits for inspection grace; **Toss/catch** allows ballistic translation and rotation before recovery. There is no destructive world-drop mode.

### Catch grace with a provisional radial

While a gun is airborne, pressing Grip just before it reaches the hand opens Visor's radial provisionally. The native hotbar task is not started yet, so hovering another slice cannot change the gun's slot during catch arbitration. Hold Grip: if the handle enters the grab zone before the deadline, the catch wins and closes the radial. If the window expires, Visor's normal hotbar task takes over for the rest of that press; a later passing gun cannot steal it. Releasing Grip early confirms the highlighted hotbar slot instead. Outside an airborne catch, normal hotbar presses are not delayed.

**Catch grace** is separate from inspection grace: OFF / 100 / 250 / 500 ms, default 250. Focus/tracking loss or a menu cancels the provisional radial. This adapter targets pinned Visor 0.5.0, including its `TaskHotBar` task; its visual behavior still needs headset testing.

Grip + Use while holding the gun is a gun interaction, not a hotbar shortcut. Holster/release first to free Grip for Visor.

## Recovery and inventory

The prop is a representation of the selected hotbar item, never a copied ItemStack. Releasing it does not remove it from inventory. Automatic recovery uses 25% of character bounding-box height above the player's feet, not headset height. Recovery also occurs on collision, after three seconds, or beyond three world-scaled metres from the player. An inspection grace of 50/100/250 ms (default 250) precedes the height/time recovery check; collision recovers immediately.

Pause/menu, loss of focus/tracking, hotbar change and VR exit abandon transient gestures. Disconnect leaves the gun in its existing slot; previous server magazine-reservation recovery still applies. Switching to flatscreen leaves the normal selected hotbar slot unchanged. No new NBT tags or inventory moves are introduced for ownership.

Remote clients receive the holster/released/temporary-held prop pose. Shots and physical manipulation are blocked on the server while not in the firing hold; the cosmetic pose packet is never used as an arbitrary bullet origin. Ordinary held shots retain the existing controller/wall validation.

## One-handed inspection

For conventional pistols with a closed, unjammed action: release Grip, then press and hold Use within the selected grace window. This enters INSPECT and tilts the gun. Trigger moves the slide a short cosmetic distance; it does **not** feed/eject a round or change ammunition. Grip returns to the firing hold even while Use remains held. Release Use before another contextual Use action can occur. Trigger must have been released before firing can resume.

This is a controller-driven visual gesture. It does not animate individual fingers, add a missing chamber-cartridge mesh, or implement inertia-operated racking.

## Explicit limits of this alpha

- Catching with a free offhand is a temporary **SUPPORT** hold. Its pistol holster side follows that catch. Re-grab with main-hand Grip to shoot or reload. Offhand-only shooting/reloading is not implemented.
- **Cross-hand hotbar-slot transfer is not implemented.** The gun stays in its original selected slot. Visor's `setActiveHand` changes interaction targeting, not inventory ownership. Correct slot transfer needs per-hand weapon identity throughout TaCZ operations, pose validation, reload sessions, and remote rendering.
- Only the selected gun is represented. Changing slots holsters/discards the old transient prop; there is no persistent multi-gun body inventory, dual wielding, or long-gun dual wield.
- No physical world item drop, grabbing by other players, physics-driven slide operation, full rigid-body collision, or collision bounce. Collision auto-holsters.
- The offhand hold currently attaches at the handle; it does not preserve the exact caught contact point. Finger IK and holster meshes are not added.
- Inspection slide movement is local-only; remote viewers receive the gun pose but not a distinct inspection-slide animation.
- Holster positions/grab radius are initial defaults, not yet exposed in calibration or external gun files.

## Headset test checklist — all pending

| ID | Test | Expected |
| --- | --- | --- |
| AH01 | Advanced OFF; repeat Glock/M4/M870 and base button-only gun controls | Previous controls, calibration, ammo counts and optics retained |
| AH02 | Enable; select Glock; Grip near/far from waist handle | Near draws, far opens Visor hotbar; selected gun does not shoot while holstered |
| AH03 | Carry, fire, return to waist, release | Holsters; Trigger cannot fire a holstered gun; next free Grip operates hotbar |
| AH04 | Release outside holster in both release policies | Auto returns after grace; toss follows motion/rotation, then returns below quarter height, on collision, distance or timeout; inventory count unchanged |
| AH05 | Toss, catch main hand while Trigger held | Catches without firing; release/repress Trigger to shoot |
| AH06 | Toss into empty offhand; return to main hand | SUPPORT hold, no offhand shots, pistol holster changes side; original slot unchanged; main re-grab restores shooting |
| AH07 | Occupied offhand; attempt catch | Does not overwrite the item or catch through it |
| AH08 | Release then hold Use at 50/100/250 ms settings; Trigger; Grip while Use held | INSPECT within window only; slide preview; no ammo changes; Use release does not eject magazine after re-grip |
| AH09 | Loaded Glock, partially used Glock, empty locked Glock | Use removes magazine safely; insert replacement; Use releases locked slide; survival ammo accounted once |
| AH10 | Offhand Grip rack/mag/pouch; Trigger at selector/release; torch elsewhere | Correct contextual actions; unrelated offhand interactions retained |
| AH11 | Long gun selected, released, caught | Central chest holster, one active gun, no dual wield; physical pump/bolt regressions checked |
| AH12 | Focus loss, controller tracking loss, disconnect, VR→flat while held/released/inspecting | Prop canceled, gun stays in original inventory slot, no stuck firing or delayed Use; flat selected item unchanged |
| AH13 | Disconnect mid magazine removal/insertion | Existing recovery preserves total live ammunition without duplication |
| AH14 | Flat friend observes draw, holster, toss, catch and shots | Prop follows state; held shots retain calibrated muzzle direction; no phantom firing from holster |
| AH15 | Left-handed mode, crouching, world scale, calibration/refit menus | Holster side/height and catch radius usable; menu calibration still shows normal controller pose |
| AH16 | Toss upward; press Grip before gun reaches hand; keep holding | Provisional radial appears; entering handle zone during catch grace catches gun and closes radial without slot change |
| AH17 | Repeat but miss; keep holding beyond catch grace | Native hotbar takes over; subsequent gun movement cannot steal the same Grip press |
| AH18 | Release provisional Grip before expiry; highlight another slot | Highlighted slot selected normally; no duplicate gun; no leftover radial |
| AH19 | Focus loss/menu/offhand becomes occupied during catch grace | Pending catch canceled safely; no late catch, phantom hotbar press or item overwrite |

Automated verification covers ownership/release/inspection latches, catch-before-deadline arbitration, cancellation and existing tests. Actual Visor/controller behavior, multiplayer visuals, and inventory recovery sequences still require the above in-game tests.

Build verification: `gradlew.bat build --console=plain` passed with **126 tests, 0 failures, 0 errors**. Output: `build/libs/visor-compat-tacz-1.20.1-0.12.0-alpha.1.jar`. No headset or live-server pass is claimed.
