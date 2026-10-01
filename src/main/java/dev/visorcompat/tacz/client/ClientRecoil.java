package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import java.util.*;
@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT)
public final class ClientRecoil {
    private record State(String key,RecoilMotion motion){}
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static Object world;
    private static void checkWorld(){var level=Minecraft.getInstance().level;if(world!=level){STATES.clear();world=level;}}
    public static void receive(CompatNetwork.RecoilShot m){checkWorld();if(world==null||!m.curve().valid())return;var s=STATES.get(m.player());if(s==null||!s.key.equals(m.key())){s=new State(m.key(),new RecoilMotion());STATES.put(m.player(),s);}s.motion.add(m.tick(),m.curve());}
    public static GunPose apply(Player p,GunPose pose,float partial){checkWorld();var s=STATES.get(p.getUUID());if(s==null)return pose;if(!s.key.equals(Profiles.key(p.getMainHandItem()))){STATES.remove(p.getUUID());return pose;}return s.motion.apply(pose,p.level().getGameTime()+partial);}
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;checkWorld();var mc=Minecraft.getInstance();if(mc.player!=null&&!ClientControls.vrActive())STATES.remove(mc.player.getUUID());
        if(mc.level!=null)STATES.entrySet().removeIf(entry->{var p=mc.level.getPlayerByUUID(entry.getKey());return p==null||!entry.getValue().key.equals(Profiles.key(p.getMainHandItem()));});
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){STATES.clear();world=null;}
}
