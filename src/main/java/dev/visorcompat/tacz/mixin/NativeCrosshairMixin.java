package dev.visorcompat.tacz.mixin;

import com.tacz.guns.client.event.RenderCrosshairEvent;
import dev.visorcompat.tacz.client.ClientControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.common.HandType;

/** The desktop reticle is camera-centered; VR weapons have controller-owned aim. */
@Mixin(value=RenderCrosshairEvent.class,remap=false)
public abstract class NativeCrosshairMixin {
    @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$controllerAim(CallbackInfo ci){
        if(ClientControls.hideWorldCursor(HandType.MAIN)||ClientControls.hideWorldCursor(HandType.OFFHAND))ci.cancel();
    }
}

