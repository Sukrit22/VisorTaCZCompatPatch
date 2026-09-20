package dev.visorcompat.tacz.physical;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/** Read-only preview check; insertion still performs TaCZ's authoritative extraction. */
public final class PouchAmmo {
    public static boolean magazineLoaded(Player player,Handling.Phase phase) {
        var stack=player.getMainHandItem();
        return phase==Handling.Phase.OLD_MAG ? stack.hasTag() && stack.getTag().getInt("visor_tacz_reserved_magazine")>0 : available(player,stack);
    }
    public static boolean available(Player player,ItemStack stack) {
        var gun=IGun.getIGunOrNull(stack);if(gun==null)return false;
        if(!IGunOperator.fromLivingEntity(player).needCheckAmmo())return true;
        if(gun.useDummyAmmo(stack))return gun.getDummyAmmoAmount(stack)>0;
        return player.getCapability(ForgeCapabilities.ITEM_HANDLER).map(items->{
            for(int i=0;i<items.getSlots();i++) {
                var item=items.getStackInSlot(i);
                if(!item.isEmpty() && item.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(stack,item)
                    && !items.extractItem(i,1,true).isEmpty())return true;
                if(item.getItem() instanceof IAmmoBox box && box.isAmmoBoxOfGun(stack,item) && box.getAmmoCount(item)>0)return true;
            }
            return false;
        }).orElse(false);
    }
}
