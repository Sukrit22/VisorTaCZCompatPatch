package dev.visorcompat.tacz.mixin;

import com.tacz.guns.event.PreventGunClick;
import dev.visorcompat.tacz.server.ServerAdvanced;
import dev.visorcompat.tacz.server.ServerDual;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restore vanilla block damage only when a selected inventory gun is out of the hand. */
@Mixin(value=PreventGunClick.class,remap=false)
public abstract class HolsteredMiningMixin {
    @Inject(method="onLeftClickBlock",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$bareHand(PlayerInteractEvent.LeftClickBlock event,CallbackInfo ci){
        var player=event.getEntity();
        if(player instanceof ServerPlayer server){
            if(ServerAdvanced.holstered(server)||ServerDual.holsteredMain(server))ci.cancel();
        }else if(player.level().isClientSide){
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                ()->()->{if(dev.visorcompat.tacz.client.AdvancedClient.holstered()
                    ||dev.visorcompat.tacz.client.DualClient.active()&&!dev.visorcompat.tacz.client.DualClient.held(org.vmstudio.visor.api.common.HandType.MAIN))ci.cancel();});
        }
    }
}
