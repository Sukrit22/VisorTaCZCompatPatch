package dev.visorcompat.tacz.mixin;

import com.tacz.guns.client.event.CameraSetupEvent;
import dev.visorcompat.tacz.client.ClientControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CameraSetupEvent.class, remap = false)
public abstract class CameraMixin {
    @Inject(method = {"applyLevelCameraAnimation", "applyItemInHandCameraAnimation",
            "applyScopeMagnification", "applyGunModelFovModifying", "initialCameraRecoil",
            "applyCameraRecoil", "onComputeMovementFov"}, at = @At("HEAD"), cancellable = true)
    private static void visorTacz$comfort(CallbackInfo ci) {
        if (ClientControls.vrActive()) ci.cancel();
    }
}
