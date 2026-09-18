package dev.visorcompat.tacz.server;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.Handling;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

public final class HandlingFeedback {
    public static void emit(ServerPlayer p,int kind,GunPose pose){emit(p,kind,pose,false);}
    public static void emit(ServerPlayer p,int kind,GunPose pose,boolean offhand){
        if(pose==null)return;
        var stack=p.getMainHandItem();var gun=IGun.getIGunOrNull(stack);if(gun==null)return;
        var index=TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);if(index==null)return;
        var profile=Profiles.get(stack);if(profile==null)return;
        var pos=pose.rotation().transform(dev.visorcompat.tacz.physical.JamProfile.of(profile,CompatNetwork.calibration(p)).port().mul(pose.worldScale())).add(pose.hand());
        if(offhand && org.vmstudio.visor.api.VisorAPI.getVRPlayer(p) instanceof org.vmstudio.visor.api.server.player.VRServerPlayer vr)
            pos.set(vr.getPoseData().getOffhand().getPosition());
        var velocity=pose.rotation().transform(new Vector3f(.8f,.45f,.12f).mul(pose.worldScale()));
        CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new CompatNetwork.Feedback(
            kind,p.getId(),gun.getGunId(stack),index.getGunData().getAmmoId(),pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z,pose.worldScale()));
    }
}
