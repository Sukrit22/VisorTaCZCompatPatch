package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.MagazineMotion;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MagazineMotionTest {
    private MagazineMotion motion(Quaternionf q){return new MagazineMotion(new Vector3f(),q,new Vector3f(0,-1,0),.10f,.25f,1);}
    @Test void stationaryMagazineSlidesBeforeFreeFlight(){var q=new Quaternionf();var m=motion(q);m.advance(new Vector3f(),q,.05f);assertFalse(m.free());assertTrue(m.distance()>0);for(int i=0;i<5;i++)m.advance(new Vector3f(),q,.05f);assertTrue(m.free());assertEquals(0,m.velocity().x,1e-6);assertEquals(0,m.velocity().z,1e-6);}
    @Test void sidewaysRailDoesNotInventDownwardExit(){var q=new Quaternionf().rotateZ((float)Math.PI/2);var m=motion(q);for(int i=0;i<20;i++)m.advance(new Vector3f(),q,.05f);assertFalse(m.free());assertEquals(0,m.distance(),1e-5);}
    @Test void tiltSlowsGravityExit(){var straight=motion(new Quaternionf());var q=new Quaternionf().rotateZ((float)Math.PI/3);var tilted=motion(q);straight.advance(new Vector3f(),new Quaternionf(),.1f);tilted.advance(new Vector3f(),q,.1f);assertTrue(tilted.distance()<straight.distance());}
    @Test void postReleaseMovementTransfersMomentum(){var q=new Quaternionf();var m=motion(q);for(int i=1;i<=5;i++)m.advance(new Vector3f(i*.025f,0,0),q,.04f);assertTrue(m.free());assertTrue(m.velocity().x>.1f);}
    @Test void staleTrackingDoesNotLaunchMagazine(){var q=new Quaternionf();var m=motion(q);m.advance(new Vector3f(10,0,0),q,1);assertEquals(0,m.velocity().length());assertFalse(m.free());}
    @Test void freeMagazineDoesNotFollowGun(){var q=new Quaternionf();var m=motion(q);m.detach();m.advance(new Vector3f(10,0,0),q,.1f);assertEquals(0,m.position().x);assertTrue(m.position().y<0);}
}
