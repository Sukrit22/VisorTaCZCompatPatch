package dev.visorcompat.tacz.mixin;
import com.tacz.guns.client.gameplay.LocalPlayerReload;
import dev.visorcompat.tacz.client.PhysicalClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=LocalPlayerReload.class,remap=false)
public abstract class PhysicalReloadMixin {
    @Inject(method="reload",at=@At("HEAD"),cancellable=true)
    private void visorTacz$physicalReload(CallbackInfo ci) { if(PhysicalClient.active()) ci.cancel(); }
}
