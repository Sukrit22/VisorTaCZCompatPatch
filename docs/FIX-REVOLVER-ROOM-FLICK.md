# Revolver room-coordinate flick fix (2026-11-05)

Follow-up to PR1. The initial change used player position rather than HMD position, but Visor moves player X/Z to the head pivot during room-scale movement. That is still head-dependent. Client PlayerPoseType.ROOM is initialized with zero origin, scale1, yaw0, so it supplies actual tracking-room metres without joystick movement or snap-turn transforms.

Client sends both room controller positions and horizontal room head-right direction once per eligible tick. Server checks slot/profile identity, active Physical handling, living/non-spectator state, fresh Visor pose, finite/bounded coordinates and a unit axis. It ignores packet bursts closer than20ms. It captures the direction on Use or baseline creation instead of rotating the projection with every head turn. The server chooses which hand holds the gun and retains authority over cylinder/ammunition/jam state; the packet never requests an open/close action.

Tracking gaps over150ms, hand changes and sample jumps over50cm reset the stroke. This is a conservative discontinuity guard, not comprehensive runtime recenter detection. Raw positional gestures cannot distinguish deliberately moving the whole body with the controller from deliberately moving that controller; headset testing remains required.

The400ms opening cooldown now blocks/reset samples before detection, including contextual Grab opening. The first unblocked sample establishes a fresh baseline; a stroke begun inside cooldown cannot carry its travel across expiry. Use permission acquired during the blocked period is discarded, so press Use again after cooldown if required. The initial PR's0.7m/s minimum closing speed remains.

Protocol37 adds CylinderRoom packet29; install matching builds on client/server. This does not add dual-wield revolver Physical handling or change break-action and gravity-unload gestures. No new release JAR is part of this fix.

Validation: Forge1.20.1, Fabric1.20.1 and Fabric1.21.1 each345 tests; NeoForge1.21.1 346; standalone core41;26.2 development harness44. All passed. Both1.21.1 native component checks also passed. These are automated/code checks, not headset or multiplayer runtime acceptance.
