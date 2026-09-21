package dev.visorcompat.tacz.server;
import dev.visorcompat.tacz.compat.GunDurabilityCompat;
import dev.visorcompat.tacz.physical.Jam;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public final class ServerJams {
    public static final String TYPE="visor_tacz_jam", LEFT="visor_tacz_obstructions";
    public static Jam read(ItemStack stack) {
        if(!stack.hasTag())return new Jam(Jam.Kind.NONE,0);
        try {return new Jam(Jam.Kind.valueOf(stack.getTag().getString(TYPE)),Math.max(0,Math.min(2,stack.getTag().getInt(LEFT))));}
        catch(IllegalArgumentException e){return new Jam(Jam.Kind.NONE,0);}
    }
    private static void write(ItemStack stack,Jam jam) {
        stack.getOrCreateTag().putString(TYPE,jam.kind().name());stack.getTag().putInt(LEFT,jam.remaining());
    }
    public static void start(ServerPlayer p,Jam.Kind requested) {
        var stack=p.getMainHandItem();var gun=IGun.getIGunOrNull(stack);
        if(!dev.visorcompat.tacz.Profiles.physical(stack) || dev.visorcompat.tacz.Profiles.manualAction(stack) || !GunDurabilityCompat.supported() || gun==null || !GunDurabilityCompat.jammed(stack) || read(stack).kind()!=Jam.Kind.NONE)return;
        var result=Jam.allocate(requested,gun.getCurrentAmmoCount(stack),gun.hasBulletInBarrel(stack));
        gun.setCurrentAmmoCount(stack,result.magazine());gun.setBulletInBarrel(stack,result.chamber());
        write(stack,result.jam());p.inventoryMenu.broadcastChanges();
        if(result.jam().kind()!=Jam.Kind.DUD)HandlingFeedback.emit(p,6,ServerPoses.validated(p));
    }
    public static void observe(ServerPlayer p) {
        var stack=p.getMainHandItem();
        if(GunDurabilityCompat.jammed(stack) && read(stack).kind()==Jam.Kind.NONE) {
            var choices=new Jam.Kind[]{Jam.Kind.STOVEPIPE,Jam.Kind.DOUBLE_FEED,Jam.Kind.DUD};
            start(p,choices[p.getRandom().nextInt(choices.length)]);
        }
    }
    /** Native desktop unjam or removing the optional mod releases un-ejected reservations. */
    public static void reconcile(ServerPlayer p,ItemStack stack) {
        var state=read(stack);if(state.kind()==Jam.Kind.NONE || GunDurabilityCompat.jammed(stack))return;
        var gun=IGun.getIGunOrNull(stack);
        if(gun!=null && state.refundable()>0) {
            var common=com.tacz.guns.api.TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);
            if(common==null)return;
            var ammo=com.tacz.guns.api.item.builder.AmmoItemBuilder.create().setId(common.getGunData().getAmmoId()).setCount(state.refundable()).build();
            stack.getTag().remove(TYPE);stack.getTag().remove(LEFT);
            p.getInventory().add(ammo);if(!ammo.isEmpty())p.drop(ammo,false);
        }
        stack.getTag().remove(TYPE);stack.getTag().remove(LEFT);
    }
    public static boolean pull(ServerPlayer p,boolean magazineOut) {return pull(p,magazineOut,false);}
    public static boolean pull(ServerPlayer p,boolean magazineOut,boolean plucked) {
        var stack=p.getMainHandItem();var before=read(stack);
        if(before.kind()==Jam.Kind.NONE)return false;
        var after=before.pull(magazineOut);write(stack,after);
        if(after.remaining()<before.remaining())HandlingFeedback.emit(p,before.kind()==Jam.Kind.STOVEPIPE?4:3,ServerPoses.validated(p),plucked);
        return true;
    }
    public static void finish(ServerPlayer p) {
        if(read(p.getMainHandItem()).remaining()==0 && GunDurabilityCompat.clear(p,p.getMainHandItem()))reconcile(p,p.getMainHandItem());
    }
}
