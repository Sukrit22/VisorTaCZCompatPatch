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
        if(dev.visorcompat.tacz.Profiles.pump(stack) && kind!=dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE || !ServerPhysical.enabled(p) || (ServerPhysical.phase(p)!=dev.visorcompat.tacz.physical.Handling.Phase.READY && ServerPhysical.phase(p)!=dev.visorcompat.tacz.physical.Handling.Phase.NEED_RACK)
            || ServerJams.read(stack).kind()!=dev.visorcompat.tacz.physical.Jam.Kind.NONE
            || !dev.visorcompat.tacz.compat.GunDurabilityCompat.setJammed(p,stack,true)) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Hold an unjammed supported gun in VR physical mode with gundb 2.2.2. Finish any existing jam first."));return 0;
        }
        ServerJams.start(p,kind);
        source.sendSuccess(()->net.minecraft.network.chat.Component.literal("Test jam: "+(dev.visorcompat.tacz.Profiles.pump(stack)?"GENERIC M870":ServerJams.read(stack).kind())+". Double feed needs two available rounds; otherwise a simpler jam is used."),false);
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
                if(dev.visorcompat.tacz.Profiles.pump(stack) && (!dev.visorcompat.tacz.network.CompatNetwork.physical(player) || !ServerPoses.isVr(player)) && stack.hasTag()){
                    stack.getTag().remove(ServerPump.OPEN);stack.getTag().remove(ServerPump.SPENT);
                }
            }
            ServerPhysical.tick(player);RemoteSync.tick(player);}
    }
    @SubscribeEvent public static void reload(com.tacz.guns.api.event.common.GunReloadEvent event) {
        if(event.getLogicalSide().isServer() && event.getEntity() instanceof ServerPlayer player && !ServerPhysical.allowsReload(player)) event.setCanceled(true);
    }
}
