# 0.12.0-alpha.5 — inspection, magazines and support transfer

Minecraft 1.20.1 / Forge / Visor 0.5.0 / TaCZ 1.1.8-hotfix. Protocol **21**: update all clients and the server together. Existing calibration files remain valid; no reset or new world is required.

## Changes and controls

- **Pistol inspection:** release Grip, press and hold main Use within the inspection grace period, then Trigger to pull the slide partially. The gun now pivots about a slide contact point with the controller fingers pointing upward relative to the pistol. This changes the gun/hand alignment; it does not animate individual Visor fingers. Release Use before its normal action becomes available again.
- **Magazine release:** with Advanced Handling and Physical Handling enabled, main Use drops a cosmetic magazine when it performs magazine removal. A stationary hand lets it fall; flicking adds translation and angular momentum at the magazine's location. Slide-release Use does not spawn a magazine. The original ammo reservation/recovery rules remain unchanged.
- **Cheap magazine simulation:** at most 16 props, 1.5-second lifetime, analytic gravity and rotation, one server-approved spawn message. No item entities or ongoing physics/network updates. Props disappear, cannot be caught or collected, and currently do not collide with floors/walls. Remaining rounds stay in the existing reload reservation, not in the falling visual. Nearby flatscreen players receive the effect too.
- **Support-hand transfer:** M700, M4A1 and M870. In Advanced Handling, keep support Grip held and release main Grip. The gun stays attached at the support contact rather than snapping its handle into that hand. Main Trigger can operate the M700 bolt or M4 charging handle near its action zone. Return main hand to the handle and press Grip to re-grip; firing is blocked while support-only. Releasing the support Grip releases the prop into the existing toss/holster behavior. Explicit Advanced Grip release permits M700 transfer even when its legacy automatic-transfer setting is BOLT NEEDED.
- **M870 one-hand pump experiment:** while transferred, the support hand holds the fore-end and the receiver can move along its 8 cm pump rail. Move along the barrel axis and reverse to cycle. Acceleration and gravity act on this single constrained degree of freedom. The server commits ejection at the rear stop and feeding on return, just as with two-hand pumping. It is not a complete weapon rigid-body simulation. Without Advanced Handling, main Use while holding the fore-end toggles transfer/re-grip near the handle.
- **M4 legacy transfer:** with Advanced Handling off, holding support and moving the main hand away can transfer; main Use also toggles transfer. Return to the handle to re-grip. M700 retains its existing legacy policy.
- **Support retention:** initial grab/touch still uses the calibrated region. After acquisition, two-hand support gets 12 cm of extra room per side, including the aim solver. Releasing the grab still releases support immediately. The existing minimum hand separation and aim-angle limits remain.
- **Fast two-hand pump return:** forward overshoot up to 35 cm and lateral/vertical deviation up to 25 cm are accepted while already holding the pump. The initial grab region is unchanged. The grab was already latched; the previous 4 cm forward-overshoot rejection could leave the action open after a strong push. Invalid/stale poses, occupied offhand and wall checks still cancel handling.

## Pending headset and multiplayer checks

Use survival ammo counts for the mechanical checks. All entries below are **untested in VR**.

| ID | Test | Expected |
|---|---|---|
| AH36 | Glock inspection, then a second magazine pistol | Fingers point upward relative to the gun; slide stays at the hand contact. Trigger partially retracts the slide without consuming/ejecting ammo. Releasing Use and re-gripping restores normal alignment. |
| AH37 | Glock/M4 main Use while stationary, then while flicking left/right | Magazine falls or inherits flick direction. One prop per removal; Use with an empty gun whose slide can release performs release without a prop. |
| AH38 | Remove a partially full magazine, reload, disconnect mid-reload | Total ammo follows the existing reservation rules. Cosmetic disappearance does not discard or duplicate rounds. Flat friend sees the magazine. |
| AH39 | M4 support Grip held, release main Grip; re-grip at handle | Gun remains at support contact. It cannot fire while transferred. Main Trigger at charging handle can rack. Re-gripping restores two-hand support without requiring the support hand to release. |
| AH40 | M870 fore-end Grip held, release main Grip; move back/forward | Receiver slides relative to fore-end. Full rear travel ejects once; return chambers once. Partial travel does not feed. Count live rounds when cycling without shooting. Return main Grip to fire. |
| AH41 | M700 transfer and bolt cycle; MP5 ordinary support/reload | M700 still lifts/back/forward/lowers; re-grip works. MP5 remains its existing temporary Advanced support hold, not the new anchored transfer. |
| AH42 | Glock touch support and M4 held support: move 5–10 cm outside acquisition region, then far away | Two-hand aiming remains during modest drift, drops outside the expanded region. A new grab/touch cannot start outside the original region. |
| AH43 | M870 rapid two-hand pumps, deliberately pushing past front stop; then lose focus/reconnect | Pump returns without requiring a precise stop. No repeated eject/feed while held at either stop. Focus loss or release clears ownership; reconnect does not strand inputs. |

## Verification

Java 17 `gradlew.bat build` succeeded; the JAR was reobfuscated and all **145 tests passed** (zero failures/errors). New checks cover stationary/flicked release velocities, speed limits, distinct support acquisition/retention, pump overshoot, constrained inertia stops and invalid samples. Headset behavior and real client/server startup still require the checks above.
