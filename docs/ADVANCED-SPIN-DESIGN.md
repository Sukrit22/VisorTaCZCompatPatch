# Advanced trigger-guard spin proposal

Status: planned, not enabled in the Physical dual Glock milestone.

Entry: release Grab, then freshly press and hold Trigger while the finger/controller is inside the gun's trigger-guard interaction zone. An existing Trigger hold must not capture a passing gun. The gun stays tied to its real inventory slot; spin never creates an inventory copy.

During spin, the finger is a pivot. A lightweight rigid-body approximation uses a gun-local centre of mass and rotational inertia, gravity, controller movement and damping. Barrel/receiver/grip mass distribution changes feel without requiring expensive mesh physics. Fixed simulation steps plus interpolated rendering avoid the previous low-FPS toss appearance. Limit extreme tracking spikes; tracking loss cancels into the existing safe holster lifecycle.

Default: automatic firing suppression for the entire spin claim and until Trigger is released afterward. Holding the same Trigger to spin and shoot must not fall through into a shot when ownership changes. Explicit Grab near the handle exits into a held gun; Trigger release exits according to the established release/holster policy.

Optional force-triggered discharge is a separate, later game mechanic, disabled by default. It requires a deliberate setting and server-side ownership/cooldown/ammo validation. It must not be presented as a realistic firearm safety simulation: tracking impulses and a controller button do not measure finger pressure. Keep visual spin independent of the authoritative firing decision.

The future Bridge hand claim should represent SPIN plus its accepted Trigger input. A second held gun or another mod's existing hand claim prevents capture. Per-gun spin state belongs to the session/slot, not permanently to left/right or main/offhand.

Implement after per-gun Physical handling and Advanced toss/catch sessions are isolated. A separate implementation allows the reload milestone to be tested without adding a new release/Trigger ownership transition at the same time.
