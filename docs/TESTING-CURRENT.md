# Consolidated testing checklist - 0.9.2

This replaces the scattered current instructions in the older release guides.
Older checklists/results are retained in TESTING-HISTORY-THROUGH-0.9.2.md and
release notes. Test IDs are retained where possible. A past pass is evidence for
that version, not a pass for every subcase or for newly changed behavior.

**Current build:** 0.9.2, protocol 16; 100 automated tests passed. No 0.9.1/0.9.2
headset results have been reported yet. Automated tests do not close VR checks.

## Recommended next session: Glock first

Do these before expanding the shared pistol implementation:

1. **F01 + G01:** Empty reload, main Use slide release, offhand release zone,
   and normal racking. Then tactical reload, no spare ammo, repeated release.
2. **F02:** Blue support region and two-hand ADS: grip + eye alignment enters,
   releasing support exits; toggle the option and compare.
3. **G02:** Partial rack, full rack, hold rearward, repeated loaded-chamber racks:
   ejection timing and exact round count.
4. **J01-J03:** Stovepipe, double feed, dud. Check clearing and ammo totals,
   not only the visual model.
5. **C04 + F05:** Save grip/zone/casing calibration, change gun, reload/restart:
   values and model size remain consistent.
6. **I01 + R01:** Lose focus while holding an input; disconnect mid-reload and
   rejoin twice. No stuck firing, phantom grab, or extra rounds.
7. **I04:** Switch to button mode/flatscreen and back; normal TaCZ still works.

If those pass, add one more conventional pistol first (M1911), test its model,
release and ammo behavior, then add a batch. A Glock pass validates the shared
mechanism; it does not validate other guns' model bones, pivots, sound files,
capacities, ammo types, or firing modes.

You do not need to finish this entire document before starting the first new
pistol profile. The seven checks above are the shared-pistol readiness gate.
Other sections retain outstanding coverage and targeted regressions; repeat
previously passed basic cases only where a new change or failure warrants it.

## Setup and reporting

- Install only one addon JAR, **0.9.2 on every client and server**. Single-player
  has an integrated server. Keep your existing calibration; do not zero it.
- Pico 4 Ultra + Virtual Desktop + SteamVR. Start with shaders OFF, physical
  handling ON, debug regions ON, and Visor's assigned offhand hotbar slot empty.
- Save calibration before testing grabs. Unsaved preview geometry is not the
  server's saved geometry. Colors can vary with your bundled hints: blue support,
  orange rack/pump, green magazine/tube port, pink ejection/side port, cyan release.
- Use survival and record inventory + gun magazine/tube + chamber ammo before
  and after accounting tests. Include all ammo containers. Ignore unlimited
  creative ammo for conservation tests. Ejected cosmetic objects are not pickups.
- Jam tests: gundb **2.2.2**, cheats/operator permission, selected supported gun,
  physical VR handling, enough ammo and no active gesture. Clear one jam before
  starting another. Run the no-gundb test separately.
- Report `0.9.2 F01 PASS - Glock, main Use and offhand Use`, or
  `0.9.2 F04 FAIL - MP5, handle latched, forward sweep did nothing`.
  Include partial results, gun/optic ID, shader state and handedness. For crashes,
  give the timestamp and active instance log/crash-report path. For geometry,
  share `config/visor_tacz-calibration.json`.

## What your reports already establish

| Area | Latest reported evidence | What remains |
| --- | --- | --- |
| Main-hand shot direction / M4 support aim | Reported solved | Regression and remote observer checks |
| Scale, XYZ boxes, basic save, HUD and mode switching | Main behavior passed in 0.7.0 | New controls, new regions and persistence subcases |
| Focus loss and HMD reconnect | Both reported working in 0.7.0 | Interrupted firing/grabs/reloads and new transfer state |
| M4 release / M870 new reload | No issue noticed in 0.9.0 | Counts, cancellations and new-build regressions |
| M700 lift/back/forward/lower | Worked in 0.9.0 | Interrupted states and support transfer |
| M700 support transfer | Failed in 0.9.0 | 0.9.1 fix plus both 0.9.2 policies untested |
| MP5 release | Failed in 0.9.0 | New latch and swept-release behavior untested |
| Jams | Stovepipe/double-feed visuals observed in 0.9.0 | Clearing/counting not confirmed; changed visuals untested |
| Optics | Later report says scopes work in old and new worlds | Exact optic/stereo/decal matrix; PK06 magnification unresolved |
| M870 old-world grab | Failed in old world, worked in new in earlier report | Same affected old gun/world not explicitly cleared by later general success |
| Torch / handedness | Torch placement failed; gun hand switches; menu position obstructed testing | Candidate torch fix and remaining left-handed cases |
| Whole-world flashing | Occurred without addon; stopped after PC restart | Report recurrence; no need to repeatedly reproduce every old crash |

## Glock and shared magazine handling

- [ ] **F01 - slide release (new behavior).** Empty Glock, replace magazine,
  wait for native LOADING to finish, press main-hand Use: slide closes and one
  round chambers. Repeat using offhand Use at cyan region with Grab=TRIGGER,
  configured Grab at that region, and slide racking. Repeated release presses
  must not consume additional ammo. Wrong-zone offhand input must not release.
- [ ] **G01 - ammunition ledger.** Tactical reload retains a live chamber;
  empty reload requires physical chambering. Test partial/no spare ammo, native
  capacity attachments, loaded-chamber racking and magazine removed. A removed
  magazine permits only an already chambered shot, not feeding hidden rounds.
- [ ] **G02 - action timing.** Partial rack ejects nothing, full rack ejects once
  at rearward completion, holding there repeats nothing. Five complete live
  racks discard five rounds if available. Record sound and live/spent size issues.
- [ ] **F02 - two-hand ADS.** With ADS 2 hands ON, eye alignment alone does not
  activate ADS. Hold blue support + align: ADS enters; release: ADS exits. Try
  both eyes, lowered gun, sprinting, reloading, menu and focus loss. ADS should
  stop sprinting. OFF restores alignment-only ADS. Repeat each supported gun;
  M870 uses its fully forward pump as support. Pistol direction stays with the
  firing controller; its close support hand gates ADS instead of steering it.
- [ ] **G03 - firing restrictions.** Compare modes/rate/cooldown with flat TaCZ.
  No shots while invalid reload/action state or through a wall. Recover when
  muzzle is clear. Sprint reload being allowed is not automatically a bug.
- [ ] **G04 - pouch ownership.** Glock/M4/MP5 pouch only supplies replacement
  during removed-magazine stage. Occupied assigned offhand blocks grabs. M870
  shell pouch is available with pump open for side loading and closed for tube.

## M700 bolt and transfer

The basic four-stage cycle (**N02**) passed in 0.9.0; these new/remaining cases
are still open. **F03** is the umbrella transfer check, split into T01-T05 below.

- [ ] **T01 - BOLT NEEDED, ready gun.** Loaded closed rifle, support held: move
  main hand away and try main Use. Neither starts transfer; gun follows main hand.
- [ ] **T02 - BOLT NEEDED, action required.** Fire, keep support held, move main
  hand over 16 cm away: rifle stays with support hand. Repeat explicit main Use.
  Also try empty chamber, lifted/open bolt and generic jam.
- [ ] **T03 - finish and return.** While transferred, main trigger grabs bolt;
  lift/back/forward/lower. No firing or ADS during transfer. Closing/lowering
  must not snap gun back. Main Use near grip returns it; releasing support also
  ends transfer. Try your narrow calibrated support region.
- [ ] **T04 - ANYTIME.** Transfer a ready loaded gun by movement and by main Use.
  Return normally; no unwanted shot and no chamber/ammo change from transfer alone.
- [ ] **T05 - saved preference.** Switch policy, restart/rejoin; it persists and
  reaches server. Two players can select different policies. Neither mode
  changes other guns or flatscreen controls.
- [ ] **N03 - interrupted bolt.** Too-short stroke, wrong direction, release
  lifted/open, re-grab and complete, disconnect while open. One ejection/feed;
  no firing with open/lifted bolt and no automatic feed from reconnect.
- [ ] **N04 - magazine/chamber.** Tactical/empty magazine reload with bolt open,
  cycle with no magazine, cycle a live chamber. Count ammo; no-mag cannot feed.

## MP5

- [ ] **F04 - latch and release.** Full rearward pull latches without lifting.
  Release Grab, insert magazine, wait for LOADING, sweep empty hand back-to-front
  through cyan region without a button. Repeat downward slap, slow/fast sweep,
  configured Grab and offhand Use. Wrong direction/outside region do nothing;
  one release feeds at most one round. Try no magazine and an active jam.
  Empty firing alone does not latch; physically pull the handle first.
- [ ] **N05 - ordinary operation.** Native firing modes/selector, trigger release,
  blue support, tactical/empty reload, no spare ammo and full inventory.
- [ ] **N06 - action accounting.** Check action model/sound, all three jam types
  and interrupted reload; repeat shared G01/G02 and J01-J04 on MP5.

## M870

- [ ] **S01 - support regression.** Fully forward held pump supports aim; barrel
  and impacts follow. Released/partially pulled pump must not count as ready ADS
  support. No firing while open or spent case requires pumping.
- [ ] **S02 - tube loading.** Closed pump, pouch to green underside port:
  inventory -1, tube +1, chamber unchanged. Empty inventory gives OUT OF AMMO and
  no shell preview. Full tube/invalid placement consumes nothing; try ammo boxes.
- [ ] **S07 - side-port loading (part of F08).** Open pump, pouch shell to pink
  port: inventory -1, chamber +1, tube unchanged. Closing consumes no tube shell
  when chamber already loaded. Cannot insert twice into occupied chamber; wrong
  port/open-pump underside insertion consumes nothing.
- [ ] **S03 - shell visuals.** Live ejection, spent ejection and held preview all
  use 12g casing asset; verify new casing scale (F05).
- [ ] **S04 - pump strokes.** Partial rear stroke does nothing; complete rear
  stroke ejects once; hold/release open/re-grab then close feeds at most one.
- [ ] **S05 - cancellations/counts.** Invalid shell release, switch, focus loss,
  disconnect: preview consumes nothing; committed insertion stays committed.
  Include full tube and empty tube/chamber. Disconnect counts previously passed;
  side-port loading still needs that check.
- [ ] **S06 - fallback and old world.** Buttons/flatscreen native reload works.
  Retest the affected old-world gun; if grab fails, compare a fresh M870 in that
  same world before creating another world.

**F08** also includes the M4 cyan release regression and shared R01/M01 checks.

## Jams and casing calibration

Test Glock first; later repeat on M4/MP5. Having seen the jam model does not yet
confirm its clearing sequence or ammunition accounting.

- [ ] **J01 - stovepipe.** `/visor_tacz_test jam stovepipe`: casing lies across
  bore, tilted upward 22.5 degrees relative to gun. Short pluck does not clear;
  full pluck does. Force again and clear by full rack. No free ammo or repeats.
- [ ] **J02 - double feed.** With at least two rounds, `/visor_tacz_test jam
  double_feed`: two casing-model obstructions. Cannot clear with magazine in;
  remove it, make two separate full pulls, reinsert and chamber. Extra pulls or
  holding rearward create no rounds; two discarded rounds accounted for. Pouch
  remains blocked while obstructions remain.
- [ ] **J03 / F06 - dud.** `/visor_tacz_test jam dud` (`misfire` alias also works).
  Action looks closed, trigger clicks, HUD reports DUD. Rack ejects one reserved
  round and permits feeding. No external protrusion or magazine-tap requirement.
- [ ] **J04 - interrupted jam.** Switch, focus loss, disconnect mid-clear:
  remaining count persists; already ejected rounds do not return. No repair of
  durability merely from clearing.
- [ ] **J05 - native clear.** Switch flat, use TaCZ Inspect (check binding;
  normally H). After native unjam, extra visuals clear; unejected reserved ammo
  returns once. Keep addon installed while testing its cleanup.
- [ ] **J06 - manual-action generic jam.** M870 and M700: `/visor_tacz_test jam`,
  then complete pump/bolt cycle. Partial gesture must not clear. These guns do
  not use the Glock/MP5 three visual jam mechanisms.
- [ ] **J07 - optional dependency.** Separate no-gundb setup: normal operation
  on all five guns without crashes/new random jams. Jam-test command should
  explain the missing integration. Earlier no-gundb gameplay worked generally.
- [ ] **F05 - casing scale.** Casing size page: preview at port; test 50%, 100%,
  150%, save/restart/reload. Jam models, ejections and M870 held shell agree.
  Gun scale multiplies this visual setting; ammo counts/hitboxes do not change.

## Calibration, configuration, HUD

- [ ] **C01 - linked scaling.** Gun scale 80/100/120%, lock ON resizes mounted
  boxes; OFF leaves their dimensions unchanged. Pouch stays independent. Grip
  stays anchored, muzzle/impacts match, toggling lock itself does not resize.
- [ ] **C02 - box bounds.** Width/height/depth on support, release, mag/tube,
  rack, pouch and M870 pink port. Saved visible region and actual pickup agree.
  Large boxes do not turn incomplete strokes into complete ones.
- [ ] **C03 - pages/hints.** Each gun exposes its actions; no overlapping text.
  Glock now has support and release; M870 pink port now has size controls.
- [ ] **C04 - persistence/cancel.** Existing nonzero offsets remain. Per-gun
  save, switch, reconnect, restart; Cancel/Escape reverts preview; page reset
  affects only selected setting. Include new release/support/casing controls.
- [ ] **C05 - file reload.** Back up JSON, edit externally; menu Reload calibration
  and `/visor_tacz reload_calibration` update visuals/server grabs. Malformed
  JSON must be reported and previous valid values retained. Restore and retry.
- [ ] **C06 - guides.** Debug OFF hides guides in play but calibration still
  shows them. Interaction/ADS unaffected; preference persists.
- [ ] **C07 - fallback/share.** Missing local gun profile uses bundled defaults;
  supplied local profile overrides. Test with a copy, preserve personal values.
- [ ] **D01 - displays.** Gun/Wrist/HUD/OFF and `/visor_tacz status`: ammo,
  chamber, ADS, target and action match; preferences persist. Verify wrist
  placement independently of normal GUI HUD.

**A01** is now covered by F02. **N01** uses C01-C04 on M700/MP5.

## Input, interruptions, multiplayer

- [ ] **I01 - input recovery.** Focus/tracking loss while firing semi/auto,
  menu, slot change and death while trigger held: no stuck fire or firing merely
  on resume. New press works. Previous ordinary focus/reconnect tests passed.
- [ ] **I02 - contextual controls.** Grab=USE and TRIGGER work. Away from valid
  zones, torch placement, sword swing, bare-hand punch, GUI pointing work with
  gun in other hand. A grabbed gun part must not punch/mine accidentally.
- [ ] **I03 - handedness.** Left-handed mode, crouch, roomscale and snap-turn;
  occupied assigned offhand blocks grabs. Include M700 transfer. Record Visor
  Essential menu positioning separately if it prevents the test. No dual wield.
- [ ] **I04 - AUTO/flat.** OFF/AUTO and Physical/Buttons transitions, restart:
  normal flat TaCZ behavior, no stuck action. OFF is not complete mod unloading.
- [ ] **R01 - interrupted magazine reload.** Disconnect before/after native
  ammo commit and with mag removed. Rejoin twice, selected and unselected gun;
  recover once, keep committed ammo and charging requirement. Orderly server
  restart too; include Glock/M4/MP5/M700.
- [ ] **R02 / F07 - other interruptions.** During open action, shell preview,
  jam clearing or M700 transfer: menu, slot, focus, mode change, death/drop in a
  disposable test world. No duplicated ammo, stuck grab or accidental shot.
- [ ] **M01 - flat friend / remote models.** Friend plays native TaCZ normally.
  They see calibrated weapon, parts, jam/case size and M700 support transfer.
  Reconnect/teleport/switch/out-of-view causes no duplicate/missing gun.
- [ ] **M02 - server impacts.** Gun pointed away from face, last interaction
  from opposite hand: impacts follow gun/muzzle, including support aim and
  scaling. Check near/far targets and real latency, not just tracer appearance.

**N07** is covered by I04/M01/M02; **N08** is the Glock/M4/M870 regression groups.

## Optics and stability (after handling tests)

- [ ] **O01 - named optics.** Later general scope success supersedes the old
  blanket black-optics failure. Confirm named examples (QMK-152, TA31 ACOG,
  previously black red dots) by exact ID, each eye, head movement and lens clipping.
- [ ] **O02 - QMK scene/decal.** Shader OFF: magnification, off-axis behavior,
  near/far targets, bullet marks inside lens, no unzoomed decal visible over lens,
  and no incorrect muzzle/front-sight intrusion. Record scale/zoom/gun.
- [ ] **O03 - settings/shaders.** Optics and ADS toggles persist. Shader runs
  stay clear/stable; custom shader-pack magnification remains unsupported.
- [ ] **O04 - PK06 evidence.** Record exact variant ID, viewing area and shader
  state. Missing lower-view magnification remains unresolved; not a promised
  0.9.2 fix and not a blocker for validating the pistol reload mechanism.
- [ ] **R03 - stability regression.** Old/new world holding scope, Z attachment
  GUI/slot changes/rejoin in flat/VR, main-menu VR transition, shaders off/on.
  Report crash or flashing timestamp. Flashing previously occurred without addon.

## What to implement after the Glock gate

Pinned default-pack source has **14 pistol-category entries**, including Glock.
This is not a promise of automatic support for arbitrary third-party gun packs.

| Batch | Default IDs (tacz namespace) | Reason |
| --- | --- | --- |
| First proving profile | m1911 | Semi-auto, magazine/slide parts present; different ammo/model tests profile separation |
| Conventional magazine pistols | p320, m9a4, deagle, deagle_golden, timeless50 | Similar high-level cycle, but different parts/pivots and some compound slide assemblies |
| Firing-mode exceptions | b93r, cz75, hk_mk23 | Pinned pack uses burst/semi, auto, and semi/burst respectively; selector and interruption checks required |
| Distinct mechanisms | rhino357, taurus500, taurus943, lonetrail | Pinned data uses open_bolt; cylinder/round-shell or different loading geometry, so do not copy Glock's slide/mag assumptions |

Before adding a batch, make slide/bolt/magazine part names, sound mappings and
support/release capabilities profile-driven. Current code has Glock/M4-specific
assumptions. For example M9A4 uses slide_base, and several models have multiple
slide/magazine nodes. Each new profile still needs basic in-headset calibration,
empty/tactical reload and ammo tests. Shared mechanism passes reduce repeated
work; they do not eliminate per-gun testing.

Defer physical attachment insertion, individually tracked magazine items, dual
wield, general item pouch, and further new mechanism families until current
input/state behavior is confirmed. Polish sounds/animations after functional
blockers; the M700/MP5 fixes can be tested separately from the Glock gate.
