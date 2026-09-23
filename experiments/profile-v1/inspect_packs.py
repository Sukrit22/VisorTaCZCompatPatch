"""Read-only pack inventory, accepts extracted roots or ZIPs; never executes pack scripts."""
import json
import re
import sys
import zipfile
from pathlib import Path

class Pack:
    def __init__(self,path):
        self.path=Path(path);self.zip=zipfile.ZipFile(path) if self.path.is_file() else None
        if self.zip:
            names=self.zip.namelist();roots=[x[:-len('gunpack.meta.json')] for x in names if x.endswith('gunpack.meta.json')]
            if len(roots)!=1:raise ValueError('Expected one gunpack.meta.json')
            self.prefix=roots[0];self.files=[n[len(self.prefix):] for n in names if n.startswith(self.prefix)]
        else:self.files=[p.relative_to(self.path).as_posix() for p in self.path.rglob('*') if p.is_file()]
        self.warnings=[]
    def text(self,name):
        return (self.zip.read(self.prefix+name).decode('utf-8-sig') if self.zip else (self.path/name).read_text(encoding='utf-8-sig'))
    def read(self,name):
        text=self.text(name)
        # Preserve strings while stripping JSON comments, including URLs.
        text=re.sub(r'"(?:\\.|[^"\\])*"|/\*[\s\S]*?\*/|//[^\n]*',lambda m:m[0] if m[0].startswith('"') else '',text)
        # Audit-only treatment of two observed lenient literals. Not a runtime repair.
        for pattern,replacement,label in [(r':\s*flase\b', ': "flase"','unquoted flase'),(r'(?<![\w.])(\d+)\.(?=\s*[,}\]])',r'\1.0','number ending in decimal point')]:
            text,n=re.subn(pattern,replacement,text)
            if n:self.warnings.append(name+': '+label+' normalized for inspection only')
        return json.loads(text)
    def ref(self,side,kind,value):
        ns,path=value.split(':',1)
        if '..' in path.split('/') or path.startswith('/'):raise ValueError('Invalid resource path')
        return f'{side}/{ns}/{kind}/{path}.json'
    def inspect(self):
        rows=[]
        for file in sorted(f for f in self.files if re.fullmatch(r'data/[^/]+/index/guns/[^/]+\.json',f)):
            row={'id':file.split('/')[1]+':'+Path(file).stem}
            try:
                index=self.read(file);data=self.read(self.ref('data','data/guns',index['data']));display=self.read(self.ref('assets','display/guns',index['display']))
                model=self.read(self.ref('assets','geo_models',display['model']));nodes={n['name']:n for n in model['minecraft:geometry'][0]['bones']}
                row.update(bolt=data.get('bolt'),reload=data.get('reload',{}).get('type'),script=data.get('script'),model=display['model'],missing_markers=[n for n in ['thirdperson_hand','muzzle_flash','shell'] if n not in nodes],parts=[n for n in nodes if any(x in n.lower() for x in ['magazine','slide','bolt','pump','charge'])])
                if 'animation' in display:
                    ns,name=display['animation'].split(':',1);row['animation_formats']=[suffix for suffix in ['animation.json','gltf','glb'] if f'assets/{ns}/animations/{name}.{suffix}' in self.files]
            except (OSError,ValueError,KeyError) as e:row['inspection_error']=str(e)
            rows.append(row)
        if self.zip:self.zip.close()
        return {'pack':self.path.name,'gun_indices':len(rows),'guns':rows,'warnings':self.warnings}

if __name__=='__main__':
    print(json.dumps([Pack(p).inspect() for p in sys.argv[1:]],indent=2))
