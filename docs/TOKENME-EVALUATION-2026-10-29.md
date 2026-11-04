# Tokenme worker evaluation - 2026-10-29

## Scope and containment

Two read-only workers received the same task: trace index/display/model references for Glock17, M1014, SPAS-12 and AA12; count bones, name candidate nodes, inspect existing TOML mappings, and distinguish evidence from runtime speculation. They received an isolated 290-file copy of relevant assets/source at `E:/visor-compat/worker-evaluations/tacz-parts-20261029`. No write permission, builds, network tools or credential inspection were authorized. SHA-256 comparison confirmed all 290 evidence files unchanged afterward. No worker-authored gameplay patch was accepted.

Initial launches against the full repository were stopped before model turns: the launcher's pre-run recursive snapshot was too broad for this checkout. Cancellation during that pre-launch phase left queued metadata, which the coordinator marked canceled after terminating its own runners. This is launcher overhead, not a model-quality result. Prefer small evidence bundles for read-only tasks; use proper isolated worktrees for future implementation trials.

## Observed outcomes

| Model / worker | Execution | Result |
| --- | --- | --- |
| `unsloth/qwen3.8-27b-nvfp4` / parts-qwen-small | First run 159 s, 16 completed shell commands, two failed shell commands; stopped at tool-event budget. Follow-up 49 s with no tools. | Follow-up reported turn completion but emitted no user-facing report. Operational task completion failed; do not mistake launcher completed status for usable work. |
| `deepseek-ai/deepseek-v4-flash-0731` / parts-deepseek-small | First run 78 s, interrupted by provider HTTP 502. Resumed same session: 104 s, report delivered. Across runs 24 completed shell commands, four failed shell commands. | Correct four bone counts and key hierarchy/mapping facts, but important overclaims and enumeration errors require correction. |

Counts independently verified by direct JSON parsing: Glock17 **57**, M1014 **61**, SPAS-12 **88**, AA12 **84**.

DeepSeek correctly identified Glock `slide`, M1014 `Bolt`, SPAS `pump`/`bolt2`/child `bolt`, and AA12 `bolt`. Its report was not accepted verbatim:

- It generalized from the reduced snapshot to claims that our source has no mixins/animation references. The real repository has mixins, and an incomplete source bundle cannot establish their absence. Some searches failed, so they cannot establish negative results either.
- It suggested HUD/slot references as models. `GunDisplayInstance` stores these as textures, not Bedrock part trees.
- Its enumeration advice named a fixed namespace/model folder and first geometry only; a reusable tool must resolve actual resource IDs and report missing dependencies, including display variants.
- The TOML snippet omitted required fields; it is only an excerpt, not an installable profile. Some supplied line citations were inaccurate (for example Glock's animation field).
- Its appropriate caution that runtime visual interference remains unproven was retained. Asset inspection alone cannot reproduce the old shotgun report.

No billing estimate is inferred from raw CLI token counters: resumed runs may include cumulative history. No broad quality ranking follows from this small sample. DeepSeek is provisionally useful for bounded read-only extraction with verification; neither result justifies unattended gameplay edits. Qwen needs another smaller completion trial before assigning implementation.

## Accepted deliverables

Coordinator-authored deterministic `scripts/catalog-gun-parts.py` generated 54 guns, 54 displays and 4,461 bone entries, with zero unresolved references in this local base-pack snapshot. Five Python fixture tests pass; a real-pack ZIP and directory comparison matches all gun/display data and source hashes. [Catalogue](generated/TACZ-MODEL-PARTS.md), [mapping guide](GUN-MODEL-PARTS-GUIDE.md).

Local worker evidence remains under `%LOCALAPPDATA%/Tokenme/workers/sessions/<owning chat>/workers/parts-qwen-small` and `parts-deepseek-small`; a compact local result copy is `build/worker-evaluation/results.json`. These workers are external Codex CLI processes, not native sidebar agents.

## Next safe assignments

1. Have a worker review a supplied handful of extracted model/profile rows with a strict claim/evidence format and small tool budget.
2. For automatic shotgun refinement, capture phase/shot-motion behavior first; then assign one reproduced regression and a narrow test in an isolated worktree. Review full diff and run relevant loader checks before integration.
3. Do not bulk-generate controller bindings: the current TOML parser rejects custom bindings/transitions/poses. Design that extension separately, then batch author against validated schema.
