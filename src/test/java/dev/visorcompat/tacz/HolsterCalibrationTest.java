package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;
class HolsterCalibrationTest {
    @Test void pistolCantsThirtyDegreesForwardFromVertical(){var c=HolsterCalibration.defaults(true);var muzzle=c.rotation(0,1).transform(new Vector3f(0,0,-1));assertEquals(-.8660254f,muzzle.y,1e-5);assertEquals(-.5f,muzzle.z,1e-5);assertTrue(c.z()<0);}
    @Test void longGunPointsDownFortyFiveAndRightSideFacesFront(){var c=HolsterCalibration.defaults(false);var q=c.rotation(0,1);var muzzle=q.transform(new Vector3f(0,0,-1));var right=q.transform(new Vector3f(1,0,0));assertEquals(-.7071068f,muzzle.y,1e-5);assertEquals(-1,right.z,1e-5);assertTrue(c.y()<1.2f);assertTrue(c.z()<-.3f);}
    @Test void mirrorPositionDoesNotMoveHeightOrDepth(){var c=HolsterCalibration.defaults(true);var a=c.position(new Vector3f(),0,1,1,1);var b=c.position(new Vector3f(),0,1,1,-1);assertEquals(a.x,-b.x);assertEquals(a.y,b.y);assertEquals(a.z,b.z);}
    @Test void rejectInvalidFiles(){assertFalse(new HolsterCalibration(Float.NaN,1,0,0,0,0).valid());assertFalse(new HolsterCalibration(0,-1,0,0,0,0).valid());assertTrue(HolsterCalibration.defaults(false).valid());}
    @Test void holsterHeightUsesRoomFloorNotEyeHeight(){
        var c=HolsterCalibration.defaults(true);var floor=new Vector3f(10,64,20);var eyes=new Vector3f(10,65.7f,20);
        var handle=c.fromRoomFloor(floor,eyes,0,1,1);
        assertEquals(64.94f,handle.y,1e-5);assertTrue(handle.y<eyes.y());
        assertEquals(handle,c.fromRoomFloor(floor,new Vector3f(10,66.2f,20),0,1,1));
    }
    @Test void zeroHeightIsOnVrFloorAndScaleAppliedOnce(){
        var floor=new Vector3f(0,100,0);var head=new Vector3f(0,103.4f,0);
        assertEquals(100f,new HolsterCalibration(.25f,0,0,-60,0,0).fromRoomFloor(floor,head,0,2,1).y,1e-5);
        assertEquals(101.88f,HolsterCalibration.defaults(true).fromRoomFloor(floor,head,0,2,1).y,1e-5);
    }
    @Test void crouchFollowsTwoThirdsAtAnyWorldScale(){
        var c=HolsterCalibration.defaults(true);
        for(float scale:new float[]{.5f,1f,2f}){
            var floor=new Vector3f(0,64,0);var head=new Vector3f(0,64+1.62f*scale,0);
            float standing=c.followingCrouch(floor,head,0,scale,1).y;
            head.y-=.30f*scale;
            assertEquals(.20f*scale,standing-c.followingCrouch(floor,head,0,scale,1).y,1e-5);
        }
    }
}
