package dev.visorcompat.tacz.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import java.lang.reflect.*;

/** Optional adapter for the inspected gundb 2.2.2 Jammed tag and sync packet. */
public final class GunDurabilityCompat {
    private GunDurabilityCompat() {}
    public static boolean jammed(ItemStack stack) {
        return ModList.get().isLoaded("gundb") && stack.hasTag() && stack.getTag().getBoolean("Jammed");
    }
    private static boolean checked;
    private static Constructor<?> sync;
    private static Method send, modifiers;
    public static boolean supported() {return available();}
    private static boolean available() {
        if(checked)return sync!=null;
        checked=true;
        var mod=ModList.get().getModContainerById("gundb");
        if(mod.isEmpty())return false;
        if(!mod.get().getModInfo().getVersion().toString().equals("2.2.2")) {
            com.mojang.logging.LogUtils.getLogger().warn("TaCZ VR: physical unjam requires verified gundb 2.2.2; leaving normal unjam controls intact");
            return false;
        }
        try {
            var constructor=Class.forName("mod.cdv.gdb.network.SyncJammedPacket").getConstructor(boolean.class);
            send=Class.forName("mod.cdv.gdb.network.NetworkHandler").getMethod("sendToClient",Object.class,ServerPlayer.class);
            modifiers=Class.forName("mod.cdv.gdb.DataLookup").getMethod("getModifiers",ItemStack.class);
            sync=constructor;
        } catch(ReflectiveOperationException | LinkageError e) {
            com.mojang.logging.LogUtils.getLogger().error("TaCZ VR: optional physical unjam unavailable",e);
        }
        return sync!=null;
    }
    /** Called only after the server validates a full held-gun rack and release. */
    public static boolean clear(ServerPlayer player,ItemStack stack) {
        if(!jammed(stack))return false;
        return setJammed(player,stack,false);
    }
    /** Operator-only test command also uses the native client sync packet. */
    public static boolean setJammed(ServerPlayer player,ItemStack stack,boolean jammed) {
        if(stack!=player.getMainHandItem() || !available())return false;
        try {
            if(modifiers.invoke(null,stack)==null)return false;
            Object packet=sync.newInstance(jammed);
            boolean old=jammed(stack);
            stack.getOrCreateTag().putBoolean("Jammed",jammed);
            try { send.invoke(null,packet,player); }
            catch(ReflectiveOperationException | LinkageError e) {stack.getTag().putBoolean("Jammed",old);throw e;}
            player.inventoryMenu.broadcastChanges();
            return true;
        } catch(ReflectiveOperationException | LinkageError e) {
            com.mojang.logging.LogUtils.getLogger().error("TaCZ VR: cannot synchronize physical unjam",e);
            return false;
        }
    }
}
