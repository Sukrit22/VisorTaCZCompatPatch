package dev.visorcompat.tacz.physical;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.world.item.ItemStack;
/** Preserve native open-bolt ammo accounting: those guns fire directly from the magazine. */
public final class NativeAction {
    private NativeAction() {}
    public static boolean openBolt(ItemStack stack){var gun=IGun.getIGunOrNull(stack);return gun!=null&&TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).map(i->i.getGunData().getBolt()==Bolt.OPEN_BOLT).orElse(false);}
    public static boolean ready(ItemStack stack){var gun=IGun.getIGunOrNull(stack);return gun!=null&&ready(openBolt(stack),gun.getCurrentAmmoCount(stack),gun.hasBulletInBarrel(stack));}
    public static boolean ready(boolean openBolt,int magazine,boolean barrel){return openBolt?magazine>0:barrel;}
    public static Chamber close(Chamber chamber,boolean openBolt,boolean magazinePresent,boolean jammed){return magazinePresent&&!jammed&&!openBolt?chamber.feed():chamber;}
}
