package dev.visorcompat.tacz.server;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.DualNetwork;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.entity.ShootResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import java.util.*;
/** Server owns each slot binding and validates every native shot/burst against live tracking. */
public final class ServerDual {
 private static ItemStack slotStack(ServerPlayer p,int slot){return slot>=0&&slot<9?p.getInventory().getItem(slot):ItemStack.EMPTY;}
 private static final Map<UUID,State> STATES=new HashMap<>();
 private static final Map<UUID,Long> SEQUENCES=new HashMap<>();
 private static final class State {
  final int[] slots;final ItemStack[] stacks;final DualGunOperator[] operators=new DualGunOperator[2];
  final RecoilMotion[] recoil={new RecoilMotion(),new RecoilMotion()};final long[] generation={System.nanoTime(),System.nanoTime()};final long[] round={0,0},last={-1,-1},lastCue={-1,-1};
  final dev.visorcompat.tacz.physical.DualJamHold<ItemStack>[] jams=new dev.visorcompat.tacz.physical.DualJamHold[]{new dev.visorcompat.tacz.physical.DualJamHold<>(),new dev.visorcompat.tacz.physical.DualJamHold<>()};
  final int[] lastAction={0,0};final boolean[] finishing={false,false};
  final DualTriggerLatch[] triggers={new DualTriggerLatch(),new DualTriggerLatch()};DualNetwork.Input input;long received;
  State(ServerPlayer p,DualNetwork.Input m){slots=new int[]{m.mainSlot(),m.offSlot()};stacks=new ItemStack[]{slotStack(p,slots[0]),slotStack(p,slots[1])};input=m;received=System.nanoTime();}
 }
 public static boolean holsteredMain(ServerPlayer p){
  var s=STATES.get(p.getUUID());
  return s!=null&&ServerPoses.isVr(p)&&p.isAlive()&&!p.isSpectator()&&System.nanoTime()-s.received<=500_000_000L
   &&s.operators[0]!=null&&s.operators[0].valid()&&!s.input.mainHeld()&&p.getInventory().selected==s.slots[0]
   &&p.getInventory().getItem(s.slots[0])==s.stacks[0]&&Profiles.get(s.stacks[0])!=null;
 }
 public static int slot(ServerPlayer p,int hand){var s=STATES.get(p.getUUID());return s==null?-1:s.slots[hand];}
 public static ItemStack stack(ServerPlayer p,int hand){var s=STATES.get(p.getUUID());return s==null?ItemStack.EMPTY:s.stacks[hand];}
 public static boolean held(ServerPlayer p,int hand){var s=STATES.get(p.getUUID());return s!=null&&(hand==0?s.input.mainHeld():s.input.offHeld());}
 public static boolean grip(ServerPlayer p,int hand){var s=STATES.get(p.getUUID());return s!=null&&(hand==0?s.input.mainGrip():s.input.offGrip())==1;}
 public static boolean gripCanceled(ServerPlayer p,int hand){var s=STATES.get(p.getUUID());return s!=null&&(hand==0?s.input.mainGrip():s.input.offGrip())>1;}
 public static boolean physicalRequested(ServerPlayer p){var s=STATES.get(p.getUUID());return s!=null&&s.input.physical();}
 public static Calibration calibration(ServerPlayer p,int hand){var s=STATES.get(p.getUUID());return s==null?Calibration.ZERO:hand==0?s.input.mainCalibration():s.input.offCalibration();}
 public static DualGunOperator operator(ServerPlayer p,int hand){var s=STATES.get(p.getUUID());return s==null?null:s.operators[hand];}
 private static GunPose firingPose(ServerPlayer p,int hand){return DualPhysicalServer.canFire(p,hand)?validated(p,hand):null;}
 public static boolean active(ServerPlayer p){return STATES.containsKey(p.getUUID());}
 public static void receive(ServerPlayer p,DualNetwork.Input m){
  if(m.sequence()<=SEQUENCES.getOrDefault(p.getUUID(),-1L))return;SEQUENCES.put(p.getUUID(),m.sequence());
  if(!m.enabled()){clear(p);return;}
  if(!ServerPoses.isVr(p)||!p.isAlive()||p.isSpectator()||!m.mainCalibration().valid()||!m.offCalibration().valid()
   ||m.mainSlot()<0||m.mainSlot()>8||m.offSlot()<-1||m.offSlot()>8||m.mainSlot()==m.offSlot()
   ||!(VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr)){clear(p);return;}
  // Slot selection packets can cross the VR input packet; reject stale input without closing the other hand.
  if(m.mainSlot()!=p.getInventory().selected||vr.getOffhandSlot()!=m.offSlot())return;
  var a=Profiles.get(p.getInventory().getItem(m.mainSlot()));var b=Profiles.get(slotStack(p,m.offSlot()));
  if(a==null&&b==null){clear(p);return;}
  var s=STATES.get(p.getUUID());
  if(s==null){
   ((com.tacz.guns.api.entity.IGunOperator)p).cancelReload();((com.tacz.guns.api.entity.IGunOperator)p).getDataHolder().isBolting=false;ServerPhysical.clear(p);ServerAdvanced.clear(p);DualPhysicalServer.recoverInventory(p);s=new State(p,m);STATES.put(p.getUUID(),s);
  }
  for(int h=0;h<2;h++){
   int slot=h==0?m.mainSlot():m.offSlot();var stack=slotStack(p,slot);
   boolean gun=Profiles.get(stack)!=null;
   if(s.slots[h]!=slot||s.stacks[h]!=stack||(gun!=(s.operators[h]!=null))){
    if(s.operators[h]!=null)s.operators[h].close();s.operators[h]=null;
    if(s.jams[h].active())s.jams[h].finish();s.slots[h]=slot;s.stacks[h]=stack;s.triggers[h].reset();s.recoil[h]=new RecoilMotion();
    s.generation[h]=System.nanoTime();s.round[h]=0;s.last[h]=s.lastCue[h]=-1;s.lastAction[h]=0;s.finishing[h]=false;
   }
   if(gun&&s.operators[h]==null){final State state=s;final int hand=h;s.operators[h]=new DualGunOperator(p,()->p.getInventory().getItem(state.slots[hand]),()->(hand==0?state.input.mainJam():state.input.offJam())?null:firingPose(p,hand));}
  }
  s.input=m;s.received=System.nanoTime();
  for(int h=0;h<2;h++){boolean held=h==0?m.mainHeld():m.offHeld(),down=h==0?m.mainTrigger():m.offTrigger();
   if(s.operators[h]==null){s.triggers[h].reset();continue;}
   boolean jam=h==0?m.mainJam():m.offJam();
   s.jams[h].input(s.stacks[h],jam,held&&validated(p,h)!=null&&dev.visorcompat.tacz.compat.GunDurabilityCompat.supported()&&dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(s.stacks[h])&&!s.operators[h].getSynReloadState().getStateType().isReloading()&&!s.operators[h].getSynIsBolting(),p.level().getGameTime());
   if(!held){s.triggers[h].reset();s.operators[h].cancelReload();s.operators[h].aim(false);continue;}
   s.triggers[h].input(held,down&&!jam);
   if(jam)continue;
   if((h==0?m.mainReload():m.offReload())&&validated(p,h)!=null){if(DualPhysicalServer.supported(p,s.stacks[h])){DualPhysicalServer.use(p,h);continue;}var gun=IGun.getIGunOrNull(s.stacks[h]);
    var index=com.tacz.guns.api.TimelessAPI.getCommonGunIndex(gun.getGunId(s.stacks[h])).orElse(null);
    if(index!=null&&index.getGunData().getBolt()==com.tacz.guns.resource.pojo.data.gun.Bolt.MANUAL_ACTION&&!gun.hasBulletInBarrel(s.stacks[h])&&gun.getCurrentAmmoCount(s.stacks[h])>0)s.operators[h].bolt();else s.operators[h].reload();
   }
  }
 }
 public static GunPose validated(ServerPlayer p,int hand){
  var s=STATES.get(p.getUUID());if(s==null||s.operators[hand]==null||Profiles.get(s.stacks[hand])==null||!ServerPoses.isVr(p)||!p.isAlive()||p.isSpectator()||System.nanoTime()-s.received>500_000_000L
   ||!(hand==0?s.input.mainHeld():s.input.offHeld())||(hand==0&&p.getInventory().selected!=s.slots[0])||p.getInventory().getItem(s.slots[hand])!=s.stacks[hand])return null;
  if(!(VisorAPI.getVRPlayer(p) instanceof VRServerPlayer vr)||(hand==1&&vr.getOffhandSlot()!=s.slots[1])||!vr.hasPoseData()||!(vr instanceof FreshPose f)||f.visorTacz$lastPoseNanos()==0||System.nanoTime()-f.visorTacz$lastPoseNanos()>500_000_000L)return null;
  var other=s.stacks[1-hand];boolean free=!(hand==0?s.input.offHeld():s.input.mainHeld())&&(other.isEmpty()||Profiles.get(other)!=null);
  var pose=DualPose.resolve(vr.getPoseData(),Profiles.get(s.stacks[hand]),hand==0?s.input.mainCalibration():s.input.offCalibration(),hand==1,free);if(pose==null)return null;
  if(ServerRecoil.isEnabled(p))pose=s.recoil[hand].apply(pose,p.level().getGameTime());
  var head=vr.getPoseData().getHmd().getPositionVec3();var grip=new Vec3(pose.hand());var muzzle=new Vec3(pose.muzzle());
  return head.distanceTo(p.position())<=3*pose.worldScale()&&head.distanceTo(grip)<=1.5*pose.worldScale()&&grip.distanceTo(muzzle)<=3*pose.worldScale()
   &&!ServerPoses.blocked(p,p.getEyePosition(),head)&&!ServerPoses.blocked(p,head,grip)&&!ServerPoses.blocked(p,grip,muzzle)?pose:null;
 }
 public static void tick(ServerPlayer p){var s=STATES.get(p.getUUID());if(s==null)return;
  if(!ServerPoses.isVr(p)||!p.isAlive()||p.isSpectator()||System.nanoTime()-s.received>500_000_000L){clear(p);return;}
  DualPhysicalServer.tick(p);
  for(int h=0;h<2;h++){var op=s.operators[h];if(op==null)continue;if(!op.valid()){op.close();s.operators[h]=null;s.triggers[h].reset();continue;}op.tick();status(p,s,h);var pose=validated(p,h);
   op.aim(pose!=null&&(h==0?s.input.mainAim():s.input.offAim())&&!op.getSynReloadState().getStateType().isReloading()&&!op.getSynIsBolting());
   var jam=s.jams[h];long tick=p.level().getGameTime();
   if(jam.active()){
    if(!jam.valid(s.stacks[h],pose!=null&&dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(s.stacks[h])&&!op.getSynReloadState().getStateType().isReloading()&&!op.getSynIsBolting(),tick))jam.finish();
    else if(jam.complete(tick)){if(dev.visorcompat.tacz.compat.GunDurabilityCompat.clearBound(p,s.stacks[h]))ServerJams.reconcile(p,s.stacks[h]);jam.finish();}
   }
   if(pose==null||!DualPhysicalServer.canFire(p,h)||(h==0?s.input.mainJam():s.input.offJam())){s.triggers[h].reset();op.resetCharge();continue;}
   {var result=op.triggerShot(s.triggers[h].pending(),h==0?s.input.mainTrigger():s.input.offTrigger(),PoseMath.pitch(pose.direction()),PoseMath.yaw(pose.direction()));
    if(result==null)continue;
    if(result==ShootResult.SUCCESS){var gun=IGun.getIGunOrNull(s.stacks[h]);if(gun.getFireMode(s.stacks[h])!=FireMode.AUTO){s.triggers[h].reset();}}
    else if(result!=ShootResult.COOL_DOWN&&result!=ShootResult.IS_DRAWING){s.triggers[h].reset();}
   }
  }
  broadcast(p,s);
 }
 public static void begin(ServerPlayer p){var s=STATES.get(p.getUUID());var op=DualGunOperator.current(p);if(s!=null&&op!=null)for(int h=0;h<2;h++)if(s.operators[h]==op)s.round[h]++;}
 public static void shot(ServerPlayer p,com.tacz.guns.resource.pojo.data.gun.GunData data){var s=STATES.get(p.getUUID());var op=DualGunOperator.current(p);if(s==null||op==null)return;
  for(int h=0;h<2;h++)if(s.operators[h]==op&&s.lastCue[h]!=s.round[h]){DualPhysicalServer.shot(p,h);s.lastCue[h]=s.round[h];boolean silent=false;var cache=op.getCacheProperty();if(cache!=null){it.unimi.dsi.fastutil.Pair<Integer,Boolean> pair=cache.getCache(com.tacz.guns.resource.modifier.custom.SilenceModifier.ID);silent=pair!=null&&pair.right();}
   dev.visorcompat.tacz.network.CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->p),new DualNetwork.Shot(s.slots[h],s.stacks[h],silent));}
  if(!ServerRecoil.isEnabled(p))return;
  for(int h=0;h<2;h++)if(s.operators[h]==op&&s.last[h]!=s.round[h]){s.last[h]=s.round[h];var curve=ServerRecoil.dualCurve(data,op.getCacheProperty());if(curve==null)return;long tick=p.level().getGameTime();s.recoil[h].add(tick,curve);
   dev.visorcompat.tacz.network.CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new dev.visorcompat.tacz.network.CompatNetwork.RecoilShot(p.getUUID(),Profiles.key(s.stacks[h]),s.slots[h],s.generation[h],tick,curve));}
 }
 private static void status(ServerPlayer p,State s,int h){
  var op=s.operators[h];var reload=op.getSynReloadState();boolean held=h==0?s.input.mainHeld():s.input.offHeld();
  int action=s.jams[h].active()?3:reload.getStateType().isReloading()?1:op.getSynIsBolting()?2:0;
  int cue=action==3||s.lastAction[h]==3?0:DualActionFeedback.cue(s.lastAction[h],action,s.finishing[h],held);
  var gun=IGun.getIGunOrNull(s.stacks[h]);
  dev.visorcompat.tacz.network.CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->p),
   new DualNetwork.Status(s.slots[h],Profiles.key(s.stacks[h]),gun.getCurrentAmmoCount(s.stacks[h]),gun.hasBulletInBarrel(s.stacks[h]),action,action==3?dev.visorcompat.tacz.physical.ButtonJamHold.DURATION-s.jams[h].elapsed(p.level().getGameTime()):reload.getCountDown(),reload.getStateType().isReloadingEmpty(),cue));
  s.lastAction[h]=action;s.finishing[h]=reload.getStateType().isReloadFinishing();
 }
 private static GunPose visualPose(ServerPlayer p,State s,int hand){
  if(s.operators[hand]==null)return null;
  if(hand==0?s.input.mainHeld():s.input.offHeld())return validated(p,hand);
  var pose=hand==0?s.input.mainPose():s.input.offPose();return pose!=null&&new Vec3(pose.hand()).distanceTo(p.position())<3*pose.worldScale()?pose:null;
 }
 private static void broadcast(ServerPlayer p,State s){dev.visorcompat.tacz.network.CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(()->p),new DualNetwork.Visual(p.getUUID(),true,s.stacks[0],s.stacks[1],visualPose(p,s,0),visualPose(p,s,1),s.input.mainCalibration(),s.input.offCalibration()));}
 public static void clear(ServerPlayer p){DualPhysicalServer.clear(p);var s=STATES.remove(p.getUUID());if(s!=null){for(var op:s.operators)if(op!=null)op.close();dev.visorcompat.tacz.network.CompatNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(()->p),new DualNetwork.Visual(p.getUUID(),false,ItemStack.EMPTY,ItemStack.EMPTY,null,null,Calibration.ZERO,Calibration.ZERO));}}
 public static void logout(ServerPlayer p){clear(p);SEQUENCES.remove(p.getUUID());}
 public static void clearAll(){DualPhysicalServer.clearAll();for(var s:STATES.values())for(var op:s.operators)if(op!=null)op.close();STATES.clear();SEQUENCES.clear();}
}
