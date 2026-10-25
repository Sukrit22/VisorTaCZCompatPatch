package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.DualNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.joml.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;

/** Independent hotbar-backed hands. No temporary inventory or active-hand swapping. */
public final class DualClient {
    private static final boolean[] held=new boolean[2],trigger=new boolean[2],reload=new boolean[2],jam=new boolean[2];
    private static final boolean[][] owned=new boolean[2][3];
    private static int main=-1,off=-1;
    private static int drawingSlot=-1;
    /** Synchronous permission for a fresh, validated holster Grab only. */
    public static int drawingSlot(){return drawingSlot;}
    private static ItemStack mainStack=ItemStack.EMPTY,offStack=ItemStack.EMPTY;
    private static boolean sent;private static float mountHeading;
    private static int index(HandType hand){return hand==HandType.MAIN?0:1;}
    public static boolean active(){
        var mc=Minecraft.getInstance();
        if(!CompatSettings.dualWield()||!CompatSettings.active()||mc.player==null||VisorAPI.client()==null)return false;
        if(FourHolsters.active())return true;
        int a=mc.player.getInventory().selected,b=VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
        return a>=0&&a<9&&b>=0&&b<9&&a!=b&&(Profiles.get(mc.player.getInventory().getItem(a))!=null||Profiles.get(mc.player.getInventory().getItem(b))!=null);
    }
    public static ItemStack stack(HandType hand){
        var mc=Minecraft.getInstance();if(mc.player==null||VisorAPI.client()==null)return ItemStack.EMPTY;
        int slot=hand==HandType.MAIN?mc.player.getInventory().selected:VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
        return slot>=0&&slot<9?mc.player.getInventory().getItem(slot):ItemStack.EMPTY;
    }
    public static boolean assigned(HandType hand){return active()&&Profiles.get(stack(hand))!=null;}
    /** Last accepted hand binding, available during Visor's transient slot collision. */
    public static int boundSlot(HandType hand){return hand==HandType.MAIN?main:off;}
    public static boolean held(HandType hand){return held[index(hand)];}
    private static boolean otherAvailable(HandType hand){var other=hand==HandType.MAIN?HandType.OFFHAND:HandType.MAIN;var stack=stack(other);return !held(other)&&(stack.isEmpty()||Profiles.get(stack)!=null)&&available(other);}
    private static Calibration calibration(HandType hand){
        return CalibrationStore.get(Profiles.key(stack(hand)));
    }
    public static GunPose pose(HandType hand,PlayerPoseType type){
        if(!assigned(hand))return null;
        var vr=VisorAPI.client().getVRLocalPlayer();var pose=vr.getPoseData(type);var p=Profiles.get(stack(hand));
        float scale=pose.getWorldScale();if(!Float.isFinite(scale)||scale<.25f||scale>4f)return null;
        var c=calibration(hand);Vector3f position;Quaternionf rotation;
        if(held(hand)){
            return DualRecoil.apply(stack(hand),DualPose.resolve(pose,p,c,hand==HandType.OFFHAND,otherAvailable(hand)),type==PlayerPoseType.RENDER?Minecraft.getInstance().getFrameTime():0);
        }else{
            if(FourHolsters.active()){int mount=FourHolsters.mount(hand==HandType.MAIN?Minecraft.getInstance().player.getInventory().selected:vr.getOffhandSlot());return mount<0?null:FourHolsters.pose(mount,type);}
            boolean pistol=p.supportDistance()==0;float side=(vr.isLeftHanded()?-1:1)*(hand==HandType.MAIN?1:-1);
            var h=DualHolsterStore.get(Profiles.key(stack(hand)),pistol,hand==HandType.OFFHAND);
            // Long weapons occupy chest for main hand and back for offhand.

            position=h.followingCrouch(pose.getOrigin(),pose.getHmd().getPosition(),mountHeading,scale,pistol?side:(vr.isLeftHanded()?-1:1));
            rotation=h.rotation(mountHeading,pistol?side:(vr.isLeftHanded()?-1:1));
        }
        var muzzle=new Quaternionf(rotation).transform(c.muzzleOffset(p.muzzleOffset()).mul(scale)).add(position);
        var direction=new Quaternionf(rotation).transform(new Vector3f(0,0,-1));
        return PoseMath.finite(position)&&PoseMath.finite(muzzle)&&PoseMath.finite(direction)?new GunPose(position,rotation,muzzle,direction,scale):null;
    }
    static boolean available(HandType hand){
        var mc=Minecraft.getInstance();var vr=VisorAPI.client().getVRLocalPlayer();
        return mc.screen==null&&!mc.isPaused()&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&VisorAPI.clientState().stateMode().isFocused()&&vr.getRawController(hand).isTracking()&&!VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(hand)&&VisorAPI.client().getDecorationRenderer().getHandState(hand).isWorldHand();
    }
    public static boolean action(ActionButtonVREvent event,boolean ignored){
        if(!active())return false;
        if(DualPhysicalClient.action(event))return true;
        var mc=Minecraft.getInstance();
        var input=VisorAPI.client().getInputManager();
        for(var hand:HandType.values()){
            int i=index(hand),kind=-1;String id=event.getActionButton().getId();
            if(event.getActionButton()==input.getActionLeftMouse(hand))kind=0;
            else if(event.getActionButton()==input.getActionRightMouse(hand))kind=1;
            else if(id.equals(hand==HandType.MAIN?"hotbar_main":"hotbar_offhand"))kind=2;
            if(kind<0)continue;
            int slot=hand==HandType.MAIN?mc.player.getInventory().selected:VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
            if(!event.isPressEvent()&&owned[i][kind]){
                owned[i][kind]=false;if(kind==0)trigger[i]=false;if(kind==1)jam[i]=false;if(kind==2){held[i]=false;trigger[i]=false;jam[i]=false;DualEffects.clearSlot(hand==HandType.MAIN?main:off);}
                event.setCanceled(true);return true;
            }
            if(event.isCanceled()||!event.isPressEvent()||!available(hand))return false;
            if(slot!=(hand==HandType.MAIN?main:off))return false;
            if(kind==2&&FourHolsters.active()&&!held[i]){
                int mount=FourHolsters.nearby(hand);if(mount<0)return false;
                int target=FourHolsters.slot(mount);resetHand(i);
                var vr=VisorAPI.client().getVRLocalPlayer();
                drawingSlot=target;
                try{
                    if(hand==HandType.MAIN){
                        if(vr.getOffhandSlot()==target){resetHand(1);vr.setOffhandSlot(-1);off=-1;offStack=ItemStack.EMPTY;}
                        mc.player.getInventory().selected=target;main=target;mainStack=stack(hand);
                    }else{
                        vr.setOffhandSlot(target);
                        if(vr.getOffhandSlot()!=target)return false;
                        // Native Visor moves the displaced main selection. Accept it immediately,
                        // invalidating the old owner's input lease before the next network tick.
                        if(main!=mc.player.getInventory().selected){resetHand(0);main=mc.player.getInventory().selected;mainStack=stack(HandType.MAIN);}
                        off=target;offStack=stack(hand);
                    }
                    mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(mc.player.getInventory().selected));
                }finally{drawingSlot=-1;}
                held[i]=true;owned[i][2]=true;event.setCanceled(true);return true;
            }
            if(!assigned(hand))return false;
            if(kind==2){
                var gun=pose(hand,PlayerPoseType.TICK);var at=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.TICK).getHand(hand);
                if(!held[i]&&(gun==null||at.getPosition().distance(gun.hand())>.22f*gun.worldScale()))return false;

                held[i]=true;
            }else if(!held[i])return false;
            else if(kind==0){if(!jam[i])trigger[i]=true;}else if(DualPhysicalClient.supported(hand))reload[i]=true;else if(dev.visorcompat.tacz.compat.GunDurabilityCompat.supported()&&dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack(hand))){jam[i]=true;trigger[i]=false;}else reload[i]=true;
            owned[i][kind]=true;event.setCanceled(true);return true;
        }return false;
    }
    public static void tick(boolean ignored){
        FourHolsters.tick();
        if(!active()){reset();return;}
        mountHeading=AdvancedClient.holsterHeading();
        var mc=Minecraft.getInstance();int a=mc.player.getInventory().selected,b=VisorAPI.client().getVRLocalPlayer().getOffhandSlot();
        if(a!=main||!Profiles.key(stack(HandType.MAIN)).equals(Profiles.key(mainStack))){resetHand(0);main=a;mainStack=stack(HandType.MAIN);}
        if(b!=off||!Profiles.key(stack(HandType.OFFHAND)).equals(Profiles.key(offStack))){resetHand(1);off=b;offStack=stack(HandType.OFFHAND);}
        for(var hand:HandType.values())if(!assigned(hand)||!available(hand)){int i=index(hand);held[i]=false;trigger[i]=false;reload[i]=false;jam[i]=false;}
        DualPhysicalClient.tick();
        DualAds.tick();
        DualNetwork.send(true,main,off,held[0],held[1],trigger[0],trigger[1],reload[0],reload[1],DualAds.aiming(HandType.MAIN),DualAds.aiming(HandType.OFFHAND),jam[0],jam[1],DualPhysicalClient.enabled(),DualPhysicalClient.grip(HandType.MAIN),DualPhysicalClient.grip(HandType.OFFHAND),calibration(HandType.MAIN),calibration(HandType.OFFHAND));DualPhysicalClient.sent();sent=true;reload[0]=reload[1]=false;
    }
    private static void resetHand(int i){int slot=i==0?main:off;DualStatus.clearSlot(slot);DualRecoil.clearSlot(slot);DualEffects.clearSlot(slot);held[i]=trigger[i]=reload[i]=jam[i]=false;for(int j=0;j<3;j++)owned[i][j]=false;}
    public static void reset(){
        if(sent&&Minecraft.getInstance().getConnection()!=null)DualNetwork.send(false,main,off,false,false,false,false,false,false,false,false,false,false,false,0,0,Calibration.ZERO,Calibration.ZERO);
        DualPhysicalClient.reset();DualAds.clear();DualStatus.clear();DualRecoil.clear();DualEffects.clear();sent=false;main=off=-1;mainStack=offStack=ItemStack.EMPTY;
        for(int i=0;i<2;i++){held[i]=trigger[i]=reload[i]=jam[i]=false;for(int j=0;j<3;j++)owned[i][j]=false;}
    }
}
