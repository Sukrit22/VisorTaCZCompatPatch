# Profile architecture experiment (not connected to Minecraft yet)

Branch: `codex/data-driven-gun-profiles`. Base checkpoint: `3638343` (0.11.0). No JAR/gameplay changes in this experiment; the current Java runtime still uses its existing profiles.

- `examples/immersive-pistol.toml`: named parts, one slide joint, grip regions, validated mechanism commands, persistent locked-open pose. Model node names were checked in the supplied pack; interaction coordinates/travel are illustrative, not calibrated.
- `examples/project-zero-famas.toml`: the same mechanism with different part mappings (`magazine2`, `bolt`). The supplied folder targets 1.21.1; this is not a 1.20.1 runtime compatibility claim.
- `validate.py`: offline TOML syntax/reference/constraint prototype for these examples. Not the complete final schema or a gameplay engine; external profiles are not loaded by the mod.
- `inspect_packs.py`: read-only source inventory for extracted packs or ZIPs; never executes Lua or installs assets. Two observed lenient literals are normalized for inventory only, reported as warnings. Runtime Gson compatibility is not established by this scanner.
- `pack-inspection.json`: reproducible metadata snapshot from the supplied folders, including missing references and inspection warnings. No textures/models/audio are copied.

```powershell
python -m unittest discover -s experiments/profile-v1 -p "test_*.py"
python experiments/profile-v1/validate.py experiments/profile-v1/examples/immersive-pistol.toml experiments/profile-v1/examples/project-zero-famas.toml
python experiments/profile-v1/inspect_packs.py "path/to/extracted-pack" "path/to/another-pack.zip"
```

The design and staged implementation plan are in `docs/PROFILE-ARCHITECTURE-V1.md`.
