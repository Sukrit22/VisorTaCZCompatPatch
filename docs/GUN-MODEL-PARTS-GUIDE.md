# Finding TaCZ parts for a TOML profile

Start with the [generated catalogue](generated/TACZ-MODEL-PARTS.md). It lists **54 base-pack guns, 54 displays and 4,461 bone entries** from this checkout's upstream TaCZ pack. These are a local asset snapshot, not a claim about every released pack/version. The [JSON](generated/TACZ-MODEL-PARTS.json) includes exact names, parents, pivots, cube counts, animation channels and source hashes. No model geometry/textures are copied into the catalogue.

## Where to look

In the unpacked pack root, follow these references (Glock example):

```text
data/tacz/index/guns/glock_17.json
  display = tacz:glock_17_display
    assets/tacz/display/guns/glock_17_display.json
      model = tacz:gun/glock_17_geo
        assets/tacz/geo_models/gun/glock_17_geo.json
          minecraft:geometry[].bones[].name / parent / pivot
      animation = tacz:glock_17
        assets/tacz/animations/glock_17.animation.json
          animations.<clip>.bones.<exact name>.position/rotation/scale
```

The `data` reference in the gun index leads to gameplay stats, not mesh parts. The display's `lod.model` is a low-detail alternative; start from its top-level `model` when mapping first-person parts. Resource IDs before the colon select a namespace; do not assume another pack uses `tacz`.

A **bone** is a named group. It may hold cubes, contain other bones, or serve as an invisible anchor. Moving a parent moves its descendants. A zero-cube bone can therefore be the correct action node. Names are case-sensitive: `Bolt` and `bolt` are different. A suggestive name is evidence to inspect, not proof of a mechanism.

Native `pivot` values are model pixels, not the metres used for interaction zones. Do not paste a pivot directly into a zone position. Our profile scale, calibrated grip and Bedrock coordinate conversion sit between them. Likewise, authored animation rotations/positions must be interpreted in their hierarchy; they are not ready-made controller movement vectors.

## Examples verified from current assets and profiles

| Gun | Manual action node | Separate bolt node in current profile | Shot visual node | Important hierarchy |
| --- | --- | --- | --- | --- |
| Glock 17 | `slide` | empty | `slide` | `slide` under `g17`; `bullet_in_mag` under `magazine` |
| M1014 | `Bolt` | empty | `Bolt` | `bolt2` and chamber round descend from capitalized `Bolt` |
| SPAS-12 | `pump` | `bolt2` | `bolt2` | `bolt` is a child of `bolt2`; pump is separate |
| AA12 | `bolt` | empty | `bolt` | `bolt` under `AA12_all`; `magazine` under `magazine_and_bullet` |

For example, these are **sections inside a complete Glock profile**, not a standalone valid profile:

```toml
[guns.parts.magazine]
node = "magazine"
[guns.parts.action]
node = "slide"
[guns.parts.bolt]
node = ""
[guns.visual]
shot_node = "slide"
shot_travel = 0.055
```

Copy a complete compatible entry from `src/main/resources/gun-profiles.toml`, then change verified part names and geometry. Keep the correct tested `mechanism`/`variant`. Do not map both parent and child to two independent motions unless the template actually requires it; that can double movement. Test with debug zones and desktop calibration before headset testing. Follow [runtime profiles](RUNTIME-PROFILES.md) for file placement and reload rules.

## What TOML can currently do

It selects tested mechanism templates, part nodes, zone positions, cosmetic shot/hammer/cylinder/round nodes and supported manual-cycle modes. It does **not** define arbitrary controller chords, transitions, joint axes/limits or scripted motion. `DescriptorProfiles` explicitly rejects `bindings`, `poses` and `transitions`; zone sizes remain in calibration JSON. Batch-generating those unsupported keys would not add functionality.

The catalogue makes mapping existing templates easier. New mechanisms still need Java behavior and validation before a TOML file can select them.

## Generate your own catalogue

Python 3.11+; no third-party Python packages required. From the repository root:

```powershell
python -X utf8 scripts/catalog-gun-parts.py `
  '.upstream/TACZ/src/main/resources/assets/tacz/custom/tacz_default_gun' `
  --output 'docs/generated/TACZ-MODEL-PARTS'

python -X utf8 scripts/catalog-gun-parts.py `
  'F:\your-instance\tacz\your-gun-pack.zip' `
  --output 'build/your-pack-parts'

python -X utf8 scripts/test_catalog_gun_parts.py
```

For an unpacked pack, supply the folder directly containing `assets` and `data`. For a TaCZ JAR, first locate its embedded pack; this tool does not guess embedded-JAR roots. ZIP files are read without extraction. Duplicate ZIP members are rejected. Models supplied by another pack are reported as unresolved; the tool does not simulate resource-pack precedence. All display files are included, even ones not selected by a base gun index. LOD references are recorded but their bones are not substituted for the primary model. Only Bedrock `minecraft:geometry` models are inventoried; missing/unsupported resources are reported rather than invented.

## Automatic shotgun refinement: evidence before changes

M1014 and SPAS-12 already map their actual action nodes. Their native clips animate these nodes, and our `CosmeticParts` also adds shot displacement while `PhysicalModel` adds manual travel. The cosmetic path is suppressed during manual-busy states. That makes phase gating and additive/native animation overlap worth checking; it does not prove the user's old motion issue is still present.

Next focused capture: M1014, SPAS-12 SEMI, SPAS-12 pump mode, then AA12; each in Button/Physical/Advanced, one shot, last shot and a manual cycle. Record whether the wrong part moves, travel is too large, motion is missing, or it stays open. Compare hands off the action versus holding it. Avoid bulk profile edits until that distinguishes mapping errors from animation ownership/timing.
