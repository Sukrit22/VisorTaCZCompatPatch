package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.DualNetwork;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.sound.GunSoundInstance;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.client.Minecraft;
import org.vmstudio.visor.api.common.HandType;
import java.util.*;

/** Owner-only, server-authored ammo/action snapshots. Sounds belong to slots, not the native singleton. */
public final class DualStatus {
 private record Entry(DualNetwork.Status state,long received){}
 private static final Map<Integer,Entry> STATES=new HashMap<>();
 private static final Map<Integer,GunSoundInstance> SOUNDS=new HashMap<>();
 private static final Map<Integer,DualSoundTimeline> TIMELINES=new HashMap<>();
 public static void receive(DualNetwork.Status state){
  var mc=Minecraft.getInstance();if(!DualClient.active()||mc.player==null||state.slot()<0||state.slot()>8)return;
  int main=mc.player.getInventory().selected,off=org.vmstudio.visor.api.VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
  if(state.slot()!=main&&state.slot()!=off)return;
  var stack=mc.player.getInventory().getItem(state.slot());if(!Profiles.key(stack).equals(state.key()))return;
  STATES.put(state.slot(),new Entry(state,System.nanoTime()));
  if(state.cue()==0){var timeline=TIMELINES.get(state.slot());if(timeline!=null)timeline.tick(state.slot());return;}
  stop(state.slot());
  var display=TimelessAPI.getGunDisplay(stack).orElse(null);
  if(display!=null&&(state.cue()==1||state.cue()==4)){
   String sound=state.cue()==4?SoundManager.BOLT_SOUND:state.emptyReload()?SoundManager.RELOAD_EMPTY_SOUND:SoundManager.RELOAD_TACTICAL_SOUND;
   var timeline=new DualSoundTimeline(stack,sound);
   GunSoundInstance playing=null;
   if(!timeline.empty()){TIMELINES.put(state.slot(),timeline);timeline.tick(state.slot());}
   else playing=DualSounds.play(state.slot(),display.getSounds(sound),1,1,16);
   if(playing!=null)SOUNDS.put(state.slot(),playing);
  }else if(state.cue()==2){
   // A short confirmation, independent of pack reload clip length.
   mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),1.3f,.2f));
  }
 }
 public static boolean reloading(HandType hand){
  var mc=Minecraft.getInstance();if(mc.player==null)return false;
  int slot=hand==HandType.MAIN?mc.player.getInventory().selected:org.vmstudio.visor.api.VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
  var entry=STATES.get(slot);return entry!=null&&System.nanoTime()-entry.received()<1_000_000_000L&&entry.state().key().equals(Profiles.key(DualClient.stack(hand)))&&entry.state().action()!=0;
 }
 public static void clearSlot(int slot){stop(slot);STATES.remove(slot);}
 public static String[] lines(HandType hand){
  var mc=Minecraft.getInstance();var stack=DualClient.stack(hand);var gun=com.tacz.guns.api.item.IGun.getIGunOrNull(stack);if(mc.player==null||gun==null)return new String[0];
  int slot=hand==HandType.MAIN?mc.player.getInventory().selected:org.vmstudio.visor.api.VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
  var entry=STATES.get(slot);if(entry==null||System.nanoTime()-entry.received()>1_000_000_000L||!Profiles.key(stack).equals(entry.state().key()))return new String[]{"SYNCING..."};
  var s=entry.state();String ammo=(Profiles.cylinder(stack)?"LIVE ":Profiles.pump(stack)?"TUBE ":"MAG ")+s.ammo();
  if(!Profiles.cylinder(stack))ammo+=dev.visorcompat.tacz.physical.NativeAction.openBolt(stack)?" | OPEN BOLT":" | CH "+(s.chamber()?1:0);
  String action=s.action()==3?"CLEARING JAM "+Math.max(0,Math.min(100,(40-s.remaining())*100/40))+"%":s.action()==1?"RELOADING":s.action()==2?"CYCLING":!DualClient.held(hand)?"HOLSTERED":dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack)?"JAMMED":gun.isOverheatLocked(stack)?"OVERHEATED":s.ammo()==0&&!s.chamber()?"EMPTY":!dev.visorcompat.tacz.physical.NativeAction.ready(stack)?"USE TO CYCLE":DualAds.aiming(hand)?"ADS":"READY";
  if(DualPhysicalClient.supported(hand)&&DualClient.held(hand))action=DualPhysicalClient.status(hand);
  return new String[]{(hand==HandType.MAIN?"MAIN | ":"OFF | ")+ammo,action};
 }
 private static void stop(int slot){var timeline=TIMELINES.remove(slot);if(timeline!=null)timeline.stop();var old=SOUNDS.remove(slot);if(old!=null)old.setStop();}
 public static void clear(){for(var timeline:TIMELINES.values())timeline.stop();TIMELINES.clear();for(var sound:SOUNDS.values())sound.setStop();SOUNDS.clear();STATES.clear();}
}
