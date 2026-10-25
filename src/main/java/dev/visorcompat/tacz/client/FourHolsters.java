package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.joml.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import java.util.Arrays;
/** Mounts contain slot references only. Never inserts, restores or copies inventory items. */
public final class FourHolsters {
 private static int[] slots={-1,-1,-1,-1};
 private static final String[] warned={"","","",""};
 private static Object world;
 public static boolean active(){return FourHolsterSettings.enabled()&&CompatSettings.dualWield()&&CompatSettings.active()&&Minecraft.getInstance().player!=null;}
 public static int slot(int mount){return slots[mount];}
 public static ItemStack stack(int mount){var p=Minecraft.getInstance().player;return p==null||slots[mount]<0?ItemStack.EMPTY:p.getInventory().getItem(slots[mount]);}
 public static int mount(int slot){for(int i=0;i<4;i++)if(slot>=0&&slots[i]==slot)return i;return -1;}
 public static boolean heldSlot(int slot){return slot>=0&&((DualClient.held(HandType.MAIN)&&DualClient.boundSlot(HandType.MAIN)==slot)||(DualClient.held(HandType.OFFHAND)&&DualClient.boundSlot(HandType.OFFHAND)==slot));}
 private static HolsterAssignments.Kind kind(ItemStack stack){var gun=IGun.getIGunOrNull(stack);if(gun==null||Profiles.get(stack)==null)return HolsterAssignments.Kind.NONE;
  var index=TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);if(index==null)return HolsterAssignments.Kind.NONE;
  return "pistol".equalsIgnoreCase(index.getType())?HolsterAssignments.Kind.HANDGUN:HolsterAssignments.Kind.LONG_GUN;
 }
 public static void tick(){
  var mc=Minecraft.getInstance();if(world!=mc.level){world=mc.level;Arrays.fill(slots,-1);Arrays.fill(warned,"");}
  if(!active()){Arrays.fill(slots,-1);return;}
  var kinds=new HolsterAssignments.Kind[9];for(int i=0;i<9;i++)kinds[i]=kind(mc.player.getInventory().getItem(i));
  int[] bindings=null;if(FourHolsterSettings.order()==2){bindings=new int[4];for(int i=0;i<4;i++)bindings[i]=FourHolsterSettings.binding(i);}
  int[] pinned={-1,-1,-1,-1};for(int i=0;i<4;i++)if(heldSlot(slots[i]))pinned[i]=slots[i];
  var next=HolsterAssignments.resolvePinned(kinds,FourHolsterSettings.order()==1,bindings,pinned);
  for(int i=0;i<4;i++){
   String invalid="";int b=bindings==null?-1:bindings[i];
   if(b>=0&&pinned[i]<0&&next[i]!=b&&!mc.player.getInventory().getItem(b).isEmpty())invalid=b+":"+Profiles.key(mc.player.getInventory().getItem(b))+":"+mc.player.getInventory().getItem(b).getItem();
   if(!invalid.isEmpty()&&!invalid.equals(warned[i]))mc.player.displayClientMessage(Component.literal("TaCZ VR: "+HolsterAssignments.Mount.values()[i]+" holster rejects slot "+(b+1)+" (wrong gun type or duplicate binding)."),false);
   warned[i]=invalid;
  }slots=next;
 }
 public static GunPose pose(int mount,PlayerPoseType type){
  var stack=stack(mount);var p=Profiles.get(stack);if(!active()||p==null)return null;
  var vr=VisorAPI.client().getVRLocalPlayer();var pose=vr.getPoseData(type);float scale=pose.getWorldScale();if(!Float.isFinite(scale)||scale<.25f||scale>4)return null;
  var c=CalibrationStore.get(Profiles.key(stack));var h=FourMountStore.get(Profiles.key(stack),mount);
  float side=mount==1?-1:1,heading=AdvancedClient.holsterHeading();
  var pos=h.followingCrouch(pose.getOrigin(),pose.getHmd().getPosition(),heading,scale,side);var rot=h.rotation(heading,side);
  var muzzle=rot.transform(new Vector3f(c.muzzleOffset(p.muzzleOffset())).mul(scale)).add(pos);
  return new GunPose(pos,rot,muzzle,rot.transform(new Vector3f(0,0,-1)),scale);
 }
 public static boolean free(HandType hand){var stack=DualClient.stack(hand);int slot=hand==HandType.MAIN?Minecraft.getInstance().player.getInventory().selected:VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
  return !DualClient.held(hand)&&(stack.isEmpty()||mount(slot)>=0);
 }
 public static int nearby(HandType hand){
  if(!active()||DualClient.held(hand)||(FourHolsterSettings.emptyRequired()&&!free(hand)))return -1;
  var vr=VisorAPI.client().getVRLocalPlayer();var point=vr.getPoseData(PlayerPoseType.TICK).getHand(hand).getPosition();int best=-1;float distance=Float.MAX_VALUE;
  for(int i=0;i<4;i++){if(slots[i]<0||heldSlot(slots[i]))continue;var p=pose(i,PlayerPoseType.TICK);if(p==null)continue;float d=point.distance(p.hand());if(d<.22f*p.worldScale()&&d<distance){best=i;distance=d;}}
  return best;
 }
}
