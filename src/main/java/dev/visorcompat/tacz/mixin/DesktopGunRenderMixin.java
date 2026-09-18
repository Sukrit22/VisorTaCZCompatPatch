package dev.visorcompat.tacz.mixin;

import com.tacz.guns.client.event.FirstPersonRenderEvent;
import dev.visorcompat.tacz.client.ClientControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FirstPersonRenderEvent.class, remap = false)
public abstract class DesktopGunRenderMixin {
    @Inject(method = "onRenderHand", at = @At("HEAD"), cancellable = true)
    private static void visorTacz$skipDesktop(CallbackInfo ci) {
        if (ClientControls.vrActive()) ci.cancel();
    }
}
