# Gun and attachment ADS calibration

On the calibration screen, navigate to the sight page. Its title button switches between **Sight: gun baseline** and **Sight: attachment + view**. The arrow buttons still change pages. The tooltip identifies the gun, attachment and selected sight view being edited.

The baseline stays in the existing `config/visor_tacz-calibration.json`. The optional extra adjustment is stored separately in `config/visor_tacz-sight-calibration.json`; existing gun files need no migration. A missing entry adds zero offset and inherits the baseline box dimensions.

The effective position is `(model sight position + gun baseline offset + attachment offset) × gun model scale`. XYZ are gun-local right/up/back coordinates. Position readouts use millimetres and allow −2000 through +2000 mm for sight offsets only. Other interaction offsets retain their existing limits. Box width/height/depth are full dimensions in room metres at world scale one, independent of model scale.

On the attachment page, changing any box dimension creates an explicit box override. **Inherit gun box** removes that override; later baseline box changes then apply automatically. **Zero this page** clears only the additional attachment position. Save commits both current contexts; Cancel discards unsaved previews. The normal reload-calibration command also reloads attachment sight settings.

Entries use the existing gun/display calibration key plus the exact attachment ID and zero-based selected view, for example:

```json
{
  "tacz:m700|tacz:default|sight=tacz:scope_mk5hd|view=0": {
    "x": 0.0,
    "y": 0.0,
    "z": -0.7,
    "box": { "width": 0.1, "height": 0.1, "depth": 0.4 }
  }
}
```

This is an illustrative key, not a shipped M700 preset. Use the exact key shown by the screen. Omitting `box` inherits the gun baseline; offsets are metres in the file. Different guns, attachments and views keep separate entries. Iron sights also have a separate context using TaCZ's empty attachment ID.

The sight geometry selects the attachment view through TaCZ's zoom-number / `views` mapping instead of always choosing the first view. Single-gun ADS, dual ADS and calibration guides share the same effective position and box calculation. No broader automatic ADS entry angle or depth was introduced: a gun at hip height must still align an eye with its sight zone. These changes need headset checks for the specific M700 hybrid and 552/HCOG combinations; no hardware outcome is claimed by unit tests.

## Physical aperture versus desktop camera marker

For a nonmagnifying sight aperture, ADS now starts from the center of the actual ocular mesh, transformed through the native gun mount and complete attachment-part hierarchy. TaCZ's `scope_view` node is a desktop camera marker, which can sit well behind that mesh. The inspected default 552 asset places `scope_view` at Z=16 model units but its ocular cube at Z approximately −0.157; EXP3 uses camera Z=15 and a similarly near-zero lens plane. Treating these camera markers as physical lens positions introduced an offset of several hundred millimetres after model scaling. This is an attachment-geometry error, rather than a different rail calibration required for each gun.

Magnifying scope apertures retain their authored view/eye-relief marker. Hybrid backup sights use their selected nonmagnifying ocular; the magnifying view keeps its prior behavior. If a custom ocular is absent or uses an unrecognized cube representation, the code conservatively falls back to the native view marker, then the gun's iron-sight marker when no attachment view exists. It does not invent a replacement mount position.

Existing saved offsets are never rewritten. A −300 or −500 mm adjustment that compensated for the old camera-marker position may now overcorrect. Inspect the effective guide and zero that workaround manually when appropriate. The same effective guide appears while editing either the gun baseline or attachment adjustment because it intentionally shows their combined result; switching editing context does not disable the other saved contribution.

Native mounting plus physical aperture geometry should make separate per-gun corrections unnecessary for ordinary correctly authored sights. A future shared attachment correction should live in attachment-local coordinates before the mount transform; blindly reusing the current gun-local offset across differently rotated/scaled mounts would be incorrect. No shared-correction schema was added without that additional design.

The dual long-gun support gate also now checks the same raw controller pose and retention box used to align the supported gun. Previously the model could retain its supported orientation while ADS tested a smaller box against the already aligned/recoiled pose, causing ADS to drop despite a stable-looking gun. Tracking, free-hand ownership, minimum hand separation, and angle requirements remain in force.

Evidence: dependency `timeless-and-classics-zero-yOVIzIJR.jar`, default assets `sight_552_geo.json` / `sight_exp3_geo.json`; TaCZ `BedrockAttachmentModel` camera-view paths and ocular paths; addon `OpticAperture`, `OpticGeometry`, `DualPose`, `PoseMath`, and `DualAds`. Unit tests cover the source 552 window dimensions, native parent/mount transforms, unknown mesh fallback, and raw support retention eligibility. They do not establish an observed headset result.
