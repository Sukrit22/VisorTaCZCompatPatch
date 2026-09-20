package dev.visorcompat.tacz;
import dev.visorcompat.tacz.physical.*;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HandlingFollowupTest {
    @Test void sweptReleaseCatchesForwardPassEvenIfBothSamplesOutsideBox(){
        var c=new Vector3f();var b=new ZoneSizes.Box(.06f,.06f,.06f);
        assertTrue(ActionCycle.releaseSweep(new Vector3f(0,0,.08f),new Vector3f(0,0,-.08f),c,b));
        assertTrue(ActionCycle.releaseSweep(new Vector3f(0,.08f,0),new Vector3f(0,-.08f,0),c,b));
        assertTrue(ActionCycle.releaseSweep(new Vector3f(0,0,.005f),new Vector3f(0,0,-.005f),c,b));
        assertFalse(ActionCycle.releaseSweep(new Vector3f(0,0,-.08f),new Vector3f(0,0,.08f),c,b));
        assertFalse(ActionCycle.releaseSweep(new Vector3f(.1f,0,.08f),new Vector3f(.1f,0,-.08f),c,b));
        assertFalse(ActionCycle.releaseSweep(new Vector3f(0,0,1),new Vector3f(0,0,-1),c,b));
        assertFalse(ActionCycle.releaseSweep(c,c,c,b));
        assertFalse(ActionCycle.releaseSweep(new Vector3f(Float.NaN,0,0),c,c,b));
    }
    @Test void pistolHasDistinctSupportAndMagazineTargets(){
        var p=Profiles.byId("tacz:glock_17");var c=Calibration.ZERO;
        assertEquals(Handling.Target.SUPPORT,Handling.target(Handling.Phase.READY,Handling.support(p,c),false,p,c));
        assertEquals(Handling.Target.MAGAZINE,Handling.target(Handling.Phase.READY,Handling.magazine(p,c),false,p,c));
        assertEquals(Handling.Target.RACK,Handling.target(Handling.Phase.NEED_RACK,Handling.rack(p,c),false,p,c));
        assertTrue(CalibrationLayout.pages("tacz:glock_17").stream().anyMatch(page->page.id().equals("support")));
    }
    @Test void rifleSupportIsAvailableWhileBoltNeedsCycling(){
        var p=Profiles.byId("tacz:m700");
        assertEquals(Handling.Target.SUPPORT,Handling.target(Handling.Phase.NEED_RACK,Handling.support(p,Calibration.ZERO),false,p,Calibration.ZERO,false,false,false));
        assertEquals(Handling.Target.SUPPORT,Handling.target(Handling.Phase.NEED_RACK,Handling.support(p,Calibration.ZERO),false,p,Calibration.ZERO,true,false,true));
    }
    @Test void casingScaleRoundTripsAndOldProfilesDefaultWithoutChangingOffsets(){
        var gson=new com.google.gson.Gson();var old=gson.fromJson("{\"y\":-0.01,\"z\":0.03,\"gunScale\":0.8}",Calibration.class);
        assertEquals(1f,old.casingScale());assertEquals(-.01f,old.y());assertEquals(.03f,old.z());
        var c=new Calibration(0,0,0,0,0,0,0,0,0,InteractionOffsets.ZERO,.8f,ZoneSizes.DEFAULT,true,.6f);
        assertEquals(c,gson.fromJson(gson.toJson(c),Calibration.class));
        var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{dev.visorcompat.tacz.network.CalibrationCodec.write(b,c);assertEquals(c,dev.visorcompat.tacz.network.CalibrationCodec.read(b));assertEquals(0,b.readableBytes());}finally{b.release();}
        assertFalse(gson.fromJson("{\"casingScale\":0}",Calibration.class).valid());
        assertFalse(gson.fromJson("{\"casingScale\":3.1}",Calibration.class).valid());
    }
}
