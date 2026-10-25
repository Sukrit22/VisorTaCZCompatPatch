package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.Handling;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;

/** Per-slot contextual aiming; never changes TaCZ's singleton client operator or zoom. */
public final class DualAds {
    private static final boolean[] AIMING=new boolean[2];
    private DualAds(){}
    public static boolean aiming(HandType hand){return DualClient.active()&&AIMING[hand==HandType.MAIN?0:1];}
    /** Passive support claims the cursor, not the other hand's buttons. */
    public static boolean supporting(HandType hand){
        if(!DualClient.active()||!DualClient.held(hand))return false;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return false;
        var vr=VisorAPI.client().getVRLocalPlayer();var other=hand==HandType.MAIN?HandType.OFFHAND:HandType.MAIN;
        if(!vr.getRawController(other).isTracking()||VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(other)
            ||!VisorAPI.client().getDecorationRenderer().getHandState(other).isWorldHand())return false;
        var otherStack=DualClient.stack(other);
        if(!(otherStack.isEmpty()||Profiles.get(otherStack)!=null&&!DualClient.held(other)))return false;
        if(DualClient.held(other)||!DualClient.available(other))return false;
        var stack=DualClient.stack(hand);var profile=Profiles.get(stack);var gun=DualClient.pose(hand,PlayerPoseType.TICK);
        if(profile==null||gun==null)return false;
        var c=CalibrationStore.get(Profiles.key(stack));var support=Handling.support(profile,c);
        if(profile.supportDistance()>0)return DualPose.supporting(vr.getPoseData(PlayerPoseType.TICK),profile,c,hand==HandType.OFFHAND);
        return Handling.inside(Handling.local(gun,vr.getPoseData(PlayerPoseType.TICK).getHand(other).getPosition()),support,c,ZoneSizes.Zone.SUPPORT);
    }
    public static void clear(){AIMING[0]=AIMING[1]=false;}
    public static void tick(){
        var mc=Minecraft.getInstance();
        if(!DualClient.active()||!CompatSettings.autoAds()||mc.player==null||mc.screen!=null||mc.isPaused()
            ||!mc.player.isAlive()||mc.player.isSpectator()||!VisorAPI.clientState().stateMode().isFocused()){clear();return;}
        var vr=VisorAPI.client().getVRLocalPlayer();var pose=vr.getPoseData(PlayerPoseType.TICK);
        for(var hand:HandType.values()){
            int i=hand==HandType.MAIN?0:1;boolean desired=false;
            var other=hand==HandType.MAIN?HandType.OFFHAND:HandType.MAIN;
            var stack=DualClient.stack(hand);var profile=Profiles.get(stack);var gun=DualClient.pose(hand,PlayerPoseType.TICK);
            if(DualClient.held(hand)&&!DualStatus.reloading(hand)&&vr.getRawController(hand).isTracking()&&profile!=null&&gun!=null
                &&!VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(hand)
                &&VisorAPI.client().getDecorationRenderer().getHandState(hand).isWorldHand()){
                var c=CalibrationStore.get(Profiles.key(stack));
                var ads=OpticGeometry.ads(stack,profile,c,false);
                var sight=ads==null?null:ads.sight();
                boolean supported=!CompatSettings.twoHandAds()||supporting(hand);
                if(supported&&sight!=null){
                    var look=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1)).normalize();
                    look=new Quaternionf(gun.rotation()).conjugate().transform(look);
                    desired=OpticMath.aligned(Handling.local(gun,pose.getEyeLeft().getPosition()),look,sight,AIMING[i],ads.box())
                        ||OpticMath.aligned(Handling.local(gun,pose.getEyeRight().getPosition()),look,sight,AIMING[i],ads.box());
                }
            }
            AIMING[i]=desired;
        }
        if(AIMING[0]||AIMING[1])mc.player.setSprinting(false);
    }
}
