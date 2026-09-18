package dev.visorcompat.tacz.mixin;
import com.tacz.guns.util.InputExtraCheck;
import dev.visorcompat.tacz.client.ClientControls;
import net.minecraft.client.Minecraft;
import org.vmstudio.visor.api.VisorAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=InputExtraCheck.class,remap=false)
public abstract class VrInputMixin {
    @Inject(method="isInGame",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$vrFocus(CallbackInfoReturnable<Boolean> ci) {
        if(ClientControls.vrActive() && ClientControls.holdingGun()) {
            var mc=Minecraft.getInstance();
            ci.setReturnValue(mc.player!=null && mc.screen==null && mc.getOverlay()==null
                && !mc.isPaused() && VisorAPI.clientState().stateMode().isFocused());
        }
    }
}
