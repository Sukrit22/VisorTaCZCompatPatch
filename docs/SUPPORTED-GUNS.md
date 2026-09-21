# Supported default-pack guns: 0.11.0

Pinned TaCZ 1.1.8-hotfix: 54 guns. Every entry has VR button aiming/shooting/reload support. Physical handling is implemented for 18, including four experimental cylinder/breech workflows. The remaining 36 automatically use Buttons even when the global preference is Physical. These are implemented capabilities, not claims that every gun has passed headset tests.

Only default display geometry is covered; alternate displays and external gun packs are not automatically registered in this release.

| Gun ID | Pack category | Maximum handling | Muzzle source |
| --- | --- | --- | --- |
| `tacz:aa12` | shotgun | Buttons only | muzzle_flash |
| `tacz:ai_awp` | sniper | Buttons only | muzzle_flash |
| `tacz:ak47` | rifle | Buttons only | muzzle_flash |
| `tacz:aug` | rifle | Buttons only | muzzle_flash |
| `tacz:b93r` | pistol | Physical | muzzle_flash |
| `tacz:cz75` | pistol | Physical | muzzle_flash |
| `tacz:db_long` | shotgun | Buttons only | muzzle_flash |
| `tacz:db_short` | shotgun | Buttons only | muzzle_flash |
| `tacz:deagle` | pistol | Physical | muzzle_flash |
| `tacz:deagle_golden` | pistol | Physical | muzzle_flash |
| `tacz:fn_evolys` | mg | Buttons only | muzzle_flash |
| `tacz:fn_fal` | rifle | Buttons only | muzzle_flash |
| `tacz:g36k` | rifle | Buttons only | muzzle_flash |
| `tacz:glock_17` | pistol | Physical | muzzle_flash |
| `tacz:hk416d` | rifle | Buttons only | muzzle_flash |
| `tacz:hk_g3` | rifle | Buttons only | muzzle_flash |
| `tacz:hk_mk23` | pistol | Physical | muzzle_flash |
| `tacz:hk_mp5a5` | smg | Physical | muzzle_flash |
| `tacz:kar98` | sniper | Buttons only | muzzle_flash |
| `tacz:lonetrail` | pistol | Physical (experimental) | muzzle_flash |
| `tacz:m1014` | shotgun | Buttons only | muzzle_flash |
| `tacz:m107` | sniper | Buttons only | muzzle_flash |
| `tacz:m16a1` | rifle | Buttons only | muzzle_flash |
| `tacz:m16a4` | rifle | Buttons only | muzzle_flash |
| `tacz:m1911` | pistol | Physical | muzzle_flash |
| `tacz:m249` | mg | Buttons only | muzzle_flash |
| `tacz:m320` | rpg | Buttons only | manual visual estimate |
| `tacz:m4a1` | rifle | Physical | muzzle_flash |
| `tacz:m700` | sniper | Physical | muzzle_flash |
| `tacz:m870` | shotgun | Physical | muzzle_flash |
| `tacz:m95` | sniper | Buttons only | muzzle_flash |
| `tacz:m9a4` | pistol | Physical | muzzle_flash |
| `tacz:minigun` | mg | Buttons only | muzzle_flash |
| `tacz:mk14` | rifle | Buttons only | muzzle_flash |
| `tacz:p320` | pistol | Physical | muzzle_flash |
| `tacz:p90` | smg | Buttons only | muzzle_flash |
| `tacz:qbz_191` | rifle | Buttons only | muzzle_flash |
| `tacz:qbz_95` | rifle | Buttons only | muzzle_flash |
| `tacz:rhino357` | pistol | Physical (experimental) | muzzle_flash |
| `tacz:rpg7` | rpg | Buttons only | manual visual estimate |
| `tacz:rpk` | mg | Buttons only | muzzle_flash |
| `tacz:scar_h` | rifle | Buttons only | muzzle_flash |
| `tacz:scar_l` | rifle | Buttons only | muzzle_flash |
| `tacz:sks_tactical` | rifle | Buttons only | muzzle_flash |
| `tacz:spas_12` | shotgun | Buttons only | muzzle_flash |
| `tacz:spr15hb` | rifle | Buttons only | muzzle_flash |
| `tacz:springfield1873` | sniper | Buttons only | muzzle_flash |
| `tacz:taurus500` | pistol | Physical (experimental) | muzzle_flash |
| `tacz:taurus943` | pistol | Physical (experimental) | muzzle_flash |
| `tacz:timeless50` | pistol | Physical | muzzle_flash |
| `tacz:type_81` | rifle | Buttons only | muzzle_flash |
| `tacz:ump45` | smg | Buttons only | muzzle_flash |
| `tacz:uzi` | smg | Buttons only | muzzle_flash |
| `tacz:vector45` | smg | Buttons only | muzzle_flash |

M320/RPG-7 muzzle origins are visual estimates because their models lack a muzzle marker. Calibrate before judging projectile alignment. Button reload preserves native timing/ammo/scripts, but camera-relative desktop reload animation is suppressed by the existing VR renderer; do not expect animated hands or complete native weapon motion.
