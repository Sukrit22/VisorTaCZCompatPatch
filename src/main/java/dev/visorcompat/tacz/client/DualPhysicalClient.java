package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IGun;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.DualNetwork;
import dev.visorcompat.tacz.physical.*;
import dev.visorcompat.tacz.physical.Handling.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import java.util.*;

/** Input and owner snapshots only; ammo changes always stay on the server. */
public final class DualPhysicalClient {
    private record Snapshot(DualNetwork.Physical state,long received){}
    private static final Map<Integer,Snapshot> STATES=new HashMap<>();
    private static final boolean[] down=new boolean[2];private static final int[] canceled=new int[2];
    private static int owner=-1,gunSlot=-1;private static String gunKey="";
    private static int index(HandType hand){return hand==HandType.MAIN?0:1;}
    private static HandType hand(int index){return index==0?HandType.MAIN:HandType.OFFHAND;}
    public static boolean enabled(){return CompatSettings.handlingMode()==CompatSettings.HandlingMode.PHYSICAL;}
    public static boolean supported(HandType hand){var stack=DualClient.stack(hand);var gun=IGun.getIGunOrNull(stack);return enabled()&&DualClient.active()&&Profiles.physical(stack)&&gun!=null&&DualPhysicalMagazine.supported(gun.getGunId(stack).toString());}
    public static boolean supportedStack(net.minecraft.world.item.ItemStack stack){var gun=IGun.getIGunOrNull(stack);return enabled()&&DualClient.active()&&Profiles.physical(stack)&&gun!=null&&DualPhysicalMagazine.supported(gun.getGunId(stack).toString());}
    private static Snapshot state(net.minecraft.world.item.ItemStack stack){var player=Minecraft.getInstance().player;if(player==null)return null;for(int i=0;i<9;i++)if(player.getInventory().getItem(i)==stack){var s=STATES.get(i);return s!=null&&s.state().key().equals(Profiles.key(stack))&&System.nanoTime()-s.received()<1_000_000_000L?s:null;}return null;}
    public static Phase phase(net.minecraft.world.item.ItemStack stack){var s=state(stack);if(s!=null)return Phase.values()[s.state().phase()];return stack.hasTag()&&stack.getTag().contains("visor_tacz_dual_reserved_magazine")?Phase.NO_MAG:Phase.READY;}
    public static float pull(net.minecraft.world.item.ItemStack stack){var s=state(stack);return s==null?0:s.state().pull()*.005f;}
    private static Snapshot state(HandType hand){var s=STATES.get(DualClient.boundSlot(hand));return s!=null&&s.state().key().equals(Profiles.key(DualClient.stack(hand)))&&System.nanoTime()-s.received()<1_000_000_000L?s:null;}
    public static Phase phase(HandType hand){return phase(DualClient.stack(hand));}
    public static float pull(HandType hand){var s=state(hand);return s==null?0:s.state().pull()*.005f;}
    private static int interaction(){for(int gun=0;gun<2;gun++){var other=DualClient.stack(hand(1-gun));if(supported(hand(gun))&&DualClient.held(hand(gun))&&!DualClient.held(hand(1-gun))&&(other.isEmpty()||Profiles.get(other)!=null)&&(Minecraft.getInstance().player.getInventory().offhand.get(0).isEmpty()||Profiles.get(Minecraft.getInstance().player.getInventory().offhand.get(0))!=null))return 1-gun;}return -1;}
    private static boolean available(HandType hand){var mc=Minecraft.getInstance();var vr=VisorAPI.client().getVRLocalPlayer();return mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&mc.screen==null&&!mc.isPaused()&&VisorAPI.clientState().stateMode().isFocused()&&vr.getRawController(hand).isTracking()&&!VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(hand)&&VisorAPI.client().getDecorationRenderer().getHandState(hand).isWorldHand();}
    private static Vector3f point(HandType hand,Vector3f point){/* TaCZ geometry stays authored in both hands. */return point;}
    public static Handling.Target target(HandType gunHand){int free=interaction();if(free<0||index(gunHand)==free||!available(hand(free)))return Handling.Target.NONE;var gun=DualClient.pose(gunHand,PlayerPoseType.TICK);if(gun==null)return Handling.Target.NONE;var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.TICK);var at=pose.getHand(hand(free)).getPosition();var local=Handling.local(gun,at);var profile=Profiles.get(DualClient.stack(gunHand));var c=CalibrationStore.get(Profiles.key(DualClient.stack(gunHand)));var phase=phase(gunHand);var forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        if(phase==Phase.NO_MAG&&Handling.inPouch(at,pose.getHmd().getPosition(),forward,gun.worldScale(),c))return Handling.Target.POUCH;
        if(phase!=Phase.READY&&phase!=Phase.NEED_RACK&&phase!=Phase.NO_MAG)return Handling.Target.NONE;
        if(ActionState.locked(DualClient.stack(gunHand))&&Handling.inside(local,point(gunHand,Handling.release(profile,c)),c,ZoneSizes.Zone.RELEASE))return Handling.Target.RELEASE;
        if(Handling.inside(local,point(gunHand,Handling.rack(profile,c)),c,ZoneSizes.Zone.RACK))return Handling.Target.RACK;
        return !Handling.magazineOut(phase)&&Handling.inside(local,point(gunHand,Handling.magazine(profile,c)),c,ZoneSizes.Zone.MAGAZINE)?Handling.Target.MAGAZINE:Handling.Target.NONE;
    }
    public static boolean action(ActionButtonVREvent event){
        var input=VisorAPI.client().getInputManager();
        for(int free=0;free<2;free++){
            var grab=CompatSettings.grab()==CompatSettings.Grab.TRIGGER?input.getActionLeftMouse(hand(free)):input.getActionRightMouse(hand(free));
            if(event.getActionButton()!=grab)continue;
            if(!event.isPressEvent()&&down[free]){down[free]=false;if(!available(hand(free)))canceled[free]=2;owner=-1;event.setCanceled(true);return true;}
            if(event.isCanceled()||!event.isPressEvent()||free!=interaction()||!available(hand(free))||target(hand(1-free))==Handling.Target.NONE)return false;
            owner=free;gunSlot=DualClient.boundSlot(hand(1-free));gunKey=Profiles.key(DualClient.stack(hand(1-free)));down[free]=true;event.setCanceled(true);return true;
        }
        return false;
    }
    public static void tick(){if(owner>=0&&(interaction()!=owner||!available(hand(owner))||gunSlot!=DualClient.boundSlot(hand(1-owner))||!gunKey.equals(Profiles.key(DualClient.stack(hand(1-owner)))))){canceled[owner]=2;down[owner]=false;owner=-1;}}
    public static int grip(HandType hand){int i=index(hand);return canceled[i]>0?2:down[i]?1:0;}
    public static void sent(){canceled[0]=canceled[1]=0;}
    public static void reset(){for(int i=0;i<2;i++){down[i]=false;canceled[i]=0;}owner=-1;gunSlot=-1;gunKey="";STATES.clear();}
    public static void receive(DualNetwork.Physical state){var mc=Minecraft.getInstance();if(mc.player==null||!DualClient.active()||state.slot()<0||state.slot()>8||state.phase()<0||state.phase()>=Phase.values().length||state.pull()<0||state.pull()>14)return;var stack=mc.player.getInventory().getItem(state.slot());if(!Profiles.key(stack).equals(state.key()))return;STATES.put(state.slot(),new Snapshot(state,System.nanoTime()));if(state.cue()>=0){DualEffects.physical(state.slot(),stack,state.cue());for(int i=0;i<2;i++)if(DualClient.boundSlot(hand(i))==state.slot())VisorAPI.client().getInputManager().triggerHapticPulse(hand(1-i),90f,.3f,.025f);}}
    public static String status(HandType hand){return switch(phase(hand)){case REMOVING->"PULL MAGAZINE DOWN";case OLD_MAG->"RELEASE TO STOW MAGAZINE";case NO_MAG->"MAGAZINE OUT | GRAB POUCH";case NEW_MAG->"INSERT MAGAZINE AND RELEASE";case RACKING,RACKING_EMPTY->"PULL SLIDE AND RELEASE";case NEED_RACK->"RACK SLIDE / USE SLIDE RELEASE";default->"PHYSICAL GLOCK";};}
    public static void guides(PoseStack matrices,HandType hand,GunPose gun){if(!supported(hand)||!DualClient.held(hand)||!CompatSettings.debugCubes())return;var stack=DualClient.stack(hand);var c=CalibrationStore.render(Profiles.key(stack));var profile=Profiles.get(stack);box(matrices,point(hand,Handling.magazine(profile,c)),c.zones().magazine(),.2f,1,.3f);box(matrices,point(hand,Handling.rack(profile,c)),c.zones().rack(),1,.5f,.1f);box(matrices,point(hand,Handling.release(profile,c)),c.zones().release(),.2f,.7f,1);if(phase(hand)==Phase.NO_MAG){var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);var forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));box(matrices,Handling.local(gun,Handling.pouch(pose.getHmd().getPosition(),forward,gun.worldScale(),c)),c.zones().pouch(),.2f,1,.3f);}}
    private static void box(PoseStack matrices,Vector3f at,ZoneSizes.Box box,float r,float g,float b){LevelRenderer.renderLineBox(matrices,Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines()),at.x-box.width()/2,at.y-box.height()/2,at.z-box.depth()/2,at.x+box.width()/2,at.y+box.height()/2,at.z+box.depth()/2,r,g,b,.7f);}
}
