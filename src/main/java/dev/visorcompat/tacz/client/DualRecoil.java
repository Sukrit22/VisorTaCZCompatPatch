package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import java.util.*;
public final class DualRecoil {
 private record Entry(String key,long generation,RecoilMotion motion){}
 private static final Map<Integer,Entry> entries=new HashMap<>();
 public static void receive(CompatNetwork.RecoilShot p){var mc=Minecraft.getInstance();if(mc.player==null||!mc.player.getUUID().equals(p.player())||!DualClient.active()||p.slot()<0||p.slot()>8)return;
  var stack=mc.player.getInventory().getItem(p.slot());if(!Profiles.key(stack).equals(p.key()))return;
  var e=entries.get(p.slot());if(e!=null&&p.generation()<e.generation)return;if(e==null||e.generation!=p.generation()||!e.key.equals(p.key())){e=new Entry(p.key(),p.generation(),new RecoilMotion());entries.put(p.slot(),e);}e.motion.add(p.tick(),p.curve());
 }
 public static GunPose apply(ItemStack stack,GunPose pose,double partial){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!CompatSettings.recoil())return pose;for(int i=0;i<9;i++)if(mc.player.getInventory().getItem(i)==stack){var e=entries.get(i);return e!=null&&e.key.equals(Profiles.key(stack))?e.motion.apply(pose,mc.level.getGameTime()+partial):pose;}return pose;}
 public static void clearSlot(int slot){entries.remove(slot);}
 public static void clear(){entries.clear();}
}
