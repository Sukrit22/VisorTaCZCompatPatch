package dev.visorcompat.tacz.client;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.Handling;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;

@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT)
public final class AutoAds {
    private static boolean owned,aiming;
    public static boolean ownsInput() { return CompatSettings.autoAds() && CompatSettings.active() && ClientControls.supported(); }
    public static boolean aiming() { return aiming; }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        if(mc.player==null) {owned=false;aiming=false;return;}
        boolean owns=ownsInput(),desired=false;
        if(owns && PhysicalClient.anchor()==null && mc.screen==null && !mc.isPaused() && mc.player.isAlive()
            && VisorAPI.clientState().stateMode().isFocused()
            && VisorAPI.client().getVRLocalPlayer().getRawController(HandType.MAIN).isTracking()
            && !IGunOperator.fromLivingEntity(mc.player).getSynReloadState().getStateType().isReloading()
            && (!PhysicalClient.active() || Handling.fireable(PhysicalClient.phase()))) {
            var stack=mc.player.getMainHandItem();var profile=Profiles.get(stack);
            var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.TICK);
            var gun=GunPose.resolve(pose,profile,mc.player.getOffhandItem().isEmpty() && PhysicalClient.supporting(),CalibrationStore.get(Profiles.key(stack)),PhysicalClient.anchor());
            Vector3f sight=OpticGeometry.sight(stack,profile);
            if(sight!=null)sight.add(CalibrationStore.get(Profiles.key(stack)).interactions().sight().vector()).mul(CalibrationStore.get(Profiles.key(stack)).gunScale());
            boolean supported=!CompatSettings.twoHandAds() || VisorAPI.client().getVRLocalPlayer().getRawController(HandType.OFFHAND).isTracking() && (PhysicalClient.active()?PhysicalClient.supporting():mc.player.getOffhandItem().isEmpty() && gun!=null && Handling.inside(Handling.local(gun,pose.getOffhand().getPosition()),Handling.support(profile,CalibrationStore.get(Profiles.key(stack))),CalibrationStore.get(Profiles.key(stack)),profile.pump()?ZoneSizes.Zone.RACK:ZoneSizes.Zone.SUPPORT));
            if(gun!=null && sight!=null && supported) {
                Vector3f look=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1)).normalize();
                look=new Quaternionf(gun.rotation()).conjugate().transform(look);
                desired=OpticMath.aligned(Handling.local(gun,pose.getEyeLeft().getPosition()),look,sight,aiming,CalibrationStore.get(Profiles.key(stack)).zones().sight())
                    || OpticMath.aligned(Handling.local(gun,pose.getEyeRight().getPosition()),look,sight,aiming,CalibrationStore.get(Profiles.key(stack)).zones().sight());
            }
        }
        if(desired)mc.player.setSprinting(false);
        var operator=IClientPlayerGunOperator.fromLocalPlayer(mc.player);
        if((owns || owned) && operator.isAim()!=desired)operator.aim(desired);
        aiming=desired;owned=owns;
    }
}
