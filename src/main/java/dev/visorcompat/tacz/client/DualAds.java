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
                var sight=OpticGeometry.sight(stack,profile);
                boolean supported=!CompatSettings.twoHandAds();
                if(!supported&&vr.getRawController(other).isTracking()){
                    var otherStack=DualClient.stack(other);
                    boolean free=otherStack.isEmpty()||Profiles.get(otherStack)!=null&&!DualClient.held(other);
                    var support=Handling.support(profile,c);if(hand==HandType.OFFHAND)support.x=-support.x;
                    supported=free&&Handling.inside(Handling.local(gun,pose.getHand(other).getPosition()),support,c,ZoneSizes.Zone.SUPPORT);
                }
                if(supported&&sight!=null){
                    sight.add(c.interactions().sight().vector()).mul(c.gunScale());if(hand==HandType.OFFHAND)sight.x=-sight.x;
                    var look=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1)).normalize();
                    look=new Quaternionf(gun.rotation()).conjugate().transform(look);
                    desired=OpticMath.aligned(Handling.local(gun,pose.getEyeLeft().getPosition()),look,sight,AIMING[i],c.zones().sight())
                        ||OpticMath.aligned(Handling.local(gun,pose.getEyeRight().getPosition()),look,sight,AIMING[i],c.zones().sight());
                }
            }
            AIMING[i]=desired;
        }
        if(AIMING[0]||AIMING[1])mc.player.setSprinting(false);
    }
}
