# 0.14.0-alpha.8 source update

This milestone introduces experimental Physical handling for **Glock 17 in either independent dual hand**. Other dual guns keep Button handling. Advanced dual manipulation and finger spinning are not enabled.

## Physical Glock

Choose Physical handling and enable Independent dual wield. Draw a Glock; leave the other gun stowed so that controller can remove its magazine, collect a magazine at the waist pouch, insert it, and rack the slide. The configured physical interaction button still applies. Two held guns leave no free hand to manipulate either one: stow one first.

Each hotbar slot has its own server session tied to the actual stack. Removed magazine rounds are reserved on that stack; reconnect recovery restores reservations without recreating the item. Inventory deletion, dropping and interruptions require the A8-LIFE headset/server test before this is considered stable. Existing single-gun results do not certify this new path.

## Alpha.7 feedback

- M700 keeps the bolt grabbed after closing it. While still holding Grab, lift it again for another cycle. The unintended automatic return to the handle has been removed.
- Offhand sights, muzzle and interaction zones now keep the model's authored local coordinates; only whole-gun controller placement changes between hands.
- Dual long guns can use an available support controller to steer model and shot direction. M1014 support is separate from its action handle.
- Dual reload audio skips missing animation sound assets so a valid aggregate clip can play. Native duplicate bolt/reload playback is suppressed during dual handling.
- ADS calibration now combines the gun baseline with an optional attachment-and-view offset and box override. Sight offsets allow ±2000 mm. See [ADS calibration](ADS-CALIBRATION.md).
- Advanced Handling settings use pages, with Back kept at the bottom of a short VR canvas.
- M700 support/return hints use shorter stacked lines.

## Compatibility and remaining work

Network protocol is **34**: use matching client/server versions. Personal gun calibration remains in its existing file; optional sight corrections use `config/visor_tacz-sight-calibration.json` and are included in Reload calibration.

This is the first dual Physical gun, not a port of every single-gun mechanism. Physical rifles, pumps, cylinders, advanced transfers and the spinning proposal remain later work. Finger spinning is documented in [the design note](ADVANCED-SPIN-DESIGN.md), with firing suppressed by default while spinning.

Follow the **A8** rows in [the master checklist](TESTING-CURRENT.md); previous reported passes remain recorded. Automated compile/tests do not establish headset behavior, stereo rendering, audio timing or multiplayer inventory safety.

## Validation

All four targets compile and pass tests: 303 each on Forge/Fabric 1.20.1 and Fabric 1.21.1; 304 on NeoForge 1.21.1. Forge dedicated-server startup and a Glock-equipped entity smoke check also passed using the development dependencies. No headset session was run and no release JAR was packaged.
