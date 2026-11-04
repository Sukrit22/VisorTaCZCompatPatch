"""Inventory authored TaCZ bones; never infer mechanisms or modify a gun pack.
Python 3.11+. Accept an unpacked pack root (assets/ + data/) or a pack ZIP.
"""
import argparse
import hashlib
import json
import re
import zipfile
from pathlib import Path


def json_data(raw):
    # Preserve quoted strings (including URLs); strip only actual JSON comments.
    text = raw.decode('utf-8-sig')
    text = re.sub(r'("(?:\\.|[^"\\])*"|//[^\r\n]*|/\*[\s\S]*?\*/)',
                  lambda m: m[0] if m[0].startswith('"') else ' ', text)
    return json.loads(text)


class Pack:
    def __init__(self, path):
        self.path = Path(path)
        self.archive = zipfile.ZipFile(path) if self.path.is_file() else None
        self.names = sorted(n for n in (self.archive.namelist() if self.archive else
                            (p.relative_to(self.path).as_posix() for p in self.path.rglob('*.json')))
                            if n.endswith('.json'))
        if len(self.names) != len(set(self.names)):
            raise ValueError('Duplicate ZIP members: ambiguous resource precedence')

    def read(self, name):
        return self.archive.read(name) if self.archive else (self.path / name).read_bytes()

    def close(self):
        if self.archive:
            self.archive.close()


def resource(ref, folder, suffix='.json'):
    if not isinstance(ref, str) or not re.fullmatch(r'[a-z0-9_.-]+:[a-z0-9_./-]+', ref):
        raise ValueError(f'Expected explicit namespace:path resource, got {ref!r}')
    namespace, name = ref.split(':', 1)
    if '..' in name.split('/'):
        raise ValueError('Parent paths are not resources')
    return f'assets/{namespace}/{folder}/{name}{suffix}'


def collect(path):
    pack = Pack(path)
    hashes, warnings, guns, displays = {}, [], [], []
    def load(name):
        raw = pack.read(name)
        hashes[name] = hashlib.sha256(raw).hexdigest()
        return json_data(raw)
    try:
        for name in pack.names:
            match = re.fullmatch(r'data/([^/]+)/index/guns/(.+)\.json', name)
            if match:
                data = load(name)
                guns.append(dict(id=f'{match[1]}:{match[2]}', index=name,
                                 display=data.get('display'), type=data.get('type')))
        if not guns:
            raise ValueError('No data/<namespace>/index/guns/*.json; choose the pack root, not an outer folder/JAR')
        for name in pack.names:
            match = re.fullmatch(r'assets/([^/]+)/display/guns/(.+)\.json', name)
            if not match:
                continue
            display = load(name)
            row = dict(id=f'{match[1]}:{match[2]}', display=name, model=display.get('model'),
                       lod=display.get('lod'), animation=display.get('animation'), geometries=[])
            displays.append(row)
            try:
                model_path = resource(row['model'], 'geo_models')
                row['model_file'] = model_path
                model = load(model_path)
            except (KeyError, FileNotFoundError, ValueError) as ex:
                warnings.append(f'{name}: unresolved model: {ex}');continue
            animation = {}
            if row['animation']:
                try:
                    anim_path = resource(row['animation'], 'animations', '.animation.json')
                    row['animation_file'] = anim_path
                    animation = load(anim_path).get('animations', {})
                except (KeyError, FileNotFoundError, ValueError) as ex:
                    warnings.append(f'{name}: unresolved animation: {ex}')
            geometries = model.get('minecraft:geometry', [])
            if not geometries:
                warnings.append(f'{model_path}: unsupported/empty geometry format')
            for geometry in geometries:
                bones = []
                for bone in geometry.get('bones', []):
                    channels = {}
                    for clip, definition in animation.items():
                        transform = definition.get('bones', {}).get(bone['name'])
                        if transform is not None:
                            channels[clip] = sorted(transform) if isinstance(transform, dict) else ['unknown']
                    bones.append(dict(name=bone['name'], parent=bone.get('parent'),
                                      pivot=bone.get('pivot'), rotation=bone.get('rotation'),
                                      cubes=len(bone.get('cubes', [])), animation_channels=channels))
                names = [b['name'] for b in bones]
                if len(names) != len(set(names)):
                    warnings.append(f'{model_path}: duplicate bone names')
                for b in bones:
                    if b['parent'] and b['parent'] not in names:
                        warnings.append(f'{model_path}: missing parent {b["parent"]}')
                row['geometries'].append(dict(identifier=geometry.get('description', {}).get('identifier'), bones=bones))
        known = {d['id'] for d in displays}
        for gun in guns:
            if gun['display'] not in known:
                warnings.append(f'{gun["id"]}: display {gun["display"]} is not in this pack')
        return dict(schema=1, source=pack.path.name, guns=guns, displays=displays,
                    warnings=warnings, sha256=hashes)
    finally:
        pack.close()


def markdown(report):
    def cell(value):
        return str(value if value is not None else '-').replace('|', '\\|').replace('\n', ' ')
    lines = ['# TaCZ authored model-parts catalogue', '',
             'Generated by `scripts/catalog-gun-parts.py`. Exact authored names, not inferred functions.',
             'All gun display files are included, including displays not selected by a base index. LOD is metadata only.',
             'Animation columns name authored clips/channels; they do not prove runtime motion or template support.', '',
             '| Gun ID | Type | Display ID | Index file |', '| --- | --- | --- | --- |']
    for g in report['guns']:
        lines.append('| ' + ' | '.join(cell(g[k]) for k in ('id','type','display','index')) + ' |')
    for d in report['displays']:
        lines += ['', f'## {d["id"]}', '', f'Display: `{d["display"]}`', '',
                  f'Model: `{d.get("model_file", "unresolved")}`', '',
                  f'Animation: `{d.get("animation_file", "none")}`', '', f'LOD reference: `{d["lod"]}`']
        for geo in d['geometries']:
            lines += ['', f'Geometry: `{geo["identifier"]}`; **{len(geo["bones"])} bones**.', '',
                      '| Bone | Parent | Pivot (native pixels) | Rotation | Cubes | Authored animation channels |',
                      '| --- | --- | --- | --- | --- | --- |']
            for b in geo['bones']:
                clips = '; '.join(f'{k}: {",".join(v)}' for k,v in b['animation_channels'].items())
                lines.append('| ' + ' | '.join(cell(v) for v in [b['name'],b['parent'],b['pivot'],b['rotation'],b['cubes'],clips]) + ' |')
    lines += ['', '## Unresolved references', ''] + (report['warnings'] or ['None.'])
    lines += ['', 'The companion JSON contains SHA-256 hashes of every consumed source for reproducibility.', '']
    return '\n'.join(lines)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('pack', type=Path)
    parser.add_argument('--output', required=True, type=Path, help='Output stem; writes .json and .md')
    args=parser.parse_args()
    result=collect(args.pack)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.with_suffix('.json').write_text(json.dumps(result, indent=2)+'\n', encoding='utf-8')
    args.output.with_suffix('.md').write_text(markdown(result), encoding='utf-8')
    print(f'{len(result["guns"])} guns, {len(result["displays"])} displays, {len(result["warnings"])} warnings')

if __name__ == '__main__':
    main()
