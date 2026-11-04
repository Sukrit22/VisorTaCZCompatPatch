package dev.visorcompat.tacz.physical;

import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Native gun data stays authoritative; these tags describe only detached magazines. */
public final class TacticalState {
    public static final String PREFIX="visor_tacz_tactical_";
    private TacticalState(){}
    public static boolean exists(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(PREFIX+"active");}
    public static TacticalLedger read(ItemStack stack){return new TacticalLedger(count(stack,false),count(stack,true),out(stack));}
    public static int count(ItemStack stack,boolean pinch){String key=PREFIX+(pinch?"pinch":"grab");return stack.hasTag()&&stack.getTag().contains(key)?Math.max(-1,Math.min(4096,stack.getTag().getInt(key))):-1;}
    public static boolean out(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean(PREFIX+"out");}
    public static void write(ItemStack stack,TacticalLedger ledger){stack.getOrCreateTag().putBoolean(PREFIX+"active",true);stack.getOrCreateTag().putBoolean(PREFIX+"out",ledger.out());stack.getOrCreateTag().putInt(PREFIX+"grab",ledger.rounds(false));stack.getOrCreateTag().putInt(PREFIX+"pinch",ledger.rounds(true));}
    public static void refundable(ItemStack stack,boolean pinch,boolean value){stack.getOrCreateTag().putBoolean(PREFIX+(pinch?"pinch_paid":"grab_paid"),value);}
    public static boolean refundable(ItemStack stack,boolean pinch){return stack.hasTag()&&stack.getTag().getBoolean(PREFIX+(pinch?"pinch_paid":"grab_paid"));}
    public static void guide(ItemStack stack,int value){stack.getOrCreateTag().putInt(PREFIX+"guide",value);}
    public static int guide(ItemStack stack){return exists(stack)?stack.getTag().getInt(PREFIX+"guide"):0;}
    public static void capture(ItemStack stack,Vector3f position,Quaternionf rotation){capture(stack,false,position,rotation);}
    public static void capture(ItemStack stack,boolean pinch,Vector3f position,Quaternionf rotation){
        String suffix=pinch?"pinch_":"";
        stack.getOrCreateTag().putBoolean(PREFIX+suffix+"captured",true);
        float[] values={position.x,position.y,position.z,rotation.x,rotation.y,rotation.z,rotation.w};
        for(int i=0;i<values.length;i++)stack.getOrCreateTag().putInt(PREFIX+suffix+"pose"+i,Float.floatToIntBits(values[i]));
    }
    public static void unanchor(ItemStack stack){unanchor(stack,false);}
    public static void unanchor(ItemStack stack,boolean pinch){stack.getOrCreateTag().putBoolean(PREFIX+(pinch?"pinch_":"")+"captured",false);}
    public static void old(ItemStack stack,boolean pinch,boolean old){stack.getOrCreateTag().putBoolean(PREFIX+(pinch?"pinch_old":"grab_old"),old);}
    public static boolean old(ItemStack stack,boolean pinch){return stack.hasTag()&&stack.getTag().getBoolean(PREFIX+(pinch?"pinch_old":"grab_old"));}
    public static boolean captured(ItemStack stack,boolean pinch){return stack.hasTag()&&stack.getTag().getBoolean(PREFIX+(pinch?"pinch_":"")+"captured");}
    private static float value(ItemStack stack,boolean pinch,int i){return Float.intBitsToFloat(stack.getTag().getInt(PREFIX+(pinch?"pinch_":"")+"pose"+i));}
    public static Vector3f position(ItemStack stack,boolean pinch){return new Vector3f(value(stack,pinch,0),value(stack,pinch,1),value(stack,pinch,2));}
    public static Quaternionf rotation(ItemStack stack,boolean pinch){return new Quaternionf(value(stack,pinch,3),value(stack,pinch,4),value(stack,pinch,5),value(stack,pinch,6));}
    public static void clear(ItemStack stack){if(!stack.hasTag())return;for(String key:java.util.List.copyOf(stack.getTag().getAllKeys()))if(key.startsWith(PREFIX))stack.getTag().remove(key);}
}
