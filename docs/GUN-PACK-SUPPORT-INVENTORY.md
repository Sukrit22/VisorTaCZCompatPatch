# Gun-pack support inventory

Source inventory only; entries below are not validated support claims. Project Zero folder is labelled 1.21.1; compatibility with the target Minecraft 1.20.1 TaCZ build must be checked.

| Pack | Gun ID | Declared type | Display | Data |
|---|---|---|---|---|
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:assult_rifle | rifle | immersive_armorer:assult_rifle_display | immersive_armorer:assult_rifle_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:chemical_thrower | rpg | immersive_armorer:chemical_thrower_display | immersive_armorer:chemical_thrower_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:double_shotgun | shotgun | immersive_armorer:double_shotgun_display | immersive_armorer:double_shotgun_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:dual_barrel_cannon | rpg | immersive_armorer:dual_barrel_cannon_display | immersive_armorer:dual_barrel_cannon_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:pistol_9mm | pistol | immersive_armorer:pistol_9mm_display | immersive_armorer:pistol_9mm_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:plasma_gun | rpg | immersive_armorer:plasma_gun_display | immersive_armorer:plasma_gun_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:pump_shotgun | shotgun | immersive_armorer:pump_shotgun_display | immersive_armorer:pump_shotgun_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:railgun | sniper | immersive_armorer:railgun_display | immersive_armorer:railgun_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:revolver | pistol | immersive_armorer:revolver_display | immersive_armorer:revolver_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:short_smg | smg | immersive_armorer:short_smg_display | immersive_armorer:short_smg_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:standard_rail_pistol_mk2 | pistol | immersive_armorer:standard_rail_pistol_mk2_display | immersive_armorer:standard_rail_pistol_mk2_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:standard_rail_pistol_mk3 | pistol | immersive_armorer:standard_rail_pistol_mk3_display | immersive_armorer:standard_rail_pistol_mk3_data |
| Immersive Armorer-v1.2.1-for117 | immersive_armorer:standard_rail_smg_mk1 | smg | immersive_armorer:standard_rail_smg_mk1_display | immersive_armorer:standard_rail_smg_mk1_data |
| Project Zero v0.5+1.21.1 | project_zero:crossbow | sniper | project_zero:crossbow_display | project_zero:crossbow_data |
| Project Zero v0.5+1.21.1 | project_zero:famas | rifle | project_zero:famas_display | project_zero:famas_data |
| Project Zero v0.5+1.21.1 | project_zero:fat | sniper | project_zero:fat_display | project_zero:fat_data |
| Project Zero v0.5+1.21.1 | project_zero:g11 | rifle | project_zero:g11_display | project_zero:g11_data |
| Project Zero v0.5+1.21.1 | project_zero:hcar | rifle | project_zero:hcar_display | project_zero:hcar_data |
| Project Zero v0.5+1.21.1 | project_zero:l39 | rpg | project_zero:l39_display | project_zero:l39_data |
| Project Zero v0.5+1.21.1 | project_zero:lgs | rpg | project_zero:lgs_display | project_zero:lgs_data |
| Project Zero v0.5+1.21.1 | project_zero:makarov | pistol | project_zero:makarov_display | project_zero:makarov_data |
| Project Zero v0.5+1.21.1 | project_zero:r4 | rifle | project_zero:r4_display | project_zero:r4_data |
| Project Zero v0.5+1.21.1 | project_zero:rossi | pistol | project_zero:rossi_display | project_zero:rossi_data |
| Project Zero v0.5+1.21.1 | project_zero:sig | rifle | project_zero:sig_display | project_zero:sig_data |
| Project Zero v0.5+1.21.1 | project_zero:svd | sniper | project_zero:svd_display | project_zero:svd_data |
| Project Zero v0.5+1.21.1 | project_zero:usp | pistol | project_zero:usp_display | project_zero:usp_data |
| Project Zero v0.5+1.21.1 | project_zero:wa2000 | sniper | project_zero:wa2000_display | project_zero:wa2000_data |

## Current blockers

- Button fallback now accepts guns present in TaCZ common gun indexes, including unknown gun IDs and custom stack displays. TaCZ resolves the actual display. Generic geometry needs grip/muzzle/sight calibration; unusual Lua mechanics still require in-game testing.
- Pack index display IDs and stack display overrides are different: `tacz:default` can already resolve a pack-specific default display.
- 27 bundled base guns remain button-only. Physical profiles need actual model bones, movement direction, chamber/bolt semantics and calibrated interaction geometry.
- Existing TOML templates do not describe arbitrary mechanics. Unsupported mechanisms need Java changes rather than a guessed template.
- External model output is a draft. Check referenced assets locally, parse profiles with existing validators, then run Forge/Fabric tests and in-game checks.

## 9ARM endpoint review

Model: `qwen3.8-27b-fp8`. Review completed with finish_reason=stop, 2109 completion tokens (1041 reasoning), and explicit END_REVIEW marker under an 8192-token output budget.

It identified singleton state and Visor hotbar distinctions, but still proposed invalid/unverified calls such as a no-argument offhand list get and a two-argument Map.get. Suitable for bounded drafts and asset inventory with review; not autonomous acceptance of Java patches. Provider comparison is inconclusive because the output budgets and supplied source context differed.

## First physical batch: AK47 and UMP45

Both now use the closed-bolt magazine template. AK47 operates `bolt`; UMP45 operates `ump45_charge_handle` with linked `ump45_bolt`, while firing animation moves only the bolt. Existing grip/muzzle geometry and saved calibration are preserved. Interaction targets are initial asset-derived positions, not verified calibration.

The template simplifies magazine insertion and action travel. UMP handle/bolt use the existing shared pull travel, rather than the exact unequal native animation distances. These profiles do not claim a fully simulated AK rocking magazine or a bespoke UMP action. Existing generic release behavior remains template behavior.

Test each gun: remove/insert magazine, empty reload then rack, repeatedly rack without firing (ammo accounting), fire with support hand, selector, last-round behavior, jam clearing, muzzle alignment and reload/disconnect recovery. For UMP check the charging handle does not reciprocate during shots. Try standard and extended magazines. Check with a remote observer.

For pack fallback, test a pack pistol and rifle in Button mode, calibrate grip/muzzle/sight, then shoot, reload and switch slots. Check unsupported physical zones do not appear. Calibration is per gun/display. Pack scripts, launchers, energy weapons and unusual attachments require separate checks; generic registration cannot guarantee all their behavior.
