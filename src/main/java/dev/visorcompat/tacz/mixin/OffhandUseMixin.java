package dev.visorcompat.tacz.mixin;

import com.tacz.guns.client.event.ClientPreventGunClick;
import dev.visorcompat.tacz.client.ClientControls;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ClientPreventGunClick.class,remap=false)
public abstract class OffhandUseMixin {
    @Inject(method="onClickInput",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$offhand(InputEvent.InteractionKeyMappingTriggered event,CallbackInfo ci) {
        var player=Minecraft.getInstance().player;
        if(ClientControls.vrActive() && player!=null && event.isUseItem()
            && event.getHand()==InteractionHand.OFF_HAND && !player.getOffhandItem().isEmpty())ci.cancel();
    }
}
