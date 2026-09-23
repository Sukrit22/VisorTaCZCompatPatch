# 0.12.0-alpha.9 — source changes, not packaged

- TOML is the sole gun catalog: 54 base guns, 20 physical and 34 button-only. Java/JSON legacy gun tables and the backend switch are removed. Calibration overlays remain unchanged.
- Gun lookup no longer depends on per-stack backend binding. This addresses a plausible SCAR support-loss race when ammo synchronization replaces a client item stack; hardware confirmation remains pending.
- Cosmetic slide/bolt shot cycles for configured physical profiles, including in Button mode. Shared model state is restored after each render.
- Model 943 / Taurus 500 / Rhino cylinder rounds now follow native live ammo plus spent count; local hammer/cylinder charge animations use TaCZ charge progress. Remote prefire charge synchronization is not implemented.
- Advanced gravity ejection requires tilt-up plus a 4 cm shake and 2 cm reversal. Pink-zone extraction remains available.
- Taurus 500/Rhino pouch Grip/Use supplies a native-model speedloader; Trigger supplies one round. Insertion consumes only available inventory ammo and respects live/spent occupancy. Holder rounds are a cosmetic capacity preview, not a persistent magazine inventory.
- MK23 manual/slower mode requires a physical rack after a shot; its other mode cycles automatically. Ammo remains native and the rack does not consume a second round.
- World-framebuffer stencil preparation is restricted to active VR. Existing item/attachment safeguards remain. Flatscreen flicker is not yet reproduced or confirmed fixed.

Protocol 25 requires matching client/server builds. Identical TOML files are required for compatibility; mismatch/invalid files pause compatibility and emit a diagnostic. There is no silent Legacy fallback.

Validation: 174 unit tests pass; 17 configured visual profiles were checked against the pinned native model bone names with no missing names. No headset, render-output, multiplayer runtime test, JAR build, installation, commit, or push was performed for this source change.

Start with T09-01 through T09-12 in [the consolidated pending checklist](TESTING-CURRENT.md). Authoring details: [runtime profiles](RUNTIME-PROFILES.md).
