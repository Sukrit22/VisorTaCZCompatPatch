package dev.visorcompat.tacz.mixin;
import com.tacz.guns.client.gameplay.LocalPlayerBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=LocalPlayerBolt.class,remap=false)
public abstract class ClientPumpBoltMixin {
    @Inject(method="bolt",at=@At("HEAD"),cancellable=true)
    private void physicalPump(CallbackInfo ci){var p=net.minecraft.client.Minecraft.getInstance().player;if(p!=null && dev.visorcompat.tacz.Profiles.pump(p.getMainHandItem()) && dev.visorcompat.tacz.client.PhysicalClient.active())ci.cancel();}
}
