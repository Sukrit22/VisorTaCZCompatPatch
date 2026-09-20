package dev.visorcompat.tacz.physical;
import net.minecraft.world.item.ItemStack;
/** Only action flags; live ammunition stays in native TaCZ fields. */
public final class ActionState {
    public static final String LOCKED="visor_tacz_action_locked", LIFTED="visor_tacz_bolt_lifted";
    public static boolean locked(ItemStack s){return s.hasTag()&&s.getTag().getBoolean(LOCKED);}
    public static boolean lifted(ItemStack s){return s.hasTag()&&s.getTag().getBoolean(LIFTED);}
    public static void locked(ItemStack s,boolean value){s.getOrCreateTag().putBoolean(LOCKED,value);}
    public static void lifted(ItemStack s,boolean value){s.getOrCreateTag().putBoolean(LIFTED,value);}
}
