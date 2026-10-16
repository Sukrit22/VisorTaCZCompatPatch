package dev.visorcompat.tacz.client;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.sound.GunSoundInstance;
import dev.visorcompat.tacz.mixin.AnimationSoundsAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
/** Read-only animation sound channels, with independent clocks and cancellation per slot. */
public final class DualSoundTimeline {
 private record Cue(double second,ResourceLocation sound){}
 private final List<Cue> cues=new ArrayList<>();
 private final List<GunSoundInstance> playing=new ArrayList<>();
 private long start;private int cursor;
 public DualSoundTimeline(ItemStack stack,String name){
  var display=TimelessAPI.getGunDisplay(stack).orElse(null);if(display==null)return;
  var machine=display.getAnimationStateMachine();if(machine==null)return;
  var animations=((AnimationSoundsAccess)machine.getAnimationController()).visorTacz$prototypes();
  var gun=IGun.getIGunOrNull(stack);var index=gun==null?null:TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);
  int ext=index==null?0:com.tacz.guns.util.AttachmentDataUtils.getMagExtendLevel(stack,index.getGunData());
  var animation=ext>0&&animations.containsKey(name+"_xmag")?animations.get(name+"_xmag"):animations.get(name);
  if(animation==null||animation.getSoundChannel()==null||animation.getSoundChannel().content==null)return;
  var content=animation.getSoundChannel().content;
  if(content.keyframeTimeS==null||content.keyframeSoundName==null)return;
  for(int i=0;i<Math.min(content.keyframeTimeS.length,content.keyframeSoundName.length);i++)if(Double.isFinite(content.keyframeTimeS[i])&&content.keyframeTimeS[i]>=0&&content.keyframeSoundName[i]!=null)cues.add(new Cue(content.keyframeTimeS[i],content.keyframeSoundName[i]));
  cues.sort(Comparator.comparingDouble(Cue::second));
 }
 public boolean empty(){return cues.isEmpty();}
 public void tick(int slot){if(start==0)start=System.nanoTime();double elapsed=(System.nanoTime()-start)/1e9;
  while(cursor<cues.size()&&cues.get(cursor).second()<=elapsed){var cue=cues.get(cursor++);if(elapsed-cue.second()>.3)continue;
   var sound=DualSounds.play(slot,cue.sound(),1,1,16);if(sound!=null)playing.add(sound);
  }
  playing.removeIf(sound->!Minecraft.getInstance().getSoundManager().isActive(sound));
 }
 public void stop(){for(var sound:playing)sound.setStop();playing.clear();cursor=cues.size();}
}
