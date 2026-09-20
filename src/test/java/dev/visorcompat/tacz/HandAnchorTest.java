package dev.visorcompat.tacz;
import org.joml.*;
import org.junit.jupiter.api.Test;
import org.vmstudio.visor.api.common.player.*;
import java.lang.reflect.Proxy;
import static org.junit.jupiter.api.Assertions.*;
class HandAnchorTest {
    private VRPlayerPose pose(Vector3f off,Quaternionf rotation,float scale){
        var controller=(VRPose)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{VRPose.class},(p,m,a)->switch(m.getName()){
            case "getPosition" -> off;case "getRotation" -> new Matrix4f().rotate(rotation);default -> null;
        });
        return (VRPlayerPose)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{VRPlayerPose.class},(p,m,a)->switch(m.getName()){
            case "getOffhand" -> controller;case "getWorldScale" -> scale;default -> null;
        });
    }
    @Test void captureDoesNotJumpAndTracksSupportInsteadOfMainHand(){
        var profile=Profiles.byId("tacz:m700");var c=Calibration.ZERO;
        var off=new Vector3f(.3f,1.4f,-.2f);var rot=new Quaternionf().rotationY(.7f);var origin=new Vector3f(.2f,1.3f,.1f);
        var gun=new GunPose(origin,new Quaternionf().rotationYXZ(.4f,.2f,.1f),new Vector3f(),new Vector3f(),2);
        var initial=pose(off,rot,2);var anchor=HandAnchor.capture(gun,initial);var same=anchor.resolve(initial,profile,c);
        assertTrue(same.hand().distance(origin)<1e-5);assertEquals(1f,java.lang.Math.abs(same.rotation().dot(gun.rotation())),1e-5);
        var shifted=anchor.resolve(pose(new Vector3f(off).add(1,0,0),rot,2),profile,c);
        assertTrue(shifted.hand().distance(new Vector3f(origin).add(1,0,0))<1e-5);
    }
    @Test void anchorPacketRoundTripsIncludingAbsentState(){
        var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{var a=new HandAnchor(new Quaternionf().rotationX(.5f),new Vector3f(.1f,.2f,.3f));HandAnchor.write(b,a);HandAnchor.write(b,null);
            assertEquals(a,HandAnchor.read(b));assertNull(HandAnchor.read(b));assertEquals(0,b.readableBytes());}finally{b.release();}
    }
}
