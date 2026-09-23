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
    public static void cylinderEject(ServerPlayer p,GunPose pose,int live,int spent,boolean gravity){
        var stack=p.getMainHandItem();var gun=IGun.getIGunOrNull(stack);if(gun==null)return;
        var index=TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);if(index==null)return;
        var c=CompatNetwork.calibration(p);var profile=Profiles.get(stack);
        var center=Handling.magazine(profile,c);
        for(int i=0;i<live+spent;i++){
            float angle=(float)(i*Math.PI*2/Math.max(1,live+spent));
            var spread=new Vector3f((float)Math.cos(angle)*.014f,(float)Math.sin(angle)*.014f,0).mul(c.gunScale());
            var pos=new org.joml.Quaternionf(pose.rotation()).transform(new Vector3f(center).add(spread).mul(pose.worldScale())).add(pose.hand());
            var velocity=new org.joml.Quaternionf(pose.rotation()).transform(new Vector3f(spread).mul(5).add(0,0,gravity?.05f:1.5f).mul(pose.worldScale()));
            CompatNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new CompatNetwork.Feedback(i<live?7:8,p.getId(),gun.getGunId(stack),index.getGunData().getAmmoId(),pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z,pose.worldScale(),c.gunScale()*c.casingScale()));
        }
        if(!gravity)emit(p,2,pose);
    }
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
            kind,p.getId(),gun.getGunId(stack),index.getGunData().getAmmoId(),pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z,pose.worldScale(),CompatNetwork.calibration(p).gunScale()*CompatNetwork.calibration(p).casingScale()));
    }
}
