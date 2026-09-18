# 0.6.1 — scope crash candidate fix and reconnect recovery

Install on clients and server. Protocol remains 10. Includes the 0.6.0 M870 demo.
No user worlds, installed mods, or calibration files were modified during this investigation.

## Crash findings

The September 18 reports at 16:57, 18:00, 18:06 and 18:17 all report
`post AFTER_TRANSLUCENT events stage OpenGL Error Code: 1282`. The associated
logs identify incompatible source/destination internal formats and BSL enabled.
These sessions used 0.5.0, 0.6.0, 0.4.3, and 0.6.0 respectively. The nearest
retained morning session starts at 07:53 with 0.4.0; no 07:49 crash report was found.

TaCZ's first-person attached optics lazily enable Forge stencil, changing the
main depth texture to DEPTH32F_STENCIL8. Oculus has already prepared depth-copy
textures for the frame. Visor's later GL check reports the resulting error;
the check is not itself proof that Visor caused it. A held scoped gun can trigger
this on world entry, whereas a fresh world without one may load normally.
There is no evidence in these reports of addon save-data migration failure.

The addon now enables stencil at GameRenderer.renderLevel HEAD, before
LevelRenderer/Oculus begin their world pipeline. It checks the current target
each pass and does nothing when stencil is already enabled. Existing scope
rendering remains available; no GL error checks are suppressed. This stability
patch remains active with `/visor_tacz off`.

This is a candidate fix for the reported modpack crashes, not a confirmed
in-game reproduction/fix. Full modpack, BSL, and VR retesting remain necessary.

## M4/Glock interrupted reloads

- Removing a magazine already saved its ammo on the gun. Login now recovers
  those reservations across the whole inventory, including unselected guns.
- A removed magazine returns with its saved rounds; the temporary held-magazine
  gesture is discarded. Repeating login does not grant another magazine's ammo.
- A new persisted empty-reload marker preserves the requirement to charge the
  weapon if a native reload commits ammunition before interruption. Recovery
  moves an automatically chambered round back into the magazine exactly once.
- Already loaded ammo stays loaded; fired/ejected rounds stay spent. We do not
  rewind the player's whole gun/inventory snapshot, which could duplicate ammo.
- M870's held shell is only a preview before insertion, so disconnecting with
  one in hand costs no shell. Inserted shells remain in the tube.
- Jam state is not cured by reconnecting. Existing jam reconciliation rules apply.

Recovery uses the latest saved player data; it cannot recover actions after a
server's last disk save. Older releases did not write the new empty-reload
marker, so that particular charging requirement cannot be reconstructed for
an interruption already saved by an old release.

## Validation and user checks

Build and existing 57 tests pass. `tools/CheckDepthFormat.java` reproduces
GL 1282 on a real driver using the mismatched depth formats, then passes 100
copies with matching formats. This verifies the graphics failure mechanism,
not the complete Minecraft render pipeline. No headset or reconnect session
was run for this release.

1. Reopen the previously failing world with the same gun/scope and BSL setting.
2. In flatscreen, attach/detach a scope through Z; aim, change slots, then rejoin
   while holding the scoped gun. Repeat with shaders off.
3. In VR, disconnect with the M4 magazine removed; rejoin and compare total ammo.
4. Disconnect during an empty-magazine insertion/reload, both before and after
   ammo transfers. Loaded rounds should be retained, and charging still required.
5. Rejoin twice to confirm ammo does not grow. Repeat with the gun unselected.
6. Check ordinary flatscreen reloads and M870 shell insertion still work.
