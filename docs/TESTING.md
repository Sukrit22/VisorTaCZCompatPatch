# Acceptance tests

For current testing, use [the rolling manual test tracker](TESTING-CURRENT.md).
The remainder of this document is historical acceptance criteria; several
limitations and controls below have changed. Do not use it as current results.

Build/unit tests cannot establish headset comfort, stereo alignment or real-server
behavior. Record hardware, exact versions, logs and results when running these.

Target user setup: Pico 4 Ultra, Virtual Desktop, SteamVR.
Compare `/visor_tacz on` and `/visor_tacz off` in each relevant test; off should
restore normal upstream input, model rendering, camera effects and head-origin
shots. Check that the selection persists after reconnecting and restarting.

For AUTO mode (0.1.1+), start with the default `enabled = true` or run
`/visor_tacz auto` once after an intentional OFF override. Check that `/status`
under `/visor_tacz` reports AUTO/inactive in flatscreen and AUTO/active in VR.
Switch Visor modes without addon commands and verify both client behavior and
server shot origin follow the transition. If you originally joined with Visor's
play mode DISABLED, reconnect after enabling VR (Visor 0.5.0 login limitation). Repeat with a flatscreen friend online:
their behavior must remain normal. An explicit OFF override must stay off across
VR changes and reconnects until `auto` or its `on` alias is selected.

## Checks completed during implementation

- Java 17 / Gradle build and reobfuscation completed successfully.
- Twenty-five JUnit geometry/calibration/handling/optic tests passed (angles, support-hand gating/alignment,
  muzzle conversion and non-finite coordinates).
- Forge dedicated-server bootstrap with the pinned mods reached the EULA gate
  after correcting a development-only duplicate annotation dependency.
  No EULA was accepted and no world was started. This is not a full server or
  gameplay validation; client rendering and live mixin behavior still need testing.

## Headset / multiplayer checklist

1. Start a dedicated server with the pinned mods. Confirm no client-only class
   loading or mixin failures. Verify that a client without the addon cannot join.
2. Join in desktop mode. Fire/reload a Glock and M4A1, check ammo, cooldowns,
   spread, damage and ordinary camera effects. Test another TaCZ gun.
3. Join in VR with iron sights. Compare left and right eye images; the model and
   flash should have correct depth and no duplicate camera-attached gun or hands.
4. Point the gun at a target and turn your head away. Confirm impacts follow the
   controller. Shoot targets at 1, 5 and 20 blocks, accounting for TaCZ spread.
5. Repeat standing, crouching, roomscale moving, snap turning and left-handed.
6. Push the muzzle, then the entire controller, through a wall. Neither should
   fire through it. Move clear and check firing recovers.
7. Glock: one shot per trigger press. M4A1: cycle modes and check automatic fire,
   burst timing, ammo consumption and release behavior against desktop TaCZ.
8. Hold fire, then open inventory, remove headset focus, lose controller tracking,
   switch slots, die, disconnect/reconnect and change VR mode. No stuck trigger
   or automatic resumption after the transition.
9. Reload empty and partially full guns. Test ammo exhaustion and shooting while
   reloading/sprinting; TaCZ's restrictions must remain intact.
10. Bring empty offhand to M4A1 foregrip; aim with two hands and release. Try the
    same movement with an item in offhand: support aiming must not activate.
11. Test a VR and a desktop client together. Compare server damage with visible
    impacts. Verify other players' ordinary guns and projectiles remain unaffected.
12. Repeat at realistic network latency. Check tracking timeout rejection and
    recovery, especially during bursts. Review logs for exceptions.

Initial scope excludes optical scopes, shader verification, physical reloads and
custom/third-party model packs. Do not describe these as tested or supported.

## Calibration acceptance (0.1.2)

1. Hold Glock, open `/visor_tacz calibrate` in VR. Verify the gun and hand remain
   visible; operate buttons with the offhand. Ensure clicking cannot fire a shot.
2. Adjust each translation and rotation separately, then fine/coarse step sizes.
   Check Y can correct the reported grip-height mismatch. Check left-handed mode.
3. Cancel/Escape restores saved alignment. Reset + Save persists zero offsets.
4. Save a nonzero profile; switch to M4A1 and back, reconnect and restart. Verify
   values and placement persist separately per gun and display ID.
5. Fire after saving and check controller direction and muzzle impacts, including
   head turned away, walls, two-handed M4A1 support and world-scale changes.
6. With a flatscreen friend, compare actual impacts and trajectories. Remote gun
   pose and tracer visuals are not calibrated by this addon; record that separately.
7. Copy `config/visor_tacz-calibration.json` for feedback. Malformed JSON must not
   crash the game or be overwritten by Save; check log/menu reports the problem.

Headset UI, live networking and mixed-player gameplay remain unverified locally.

## Bullet-origin acceptance (0.1.3)

- Open an existing 0.1.2 calibration file: grip stays unchanged, muzzle axes start
  at zero. Switch pages, adjust both, resize the window, and verify values persist.
- Move each bullet-origin axis and turn the controller: cyan marker follows the
  calibrated gun axes while model position and aiming direction stay unchanged.
- Reset only the bullet page; confirm grip is retained. Cancel drops unsaved edits.
- Save nonzero muzzle offsets, reconnect/restart, and verify JSON values and marker.
- Fire after saving; check muzzle-origin impacts and wall rejection on the server.
- Check world scale, two-hand support and flatscreen observers. These runtime checks
  require headset/multiplayer testing; unit tests do not establish visual alignment.

## Physical handling acceptance (0.2.0)

These are required live checks, not claimed test results. Use a separate test world.

1. Enable `/visor_tacz handling physical`. Use an empty offhand slot, default Glock,
   and iron sights. Verify green magwell, orange slide, yellow selector and cyan
   pouch guides. Check each gesture in both handedness modes and while crouching.
2. Record total gun + inventory ammo. Pull mag down, release, pick up from pouch,
   insert and wait. Tactical reload retains its chambered round. Empty reload must
   wait for rack; rack must consume exactly one magazine round into the chamber.
3. Repeat with M4A1, then extended magazines, partial ammo inventory and no spare
   ammo. Record behavior with the instance's TaCZ Tweaks / TaCZ Addon mods.
4. Test too-short, sideways and wrong-zone pulls; these must not complete actions.
   Confirm animation follows the offhand and restores normal models on exit.
5. Fire the remaining chamber round with magazine removed. Verify the next shot
   fails. Racking a loaded gun ejects exactly one round and never increases totals.
6. Hold M4 foregrip Grab to aim two-handed; release, move far away and lose tracking
   to disengage. Selector requires offhand proximity and main-hand use.
7. Interrupt every phase with GUI, focus/tracking loss, item switch, offhand item,
   death, disconnect and AUTO/OFF/button changes. Verify reserved ammo is restored
   once, no stuck trigger/grab, no accidental new shots and no duplicated ammo.
8. Restart a test server while a magazine is removed; hold the gun again and verify
   reservation recovery. Check transferring/dropping the gun during a reload.
9. With a flatscreen friend, confirm their reload/fire controls remain ordinary.
   Compare VR damage/shot direction. Remote physical part animation is not provided.
10. Test on dedicated server with latency. Check phase acknowledgements, burst-round
    blocking, rejoining and logs. Unit tests cover geometry and ammo arithmetic,
    not these integration/lifecycle behaviors.
