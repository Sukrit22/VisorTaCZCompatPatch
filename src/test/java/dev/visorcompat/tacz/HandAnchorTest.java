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
    @Test void mainRegripPreservesScopeAndMuzzleDespiteDifferentControllerAngle(){
        var controller=new Vector3f(.21f,1.42f,.12f);var controllerRotation=new Quaternionf().rotationYXZ(.8f,-.3f,.4f);
        var base=pose(controller,controllerRotation,1);
        var mainPose=(VRPlayerPose)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{VRPlayerPose.class},(p,m,a)->switch(m.getName()){
            case "getMainHand" -> base.getOffhand();
            case "getOffhand" -> pose(new Vector3f(99),new Quaternionf(),1).getOffhand();
            case "getWorldScale" -> 1f;default -> null;
        });
        var gun=new GunPose(new Vector3f(.18f,1.4f,.1f),new Quaternionf().rotationY(.3f),new Vector3f(),new Vector3f(),1);
        var anchor=HandAnchor.captureMain(gun,mainPose);var resolved=anchor.resolve(mainPose,Profiles.byId("tacz:m700"),Calibration.ZERO);
        assertTrue(anchor.mainHand());assertTrue(resolved.hand().distance(gun.hand())<1e-5);
        assertEquals(1,java.lang.Math.abs(resolved.rotation().dot(gun.rotation())),1e-5);
        var expectedMuzzle=new Quaternionf(gun.rotation()).transform(Profiles.byId("tacz:m700").muzzleOffset()).add(gun.hand());
        assertTrue(resolved.muzzle().distance(expectedMuzzle)<1e-5);
    }
    @Test void mainAnchorPacketPreservesOwner(){
        var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{var anchor=new HandAnchor(new Quaternionf(),new Vector3f(.03f,-.02f,.01f),true,new Vector3f(0,0,-.3f));HandAnchor.write(b,anchor);assertEquals(anchor,HandAnchor.read(b));assertEquals(0,b.readableBytes());}finally{b.release();}
    }

    private VRPlayerPose both(Vector3f main,Vector3f off){
        var m=pose(main,new Quaternionf(),1).getOffhand();var o=pose(off,new Quaternionf(),1).getOffhand();
        return (VRPlayerPose)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{VRPlayerPose.class},(p,method,a)->switch(method.getName()){
            case "getMainHand" -> m;case "getOffhand" -> o;case "getWorldScale" -> 1f;default -> null;
        });
    }
    @Test void returnedMainGripStillSteersWithOffhand(){
        var p=Profiles.byId("tacz:m700");var start=both(new Vector3f(0,1,0),new Vector3f(0,1,-.3f));
        var gun=new GunPose(new Vector3f(0,1,0),new Quaternionf(),new Vector3f(),new Vector3f(0,0,-1),1);
        var anchor=HandAnchor.captureMain(gun,start);
        var same=GunPose.resolve(start,p,true,Calibration.ZERO,anchor);assertTrue(same.direction().distance(gun.direction())<1e-5);
        var moved=both(new Vector3f(0,1,0),new Vector3f(.08f,1,-.3f));
        var steered=GunPose.resolve(moved,p,true,Calibration.ZERO,anchor);
        assertTrue(steered.direction().x>.2f);assertEquals(same.hand(),steered.hand());
        assertTrue(GunPose.resolve(moved,p,false,Calibration.ZERO,anchor).direction().distance(gun.direction())<1e-5);
    }
}
