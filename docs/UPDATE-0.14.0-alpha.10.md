# 0.14.0-alpha.10 - tactical reload candidate

Source update, protocol **35**: matching addon builds required on client and server. No release JAR is built or installed in this update.

- Advanced single-gun tactical reload: carry replacement and old magazine, insert either, return unused rounds at the pouch.
- Per-gun mappings: Use new / Grab old, or Grab new / Use old. The latter permits a fresh support Grab while retaining old with Use.
- Normal/pinch poses, captured extraction anchors, magwell entry, alignment tolerances and a straight insertion rail. Desktop preview and short-panel pagination included.
- Native magazine/chamber visibility and bounded cosmetic chamber tilt where model parts permit it.
- Server-authorized ADS-equivalent hip-fire accuracy, retaining recoil. Open **Controls > Accuracy**, or `/visor_tacz_accuracy ads` and `/visor_tacz_accuracy native`. Single-player owner/server OP required; OP may append a player name. Native attachment, heat, script and pellet rules still apply: this is not zero spread or forced ADS. Flat gameplay is unaffected.
- Carried tactical reservations normalize on flat/disabled/dual/logout transitions. `/visor_tacz_prepare_uninstall` prepares carried tactical guns before addon removal.

Read [controls and limitations](TACTICAL-RELOAD.md). Dropping a tactical magazine outside the pouch discards its rounds. Advanced dual tactical handling, collectible magazine items, curved insertion paths and remote two-magazine props are not included.

Validation results are recorded in the [master checklist](TESTING-CURRENT.md#alpha10-targeted-checks). Headset interaction, live inventory packets, multiplayer and the accuracy hook still need runtime checks. Previous passes remain recorded.
