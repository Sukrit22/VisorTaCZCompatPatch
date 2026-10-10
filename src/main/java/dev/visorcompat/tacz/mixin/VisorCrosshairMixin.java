package dev.visorcompat.tacz.mixin;

import dev.visorcompat.tacz.client.ClientControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.core.client.render.decoration.effects.hand.HandEffectCrosshair;

/** Hide each occupied hand's world reticle without disabling Visor's GUI cursor. */
@Mixin(value=HandEffectCrosshair.class,remap=false)
public abstract class VisorCrosshairMixin {
    @Inject(method="isVisible",at=@At("HEAD"),cancellable=true)
    private void visorTacz$heldGun(VRDecorator decorator,HandType hand,boolean guiHand,CallbackInfoReturnable<Boolean> ci){
        if(!guiHand&&ClientControls.hideWorldCursor(hand))ci.setReturnValue(false);
    }
}
