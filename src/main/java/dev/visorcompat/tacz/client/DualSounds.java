package dev.visorcompat.tacz.client;
import com.tacz.guns.client.sound.GunSoundInstance;
import com.tacz.guns.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
/** Native TaCZ asset playback at the actual gun, without shared entity/sound concurrency eviction. */
public final class DualSounds {
 public static boolean available(ResourceLocation asset){
  if(asset==null)return false;
  var resource=new ResourceLocation(asset.getNamespace(),"tacz_sounds/"+asset.getPath()+".ogg");
  return Minecraft.getInstance().getResourceManager().getResource(resource).isPresent();
 }
 public static GunSoundInstance play(int slot,ResourceLocation asset,float volume,float pitch,int distance){
  var mc=Minecraft.getInstance();if(mc.player==null||asset==null||!DualClient.active())return null;
  if(!available(asset))return null;
  HandType hand=null;for(var candidate:HandType.values())if(DualClient.boundSlot(candidate)==slot){hand=candidate;break;}if(hand==null)return null;
  var pose=DualClient.pose(hand,PlayerPoseType.TICK);if(pose==null)return null;
  var sound=new LocatedSound(asset,volume,pitch,distance,pose.muzzle());mc.getSoundManager().play(sound);return sound;
 }
 private static final class LocatedSound extends GunSoundInstance {
  LocatedSound(ResourceLocation asset,float volume,float pitch,int distance,org.joml.Vector3f at){
   super(ModSounds.GUN.get(),SoundSource.PLAYERS,volume,pitch,Minecraft.getInstance().player,distance,asset,false);
   this.x=at.x;this.y=at.y;this.z=at.z;
  }
 }
}
