package dev.visorcompat.tacz.server;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.resource.modifier.custom.RecoilModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;
/** Never modifies interaction anchors or the player's camera. */
public final class ServerRecoil {
    private static final class State {final RecoilMotion motion=new RecoilMotion();final ItemStack stack;final int slot;final long generation=++nextGeneration;long sequence,last=-1;State(ServerPlayer p){stack=p.getMainHandItem();slot=p.getInventory().selected;}}
    private static long nextGeneration;
    private static final Set<UUID> DISABLED=new HashSet<>();
    public static void enabled(ServerPlayer p,boolean enabled){if(enabled)DISABLED.remove(p.getUUID());else DISABLED.add(p.getUUID());reset(p);}
    private static void reset(ServerPlayer p){STATES.remove(p.getUUID());CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new CompatNetwork.RecoilReset(p.getUUID()));}
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static State state(ServerPlayer p){var s=STATES.get(p.getUUID());if(s==null||s.stack!=p.getMainHandItem()||s.slot!=p.getInventory().selected){if(s!=null)reset(p);s=new State(p);STATES.put(p.getUUID(),s);}return s;}
    public static void begin(ServerPlayer p){state(p).sequence++;}
    public static void clear(UUID id){STATES.remove(id);DISABLED.remove(id);}
    public static void clearAll(){STATES.clear();DISABLED.clear();}
    public static void tick(ServerPlayer p){var s=STATES.get(p.getUUID());if(s!=null&&(!ServerPoses.isVr(p)||s.stack!=p.getMainHandItem()||s.slot!=p.getInventory().selected))reset(p);}
    public static GunPose pose(ServerPlayer p,GunPose base){return DISABLED.contains(p.getUUID())?base:state(p).motion.apply(base,p.level().getGameTime());}
    public static GunPose validated(ServerPlayer p){var pose=pose(p,ServerPoses.validated(p));return pose!=null&&!ServerPoses.blocked(p,new net.minecraft.world.phys.Vec3(pose.hand()),new net.minecraft.world.phys.Vec3(pose.muzzle()))?pose:null;}
    /** Called on a spawned bullet. Multiple shotgun pellets share one impulse. */
    public static void shot(ServerPlayer p,GunData data){if(DISABLED.contains(p.getUUID()))return;var s=state(p);if(s.last==s.sequence)return;s.last=s.sequence;
        var recoil=data.getRecoil();if(recoil==null)return;
        float pitch=1,yaw=1;
        var cache=IGunOperator.fromLivingEntity(p).getCacheProperty();
        if(cache!=null){ParameterizedCachePair<Float,Float> mods=cache.getCache(RecoilModifier.ID);if(mods!=null){pitch=(float)mods.left().eval(1f);yaw=(float)mods.right().eval(1f);}}
        try {
            var pc=recoil.genPitchSplineFunction(pitch);var yc=recoil.genYawSplineFunction(yaw);
            double duration=Math.min(1900,Math.max(end(pc),end(yc)));if(duration<=0)return;
            // Append 100 ms of settling so packs with a nonzero final key do not snap.
            float ticks=(float)(duration+100)/50;float[] ps=new float[RecoilMotion.SAMPLES],ys=new float[RecoilMotion.SAMPLES];
            for(int i=1;i<ps.length-1;i++){double at=(duration+100)*i/(ps.length-1);ps[i]=sample(pc,at);ys[i]=sample(yc,at);}
            var curve=new RecoilMotion.Curve(ticks,ps,ys);if(!curve.valid())return;
            long start=p.level().getGameTime();s.motion.add(start,curve);
            CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new CompatNetwork.RecoilShot(p.getUUID(),Profiles.key(p.getMainHandItem()),s.slot,s.generation,start,curve));
        }catch(IllegalArgumentException ignored){/* An invalid optional pack recoil curve must not break a shot. */}
    }
    public static boolean isEnabled(ServerPlayer p){return !DISABLED.contains(p.getUUID());}
    public static RecoilMotion.Curve dualCurve(GunData data,com.tacz.guns.resource.modifier.AttachmentCacheProperty cache){
        var recoil=data.getRecoil();if(recoil==null)return null;float pitch=1,yaw=1;
        if(cache!=null){ParameterizedCachePair<Float,Float> mods=cache.getCache(RecoilModifier.ID);if(mods!=null){pitch=(float)mods.left().eval(1f);yaw=(float)mods.right().eval(1f);}}
        try{var pc=recoil.genPitchSplineFunction(pitch);var yc=recoil.genYawSplineFunction(yaw);double duration=Math.min(1900,Math.max(end(pc),end(yc)));if(duration<=0)return null;
            float[] ps=new float[RecoilMotion.SAMPLES],ys=new float[RecoilMotion.SAMPLES];for(int i=1;i<ps.length-1;i++){double at=(duration+100)*i/(ps.length-1);ps[i]=sample(pc,at);ys[i]=sample(yc,at);}
            var curve=new RecoilMotion.Curve((float)(duration+100)/50,ps,ys);return curve.valid()?curve:null;
        }catch(IllegalArgumentException ex){return null;}
    }
    private static double end(PolynomialSplineFunction f){return f==null?0:f.getKnots()[f.getKnots().length-1];}
    private static float sample(PolynomialSplineFunction f,double at){if(f==null)return 0;double end=end(f);return (float)(at<=end?f.value(at):f.value(end)*Math.max(0,1-(at-end)/100));}
}
