package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.TossMotion;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TossMotionTest {
    @Test void renderSubstepsMatchOnePhysicsStep(){var p=new Vector3f(0,1,0);var v=new Vector3f(1,2,0);var half=TossMotion.advance(p,v,9.81f,.025f);var finish=TossMotion.advance(half,new Vector3f(v).add(0,-9.81f*.025f,0),9.81f,.025f);assertTrue(finish.distance(TossMotion.advance(p,v,9.81f,.05f))<1e-6);}
    @Test void fastCrossingCanBeCaughtEvenWhenEndpointsMiss(){assertTrue(TossMotion.nearSegment(new Vector3f(-.3f,0,0),new Vector3f(.3f,0,0),new Vector3f(),.18f));assertFalse(TossMotion.nearSegment(new Vector3f(-.3f,1,0),new Vector3f(.3f,1,0),new Vector3f(),.18f));}
}
