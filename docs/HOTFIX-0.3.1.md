# 0.3.1 rendering hotfix

The September 17, 22:37:50 client report ends with OpenGL error 1282 at Visor's
AFTER_TRANSLUCENT error check. The matching log gives the driver message:
"The source and destination internal formats are not compatible."
Complementary Unbound r5.9.3 + Euphoria Patches 1.10.5 was active at the crash.
The deferred error check does not identify the exact original GL call.

TaCZ's AttachmentItemRenderer passes null gun/attachment stacks for loose optic
items, but preserves the first-person display context. BedrockAttachmentModel then
enters its aimed-scope path, enabling the main target's stencil buffer. That can
recreate a depth attachment while shader pipelines still hold depth-copy targets.
0.3.1 routes loose first-person attachments through the ordinary body/ring render
path. The outer hand transform is retained. Installed gun optics keep their normal
path. This narrowly scoped stability patch also applies in flatscreen and addon
OFF mode; no loose-attachment sight overlay is drawn.

Additional guards reject null/non-gun stacks in the custom VR optic pass and skip
custom local/remote gun rendering during Oculus shadow passes. Normal third-person
gun rendering remains available for shadows. These changes do not prove that the
reported whole-world flashing is fixed. It also occurs with shaders off, according
to the user's test, and its cause remains unconfirmed.

## Install and verify

Replace the client 0.3.0 addon with 0.3.1; keep only one addon JAR. Restart fully
before testing, to recreate all framebuffer targets. Protocol remains 6, so a
0.3.0 server is compatible; no configuration/calibration reset is needed.

1. In flatscreen, hold the loose mini red dot, switch hands/slots, open inventory,
   then install it on a gun. Repeat with shaders off and on.
2. Repeat loose/installed optic checks in VR.
3. For whole-world flashing, compare an empty hand and gun in the same location.
   Compare `/visor_tacz off` and `/visor_tacz auto`; off disables local integration,
   but remote VR guns and the loose-optic stability patch remain enabled.
4. If flashing persists, compare the same local test world after restarting with
   only this addon JAR temporarily moved outside mods (leave Visor/TaCZ installed).
   Use single-player: an addon-required multiplayer server may reject that client.
   This distinguishes addon involvement from the rest of the rendering stack.
5. Report whether flashing affects the desktop window, headset view, or both;
   whether it happens empty-handed; and the comparison results. Preserve the log
   from each run before restarting. Avoid changing multiple other mods at once.

Validation: Java 17 build/reobfuscation and 25 existing tests pass. The modified
injection targets TaCZ's exact production attachment-render method descriptor.
No full-pack Minecraft reproduction or headset validation was performed here.
