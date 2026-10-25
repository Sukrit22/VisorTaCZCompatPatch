package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.*;
import dev.visorcompat.tacz.physical.*;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;

/** Slot-owned feedback, driven solely by confirmed server rounds and manipulation cues. */
public final class DualEffects {
 private static final DualShotFeedback SHOTS=new DualShotFeedback();
 private static final com.tacz.guns.client.model.SlotModel FLASH=new com.tacz.guns.client.model.SlotModel(true);
 private static HandType hand(int slot){for(var h:HandType.values())if(DualClient.boundSlot(h)==slot)return h;return null;}
 private static int slot(ItemStack stack){var p=Minecraft.getInstance().player;if(p==null)return -1;for(int i=0;i<9;i++)if(p.getInventory().getItem(i)==stack)return i;return -1;}
 public static void receive(DualNetwork.Shot shot){var mc=Minecraft.getInstance();if(!DualClient.active()||mc.player==null||shot.slot()<0||shot.slot()>8||!Profiles.key(mc.player.getInventory().getItem(shot.slot())).equals(Profiles.key(shot.stack())))return;
  var hand=hand(shot.slot());if(hand==null||!DualClient.held(hand))return;
  var gun=IGun.getIGunOrNull(shot.stack());if(gun==null)return;var display=TimelessAPI.getGunDisplay(shot.stack()).orElse(null);var index=TimelessAPI.getCommonGunIndex(gun.getGunId(shot.stack())).orElse(null);if(display==null||index==null)return;
  SHOTS.fired(shot.slot(),Profiles.key(shot.stack()),System.nanoTime(),shot.silence());
  DualSounds.play(shot.slot(),display.getSounds(shot.silence()?com.tacz.guns.sound.SoundManager.SILENCE_SOUND:com.tacz.guns.sound.SoundManager.SHOOT_SOUND),shot.silence()?.6f:.8f,.9f+mc.player.getRandom().nextFloat()*.125f,64);
  var profile=Profiles.get(shot.stack());
  // Manual mechanisms eject during their own action stroke; Glock ejects on the confirmed shot.
  if(profile!=null&&!profile.manualAction()&&!profile.cylinder())eject(shot.slot(),shot.stack(),false);
 }
 public static float cycle(ItemStack stack){return SHOTS.cycle(slot(stack),Profiles.key(stack),System.nanoTime());}
 public static void clearSlot(int slot){SHOTS.clear(slot);}
 public static void clear(){SHOTS.clear();}
 public static void physical(int slot,ItemStack stack,int cue){
  var gun=IGun.getIGunOrNull(stack);if(gun==null)return;
  var profile=Profiles.get(stack);var pistol=PistolProfiles.get(profile);
  String asset=pistol==null&&gun.getGunId(stack).toString().equals("tacz:glock_17")?DualShotFeedback.glockSound(cue):pistol==null?"":pistol.sound(cue);
  if(!asset.isEmpty())DualSounds.play(slot,new net.minecraft.resources.ResourceLocation(gun.getGunId(stack).getNamespace(),asset),.8f,1,12);
  if(cue==3)eject(slot,stack,true);
 }
 private static void eject(int slot,ItemStack stack,boolean live){
  var mc=Minecraft.getInstance();var hand=hand(slot);if(mc.player==null||hand==null||!DualClient.held(hand))return;var pose=DualClient.pose(hand,PlayerPoseType.TICK);var profile=Profiles.get(stack);var gun=IGun.getIGunOrNull(stack);if(pose==null||profile==null||gun==null)return;var index=TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);if(index==null)return;
  var c=CalibrationStore.get(Profiles.key(stack));var port=JamProfile.of(profile,c).port();
  var at=new org.joml.Quaternionf(pose.rotation()).transform(port.mul(pose.worldScale())).add(pose.hand());
  var speed=new org.joml.Quaternionf(pose.rotation()).transform(new Vector3f(.8f,.45f,.12f).mul(pose.worldScale()));
  // 7/8 are visual-only live/spent ejections; action audio is already owned by this slot.
  HandlingEffects.receive(new CompatNetwork.Feedback(live?7:8,mc.player.getId(),gun.getGunId(stack),index.getGunData().getAmmoId(),at.x,at.y,at.z,speed.x,speed.y,speed.z,pose.worldScale(),c.gunScale()*c.casingScale()));
 }
 /** Draw directly from the display texture, never the native singleton flash timestamp/matrices. */
 public static void flash(PoseStack matrices,HandType hand,ItemStack stack,GunPose gun){
  int slot=slot(stack);String key=Profiles.key(stack);long now=System.nanoTime();if(!DualClient.held(hand)||DualClient.stack(hand)!=stack||!SHOTS.flash(slot,key,now))return;
  var display=TimelessAPI.getGunDisplay(stack).orElse(null);var profile=Profiles.get(stack);if(display==null||display.getMuzzleFlash()==null||profile==null)return;
  var flash=display.getMuzzleFlash();var c=CalibrationStore.render(key);float age=SHOTS.age(slot,key,now);float scale=.5f*flash.getScale()*profile.scale()*c.gunScale()*Math.min(1,age/.025f);
  var at=Handling.local(gun,gun.muzzle());var buffers=Minecraft.getInstance().renderBuffers().bufferSource();
  matrices.pushPose();try{matrices.translate(at.x,at.y,at.z);matrices.mulPose(Axis.ZP.rotationDegrees((slot*47+key.hashCode())%360));
   matrices.pushPose();try{matrices.scale(scale,scale,scale);matrices.translate(0,-1,0);FLASH.renderToBuffer(matrices,buffers.getBuffer(RenderType.entityTranslucent(flash.getTexture())),0xF000F0,OverlayTexture.NO_OVERLAY,1,1,1,1);}finally{matrices.popPose();}
   matrices.pushPose();try{matrices.scale(scale/2,scale/2,scale/2);matrices.translate(0,-.9,0);FLASH.renderToBuffer(matrices,buffers.getBuffer(RenderType.energySwirl(flash.getTexture(),1,1)),0xF000F0,OverlayTexture.NO_OVERLAY,1,1,1,1);}finally{matrices.popPose();}
  }finally{matrices.popPose();}
 }
}
