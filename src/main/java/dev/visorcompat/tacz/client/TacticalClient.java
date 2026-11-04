package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.*;
import dev.visorcompat.tacz.physical.*;
import net.minecraft.client.Minecraft;
import org.joml.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;

public final class TacticalClient {
    private TacticalClient(){}
    private static boolean grab,pinch;private static String grabAction="",pinchAction="";private static HandType owner=HandType.OFFHAND;private static int slot=-1;private static String key="",syncedKey="";private static TacticalCalibration synced;private static boolean syncedMode;private static int lastGuide;
    public static boolean active(){var p=Minecraft.getInstance().player;return p!=null&&PhysicalClient.active()&&AdvancedClient.active()&&dev.visorcompat.tacz.server.TacticalServer.supported(p.getMainHandItem());}
    public static boolean supportAllowed(){var p=Minecraft.getInstance().player;if(!active())return false;var stack=p.getMainHandItem();String id=Profiles.key(stack);return TacticalStore.get(id.split("\\|",2)[0],id).alternativeControls()&&TacticalState.count(stack,false)<0&&TacticalState.count(stack,true)>=0&&TacticalState.old(stack,true);}
    public static void tick(){
        if((grab||pinch)&&owner!=(AdvancedClient.freeMain()?HandType.MAIN:HandType.OFFHAND))reset();
        var player=Minecraft.getInstance().player;int guide=active()?TacticalState.guide(player.getMainHandItem()):0;
        if(guide!=lastGuide&&guide!=0)VisorAPI.client().getInputManager().triggerHapticPulse(AdvancedClient.freeMain()?HandType.MAIN:HandType.OFFHAND,90f,.3f,.025f);
        lastGuide=guide;
    }
    public static boolean busy(){var p=Minecraft.getInstance().player;return active()&&(grab||!supportAllowed()&&(pinch||TacticalState.count(p.getMainHandItem(),true)>=0)||TacticalState.count(p.getMainHandItem(),false)>=0);}
    public static void sync(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.getConnection()==null){synced=null;syncedKey="";return;}
        String current=Profiles.key(mc.player.getMainHandItem());
        var value=Profiles.get(mc.player.getMainHandItem())==null?TacticalCalibration.DEFAULT:TacticalStore.get(current.split("\\|",2)[0],current);boolean mode=CompatSettings.advanced();
        if(!current.equals(syncedKey)||!value.equals(synced)||mode!=syncedMode){CompatNetwork.CHANNEL.sendToServer(new TacticalNetwork.Setting(current,value,mode));synced=value;syncedKey=current;syncedMode=mode;}
    }
    public static void reset(){if(grab)send(false,false,true);if(pinch)send(true,false,true);grab=pinch=false;grabAction=pinchAction="";slot=-1;key="";lastGuide=0;}
    private static void send(boolean pinched,boolean down,boolean canceled){if(Minecraft.getInstance().getConnection()!=null)CompatNetwork.CHANNEL.sendToServer(new TacticalNetwork.Input(pinched,down,canceled,slot,key));}
    public static boolean action(ActionButtonVREvent event,boolean canInput){
        var mc=Minecraft.getInstance();var input=VisorAPI.client().getInputManager();
        String action=event.getActionButton().getId();
        if(!event.isPressEvent()&&((grab&&action.equals(grabAction))||(pinch&&action.equals(pinchAction)))){
            boolean pinched=pinch&&action.equals(pinchAction);send(pinched,false,!canInput);if(pinched)pinch=false;else grab=false;event.setCanceled(true);return true;
        }
        tick();
        HandType hand=AdvancedClient.freeMain()?HandType.MAIN:HandType.OFFHAND;
        boolean use=event.getActionButton()==input.getActionRightMouse(hand);
        boolean grip=event.getActionButton().getId().equals(hand==HandType.MAIN?"hotbar_main":"hotbar_offhand");
        if(!use&&!grip)return false;
        boolean down=event.isPressEvent();
        if(!down&&(use?pinch:grab)){send(use,false,!canInput);if(use)pinch=false;else grab=false;event.setCanceled(true);return true;}
        if(!down||!canInput||!active()||!AdvancedClient.manipulating()||mc.screen!=null||!mc.player.getOffhandItem().isEmpty())return false;
        var vr=VisorAPI.client().getVRLocalPlayer();if(!vr.getRawController(hand).isTracking()||VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(hand)||!VisorAPI.client().getDecorationRenderer().getHandState(hand).isWorldHand())return false;
        var stack=mc.player.getMainHandItem();var c=CalibrationStore.get(Profiles.key(stack));var profile=Profiles.get(stack);
        var pose=vr.getPoseData(PlayerPoseType.TICK);var gun=AdvancedClient.interactionPose(GunPose.resolve(pose,profile,false,c,PhysicalClient.anchor()));if(gun==null)return false;
        var at=hand==HandType.MAIN?pose.getMainHand():pose.getOffhand();var local=Handling.local(gun,at.getPosition());
        var forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        boolean pouch=Handling.inPouch(at.getPosition(),pose.getHmd().getPosition(),forward,gun.worldScale(),c);
        var settings=TacticalStore.get(Profiles.key(stack).split("\\|",2)[0],Profiles.key(stack));
        boolean support=grip&&supportAllowed()&&Handling.inside(local,Handling.support(profile,c),c,ZoneSizes.Zone.SUPPORT)&&local.distance(Handling.support(profile,c))<local.distance(Handling.magazine(profile,c));
        if(support)return false;
        boolean magazine=(grip||use&&settings.alternativeControls())&&!TacticalState.out(stack)&&Handling.inside(local,Handling.magazine(profile,c),c,ZoneSizes.Zone.MAGAZINE);
        if(!pouch&&!magazine&&TacticalState.count(stack,use)<0)return false;
        if(Handling.racking(PhysicalClient.phase())||PhysicalClient.phase()==Handling.Phase.LOADING)return false;
        slot=mc.player.getInventory().selected;key=Profiles.key(stack);owner=hand;if(use){pinch=true;pinchAction=action;}else{grab=true;grabAction=action;}
        send(use,true,false);event.setCanceled(true);return true;
    }
}
