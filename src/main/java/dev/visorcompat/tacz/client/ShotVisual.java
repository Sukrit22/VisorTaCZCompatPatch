package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;
/** Shot timestamps survive inventory stack replacement. Both eyes sample the same clock. */
public final class ShotVisual {
    private record Shot(String key,long time){}
    private static final Map<UUID,Shot> SHOTS=new HashMap<>();
    public static void fired(LivingEntity shooter,ItemStack stack){SHOTS.put(shooter.getUUID(),new Shot(Profiles.key(stack),System.nanoTime()));if(SHOTS.size()>128)SHOTS.entrySet().removeIf(e->System.nanoTime()-e.getValue().time()>2_000_000_000L);}
    public static void clear(){SHOTS.clear();}
    private static net.minecraft.world.entity.player.Player owner(ItemStack stack){var mc=Minecraft.getInstance();if(mc.player!=null&&mc.player.getMainHandItem()==stack)return mc.player;if(mc.level!=null)for(var p:mc.level.players())if(p.getMainHandItem()==stack)return p;return null;}
    public static float cycle(ItemStack stack){var p=owner(stack);var shot=p==null?null:SHOTS.get(p.getUUID());return shot==null||!shot.key().equals(Profiles.key(stack))?0:dev.visorcompat.tacz.physical.ShotMotion.cycle((System.nanoTime()-shot.time())/1_000_000_000f);}
    public static float charge(ItemStack stack){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.player.getMainHandItem()!=stack)return 0;
        var gun=IGun.getIGunOrNull(stack);if(gun==null)return 0;
        var index=TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);if(index==null||index.getGunData().getChargeData(gun.getFireMode(stack))==null)return 0;
        float max=index.getGunData().getChargeData(gun.getFireMode(stack)).getMaxCharge();return max<=0?0:Math.max(0,Math.min(1,IClientPlayerGunOperator.fromLocalPlayer(mc.player).getChargeProgress()/max));
    }
}
