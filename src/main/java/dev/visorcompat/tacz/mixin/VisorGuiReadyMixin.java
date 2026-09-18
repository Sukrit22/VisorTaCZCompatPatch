package dev.visorcompat.tacz.mixin;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=VRRenderState.class,remap=false)
public abstract class VisorGuiReadyMixin {
    // Visor 0.5.0 sets ACTIVE and calls resizeDisplay before renderer.init creates
    // guiTarget. Defer the phase change itself, not just the target lookup: this
    // keeps the phase, pass and Minecraft framebuffer consistent during resize.
    @Inject(method="startVRGuiPhase",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$waitForGuiTarget(CallbackInfo ci) {
        var renderer=ClientContext.renderer;
        if(renderer==null || renderer.guiTarget==null || renderer.guiTarget.getTarget()==null) {
            VRRenderState.startVanillaPhase();
            ci.cancel();
        }
    }
}
