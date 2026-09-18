# 0.5.0 — experimental physical jam types (Glock 17 / M4A1)

Update the addon on **all clients and the server**: network protocol is now 9.
Keep existing calibration files; the new ejection-port offset defaults to zero.
This is a test build, not a headset-verified release.

## Optional Durability integration

No second addon JAR is needed. With no Gun Durability mod installed, no random
jam mechanics are added; the existing aiming, physical reload, optics and HUD
features remain available. Physical jam integration targets verified `gundb`
2.2.2. Unknown versions retain their native controls and do not receive new jam
reservations. The compatibility adapter has no compiled dependency on gundb.

When a supported gun becomes jammed in VR physical mode, the server assigns one
of three types once. Prototype selection gives each type equal weight, subject
to sufficient ammunition. This does not change Durability's jam probability.
Type and remaining obstruction count are stored on the gun, surviving switching
items and reconnects. Native desktop unjamming remains available. After it clears
the original jam, any reserved but unejected cartridges return as loose inventory
ammo (or drop beside the player if inventory is full), not extra magazine capacity.

## Simplified game interactions

- **Stovepipe:** a cosmetic spent shell is attached at the port. Grab within the
  small casing zone, move at least 4.5 cm away and release, or complete a normal
  rack. The shell follows the offhand during a grab. The Glock slide or M4 bolt
  is partly open. The cosmetic spent shell does not create collectible ammo.
- **Double feed:** two real cartridges are reserved from the existing chamber /
  magazine. Remove and stow the magazine. Each separate full pull ejects at most
  one reserved cartridge. Holding the action open does not eject again. The pouch
  is unavailable while obstructions remain. After clearing, insert a magazine
  and charge normally. With fewer than two available cartridges, a forced double
  feed is downgraded to a dud, or a stovepipe if completely empty.
- **Dud:** one real cartridge is reserved; the action stays visually closed until
  pulled. A complete rack ejects it and then feeds normally. No magazine slap is
  required in this version.

These are game mechanics, not real firearm-clearing instructions. No controller
speed/force test is used; deliberate travel is sufficient. Deliberate release is
separate from automatic cancel on lost focus/tracking. Cancellation never finishes
unjamming. Ejection already committed at full pull is not rolled back.

## Rendering, calibration and audio

The new **Ejection port / casing grab** calibration page controls its XYZ position
independently from the slide grab point. A pink guide identifies it. Initial
positions derive from default-pack shell pivots. State rules and port geometry
are separated, but profiles remain explicitly limited to Glock/M4. Adding another
gun requires reviewing its parts, geometry and loading mechanism.

Jam rendering is included for the local VR player and the remote gun renderer.
Spent shells reuse TaCZ models; live cartridges reuse an ammo entity model where
provided, otherwise use the existing simple brass fallback. Full internal breech
geometry is not added. Custom replacement gun models need their own profiles.

A blocked physical trigger press plays TaCZ's dry-fire sample. A new stovepipe /
double feed plays a quieter, lower-pitched existing action sample as a provisional
mechanical cue. It is not a newly recorded crunch sound. Future sound tuning can
replace these samples without changing jam logic.

## Test checklist

Use cheats/operator permission, hold a supported gun, enable physical handling,
and begin with no existing jam or active manipulation:

```
/visor_tacz_test jam stovepipe
/visor_tacz_test jam double_feed
/visor_tacz_test jam dud
```

1. Stovepipe: check part/casing alignment; try a short pluck (must not clear), a
   full pluck, and on a fresh jam a full rack. Check the remote friend's view.
2. Double feed: start with at least two rounds. Racking with magazine inserted
   must not eject an obstruction. Remove the magazine; pull/release twice and
   count exactly two cosmetic ejections. Extra empty pulls must not create rounds.
3. Insert the magazine, check EMPTY CHAMBER, then charge and shoot. Compare the
   total ammo before/after; two rounds were discarded by clearing the double feed.
4. Dud: closed action, click on trigger, one reserved round ejected during rack.
5. Interrupt a pull with lost focus/tracking or a gun switch. The jam must remain;
   already-ejected rounds must not reappear. Resume and finish deliberately.
6. Switch to flatscreen and use native unjamming; check remaining reserved ammo
   returns once, and remote/custom jam visuals disappear. Durability is not repaired.
7. Without gundb, check ordinary reload/rack, including cycling with no magazine.
8. Confirm older calibration JSON loads, and changing the port leaves rack and
   muzzle calibration unchanged. Gun/Wrist/HUD/Off display choices are retained.

## Next mechanism demo

Recommend **M870** next: manual pump, tube-fed single-shell loading, and the pump
hand switching between support and cycling. TaCZ uses a dedicated `m870_gun_logic`
script and shell-loading loop, so it must not inherit Glock/M4 magazine handling.
M870 support is not included in this build.

## Validation completed

Build succeeded and 49 unit tests passed, including reservation conservation,
insufficient-ammo fallback, repeated clearing, and old calibration migration.
A dedicated-server launch without gundb reached Minecraft's EULA gate without a
launch error; it did not load a world or exercise runtime gameplay. No headset,
multiplayer visual, or live gundb integration test has been performed.
