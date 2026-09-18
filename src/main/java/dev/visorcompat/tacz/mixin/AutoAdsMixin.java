package dev.visorcompat.tacz.mixin;
import com.tacz.guns.client.input.AimKey;
import dev.visorcompat.tacz.client.AutoAds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=AimKey.class,remap=false)
public abstract class AutoAdsMixin {
    @Inject(method={"onAimPress","onAimHoldingPreInput"},at=@At("HEAD"),cancellable=true)
    private static void visorTacz$ads(CallbackInfo ci) {if(AutoAds.ownsInput())ci.cancel();}
    @Inject(method="onAimControllerPress",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$adsController(boolean press,CallbackInfoReturnable<Boolean> ci) {if(AutoAds.ownsInput())ci.setReturnValue(false);}
}
