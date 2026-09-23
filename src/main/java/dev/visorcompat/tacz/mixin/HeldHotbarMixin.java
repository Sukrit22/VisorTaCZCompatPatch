package dev.visorcompat.tacz.mixin;
import dev.visorcompat.tacz.client.AdvancedClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;

/** Refuse only the occupied gun slot; all other offhand hotbar choices still work. */
@Mixin(value=TaskHotBar.class,remap=false)
public abstract class HeldHotbarMixin {
    @Inject(method="handleSlotCollision",at=@At("HEAD"),cancellable=true)
    private void visorTacz$protectHeldSlot(HandType hand,boolean switchableBack,CallbackInfo ci){
        int held=AdvancedClient.protectedSlot();var player=Minecraft.getInstance().player;
        if(hand==HandType.MAIN&&held>=0&&player!=null&&player.getInventory().selected==held&&TaskHotBar.getSlotOffhand()==held){
            TaskHotBar.setSlotOffhand(TaskHotBar.NOT_SELECTED);ci.cancel();
        }
    }
}
