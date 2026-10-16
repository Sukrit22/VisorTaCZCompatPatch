package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.Profiles;
import dev.visorcompat.tacz.compat.GunDurabilityCompat;
import dev.visorcompat.tacz.network.ButtonJamNetwork;
import dev.visorcompat.tacz.physical.ButtonJamHold;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;

/** Owns Use until release, including after completion, so it cannot become reload. */
public final class ButtonJamClient {
    private static boolean held,finished;
    private static ItemStack stack;
    private static int slot=-1,ticks,heartbeat;
    private static String key="";
    private ButtonJamClient(){}
    private static boolean mode(){return CompatSettings.handlingMode()==CompatSettings.HandlingMode.BUTTON&&!DualClient.active();}
    public static boolean busy(){return held;}
    public static void reset(){if(held&&Minecraft.getInstance().getConnection()!=null)ButtonJamNetwork.hold(false,slot,key);held=false;finished=false;stack=null;slot=-1;ticks=heartbeat=0;key="";}
    public static boolean action(ActionButtonVREvent event,boolean canInput){
        var input=VisorAPI.client().getInputManager();
        if(event.getActionButton()!=input.getActionRightMouse(HandType.MAIN))return false;
        if(!event.isPressEvent()&&held){event.setCanceled(true);reset();return true;}
        var p=Minecraft.getInstance().player;
        if(!canInput||!mode()||p==null||Profiles.get(p.getMainHandItem())==null||!GunDurabilityCompat.jammed(p.getMainHandItem()))return false;
        // Unknown optional-mod versions keep their native clear control available.
        if(!GunDurabilityCompat.supported())return false;
        event.setCanceled(true);
        if(event.isPressEvent()&&!held){held=true;finished=false;stack=p.getMainHandItem();slot=p.getInventory().selected;key=Profiles.key(stack);ticks=0;ButtonJamNetwork.hold(true,slot,key);ClientControls.stopShooting();}
        return true;
    }
    public static void tick(boolean canInput){
        if(!held)return;
        var p=Minecraft.getInstance().player;
        if(!canInput||!mode()||p==null||stack!=p.getMainHandItem()||slot!=p.getInventory().selected||!key.equals(Profiles.key(p.getMainHandItem()))){reset();return;}
        if(!GunDurabilityCompat.jammed(stack))finished=true;
        if(!finished&&++heartbeat>=4){heartbeat=0;ButtonJamNetwork.hold(true,slot,key);}
    }
    public static void receive(ButtonJamNetwork.Progress p){if(!held||slot!=p.slot()||!key.equals(p.key()))return;ticks=Math.max(0,Math.min(ButtonJamHold.DURATION,p.ticks()));if(!p.active())finished=true;}
    public static String loading(){int filled=ticks*10/ButtonJamHold.DURATION;return held&&!finished?"CLEARING ["+"=".repeat(filled)+" ".repeat(10-filled)+"] "+ticks*100/ButtonJamHold.DURATION+"%":"HOLD USE TO CLEAR JAM";}
}
