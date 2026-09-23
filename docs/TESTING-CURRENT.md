# All pending in-game tests - 0.12.0-alpha.9 (source; not packaged)

One consolidated list, including older unfinished subcases and regressions for changed behavior. **Alpha.6 feedback is recorded below; alpha.7 needs headset/multiplayer testing.** The 157 passing automated tests do not count as in-game passes.

## Alpha.9 priority follow-up — all awaiting headset testing

Use the same alpha.9 build on client/server when it is packaged (protocol 25). TOML loads automatically; the old Legacy comparison switch is gone. Saved calibration remains in effect.

| ID | Test | Expected |
|---|---|---|
| T09-01 | SCAR-L and H: maintain offhand support through single shots and a full automatic magazine; reload and repeat | Support remains engaged while the hand stays within its retention zone; ammo updates do not switch the profile to Buttons |
| T09-02 | Glock, M4, MP5, SCAR: fire slowly and rapidly, then empty, manually rack, and test a jam | Slide/bolt cycles visually after shots; the receiver stays at the controller; empty/jam/manual-action positions take priority; no extra ammo loss |
| T09-03 | Model 943, Taurus 500, Rhino: fire two rounds, open, eject; reload one round, reopen | Loaded rounds/cases disappear after ejection; one loaded round appears after insertion; no spare floating round group/loader attached to the gun |
| T09-04 | Same revolvers: briefly press Trigger and release before firing; then hold until a shot | Hammer/cylinder follow native charge and reverse on an aborted press; no extra shot from animation. Prefire charge is local-only for now |
| T09-05 | Advanced: open cylinder, tilt upward and wait; shake 4 cm then reverse at least 2 cm within about 0.65 s; repeat with gun lowered | Tilt alone keeps rounds; deliberate shake while raised ejects; lowered gun and stale tracking do not. Pink-zone pull still ejects without shake |
| T09-06 | Taurus 500/Rhino: open, remove cases; Grip at pouch (Advanced) or offhand Use (Physical/Advanced), move to green zone and release | Native speedloader preview; fills vacant chambers using inventory ammo only. Try inventory smaller than capacity and a partly occupied cylinder |
| T09-07 | Same pouch with offhand Trigger; then test Model 943 and Lonetrail | Trigger retrieves one round. Model 943/Lonetrail remain single-round loading because they have no native speedloader mesh. Empty inventory shows OUT OF AMMO |
| T09-08 | MK23 slower/manual mode: shoot, try shooting again, fully rack then release; switch to faster mode and repeat | Manual mode requires a stroke and ejects its case then; faster mode cycles automatically. Racking the spent state does not discard a second live round |
| T09-09 | Interrupt speedloader retrieval by disconnecting, changing item, or returning to flat; compare inventory+gun counts | No ammo consumed until insertion, no duplicated rounds; native gun remains usable in Button/flat |
| T09-10 | Flatscreen without shaders: play before and after Z, hold scope item, switch attachments; repeat a VR/flat transition | Watch for whole-world flicker or prior crashes. Framebuffer preparation now runs only in VR; this is a candidate isolation change, not a confirmed flicker fix |
| T09-11 | Flat friend watches shot slide motion, cylinder emptying, loader use; observers with different TOML files | Matching clients see custom rendering; mismatched observer uses native rendering. Remote prefire charging remains pending |
| T09-12 | Compare all existing grip/holster/inspection fits; test matching versus mismatched TOML on a spare server | Existing saved calibration is unchanged; mismatch pauses compatibility with a message, rather than using Legacy |

## Alpha.8 prior follow-up (profile-switch tests superseded by T09)

Source only; compile with the documented Gradle build command before testing. Client/server require matching protocol 24. Alpha.7 results remain historical evidence, not passes for these changes.

| Done | ID | Procedure and expected result |
|---|---|---|
| [ ] | AH60 | M700 Physical: transfer to offhand, cycle bolt, bring main hand to handle. It must NOT snap automatically. Press main Use near the handle: snap back and restore firing grip. Moving the offhand must still steer the muzzle. |
| [ ] | AH61 | M700 Advanced: complete bolt cycle, Grip within the 4 cm re-grip area. Preserve gun pose, then move offhand left/right/up: aim follows both hands. Release support and re-grab; repeat with flat observer and non-default calibration. |
| [ ] | AH62 | Advanced Taurus 943: open cylinder, move gun roughly 8 cm right with a deliberate motion, then stop. It closes after the stop, not during wrist rotation or a short/slow drift. Repeat Taurus 500/Rhino and different world scales. |
| [ ] | AH63 | Advanced closed revolver: press Use on the hand carrying the gun, then move roughly 2 cm left within 1.2 seconds. Opens once. Left movement without Use must not open it. Test normal main grip and offhand body carry. Lonetrail retains its existing breech controls. |
| [ ] | PF01–PF05 | Follow [runtime profile tests](RUNTIME-PROFILES.md): Legacy/TOML parity, SCAR-L/H physical loading, external override, invalid/mismatched files, mixed-player backends. |
| [ ] | CAL-P320 | Read [calibration audit](CALIBRATION-AUDIT.md). Confirm `tacz:p320|tacz:default`; compare bundled profile against any local override, then tune and share its own grip/zone data. Bundled does not mean individually verified. |

## Setup and reporting

- For alpha.8 source tests, install matching alpha.8 builds on client and server, protocol 24. Start shaders OFF, Physical Handling ON, debug regions visible, Visor-assigned offhand slot empty. Enable Advanced Handling for AH tests; compare it OFF separately.
- Keep existing calibration. Save before testing actual interactions: unsaved previews are not server-saved geometry.
- Survival ammo tests: count inventory/ammo containers, magazine/tube and chamber, allowing for reserved reload ammo and intentionally discarded live rounds. Cosmetic ejections are not pickups.
- Back up `config/visor_tacz-calibration.json`, `config/visor_tacz-holsters.json`, and `config/visor_tacz-inspections.json`. Share the relevant file for geometry failures.
- Jam tests need optional gundb 2.2.2, operator/cheats and a supported physical gun. Clear one jam before forcing another. Run no-gundb tests separately.
- Report `alpha.8 AH51 PASS/FAIL/PARTIAL - gun ID, subcase, Advanced ON/OFF, handedness, shader state`. Include ammo before/after for accounting, timestamp and active instance log/crash-report path for crashes.

## Latest feedback and alpha.7 follow-up (2026-09-23)

Your alpha.6 results: **AH01, AH02, AH03, AH04 and AH48 passed**. AH44 passed on **Glock only**. M4 Button/Physical/Advanced generally passed (AH45). AH49 and AH50 are still untested. AH08 remains uncertain. These results do not certify the new changes below.

The following checks apply to alpha.7. Both client and server need the matching build (network protocol 23).

| Done | ID | New or changed test |
|---|---|---|
| [ ] | AH52 | Controls: cycle Button -> Physical -> Advanced -> Button. Button keeps native reload; Physical has no holster/toss/inspection. Switching mid reload recovers reserved ammo once. |
| [ ] | AH53 | Advanced Taurus 943: open cylinder, flick wrist right to close without offhand grabbing. Slow rotation should not close it. Repeat Rhino/Taurus 500; Lonetrail has no cylinder flick. |
| [ ] | AH54 | Load a known count, shoot some, open cylinder horizontally. Tilt muzzle upward: that number of live rounds + spent cases falls. Repeat by grabbing pink and pulling backward: models fly rearward, separate visibly, eject once. These are cosmetic, not pickups; ejected live rounds are removed from the gun. |
| [ ] | AH55 | Grab body with either hand, grab a second part with the other, release first, repeat. No snap during carry handoff. Return the gun's handle to its holster and release the final hand; no toss required. |
| [ ] | AH56 | While held, offhand radial cannot select the gun's occupied hotbar slot; other slots still work. After holstering, slot selection is normal. |
| [ ] | AH57 | Advanced one-hand pistol reload: hold body in offhand OR leave gun holstered; main Use drops mag. Main Grip at pouch retrieves replacement; move to green magwell and release Grip. Use releases locked slide; otherwise rack with free main Grip. No shots until normal main grip restored. Repeat partial magazine, empty magazine, disconnect and focus loss; count inventory + gun + chamber. No airborne reload. |
| [ ] | AH58 | Empty locked pistol: Use drops magazine; release Use, wait briefly, press Use again. Slide closes with NO_MAG preserved and no round created. Insert mag, rack, verify count. |
| [ ] | AH59 | Inspection/carry at high refresh: smooth between game ticks. Repeat looking around and moving wrist; compare tossed prop. |

### Detailed AH08: inspection input timing

1. Select **Advanced**, Glock, **250 ms inspection grace**. Record magazine/chamber count. Point away from targets. Start holding the gun normally and release Trigger.
2. Away from the holster, release Grip and immediately **hold main Use** (within 250 ms). Expect INSPECT, the saved inspection pose, and a gun that stays with the hand.
3. Keep Use held. Press/release Trigger: slide partially retracts/returns. No shot, ejection or ammo change.
4. Still holding Use, press Grip to return to normal holding. Keep Use down: it must NOT drop a magazine or release the slide. Only releasing Use and pressing it again enables its ordinary action.
5. Repeat step 2, but release Use without pressing Grip. The gun follows the selected release/recovery policy; no magazine should drop from that release.
6. Repeat with 100 ms and 50 ms windows. A Use press after the window while the gun is still released must not enter inspection. If it has already returned to the holster, Use is now the deliberate holstered magazine-release action; distinguish this from inspection.
7. Repeat loss of focus/menu opening during inspection. On return, no stuck Trigger/Use or delayed magazine drop. Check ammo stayed unchanged throughout inspection-only steps.

## Evidence already recorded

- 0.9.2: Glock/M4/M870 basic operation and Glock/M4/MP5 jam clearing generally passed. Exact-count, interruption and changed-control subcases remain pending.
- Earlier: main-hand shot direction, M4 support aim, basic scale/HUD/mode switching and ordinary focus recovery were reported working. Rows below are targeted regressions or broader coverage, not claims these all remain broken.
- Later scopes worked in old/new worlds. Named optics, stereo/decal subcases and PK06 still need confirmation.
- Latest alpha.6 feedback confirms Glock inspection and M4 handling broadly work. New handoff, revolver and re-grip changes above need retesting.
- Flashing occurred without the addon and stopped after reboot. Report recurrence; there is no need to reproduce every historical flashing incident.

## Priority: latest fixes and changed behavior

| Done | ID | Procedure and expected result |
|---|---|---|
| [ ] | AH44 (replaces AH36) | Glock: Advanced Handling -> Calibrate inspection grip. Adjust XYZ and pitch/yaw/roll with live partial/closed-slide preview. Fine 1 mm/1 degree, coarse 1 cm/10 degrees. Save stays open and becomes Close with no Cancel; edits restore Save + Cancel. Cancel discards only changes since the last save. Restart and reload JSON; normal aiming calibration is unchanged. Repeat another magazine pistol. No forced upward pose remains. |
| [ ] | AH45 | M4 charging handle using offhand Grip, then Trigger in Advanced mode; repeat configured Use/Trigger with Advanced OFF. Rack/release correctly before and after front-support use. Moving the main hand away alone must not cause automatic M4 transfer. |
| [ ] | AH46 / AH51 | M700: shoot scoped, retain support Grip, release main Grip, operate bolt with main Grip/Trigger. Extra bolt-grab room must not snap rifle to handle. Scope can stay aligned. Close bolt, enter 4 cm handle area: one pulse. Press Grip: restore firing without changing rifle position/rotation/muzzle. Subsequent hand motion follows captured offset. Boundary hovering must not repeat buzzing; exit beyond 5.5 cm and return to re-arm. Repeat different angles, another bolt cycle, and flat observer. Physical mode now waits for main Use near the handle before snapping; pose-preserving re-grip is Advanced only. No fire while open/jammed. |
| [ ] | AH47 (AH20, AH28-AH31) | Glock waist and M4/P90 chest holsters: visible grip equals pickup location. Downward roomscale crouch follows two-thirds of head movement (30 cm head -> 20 cm holster). Walk and stop to test gradual recenter; stationary looking should not orbit holster. Test lean, roomscale, snap turn, left hand and world scale. Movement is a head-based estimate, not torso tracking. Saved Y numbers retained; menu shows Y from eyes. |
| [ ] | AH48 | Grab receiver/barrel/stock/grip with either hand from holster and toss. Contact stays put; generic carry cannot fire. Return to actual handle to fire. When main hand holds the gun, action zones win over generic carrying. Bounds are approximate, not exact mesh/attachment collision. |
| [ ] | AH49 | Focus/tracking/menu/slot/VR-flat interruption during carrying or sniper action: no stuck grab, unintended shot or duplicated item. |
| [ ] | AH50 | Cylinder/breech calibration and workflow on all four profiles: V01-V07 below. |

## Advanced Handling checks

Alpha.6 pass results are recorded above; unchecked rows retain pending subcases and changed-behavior regressions. Combined historical IDs above replace their older expectations. Tests below keep their original IDs for reporting.

| Done | ID | Test | Expected |
|---|---|---|---|
| [x] | AH01 | Advanced OFF; repeat Glock/M4/M870 and base button-only gun controls | Previous controls, calibration, ammo counts and optics retained |
| [x] | AH02 | Enable; select Glock; Grip near/far from waist handle | Near draws, far opens Visor hotbar; selected gun does not shoot while holstered |
| [x] | AH03 | Carry, fire, return to waist, release | Holsters; Trigger cannot fire a holstered gun; next free Grip operates hotbar |
| [x] | AH04 | Release outside holster in both release policies | Auto returns after grace; toss follows motion/rotation, then returns below quarter height, on collision, distance or timeout; inventory count unchanged |
| [ ] | AH05 | Toss, catch main hand while Trigger held | Catches without firing; release/repress Trigger to shoot |
| [ ] | AH06 | Toss into empty offhand; return to main hand | SUPPORT hold, no offhand shots, pistol holster changes side; original slot unchanged; main re-grab restores shooting |
| [ ] | AH07 | Occupied offhand; attempt catch | Does not overwrite the item or catch through it |
| [ ] | AH08 | Release then hold Use at 50/100/250 ms settings; Trigger; Grip while Use held | INSPECT within window only; slide preview; no ammo changes; Use release does not eject magazine after re-grip |
| [ ] | AH09 | Loaded Glock, partially used Glock, empty locked Glock | Use removes magazine safely; insert replacement; Use releases locked slide; survival ammo accounted once |
| [ ] | AH10 | Offhand Grip rack/mag/pouch; Trigger at selector/release; torch elsewhere | Correct contextual actions; unrelated offhand interactions retained |
| [ ] | AH11 | Long gun selected, released, caught | Central chest holster, one active gun, no dual wield; physical pump/bolt regressions checked |
| [ ] | AH12 | Focus loss, controller tracking loss, disconnect, VR→flat while held/released/inspecting | Prop canceled, gun stays in original inventory slot, no stuck firing or delayed Use; flat selected item unchanged |
| [ ] | AH13 | Disconnect mid magazine removal/insertion | Existing recovery preserves total live ammunition without duplication |
| [ ] | AH14 | Flat friend observes draw, holster, toss, catch and shots | Prop follows state; held shots retain calibrated muzzle direction; no phantom firing from holster |
| [ ] | AH15 | Left-handed mode, crouching, world scale, calibration/refit menus | Holster side/height and catch radius usable; gun/holster/inspection editors show their respective preview poses |
| [ ] | AH16 | Toss upward; press Grip before gun reaches hand; keep holding | Provisional radial appears; entering gun grab region during catch grace catches gun and closes radial without slot change |
| [ ] | AH17 | Repeat but miss; keep holding beyond catch grace | Native hotbar takes over; subsequent gun movement cannot steal the same Grip press |
| [ ] | AH18 | Release provisional Grip before expiry; highlight another slot | Highlighted slot selected normally; no duplicate gun; no leftover radial |
| [ ] | AH19 | Focus loss/menu/offhand becomes occupied during catch grace | Pending catch canceled safely; no late catch, phantom hotbar press or item overwrite |
| [ ] | AH21 | Select P90 and apply new bundled calibration | Latest bundled gun scale and M4-derived holster, stock/handle clear of the face; adjust per gun if needed |
| [ ] | AH22 | Change all six holster values; save; change gun; reload/restart | Per-gun values persist, another gun stays unchanged; Cancel does not save preview |
| [ ] | AH23 | Toss/catch at high headset refresh; repeat AH16–AH19 | Smooth inter-tick motion and responsive catch grace; no phantom hotbar selection |
| [ ] | AH24 | Pistol inspection, then Grip while Use held | More visible partial slide movement; no ammo change, no accidental firing or magazine removal |
| [ ] | AH25 | Close Virtual Desktop while holding Grip/Use or during catch radial; reconnect | Visor menu and addon menu buttons respond; no stuck owned inputs; gun remains in inventory |
| [ ] | AH26 | Repeat reconnect with Advanced Handling OFF | Normal Visor/flat controls remain functional; report whether failure is specific to Advanced Handling |
| [ ] | AH27 | Glock-derived and Deagle-derived pistols; cylinder exceptions | New scale and expected source calibration; verify physical zones, muzzle and sight alignment for each copied profile |
| [ ] | AH37 | Glock/M4 main Use while stationary, then while flicking left/right | Magazine falls or inherits flick direction. One prop per removal; Use with an empty gun whose slide can release performs release without a prop. |
| [ ] | AH38 | Remove a partially full magazine, reload, disconnect mid-reload | Total ammo follows the existing reservation rules. Cosmetic disappearance does not discard or duplicate rounds. Flat friend sees the magazine. |
| [ ] | AH39 | M4 support Grip held, release main Grip; re-grip at handle | Gun remains at support contact. It cannot fire while transferred. Main Trigger at charging handle can rack. Re-gripping restores two-hand support without requiring the support hand to release. |
| [ ] | AH40 | M870 fore-end Grip held, release main Grip; move back/forward | Receiver slides relative to fore-end. Full rear travel ejects once; return chambers once. Partial travel does not feed. Count live rounds when cycling without shooting. Return main Grip to fire. |
| [ ] | AH42 | Glock touch support and M4 held support: move 5–10 cm outside acquisition region, then far away | Two-hand aiming remains during modest drift, drops outside the expanded region. A new grab/touch cannot start outside the original region. |
| [ ] | AH43 | M870 rapid two-hand pumps, deliberately pushing past front stop; then lose focus/reconnect | Pump returns without requiring a precise stop. No repeated eject/feed while held at either stop. Focus loss or release clears ownership; reconnect does not strand inputs. |

## Calibration editor checks added in alpha.4

The formerly unnumbered UI checks now use AH32-AH35. Also apply these to the new inspection editor.

- [ ] **AH32:** Save writes to file, keeps screen open and becomes Close. Fine/Coarse or page changes alone leave Close unchanged.
- [ ] **AH33:** Edit/reset/change scale lock after Save: Save returns. Cancel/Escape discards only changes since the last save; reopen/restart confirms the saved values.
- [ ] **AH34:** Holster fine 1 cm/1 degree versus coarse 10 cm/10 degrees. Inspection steps are smaller as listed in AH44. Rotation and translation change the expected axes.
- [ ] **AH35:** A failed save reports an error and does not mark unsaved data saved or discard the preview. Malformed-file/reload coverage is C05 below.

## Original gun, calibration, interruption and multiplayer coverage

Basic historical passes are recorded above. These unchecked rows retain untested subcases and checks affected by later changes. Shared procedures need not be repeated twice merely because an AH row references them.

### Glock and shared magazine handling

- [ ] **F01 - slide release (new behavior).** Empty Glock, replace magazine, immediately press main-hand Use: slide closes and one round chambers. Repeat using offhand Use at cyan region with Grab=TRIGGER, configured Grab at that region, and slide racking. Repeated release presses must not consume additional ammo. Wrong-zone offhand input must not release.

- [ ] **G01 - ammunition ledger.** Tactical reload retains a live chamber; empty reload requires physical chambering. Test partial/no spare ammo, native capacity attachments, loaded-chamber racking and magazine removed. A removed magazine permits only an already chambered shot, not feeding hidden rounds.

- [ ] **G02 - action timing.** Partial rack ejects nothing, full rack ejects once at rearward completion, holding there repeats nothing. Five complete live racks discard five rounds if available. Record sound and live/spent size issues.

- [ ] **F02 - two-hand ADS.** With ADS 2 hands ON, eye alignment alone does not activate ADS. Touch blue support + align for pistols (Grab optional); other guns still require Grab. Move away: ADS exits. Try both eyes, lowered gun, sprinting, reloading, menu and focus loss. ADS should stop sprinting. OFF restores alignment-only ADS. Repeat each supported gun; M870 uses its fully forward pump as support. Pistol direction stays with the firing controller; its close support hand gates ADS instead of steering it.

- [ ] **G03 - firing restrictions.** Compare modes/rate/cooldown with flat TaCZ. No shots while invalid reload/action state or through a wall. Recover when muzzle is clear. Sprint reload being allowed is not automatically a bug.

- [ ] **G04 - pouch ownership.** Glock/M4/MP5 pouch only supplies replacement during removed-magazine stage. Occupied assigned offhand blocks grabs. M870 shell pouch is available with pump open for side loading and closed for tube.

### M700 bolt and transfer

T01/T02/T04/T05 policy checks use **Advanced OFF**. Explicit Advanced Grip transfer can override that legacy start policy. Use AH46/AH51 for the new scoped return and haptic behavior.

- [ ] **T01 - BOLT NEEDED, ready gun.** Loaded closed rifle, support held: move main hand away and try main Use. Neither starts transfer; gun follows main hand.

- [ ] **T02 - BOLT NEEDED, action required.** Fire, keep support held, move main hand over 16 cm away: rifle stays with support hand. Repeat explicit main Use. Also try empty chamber, lifted/open bolt and generic jam.

- [ ] **T03 - finish and return.** While transferred, main trigger grabs bolt; lift/back/forward/lower. No firing during support-only transfer; ADS may remain active while aligned. Closing/lowering must not snap gun back. With Advanced OFF, Main Use near grip returns it; with Advanced ON, main Grip near handle returns it with preserved pose (AH51); releasing support also ends transfer. Try your narrow calibrated support region.

- [ ] **T04 - ANYTIME.** Transfer a ready loaded gun by movement and by main Use. Return normally; no unwanted shot and no chamber/ammo change from transfer alone.

- [ ] **T05 - saved preference.** Switch policy, restart/rejoin; it persists and reaches server. Two players can select different policies. Neither mode changes other guns or flatscreen controls.

- [ ] **N03 - interrupted bolt.** Too-short stroke, wrong direction, release lifted/open, re-grab and complete, disconnect while open. One ejection/feed; no firing with open/lifted bolt and no automatic feed from reconnect.

- [ ] **N04 - magazine/chamber.** Tactical/empty magazine reload with bolt open, cycle with no magazine, cycle a live chamber. Count ammo; no-mag cannot feed.

### MP5

- [ ] **F04 - latch and release.** Full rearward pull latches without lifting. Release Grab, insert magazine, sweep empty hand back-to-front through cyan region without a button. Repeat downward slap, slow/fast sweep, configured Grab and offhand Use. Wrong direction/outside region do nothing; one release feeds at most one round. Try no magazine and an active jam. Empty firing alone does not latch; physically pull the handle first.

- [ ] **N05 - ordinary operation.** Native firing modes/selector, trigger release, blue support, tactical/empty reload, no spare ammo and full inventory.

- [ ] **N06 - action accounting.** Check action model/sound, all three jam types and interrupted reload; repeat shared G01/G02 and J01-J04 on MP5.

### M870

- [ ] **S01 - support regression.** Fully forward held pump supports aim; barrel and impacts follow. Released/partially pulled pump must not count as ready ADS support. No firing while open or spent case requires pumping.

- [ ] **S02 - tube loading.** Closed pump, pouch to green underside port: inventory -1, tube +1, chamber unchanged. Empty inventory gives OUT OF AMMO and no shell preview. Full tube/invalid placement consumes nothing; try ammo boxes.

- [ ] **S07 - side-port loading (part of F08).** Open pump, pouch shell to pink port: inventory -1, chamber +1, tube unchanged. Closing consumes no tube shell when chamber already loaded. Cannot insert twice into occupied chamber; wrong port/open-pump underside insertion consumes nothing.

- [ ] **S03 - shell visuals.** Live ejection, spent ejection and held preview all use 12g casing asset; verify new casing scale (F05).

- [ ] **S04 - pump strokes.** Partial rear stroke does nothing; complete rear stroke ejects once; hold/release open/re-grab then close feeds at most one.

- [ ] **S05 - cancellations/counts.** Invalid shell release, switch, focus loss, disconnect: preview consumes nothing; committed insertion stays committed. Include full tube and empty tube/chamber. Disconnect counts previously passed; side-port loading still needs that check.

- [ ] **S06 - fallback and old world.** Buttons/flatscreen native reload works. Retest the affected old-world gun; if grab fails, compare a fresh M870 in that same world before creating another world.

### Jams and casing calibration

- [ ] **J01 - stovepipe.** `/visor_tacz_test jam stovepipe`: casing lies across bore, tilted upward 22.5 degrees relative to gun. Short pluck does not clear; full pluck does. Force again and clear by full rack. No free ammo or repeats.

- [ ] **J02 - double feed.** With at least two rounds, `/visor_tacz_test jam double_feed`: two casing-model obstructions. Cannot clear with magazine in; remove it, make two separate full pulls, reinsert and chamber. Extra pulls or holding rearward create no rounds; two discarded rounds accounted for. Pouch remains blocked while obstructions remain.

- [ ] **J03 / F06 - dud.** `/visor_tacz_test jam dud` (`misfire` alias also works). Action looks closed, trigger clicks, HUD reports DUD. Rack ejects one reserved round and permits feeding. No external protrusion or magazine-tap requirement.

- [ ] **J04 - interrupted jam.** Switch, focus loss, disconnect mid-clear: remaining count persists; already ejected rounds do not return. No repair of durability merely from clearing.

- [ ] **J05 - native clear.** Switch flat, use TaCZ Inspect (check binding; normally H). After native unjam, extra visuals clear; unejected reserved ammo returns once. Keep addon installed while testing its cleanup.

- [ ] **J06 - manual-action generic jam.** M870 and M700: `/visor_tacz_test jam`, then complete pump/bolt cycle. Partial gesture must not clear. These guns do not use the Glock/MP5 three visual jam mechanisms.

- [ ] **J07 - optional dependency.** Separate no-gundb setup: normal operation on the original five guns and added pistol profiles without crashes/new random jams. Jam-test command should explain the missing integration. Earlier no-gundb gameplay worked generally.

- [ ] **F05 - casing scale.** Casing size page: preview at port; test 50%, 100%, 150%, save/restart/reload. Jam models, ejections and M870 held shell agree. Gun scale multiplies this visual setting; ammo counts/hitboxes do not change.

### Calibration, configuration, HUD

- [ ] **C01 - linked scaling.** Gun scale 80/100/120%, lock ON resizes mounted boxes; OFF leaves their dimensions unchanged. Pouch stays independent. Grip stays anchored, muzzle/impacts match, toggling lock itself does not resize.

- [ ] **C02 - box bounds.** Width/height/depth on support, release, mag/tube, rack, pouch and M870 pink port. Saved visible region and actual pickup agree. Large boxes do not turn incomplete strokes into complete ones.

- [ ] **C03 - pages/hints.** Each gun exposes its actions; no overlapping text. Glock now has support and release; M870 pink port now has size controls.

- [ ] **C04 - persistence/cancel.** Existing nonzero offsets remain. Per-gun save, switch, reconnect, restart; Cancel/Escape reverts preview; page reset affects only selected setting. Include new release/support/casing controls.

- [ ] **C05 - file reload.** Back up JSON, edit externally; menu Reload calibration and `/visor_tacz reload_calibration` update visuals/server grabs. Include gun, holster and inspection JSON files. Malformed JSON must be reported and previous valid values retained. Restore and retry.

- [ ] **C06 - guides.** Debug OFF hides guides in play but calibration still shows them. Interaction/ADS unaffected; preference persists.

- [ ] **C07 - fallback/share.** Missing local gun profile uses bundled defaults; supplied local profile overrides. Test with a copy, preserve personal values.

- [ ] **D01 - displays.** Gun/Wrist/HUD/OFF and `/visor_tacz status`: ammo, chamber, ADS, target and action match; preferences persist. Verify wrist placement independently of normal GUI HUD.

### Input, interruptions, multiplayer

- [ ] **I01 - input recovery.** Focus/tracking loss while firing semi/auto, menu, slot change and death while trigger held: no stuck fire or firing merely on resume. New press works. Previous ordinary focus/reconnect tests passed.

- [ ] **I02 - contextual controls.** Grab=USE and TRIGGER work. Away from valid zones, torch placement, sword swing, bare-hand punch, GUI pointing work with gun in other hand. A grabbed gun part must not punch/mine accidentally.

- [ ] **I03 - handedness.** Left-handed mode, crouch, roomscale and snap-turn; occupied assigned offhand blocks grabs. Include M700 transfer. Record Visor Essential menu positioning separately if it prevents the test. No dual wield.

- [ ] **I04 - AUTO/flat.** OFF/AUTO and Physical/Buttons transitions, restart: normal flat TaCZ behavior, no stuck action. OFF is not complete mod unloading.

- [ ] **R01 - interrupted magazine reload.** Disconnect before/after native ammo commit and with mag removed. Rejoin twice, selected and unselected gun; recover once, keep committed ammo and charging requirement. Orderly server restart too; include Glock/M4/MP5/M700.

- [ ] **R02 / F07 - other interruptions.** During open action, shell preview, jam clearing or M700 transfer: menu, slot, focus, mode change, death/drop in a disposable test world. No duplicated ammo, stuck grab or accidental shot.

- [ ] **M01 - flat friend / remote models.** Friend plays native TaCZ normally. They see calibrated weapon, parts, jam/case size and M700 support transfer. Reconnect/teleport/switch/out-of-view causes no duplicate/missing gun.

- [ ] **M02 - server impacts.** Gun pointed away from face, last interaction from opposite hand: impacts follow gun/muzzle, including support aim and scaling. Check near/far targets and real latency, not just tracer appearance.

### Optics and stability (after handling tests)

- [ ] **O01 - named optics.** Later general scope success supersedes the old blanket black-optics failure. Confirm named examples (QMK-152, TA31 ACOG, previously black red dots) by exact ID, each eye, head movement and lens clipping.

- [ ] **O02 - QMK scene/decal.** Shader OFF: magnification, off-axis behavior, near/far targets, bullet marks inside lens, no unzoomed decal visible over lens, and no incorrect muzzle/front-sight intrusion. Record scale/zoom/gun.

- [ ] **O03 - settings/shaders.** Optics and ADS toggles persist. Shader runs stay clear/stable; custom shader-pack magnification remains unsupported.

- [ ] **O04 - PK06 evidence.** Record exact variant ID, viewing area and shader state. Missing lower-view magnification remains unresolved; not a promised 0.9.2 fix and not a blocker for validating the pistol reload mechanism.

- [ ] **R03 - stability regression.** Old/new world holding scope, Z attachment GUI/slot changes/rejoin in flat/VR, main-menu VR transition, shaders off/on. Report crash or flashing timestamp. Flashing previously occurred without addon.

## Per-gun pistol matrix - each gun still needs its own result

For P01-P07, test each magazine pistol separately: **Glock 17, M1911, P320, M9A4, Deagle, Deagle Golden, Timeless 50, B93R, CZ75, HK MK23**. A shared-mechanism pass does not validate every model/zone/ammo type. P05 applies to the named mode exceptions.

For V01-V07, test **Rhino 357, Taurus 500, Taurus 943, Lonetrail** separately. Lonetrail is a single-shot breech using the experimental common gesture, not a cylinder simulation.

- [ ] **P01**: Correct model size, grip, muzzle/impact alignment; moving slide root includes the whole slide. No duplicate/hanging magazine. Calibrate and save before testing grabs.

- [ ] **P02**: Empty reload from pouch transfers correct inventory ammo immediately; main Use and cyan offhand release chamber exactly one round. Tactical reload preserves the chamber. Try partial/no spare ammo and capacity attachments.

- [ ] **P03**: Five complete back/front racks without letting go; one ejection per full rear stroke, no repeats when held back, exact remaining rounds. Native slide stays locked after last shot until released/reloaded.

- [ ] **P04**: Touch blue support without Grab; auto ADS enters/exits with alignment. Test right and left gun-hand settings. Save/reload/restart calibration, scale and casing size.

- [ ] **P05**: Burst/semi selector on B93R/MK23 at yellow region. CZ75 auto trigger release. Interrupt by reloading, focus loss, gun switching and jam: no delayed extra shots.

- [ ] **P06**: Stovepipe/double-feed/dud on supported gundb, each cleared separately; exact round counts and casing orientation. Compare without gundb.

- [ ] **P07**: Disconnect during reload, reconnect twice; no extra/lost reserved rounds. Flat friend can reload/fire normally and sees remote gun/magazine correctly.

- [ ] **V01**: Open/close model and regions match sufficiently to calibrate. No firing while open, holding a round or manipulating action. Releasing halfway does not toggle. Holding after a completed toggle does not toggle repeatedly.

- [ ] **V02**: Fire two rounds; spent count increases twice. Open; still-occupied spent slots reject loading when live + spent equals capacity. Eject all: count discarded LIVE rounds separately from spent cases.

- [ ] **V03**: Load from pouch one at a time. Wrong zone, canceled grip, tracking loss, full action, or no ammo consumes nothing. Close, fire, verify native count. Test Taurus capacities and Lonetrail single-shot behavior separately.

- [ ] **V04**: Disconnect with action open and while holding a pouch preview. Saved ammo/open/spent survive; preview does not. Rejoin twice and count all ammunition.

- [ ] **V05**: Switch physical -> buttons -> flat -> VR. Native reload/fire works; temporary open/spent clears without adding/removing live rounds. Test removing addon in a copied test world if desired.

- [ ] **V06**: Generic `/visor_tacz_test jam` blocks firing; open/close clears it. Named dud/double-feed tests are intentionally rejected. Native durability remains optional.

- [ ] **V07**: Remote flat observer sees open/closed action and held ammo preview; correct ammo model/sound. Individual chamber cartridge mesh state is not implemented.

Cylinder reminder: orange Grab + sideways ~5 cm opens/closes once; while open, pink Grab + backward ~5 cm discards ALL remaining live rounds and spent cases; pouch Grab -> green release loads ONE compatible inventory round. With Advanced ON use Grip; otherwise configured Use/Trigger. Spent cases occupy capacity. Ejected objects cannot be collected. Calibrate orange, pink, green and pouch separately.

## Button fallback / all registered base guns

Use [the complete 54-gun list](SUPPORTED-GUNS.md). There are 18 physical profiles and 36 button-only profiles. Registration is not a per-gun headset pass.

- [ ] **B01:** Global Physical preference + AK47/UMP45: Buttons (Auto), trigger fires, main Use reloads. Returning to Glock/M4 restores physical preference. Global Buttons uses native reload everywhere; physical gestures/test commands do not interfere with fallback guns.
- [ ] **B02:** Representatives AK47, UMP45, AWP/Kar98, AA12/DB Long, Minigun, M320/RPG-7: empty/tactical reload, native script/bolt/spin-up, burst/auto trigger release, survival ledger and projectile direction. Button reload does not promise complete desktop animation.
- [ ] **B03:** Long guns at default/100%/150% scale, near/far targets and walls. M320/RPG-7 muzzle origins are estimates: calibrate them. Oversized calibrated muzzle offsets may exceed server reach bounds.
- [ ] **B04:** Spot-check every remaining entry in the support list: model, draw, aim, fire, reload, release and effective handling mode. Record each ID. External packs/alternate displays are not automatically registered.
- [ ] **B05:** Flat friend native controls and remote button-gun rendering; focus loss/reconnect/switch mid-reload and exact ammo recovery.

## Scope of this list

Older alias groups F03, F08, N01/N02/N07/N08 and A01 are covered by the detailed gun/calibration/input/multiplayer rows. AH21/AH27 cover new 75% defaults and copied pistol calibration; C07 covers saved overrides. AH41 is covered by AH46/AH51 and MP5 F04/N05/N06. AH28-AH31 are replaced by head-relative holster AH47. Nothing was silently marked passed.

Do not judge alpha.6 against superseded requirements: fixed-floor holsters, forced inspection rotation, automatic M4 transfer, or no ADS during sniper transfer. Physical attachment insertion, individual magazine items, dual wield, generic item pouch and external physical-profile hot reload are future implementation work, not pending acceptance tests.
