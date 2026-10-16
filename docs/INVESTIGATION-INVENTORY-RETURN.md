# Jammed Glock returns after removal — investigation

Reported 2026-10-14. User confirms real hotbar item, Independent dual wield enabled, inventory drag-drop and creative deletion (not VR release/toss). Treat as unresolved inventory integrity issue, not a confirmed duplication exploit or cosmetic prop.

Inspected instance latest.log: only Visor+TaCZ+addons-noTaCZcompat, session 2026-10-14 00:59–01:11, addon alpha.4, TaCZ 1.1.8-hotfix, gundb 2.2.2. Log has Project Zero pack parsing failures but no inventory operation trace proving the cause. Do not attribute Glock return to those pack errors.

Source audit: addon jam reconciliation refunds ammo, not gun items. DualGunOperator captures a copy only for bullet properties; no insertion of that copy found. Native reload cancellation calls the gun interrupt script, so script behavior remains relevant. Inspected gundb SyncJammedPacket changes the current main stack Jammed tag; it does not insert a gun. These findings do not rule out indirect client/server synchronization failures.

Visor OffhandNonNullList aliases vanilla offhand reads/writes to the selected hotbar slot; OffhandSlot disables ordinary menu manipulation in two-handed VR. This is an investigation lead, not a proven bug. Need verify installed build and packet/order behavior before changing shared inventory mappings.

Reproduction target: INV01 in TESTING-CURRENT.md. Record whether original dropped item persists simultaneously with returned hotbar item, whether returned item persists after rejoin, and whether main or offhand slot was removed. Compare jammed/unjammed, dual on/off and non-gun item. Preserve user's original save/config; use a copy. No speculative inventory-write patch or JAR build made during this audit.
