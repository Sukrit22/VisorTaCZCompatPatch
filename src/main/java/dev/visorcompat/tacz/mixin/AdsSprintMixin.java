package dev.visorcompat.tacz.mixin;
import net.minecraft.client.player.LocalPlayer;
import dev.visorcompat.tacz.client.AutoAds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=LocalPlayer.class,remap=false)
public abstract class AdsSprintMixin {
    @Inject(method={"aiStep","m_8107_"},at=@At("TAIL"))
    private void visorTacz$adsStopsSprint(CallbackInfo ci) {
        // After vanilla/VR locomotion has considered sprint input, before movement packets.
        if(AutoAds.ownsInput() && AutoAds.aiming())((LocalPlayer)(Object)this).setSprinting(false);
    }
}

