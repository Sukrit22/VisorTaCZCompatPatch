package dev.visorcompat.tacz.mixin;

import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import dev.visorcompat.tacz.client.ClientControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MuzzleFlashRender.class, remap = false)
public abstract class MuzzleFlashMixin {
    @Shadow private static boolean muzzleFlashStartMark;
    @Inject(method = "renderMuzzleFlash", at = @At("HEAD"))
    private static void visorTacz$eachEye(CallbackInfo ci) {
        // TaCZ normally caches a camera-relative matrix for the entire flash. In VR
        // each eye needs its own current matrix, even within a single display frame.
        if (ClientControls.vrActive()) muzzleFlashStartMark = true;
    }
}
