package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.Profiles;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.RoomFlickSample;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;

/** One room sample per client tick; server alone decides whether a cylinder action occurs. */
final class CylinderRoomClient {
    static void tick(boolean input){
        var p=Minecraft.getInstance().player;
        if(!input||p==null||!PhysicalClient.active()||!Profiles.cylinder(p.getMainHandItem()))return;
        var vr=VisorAPI.client().getVRLocalPlayer();
        if(!vr.getRawController(HandType.MAIN).isTracking()||!vr.getRawController(HandType.OFFHAND).isTracking())return;
        var room=vr.getPoseData(PlayerPoseType.ROOM);
        var m=room.getMainHand().getPosition();var o=room.getOffhand().getPosition();
        var right=room.getHmd().getRotation().transformDirection(new Vector3f(1,0,0));right.y=0;
        if(right.lengthSquared()<.1f)return;right.normalize();
        var sample=new RoomFlickSample(m.x(),m.y(),m.z(),o.x(),o.y(),o.z(),right.x,right.z);
        if(sample.valid())CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.CylinderRoom(p.getInventory().selected,Profiles.key(p.getMainHandItem()),sample));
    }
}
