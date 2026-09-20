# Tracked magazines — proposal, not implemented

Keep this as an optional TaCZ addon with a Visor interaction bridge. The VR
compatibility mod should work without it. Current reloads continue to use
TaCZ's inventory ammunition; no individually tracked magazines were added.

## Storage and reconnect

The server owns magazine identity, ammunition type/count, and committed reload
state. Use versioned persistent item data and UUID identities, not a client
cache. Loading a cartridge, detaching, and inserting must each be a complete
server-side inventory transaction. Never have both a loaded gun and a detached
magazine own the same rounds. Reject stale requests and duplicate identities.

On disconnect, cancel hand grabs and animations while keeping the last committed
inventory state. A half-inserted magazine stays a carried item until insertion
commits. An inserted magazine remains inserted. Keep an empty chamber empty
until charged. Do not rewind the whole inventory to before the reload: shots,
trades, or dropped ammunition may already have committed. Normal world saves
persist this state; a process crash can still roll back to the last world save.

## Removal-friendly default

Prefer ordinary TaCZ ammo stacks as the underlying loaded-magazine item, with
optional magazine metadata (identity/capacity). The actual stack count owns the
rounds. When inserted, transfer those rounds into TaCZ's native gun ammo fields.
If the addon is removed, carried rounds remain ordinary TaCZ ammo and loaded
rounds remain native gun ammunition. Optional individual loading transfers one
real cartridge at a time; it does not create a second count in hidden metadata.

Empty magazines need a deliberate choice: virtual empty magazines, or a vanilla
item carrier that becomes an ordinary vanilla item after removal. Custom
registered magazine items cannot promise lossless removal without conversion.
This default favors recoverable ammunition over retaining magazine identity
after removal. Native TaCZ may merge ammo stacks once the addon is absent.

Start with one ammo type per magazine. Mixed cartridge order, different loads
in one tube, container automation, death drops, stacking, and third-party ammo
extraction require additional rules and testing before claiming safe removal.

An alternative using custom magazine items requires an explicit export/uninstall
operation, including offline players and stored containers. A mod cannot clean
up after its own JAR has already been removed, so that alternative would not
meet an unconditional "remove whenever" promise.

The current VR addon also has interrupted-reload recovery and jam bookkeeping.
Removing it mid-operation is not yet guaranteed to preserve reserved rounds;
finish reloads and clear jams before removal. The proposed persistence design
does not retroactively make current intermediate states removal-safe.
