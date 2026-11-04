package dev.visorcompat.tacz.server;

import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.server.level.ServerPlayer;

/** Server-authorized, vanilla-persisted player preference; leaves recoil and ADS state untouched. */
public final class HipFirePolicy {
    private HipFirePolicy(){}
    private static final String TAG="visor_tacz_ads_accuracy";
    public static boolean applies(net.minecraft.world.entity.LivingEntity entity){
        return entity instanceof ServerPlayer p&&p.getTags().contains(TAG)&&CompatNetwork.enabled(p)&&ServerPoses.isVr(p)
            &&(ServerDual.active(p)?DualGunOperator.current(p)!=null:ServerPoses.validated(p)!=null);
    }
    public static void commands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher){
        var root=net.minecraft.commands.Commands.literal("visor_tacz_accuracy")
            .requires(s->s.hasPermission(2)||s.getEntity() instanceof ServerPlayer p&&s.getServer().isSingleplayerOwner(p.getGameProfile()));
        for(boolean ads:new boolean[]{false,true})root.then(net.minecraft.commands.Commands.literal(ads?"ads":"native")
            .executes(c->set(c.getSource(),c.getSource().getPlayerOrException(),ads))
            .then(net.minecraft.commands.Commands.argument("player",net.minecraft.commands.arguments.EntityArgument.player()).requires(s->s.hasPermission(2))
                .executes(c->set(c.getSource(),net.minecraft.commands.arguments.EntityArgument.getPlayer(c,"player"),ads))));
        dispatcher.register(root);
    }
    private static int set(net.minecraft.commands.CommandSourceStack source,ServerPlayer p,boolean ads){
        if(ads)p.addTag(TAG);else p.removeTag(TAG);
        source.sendSuccess(()->net.minecraft.network.chat.Component.literal("TaCZ VR accuracy for "+p.getGameProfile().getName()+": "+(ads?"ADS-equivalent hip fire":"native hip fire")+". Recoil remains unchanged."),false);return 1;
    }
}
