package dev.visorcompat.tacz.mixin;

import com.tacz.guns.api.item.IGun;
import dev.visorcompat.tacz.HotbarCollisionPolicy;
import dev.visorcompat.tacz.client.AdvancedClient;
import dev.visorcompat.tacz.client.CompatSettings;
import dev.visorcompat.tacz.client.DualClient;
import dev.visorcompat.tacz.client.FourHolsterSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;

/** Reject a selector's collision without moving the hand that already owns the gun. */
@Mixin(value=TaskHotBar.class,remap=false)
public abstract class HeldHotbarMixin {
    @Shadow private static int slotMain;
    @Shadow private static int slotOffhand;
    @Shadow private static int slotMainBack;
    @Shadow private static int slotOffhandBack;
    @Unique private int visorTacz$previousMain;
    @Unique private int visorTacz$previousOff = TaskHotBar.NOT_SELECTED;
    @Unique private int visorTacz$advancedHeld = -1;

    @Inject(method="onRun",at=@At("HEAD"))
    private void visorTacz$snapshot(LocalPlayer player,CallbackInfo ci){
        if(player==null)return;
        int selected=player.getInventory().selected;
        visorTacz$previousMain=selected==slotOffhand?slotMain:selected;
        visorTacz$previousOff=slotOffhand;
        visorTacz$advancedHeld=AdvancedClient.protectedSlot();
        // Wheel/number selection arrives before onRun, and native code clears offhand
        // at its tail without invoking handleSlotCollision. Keep that owner as well.
        if(selected==slotOffhand&&selected>=0&&selected<9){
            boolean gun=IGun.getIGunOrNull(player.getInventory().getItem(selected))!=null;
            boolean held=CompatSettings.dualWield()&&DualClient.boundSlot(HandType.OFFHAND)==selected&&DualClient.held(HandType.OFFHAND);
            if(HotbarCollisionPolicy.protects(gun,held,CompatSettings.active()&&FourHolsterSettings.lockSelected())){
                slotMain=HotbarCollisionPolicy.previousSelection(true,visorTacz$previousMain,selected);
                player.getInventory().selected=slotMain;
                slotMainBack=slotOffhandBack=-2;
            }
        }
    }
    @Inject(method="setOffhandSlot",at=@At("HEAD"))
    private void visorTacz$snapshotExternal(int selected,CallbackInfo ci){
        var player=Minecraft.getInstance().player;
        if(player==null)return;
        visorTacz$previousMain=player.getInventory().selected;
        visorTacz$previousOff=slotOffhand;
        visorTacz$advancedHeld=AdvancedClient.protectedSlot();
    }
    @Inject(method="handleSlotCollision",at=@At("HEAD"),cancellable=true)
    private void visorTacz$protectHeldSlot(HandType owner,boolean switchableBack,CallbackInfo ci){
        var player=Minecraft.getInstance().player;
        if(player==null||slotMain!=slotOffhand||slotMain<0||slotMain>8)return;
        int occupied=slotMain;
        boolean gun=IGun.getIGunOrNull(player.getInventory().getItem(occupied))!=null;
        boolean held=owner==HandType.MAIN&&visorTacz$advancedHeld==occupied;
        held|=CompatSettings.dualWield()&&DualClient.boundSlot(owner)==occupied&&DualClient.held(owner);
        if(!HotbarCollisionPolicy.protects(gun,held,CompatSettings.active()&&FourHolsterSettings.lockSelected()))return;
        // The argument identifies the displaced owner: MAIN means OFFHAND is selecting.
        if(owner==HandType.MAIN){
            slotOffhand=HotbarCollisionPolicy.previousSelection(false,visorTacz$previousOff,occupied);
        }else{
            slotMain=HotbarCollisionPolicy.previousSelection(true,visorTacz$previousMain,occupied);
            player.getInventory().selected=slotMain;
        }
        // Native collision backups could later restore the rejected candidate.
        slotMainBack=slotOffhandBack=-2;
        ci.cancel();
    }
}
