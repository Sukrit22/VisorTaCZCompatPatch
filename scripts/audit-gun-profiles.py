"""Audit executable profiles against an actual TaCZ pack. Python 3.11+.

Read-only for profiles/calibration. Missing active parts and overlapping shot
transforms fail the command; inactive button-template placeholders are reported.
"""
import argparse
import importlib.util
import json
import tomllib
from pathlib import Path

spec = importlib.util.spec_from_file_location("catalog", Path(__file__).with_name("catalog-gun-parts.py"))
catalog = importlib.util.module_from_spec(spec)
spec.loader.exec_module(catalog)


def vectors(value):
    if isinstance(value, list) and len(value) == 3 and all(isinstance(v, (int, float)) for v in value):
        yield value
    elif isinstance(value, dict):
        for child in value.values():
            yield from vectors(child)


def audit(pack, profiles):
    rows, errors = [], []
    resources = set(pack.archive.namelist()) if pack.archive else {p.relative_to(pack.path).as_posix() for p in pack.path.rglob("*") if p.is_file()}
    for gun in profiles["guns"]:
        gun_id = gun["id"]
        namespace, name = gun_id.split(":", 1)
        index_path = f"data/{namespace}/index/guns/{name}.json"
        if index_path not in pack.names:
            errors.append(f"{gun_id}: missing gun index")
            continue
        index = catalog.json_data(pack.read(index_path))
        ns, display_id = index["display"].split(":", 1)
        display_path = f"assets/{ns}/display/guns/{display_id}.json"
        display = catalog.json_data(pack.read(display_path))
        model_id = display["model"]
        ns, model_name = model_id.split(":", 1)
        model_path = f"assets/{ns}/geo_models/{model_name}.json"
        model = catalog.json_data(pack.read(model_path))
        bones = {b["name"]: b for geo in model.get("minecraft:geometry", []) for b in geo["bones"]}
        ns, animation_name = display["animation"].split(":", 1)
        animation_path = f"assets/{ns}/animations/{animation_name}.animation.json"
        animations = catalog.json_data(pack.read(animation_path)).get("animations", {})
        physical = gun["mechanism"] != "button"
        references = []
        for role in ("magazine", "action", "bolt"):
            used = physical and (role != "magazine" or gun["mechanism"] in (
                "closed_bolt_magazine", "locking_charging_handle", "lifted_bolt"))
            references.append((role, gun["parts"][role]["node"], used))
        visual = gun.get("visual", {})
        for role in ("shot_node", "cylinder", "hammer", "loader"):
            references.append((role, visual.get(role, ""), True))
        for role in ("rounds", "heads", "hidden"):
            references.extend((role, node, True) for node in visual.get(role, []))
        references.extend(("shot_follower", part["node"], True) for part in visual.get("shot_followers", []))
        parts, warnings = [], []
        for sound_role, sound in gun.get("sounds", {}).items():
            if sound:
                sound_ns, sound_path = sound.split(":", 1)
                if f"assets/{sound_ns}/tacz_sounds/{sound_path}.ogg" not in resources:
                    errors.append(f"{gun_id}: {sound_role} sound missing: {sound}")
        pistol = gun.get("pistol", {})
        for sound_role in ("mag_out", "mag_in", "action_back", "action_close"):
            sound = pistol.get(sound_role, "")
            if sound:
                path = f"assets/{namespace}/tacz_sounds/{pistol.get('sound_folder', name)}/{sound}.ogg"
                if path not in resources:
                    errors.append(f"{gun_id}: {sound_role} sound missing: {path}")
        for role, node, used in references:
            if not node:
                continue
            if node not in bones:
                message = f"{role}: {node} absent" + (" (inactive template field)" if not used else "")
                (errors if used else warnings).append(f"{gun_id}: {message}")
                continue
            channels = {key: value["bones"][node] for key, value in animations.items() if node in value.get("bones", {})}
            ancestors, parent = [], bones[node].get("parent")
            while parent and parent not in [b["name"] for b in ancestors]:
                bone = bones[parent]
                ancestors.append(dict(name=parent, pivot=bone.get("pivot"), rotation=bone.get("rotation")))
                parent = bone.get("parent")
            parts.append(dict(role=role, node=node, used=used, parent=bones[node].get("parent"), ancestors=ancestors,
                              pivot=bones[node].get("pivot"), rotation=bones[node].get("rotation"), animations=channels))
        shot_nodes = [visual.get("shot_node", "")] + [p["node"] for p in visual.get("shot_followers", [])]
        for node in filter(None, shot_nodes):
            ancestor = bones.get(node, {}).get("parent")
            seen = set()
            while ancestor and ancestor not in seen:
                seen.add(ancestor)
                if ancestor in shot_nodes:
                    errors.append(f"{gun_id}: shot parts {ancestor}/{node} overlap; child would move twice")
                ancestor = bones.get(ancestor, {}).get("parent")
        action = gun["parts"]["action"]["node"]
        bolt = gun["parts"]["bolt"]["node"]
        chain, current = [], bones.get(bolt, {}).get("parent")
        while current and current not in chain:
            chain.append(current)
            current = bones.get(current, {}).get("parent")
        coupling = "inherited" if action in chain else "separate" if bolt else "single part"
        if physical and action in bones:
            animated = [key for key, value in animations.items() if action in value.get("bones", {})]
            if not animated:
                warnings.append("Action has no native animation channel; inspect parent animation and geometry manually")
        for node, travel in [(visual.get("shot_node", ""), visual.get("shot_travel", 0))] + [
                (p["node"], p["travel"]) for p in visual.get("shot_followers", [])]:
            if not node:
                continue
            samples = [v for key, animation in animations.items() if key in ("shoot", "shoot_semi")
                       for v in vectors(animation.get("bones", {}).get(node, {}).get("position", {}))]
            if not samples:
                warnings.append(f"{node}: no direct native shoot translation; inspect ancestry/alternate animation")
            else:
                peak = max(samples, key=lambda v: abs(v[2]))[2]
                if peak * travel < 0:
                    errors.append(f"{gun_id}: {node} shot direction opposes native animation")
                expected = peak * gun["geometry"]["scale"] / 16
                if abs(expected - travel) > .005:
                    warnings.append(f"{node}: configured shot {travel:g} m; native peak {expected:g} m at profile scale")
        rows.append(dict(id=gun_id, mechanism=gun["mechanism"], model=model_path, animation=animation_path,
                         coupling=coupling, geometry=gun["geometry"], zones=gun["zones"], sounds=gun.get("sounds", {}), parts=parts, warnings=warnings))
    return dict(schema=1, guns=rows, errors=errors)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pack", type=Path)
    parser.add_argument("--profiles", type=Path, default=Path("src/main/resources/gun-profiles.toml"))
    parser.add_argument("--output", type=Path, default=Path("docs/generated/PROFILE-AUDIT"))
    args = parser.parse_args()
    pack = catalog.Pack(args.pack)
    try:
        report = audit(pack, tomllib.loads(args.profiles.read_text(encoding="utf-8-sig")))
    finally:
        pack.close()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.with_suffix(".json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    lines = ["# TaCZ profile/model audit", "", "Static evidence only: this does not certify VR calibration or runtime handling.", "",
             "| Gun | Mechanism | Action / bolt coupling | Findings |", "| --- | --- | --- | --- |"]
    for row in report["guns"]:
        lines.append(f"| {row['id']} | {row['mechanism']} | {row['coupling']} | {'; '.join(row['warnings']) or 'Active references and shot direction pass'} |")
    lines += ["", "## Errors", ""] + (report["errors"] or ["None."])
    args.output.with_suffix(".md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{len(report['guns'])} profiles; {len(report['errors'])} errors; detailed evidence: {args.output}.json")
    for error in report["errors"]:
        print(error)
    return bool(report["errors"])


if __name__ == "__main__":
    raise SystemExit(main())
