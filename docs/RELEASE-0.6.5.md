# 0.6.5 — reload calibration without restarting

After editing/replacing `config/visor_tacz-calibration.json`, use:

- `/visor_tacz menu` → **Reload calibration**, or
- `/visor_tacz reload_calibration`.

Successful reload replaces the cached calibration and synchronizes the held
gun's values to the server immediately. All gun profiles are refreshed. The
menu button reports success/failure, with details in chat. This is client-side;
it does not require operator permissions or Minecraft's `/reload` command.

Malformed JSON or invalid offsets reject the entire reload, keep current values,
and prevent saving over the broken file. Fix the file and reload again to clear
the error. An active calibration preview must be saved or canceled first.
Missing files/entries fall back to bundled profiles, or zero for unknown guns.
Reloading never writes the file. Unsaved edits in an external editor are not read.

Protocol remains 10; includes previous changes. ACOG optics remain unresolved.
