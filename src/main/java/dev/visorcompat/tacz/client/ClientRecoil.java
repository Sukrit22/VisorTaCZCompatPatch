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
    private record State(String key,RecoilBinding binding,RecoilMotion motion){}
    public static void clear(UUID player){STATES.remove(player);}
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static Object world;
    private static void checkWorld(){var level=Minecraft.getInstance().level;if(world!=level){STATES.clear();world=level;}}
    public static void receive(CompatNetwork.RecoilShot m){
        checkWorld();var mc=Minecraft.getInstance();if(mc.level==null||!m.curve().valid())return;
        var p=mc.level.getPlayerByUUID(m.player());if(p==null||!m.key().equals(Profiles.key(p.getMainHandItem())))return;
        if(p==mc.player&&(!CompatSettings.recoil()||p.getInventory().selected!=m.slot()))return;
        var s=STATES.get(m.player());
        if(s==null||!s.binding.matches(m.key(),m.slot(),m.generation())){
            s=new State(m.key(),new RecoilBinding(m.key(),m.slot(),m.generation()),new RecoilMotion());STATES.put(m.player(),s);
        }
        s.motion.add(m.tick(),m.curve());
    }
    private static boolean matches(Player p,State s){return s.key.equals(Profiles.key(p.getMainHandItem()))&&(p!=Minecraft.getInstance().player||s.binding.slot()==p.getInventory().selected&&CompatSettings.recoil());}
    public static GunPose apply(Player p,GunPose pose,float partial){checkWorld();var s=STATES.get(p.getUUID());if(s==null)return pose;if(!matches(p,s)){clear(p.getUUID());return pose;}return s.motion.apply(pose,p.level().getGameTime()+partial);}
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;checkWorld();var mc=Minecraft.getInstance();if(mc.player!=null&&!ClientControls.vrActive())clear(mc.player.getUUID());
        if(mc.level!=null)STATES.entrySet().removeIf(entry->{var p=mc.level.getPlayerByUUID(entry.getKey());return p==null||!matches(p,entry.getValue());});
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){STATES.clear();world=null;}
}
