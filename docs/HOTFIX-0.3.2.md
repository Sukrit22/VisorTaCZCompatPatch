# 0.3.2: main-menu VR transition

The September 17, 23:03:56 report identifies a null Visor `renderer.guiTarget`
in `VRRenderState.startVRGuiPhase`, reached through the resize hook while
`VisorState.updateActive` changes from flatscreen to VR. This differs from the
earlier OpenGL-format crash.

Visor 0.5.0 marks the session active and resizes the display before render targets
have been created. The addon now defers entering the VR GUI phase while its target
is absent, keeping the phase, pass and framebuffer in vanilla mode together.
Visor's normal renderer setup creates the targets and calls the same phase method
again, at which point it proceeds unchanged. This does not suppress exceptions or
skip framebuffer creation. It is a client lifecycle guard, including addon OFF mode.

Replace the client addon with 0.3.2 and restart. Protocol remains 6: servers on
0.3.0 or 0.3.1 remain compatible. Keep configuration and calibration files.

Test a fresh launch into flatscreen at the main menu, switch to VR, switch back,
then switch to VR again. Repeat with shaders off and on; enter a world and check
the menu, HMD view, and firing. Report a new timestamped crash and matching log if
it fails. This patch does not establish that whole-world blinking is resolved.

Validation: Java 17 release build and 25 tests passed; the Visor production JAR
contains both targeted phase methods and the public GUI target field. Upstream
source confirms renderer setup retries the GUI phase after creating targets.
Live main-menu/VR transition testing remains required.
