package dev.visorcompat.tacz.mixin;

import com.tacz.guns.client.input.ShootKey;
import dev.visorcompat.tacz.client.AdvancedClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla attack may run while holstered, but must not also fire the selected TaCZ gun. */
@Mixin(value=ShootKey.class,remap=false)
public abstract class HolsteredShootMixin {
    @Inject(method="autoShoot",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$holstered(CallbackInfo ci){
        if(AdvancedClient.holstered()||dev.visorcompat.tacz.client.DualClient.active()){
            com.tacz.guns.client.gameplay.LocalPlayerSprint.stopSprint=false;
            ci.cancel();
        }
    }
}
