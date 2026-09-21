"""Read-only check against the pinned .upstream TaCZ source; run from repo root."""
import json, re
from pathlib import Path
base=Path('.upstream/TACZ/src/main/resources/assets/tacz/custom/tacz_default_gun')
def read(p):
    return json.loads(re.sub(r'//[^\n]*','',p.read_text(encoding='utf-8-sig')))
source=Path('src/main/java/dev/visorcompat/tacz/PistolProfiles.java').read_text(encoding='utf-8')
rows=read(Path('docs/PISTOL-ASSETS-0.10.0.json'))
for row in rows:
    name=row['id']
    display=read(base/'assets/tacz/display/guns'/f'{name}_display.json')
    model=read(base/'assets/tacz/geo_models'/(display['model'].split(':')[1]+'.json'))
    nodes={n['name']:n for n in model['minecraft:geometry'][0]['bones']}
    assert row['node'] in nodes,(name,row['node'])
    data=read(base/'data/tacz/data/guns'/f'{name}_data.json')
    assert row['ammo']==data['ammo'] and row['capacity']==data['ammo_amount']
    assert row['bolt']==data['bolt'] and row['modes']==data['fire_mode']
    if data['bolt']=='closed_bolt':
        assert 'magazine' in nodes and 'bullet_in_mag' in nodes
    for node in ['thirdperson_hand','muzzle_flash']:
        assert node in nodes
    block=source.split('new Data("'+name+'",',1)[1].split(')),',1)[0]
    strings=re.findall(r'"([^"\n]+)"',block)
    # rack + ammo precede folder and four sound names.
    folder,*clips=strings[-5:]
    for clip in clips:
        assert (base/'assets/tacz/tacz_sounds'/folder/(clip+'.ogg')).is_file(),(name,folder,clip)
print(f'Validated {len(rows)} pistol models, mechanisms, ammunition and action sound mappings.')
