# Advanced tactical reload

Alpha.10 introduces experimental tactical reload for **single-gun Advanced handling**, with Independent dual wield OFF. Bundled eligible profiles: Glock 17, M4A1, MP5A5, SCAR-L, SCAR-H, AA12, AK47 and UMP45. Eligibility uses MAGAZINE/SMG profile variants; other pistol variants, cylinders, tubes and bolts retain their previous reload path. Headset and live multiplayer validation remain pending.

## Controls

Open **Magazine hand grip > Tactical magazine poses / guide**, choose the mapping and Save. Settings are per gun ID.

| Action | Standard (default) | Alternative |
| --- | --- | --- |
| Take replacement from pouch | Hold Use/X | Hold Grab |
| Pull installed magazine out | Hold Grab at magazine, pull down | Hold Use/X at magazine, pull down |
| Insert replacement | Keep its button held; align with entry and push to seat | Same |
| Retain old magazine | Keep Grab held | Keep Use/X held |
| Support while retaining old magazine | Not available | After inserting replacement, release Grab, then freshly press Grab at support zone while retaining Use/X |
| Recover old magazine rounds | Return hand to pouch and release its holding button | Same; release support Grab before moving to pouch |

Fresh presses own their matching releases. Entering a zone while already holding another mod's Grab does not acquire a tactical magazine. A fresh press resumes a retained magazine after canceled input. The holding hand's Use can release the installed magazine latch instead of extracting it with the other hand. Existing slide-lock/release context has priority where applicable.

Each magazine has its own count and pose; the eligible magazine nearest the entry acquires the rail. Rotate your hand to align the desired magazine. The old magazine can also be reinserted.

## Ammunition and interruption

Replacement ammo is reserved from native TaCZ inventory/dummy ammo when taken from the pouch. Magazine count and chamber bit are separate: removing a magazine preserves a chambered round. Seating transfers its count into native ammo once, subject to native reload-event acceptance. Double-feed obstructions must be cleared first. Creative/infinite supplies do not generate Survival refunds.

**Return retained magazines to the pouch to recover unused rounds. Releasing them elsewhere discards those rounds.** Dropped magazine props are cosmetic, not collectible items. Persistent individual magazine items and manually loading cartridges are not implemented.

Stow/hotbar changes preserve detached counts on the actual gun and clear unfinished insertion locks. Input/tracking loss cancels gestures without discarding ammo. Switching flat, disabling Physical/Advanced, enabling independent dual, and normal logout normalize carried guns: refund held reservations once, preserve seated native ammo/chamber and clear tactical tags. Inventory overflow drops ordinary ammo. No old gun stack is written back into a slot: deletion/drop must not resurrect a gun. Dropped/stored guns retain their own data until recovered and processed; deleted guns do not refund reserved rounds.

Before removing the addon, bring affected stored/dropped guns into inventory, run **`/visor_tacz_prepare_uninstall`**, then disconnect without starting another reload. Repeat after retrieving other guns. This cleans carried tactical reservations, not containers or unloaded entities. Without cleanup, ordinary gun data remains readable but base TaCZ cannot recover detached rounds stored in addon tags. This command is not a general cleanup of every historical addon feature.

## Calibration and visual limits

Four sections cover normal grip, pinch grip, magwell entry pose, and alignment/seating tolerances. Short VR panels split these into smaller pages. Positions are stored in metres and displayed in millimetres; angles are degrees. Grip offsets are controller-relative, entry offsets gun-relative. The default normal grip inherits saved ordinary magazine calibration until a tactical entry is saved independently. Default pinch is 45 mm right with 30 degrees yaw.

Extracting an installed magazine captures the actual hand-to-magazine anchor, preserving wrist variation. Angular tolerances check magazine-to-magwell alignment at entry, not procedural finger tracking. Once engaged, the straight rail follows insertion depth and suppresses sideways shake. Withdrawal unlocks it; a short haptic pulse signals acquisition. Curved bodies can follow the rail, but authored rock-and-lock paths are deferred. Initial extraction still uses the existing magazine-zone pull threshold, not continuous constrained movement through a modeled well.

Desktop preview supports mouse rotation, zoom and X/Y/Z views. Cyan marks entry, yellow the rail and green the seat. Only **Save** writes; adjustments/Defaults remain drafts, Cancel discards. After Save, only Close remains until another change. Guide rendering may preview a draft; gameplay and network use saved values.

Native `bullet_in_mag` geometry hides when empty; chamber geometry follows the native chamber bit. Where a separate chamber part exists, partial opening permits a small tilt-dependent displacement bounded by action opening. This cosmetic guide does not eject/change ammo, simulate full cartridge dynamics, or create missing model parts. Local two-magazine rendering is implemented; remote held-magazine props remain future work.

## Files and migration

`config/visor_tacz-tactical.json` uses `schemaVersion: 1` with a `guns` object keyed by gun ID. Missing fields/axes receive defaults in memory. Unversioned data is version 0; explicit Save writes schema 1. Malformed values or future schemas block saving over the original. Finite offsets are bounded to 0.5 m, angles/tolerances to 180 degrees, entry radius to 5-500 mm, seated depth to 0-500 mm and seated tolerance to 1-200 mm. Existing ordinary magazine calibration remains separate.

Descriptor TOML remains schema 1, loaded by `DescriptorProfiles`; optional fields retain parser defaults, required geometry remains required, unsupported schemas fail closed. No automatic rewrite of custom TOML is introduced. Tactical JSON migration does not migrate descriptor schemas.

## Source map

- `TacticalCalibration`, `TacticalFiles`, `TacticalStore`, `TacticalScreen`: validated data, migration, storage and draft editor.
- `TacticalClient` / `TacticalNetwork` / `TacticalServer`: fresh input, packets and authoritative transactions.
- `TacticalLedger` / `TacticalState`: independent counts and native stack persistence.
- `MagazineGuide` / `TacticalPose` / `TacticalVisual`: rail geometry and local props.
- `PhysicalModel` / `ChamberGuide`: native round visibility and bounded chamber displacement.

See [alpha.10 checks](TESTING-CURRENT.md#alpha10-targeted-checks). Automated checks do not establish headset alignment, hand comfort or live server inventory behavior.
