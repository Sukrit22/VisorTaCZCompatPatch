package dev.visorcompat.tacz.physical;
import net.minecraft.world.item.ItemStack;

/** Persisted cosmetic action state. Native TaCZ owns all live ammunition counts. */
public final class BoltState {
    public static final String OPEN="visor_tacz_bolt_open", SPENT="visor_tacz_bolt_spent";
    public static boolean open(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(OPEN);}
    public static boolean spent(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(SPENT);}
    public static boolean blocked(ItemStack stack){return open(stack)||spent(stack);}
}
