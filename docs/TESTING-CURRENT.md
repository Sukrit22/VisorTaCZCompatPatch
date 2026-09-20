# Rolling manual test tracker — 0.7.0

Updated September 20, 2026 from the user's reports and earlier release checklists.
This is the current checklist; older release checklists are historical. No result
reported means **unverified**, not failed. Build/unit tests do not close VR tests.
Stable IDs below can be used in replies and retained in future releases.

Latest follow-up (0.8.0): scopes work in old and new worlds per user report; PK06 lower viewing area still lacks magnification. M870 pump grab fails in the old world but works in a new world; cause unresolved. Jam testing is blocked because the active only Visor+TaCZ+addons-noTaCZcompat instance has no gundb installed. Normal handling does not require gundb. Source now reports separate jam-command failure reasons. Earlier optic failures below are historical; individual optics, decals and shader cases still need specific retests.

Earlier: [0.7.0 user results and requested changes](TEST-RESULTS-0.7.0.md).
Checked entries mean the user reported the main behavior OK; unmentioned subcases
are not automatically certified. Partial and failed groups remain unchecked.

0.7.1 changes require the [focused retest](RELEASE-0.7.1.md). Checked results below
refer to the reported 0.7.0 session, not automatic validation of 0.7.1.

New M700/MP5 checks **N01�N08** are all pending in the [0.8.0 checklist](RELEASE-0.8.0.md).
Keep reporting 0.7.1 results with that version; adding guns does not close older issues.

## Setup and reporting

Use a backed-up test world, Pico 4 Ultra + Virtual Desktop + SteamVR, and default
TaCZ Glock 17, M4A1, and M870. Install 0.8.0 on all clients/server (protocol 13).
Keep only one addon version. Preserve calibration and settings. Begin with
shaders off; use survival with known ammo totals for accounting tests. Physical
grabs require Visor's assigned offhand to be empty, including its hotbar slot.
Enable debug cubes for alignment checks. Save calibration before testing server
interactions: an unsaved preview is not the server's saved profile.

Reply, for example: `S02 PASS — M870, survival, 12g in inventory` or
`J02 FAIL — M4, second pull produced no round`. Include addon version, gun/optic,
shader state, handedness, and observed result. Partial passes stay open; specify
which subcase passed. For crashes send the timestamped report and matching log.
For alignment feedback send `config/visor_tacz-calibration.json`.

## Earlier results already reported

| Area | Evidence from user | Remaining scope |
| --- | --- | --- |
| Last active hand | Solved: left-hand interaction no longer redirects right-hand shots | Small regression check after new calibration |
| M4 support aim | Solved: barrel and bullet direction turn together | New scale/box regression; remote observer |
| HMD reconnect | Full disconnect/reconnect works | Focus-only loss remains untested |
| Addon OFF comparison | Upstream VR gun behavior observed with addon off | AUTO transitions and preference persistence |
| Grip calibration | Adjusted successfully; hand reaches grip | Persistence, Cancel, new scale and boxes |
| Glock/M4 physical handling | Basic handling works; sounds and ejection observed | Exact ammo counts, timing, interruptions, sound quality |
| M870 pump | Pump operation observed | Detailed stroke/count tests and latest support fix |
| Scope stability | Later session reported no crash or blinking | Exact old-world/Z/shader/transition matrix unverified |
| QMK-152 | Magnification reported working with shaders off | Stereo, off-axis behavior and toggle persistence |
| TA31 ACOG | Black center reported | Known unresolved issue; no fix claimed |
| Whole-world blinking | Reproduced without addon; stopped after PC restart | Monitor recurrence; root cause unknown |

## First session: fixes and newly added controls

- [x] **C01 — PASS reported 0.7.0: uniform scale.** Try 80%, 100%, 120% on all three guns.
  Grip stays anchored; model, muzzle and gun-mounted points resize together.
  Pouch placement and independently set box dimensions do not resize with the gun.
  Fire at targets: shot origin follows the resized muzzle.
- [x] **C02 — PASS reported 0.7.0: box dimensions.** On magazine/loading port, rack/pump, pouch,
  and M4 support, change width, height, depth separately. Save; valid grabs match
  visible bounds. Short/sideways strokes must not complete a reload or rack merely
  because the grab box is larger. Check rotated guns and head-yaw-relative pouch.
- [x] **C03 — PASS reported 0.7.0: per-gun menu.** Hints name the visible colors/actions; no text
  overlaps buttons at your GUI scale. Hover the page title for long hints. M4 has
  support/selector pages; Glock/M870 omit unsupported selector actions. M870 pump
  is also its support point; its pink port marker has no grab-box size control.
- [x] **S01 — PASS reported 0.7.0: M870 support.** Hold the orange pump fully forward,
  keep firing hand nearly still, move offhand: barrel and impacts follow support
  aim. Release to disengage. The separate old blue point is no longer required.
- [x] **S02 — PASS reported 0.7.0: M870 pouch.** With 12g ammo in survival inventory,
  grab cyan pouch, release at green port: inventory -1, tube +1, chamber unchanged.
  Repeat with an ammo box. No ammo/full tube/invalid release must not grant or
  consume ammo. A preview appearing without inventory ammo is allowed.
- [x] **S03 — PASS reported 0.7.0: cartridge visual.** Pump an unfired chamber:
  ejected model is 12g-sized, not pistol-like. Fire then pump: one spent 12g shell.
  Check pouch preview too. Current live/spent visuals reuse the casing asset.
- [x] **D01 — PASS reported 0.7.0: displays/status.** Try Gun, Wrist, HUD, Off. Verify ammo,
  chamber/tube, ADS and handling state are visible and accurate. HUD uses ordinary
  GUI overlay placement; verify your Visor wrist overlay setup separately.
  `/visor_tacz status` should produce chat output. Restart to check preference.
- [x] **A01 — PASS reported 0.7.0: auto ADS.** After D01, align purple sight reference and
  test each eye, raise/lower, look away, sprint, reload, open/close a menu and regain
  HMD focus. ADS should activate/deactivate appropriately without whole-view zoom.
  Guide highlighting checks position only; ADS also requires look alignment.

## Calibration and settings carried forward

- [x] **C04 — PASS reported 0.7.0: save/cancel/migration.** Existing Y/Z values remain
  nonzero as saved. Change grip rotation/position, muzzle, scale, and boxes; Save,
  switch guns, reconnect and restart. Profiles stay separate. Cancel/Escape restores
  saved values. Reset affects only the current page, not other offsets.
- [ ] **C05 — UNVERIFIED: reload file.** Back up JSON; edit a value, save externally,
  then test both Reload calibration menu button and command. Held gun/server
  interactions update. Deliberately invalid JSON in the test copy must retain old
  values and refuse Save; restore valid file and reload. Reload must not rewrite it.
- [x] **C06 — PASS reported 0.7.0: hide guides.** Debug cubes OFF hides all colored guides,
  during normal play. Calibration always shows its guides in 0.7.1. Handling/ADS still work.
  Restart: preference persists. ON restores guides.
- [ ] **C07 — UNVERIFIED: shared/bundled profiles.** In a backed-up config, test
  a missing local profile falling back to bundled values and a shared local profile
  overriding them. Do not erase your only copy of personal calibration.

## Input and ordinary weapon handling carried forward

- [ ] **I01 — PARTIAL: focus and full reconnect PASS; other interruptions pending: HMD/input recovery.** Test focus loss without disconnect,
  then tracking loss while firing Glock/automatic M4. Open menus, switch slots and
  die while holding trigger. No stuck firing or shots merely from resuming; a fresh
  press works. Full-disconnect recovery was previously reported working.
- [ ] **I02 — FAIL: torch placement; swings and grab suppression PASS: contextual Use/Trigger grabs.** Both choices grab valid
  zones. Away from zones, normal Visor actions work. Offhand torch placement, sword
  swings, bare-hand punching away from gun, and GUI pointing still work. Grabbing
  near the gun must not accidentally punch/mine. Test two-handed hotbar assignments.
- [ ] **I03 — PARTIAL: gun handedness PASS; menu position obstructs remaining tests: handedness/movement.** Repeat gun aiming and grips with
  Visor left-handed mode, crouching, roomscale movement and snap turns. Occupied
  assigned offhand must not engage physical grabs/support. No dual wield is added.
- [x] **I04 — PASS reported 0.7.0: AUTO and modes.** AUTO follows VR/flatscreen transitions;
  OFF persists until AUTO selected. Switch Physical/Buttons and restart. Ordinary
  flatscreen fire/reload remains normal. Reconnect if enabling VR after joining
  with Visor disabled. OFF is not a complete mod unload.
- [x] **G01 — PASS reported 0.7.0: Glock/M4 ammo accounting.** Record inventory + gun ammo.
  Test tactical and empty reloads, partial/no spare ammo and capacity attachments.
  Tactical reload retains chamber; empty physical reload needs charging. Five
  complete loaded-chamber racks discard exactly five rounds if available; empty
  racks create none. Magazine removed: only an existing chamber round can fire.
- [ ] **G02 — PARTIAL: no issue noticed; timing recheck pending: ejection/audio timing.** Full rear pull ejects once before
  release; holding open repeats nothing, partial pulls eject nothing. Observe each
  eye and listen for removal/insertion/rack cues. Report missing/ill-fitting sounds;
  sample quality is still provisional.
- [ ] **G03 — PARTIAL: wall rejection PASS; sprint reload works: fire restrictions.** Compare Glock semi-auto and M4
  selector/modes with flat TaCZ. Correct ammo consumption, trigger release,
  cooldowns, reload/sprint restrictions. Muzzle/controller through a wall must not
  permit shooting through it; firing recovers when moved clear.
- [x] **G04 — PASS reported 0.7.0: pouch ownership.** Glock/M4 ready state should not grab
  a replacement at the waist; removed-magazine stage should. Another offhand item
  blocks grab. M870 shell-loading pouch behavior differs and is covered by S02.

## M870 remaining sequence tests

- [ ] **S04 — PARTIAL: stays open and manual close PASS; rear-hold count pending: pump state.** Partial rear stroke feeds/ejects nothing;
  full rear stroke ejects once; forward stroke chambers one shell. Hold rearward,
  release open, re-grab, push forward: no extra ejection or premature firing.
- [ ] **S05 — PARTIAL: disconnect counts PASS; other cancellations pending: shell cancellation.** Cancel preview by invalid release,
  weapon switch, focus loss or reconnect: no ammo lost/created. Inserted shells stay
  loaded. Repeat at capacity and with an empty tube/chamber.
- [ ] **S06 — UNVERIFIED: native fallback.** Buttons/flatscreen restore normal
  TaCZ M870 reload/bolt behavior without stuck pump state or altered ammo totals.

## Jams — all still awaiting reported gameplay results

Use gundb 2.2.2, cheats/operator permission, VR physical mode, a supported loaded
gun, and no active gesture. Test Glock and M4 separately. Commands force a test
jam; ordinary jam probability still belongs to Durability.

- [ ] **J01 — UNVERIFIED: stovepipe.** `/visor_tacz_test jam stovepipe`. Short
  pluck does not clear; full casing pluck clears. Force a fresh jam and clear by
  full rack. Check casing/action alignment and no collectible/free ammo.
- [ ] **J02 — UNVERIFIED: double feed.** With at least two rounds, use
  `/visor_tacz_test jam double_feed`. Magazine inserted: cannot clear obstruction.
  Remove magazine, perform two separate full pulls: exactly two rounds ejected.
  Holding open/extra pulls create none. Reinsert, charge, fire; total ammo accounts
  for two discarded rounds. Pouch stays blocked while obstructions remain.
- [ ] **J03 — UNVERIFIED: dud.** `/visor_tacz_test jam dud`. Trigger clicks; one
  completed rack ejects the dud and permits normal feeding. No magazine slap is
  required in this implementation.
- [ ] **J04 — UNVERIFIED: jam interruption.** Switch gun, lose focus, reconnect
  mid-clear. Jam/remaining obstruction count survives; already-ejected rounds do
  not return. Durability never increases from clearing.
- [ ] **J05 — UNVERIFIED: native fallback.** Switch to flatscreen holding jammed
  gun, press Inspect (**H in this instance**), let unjam finish. Custom jam visuals
  clear and unejected reserved ammo returns once. Keep addon installed for cleanup.
- [ ] **J06 — UNVERIFIED: M870 generic jam.** `/visor_tacz_test jam` on M870,
  complete pump cycle: clears generic jam. Partial/canceled cycle does not. M870
  does not implement Glock/M4 double-feed/dud sequences.
- [ ] **J07 — UNVERIFIED: no Durability installed.** In a separate test setup,
  omit gundb on client/server; load world and use all three guns normally. No missing
  class crash or new random jams; physical reloads and ordinary firing still work.

## Reconnect, multiplayer, and stability

- [ ] **R01 — UNVERIFIED: interrupted Glock/M4 reload.** Record ammo, disconnect
  with mag removed; also before/after empty-reload ammo transfer. Rejoin twice,
  including with gun unselected: recovery happens once, no ammo growth, committed
  loaded ammo retained and charging requirement preserved. Repeat with orderly
  server restart. Do not deliberately crash your main world.
- [ ] **R02 — UNVERIFIED: other interruptions.** Test menu, mode change, slot
  switch, death/drop/transfer during reload in a disposable world. No duplicated
  reserved ammo or stuck grabs. Report exact stage if recovery differs.
- [ ] **M01 — UNVERIFIED: flatscreen friend.** Friend fires/reloads supported and
  another TaCZ gun normally. They see VR gun scale/calibration, M4 support aim,
  magazine/slide, M870 pump/shell, and jam state. No duplicate/missing gun after
  reconnect, teleport, switching weapons or moving out of view.
- [ ] **M02 — UNVERIFIED: server impacts/latency.** Friend watches while gun points
  away from face: impacts originate/direction follow muzzle, not head/cursor.
  Test nearby/distant targets, support aim, scale and realistic latency. Recovery
  from rejected stale tracking must not leave input stuck. Judge impacts as well
  as tracer appearance.
- [ ] **R03 — PARTIAL: startup/render matrix.** Reopen previously failing world
  holding scoped gun; also new world. Test flat-to-VR main-menu transition and Z
  attach/detach/slot change/rejoin in flat and VR, shader off/on. No crash/whole-world
  flashing. Prior clean sessions are encouraging, not coverage of every combination.

## Optics carried forward

Latest user report: **0.7.1 still has black/solid-looking apertures on tested optics
other than QMK-152**, including the earlier named red dots. Apparent low detail is
an observation, not proof that the LOD model was selected. The 0.7.1 candidate fix
is not confirmed successful. No fresh bullet-mark/decal result was supplied in
this report. 0.8.0 carries the same optics changes, not an additional optics fix.



- [ ] **O01 — FAIL: multiple named optics black; see latest report: red dot.** Earlier black-center report lacks a confirmed
  resolution. Record exact optic ID; shaders off, each eye separately. Dot visible,
  clipped to window, stable while moving head, no duplicate overlay/flicker.
- [ ] **O02 — PARTIAL: zoom works; decal visibility and front sight artifacts: QMK-152.** Zoom already reported working with shaders off.
  Check each eye, off-axis/eye-distance blackout, near/far targets and performance.
  Outside the lens stays unzoomed. Record gun/zoom setting.
- [ ] **O03 — UNVERIFIED: toggles.** Optics OFF/ON and ADS OFF/AUTO behave as
  selected and persist after restart. Shader-enabled runs remain stable; custom
  scope magnification with shaders is not implemented, so absent zoom is expected.

**Known open issue, not an untested feature:** TA31 ACOG black center. A retest
can supply evidence, but 0.7.0 does not claim to fix it. Whole-world blinking
should be reported if it recurs; previous isolation showed it can occur without
this addon. Physical recoil, physical attachment insertion, tracked magazines,
dual wield, and general item-pouch handling are not implemented and are not
acceptance requirements for this build.

Sources: TESTING.md, TESTING-0.3.0.md, 0.4.x–0.7.0 release notes, and user reports
in this task. Latest automated validation: 84 tests/build passed, separate from
all manual checkboxes above.

