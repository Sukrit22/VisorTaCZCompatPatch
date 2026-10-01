package dev.visorcompat.tacz;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;
class RecoilMotionTest {
    private RecoilMotion.Curve curve(){float[] p=new float[33],y=new float[33];for(int i=1;i<32;i++){p[i]=2;y[i]=.5f;}return new RecoilMotion.Curve(8,p,y);}
    @Test void firstBulletUnaffectedFollowingBulletKicksAndRecovers(){var m=new RecoilMotion();m.add(100,curve());assertEquals(0,m.angles(100).x);assertEquals(2,m.angles(102).x);assertEquals(.5f,m.angles(102).y);assertEquals(0,m.angles(108).length());}
    @Test void muzzleAndDirectionRotateTogetherWithoutMovingGrip(){var m=new RecoilMotion();m.add(0,curve());var b=new GunPose(new Vector3f(1,2,3),new Quaternionf().rotateY(.7f),new Vector3f(1,2,3).add(new Quaternionf().rotateY(.7f).transform(new Vector3f(0,0,-.5f))),new Vector3f(0,0,-1),1);var k=m.apply(b,2);assertEquals(b.hand(),k.hand());assertEquals(.5f,k.muzzle().distance(k.hand()),1e-5);assertTrue(new Vector3f(k.muzzle()).sub(k.hand()).normalize().distance(k.direction())<1e-5);assertTrue(k.muzzle().y>b.muzzle().y);assertEquals(2,b.hand().y);}
    @Test void repeatedFireIsBoundedAndStateIsIndependent(){var a=new RecoilMotion();var b=new RecoilMotion();for(int i=0;i<30;i++)a.add(0,curve());assertEquals(8,a.angles(2).x);assertEquals(4,a.angles(2).y);assertEquals(0,b.angles(2).length());assertEquals(0,a.angles(40).length());}
    @Test void malformedCurvesAreIgnored(){float[] p=new float[33];p[2]=Float.NaN;var m=new RecoilMotion();m.add(0,new RecoilMotion.Curve(8,p,new float[33]));assertEquals(0,m.angles(1).length());assertFalse(new RecoilMotion.Curve(41,new float[33],new float[33]).valid());assertFalse(new RecoilMotion.Curve(8,new float[2],new float[2]).valid());}
    @Test void curveCopiesInputs(){float[] p=new float[33];var c=new RecoilMotion.Curve(8,p,p);p[2]=Float.NaN;assertTrue(c.valid());}
}
