package dev.visorcompat.tacz.server;

import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.event.common.GunFireEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ServerEvents {
    @SubscribeEvent public static void commands(net.minecraftforge.event.RegisterCommandsEvent event) {
        var jam=net.minecraft.commands.Commands.literal("jam").executes(c->testJam(c.getSource(),dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE));
        for(var kind:dev.visorcompat.tacz.physical.Jam.Kind.values())if(kind!=dev.visorcompat.tacz.physical.Jam.Kind.NONE)
            jam.then(net.minecraft.commands.Commands.literal(kind.name().toLowerCase(java.util.Locale.ROOT)).executes(c->testJam(c.getSource(),kind)));
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("visor_tacz_test")
            .requires(source->source.hasPermission(2)).then(jam));
    }
    private static int testJam(net.minecraft.commands.CommandSourceStack source,dev.visorcompat.tacz.physical.Jam.Kind kind)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var p=source.getPlayerOrException();var stack=p.getMainHandItem();
        String failure=null;
        if(dev.visorcompat.tacz.Profiles.get(stack)==null)
            failure="The server's selected main-hand item is not a supported gun: "+stack.getHoverName().getString()+". Select the gun's hotbar slot before running this command.";
        else if(!dev.visorcompat.tacz.network.CompatNetwork.modeKnown(p))
            failure="The server has not received your VR handling mode yet. Reconnect and try again.";
        else if(!dev.visorcompat.tacz.network.CompatNetwork.physical(p))
            failure="Physical handling is disabled. Enable physical handling before testing a jam.";
        else if(!ServerPoses.isVr(p))
            failure="The addon is inactive or the server does not recognize you as a Visor VR player. Enable VR and the addon; if needed, reconnect while in VR.";
        else if(dev.visorcompat.tacz.Profiles.manualAction(stack) && kind!=dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE)
            failure="This manual-action gun only supports the generic jam test. Use /visor_tacz_test jam.";
        else if(ServerPhysical.phase(p)!=dev.visorcompat.tacz.physical.Handling.Phase.READY && ServerPhysical.phase(p)!=dev.visorcompat.tacz.physical.Handling.Phase.NEED_RACK)
            failure="Finish the current physical action before testing a jam. Server phase: "+ServerPhysical.phase(p);
        else if(ServerJams.read(stack).kind()!=dev.visorcompat.tacz.physical.Jam.Kind.NONE)
            failure="Clear the existing jam first: "+ServerJams.read(stack).kind();
        else if(!dev.visorcompat.tacz.compat.GunDurabilityCompat.supported())
            failure="Jam testing requires the supported TaCZ: Durability (gundb 2.2.2) integration on the server. Check the server log for adapter errors.";
        else if(!dev.visorcompat.tacz.compat.GunDurabilityCompat.setJammed(p,stack,true))
            failure="Durability could not apply the jam: this gun may lack durability modifiers, or synchronization failed. Check the server log.";
        if(failure!=null){source.sendFailure(net.minecraft.network.chat.Component.literal(failure));return 0;}
        ServerJams.start(p,kind);
        source.sendSuccess(()->net.minecraft.network.chat.Component.literal("Test jam: "+(dev.visorcompat.tacz.Profiles.manualAction(stack)?"GENERIC MANUAL ACTION":ServerJams.read(stack).kind())+". Double feed needs two available rounds; otherwise a simpler jam is used."),false);
        return 1;
    }
    @SubscribeEvent
    public static void beforeShot(GunShootEvent event) {
        if (event.getLogicalSide().isServer() && event.getShooter() instanceof ServerPlayer player
                && ServerPoses.isVr(player) && (ServerPoses.validated(player) == null || !ServerPhysical.canFire(player))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void beforeRound(GunFireEvent event) {
        // TaCZ fires this for every delayed burst round, before consuming ammunition.
        if (event.getLogicalSide().isServer() && event.getShooter() instanceof ServerPlayer player
                && ServerPoses.isVr(player) && (ServerPoses.validated(player) == null || !ServerPhysical.canFire(player))) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if(event.phase==net.minecraftforge.event.TickEvent.Phase.END && event.player instanceof ServerPlayer player) {for(var stack:player.getInventory().items){
                ServerJams.reconcile(player,stack);
                if(dev.visorcompat.tacz.network.CompatNetwork.modeKnown(player) && dev.visorcompat.tacz.Profiles.get(stack)!=null && (!dev.visorcompat.tacz.network.CompatNetwork.physical(player) || !ServerPoses.isVr(player)) && stack.hasTag()){
                    stack.getTag().remove(dev.visorcompat.tacz.physical.ActionState.LOCKED);stack.getTag().remove(dev.visorcompat.tacz.physical.ActionState.LIFTED);
                    stack.getTag().remove(ServerPump.OPEN);stack.getTag().remove(ServerPump.SPENT);
                    stack.getTag().remove(dev.visorcompat.tacz.physical.BoltState.OPEN);stack.getTag().remove(dev.visorcompat.tacz.physical.BoltState.SPENT);
                }
            }
            ServerPhysical.tick(player);RemoteSync.tick(player);}
    }
    @SubscribeEvent public static void reload(com.tacz.guns.api.event.common.GunReloadEvent event) {
        if(event.getLogicalSide().isServer() && event.getEntity() instanceof ServerPlayer player && !ServerPhysical.allowsReload(player)) event.setCanceled(true);
    }
}
