package dev.visorcompat.tacz;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SightCalibrationTest {
    @Test void missingAttachmentAdjustmentPreservesGunBaseline(){
        var base=gun(new InteractionOffsets.Point(.01f,.02f,-.6f),1.2f);
        assertTrue(base.valid());
        var result=SightCalibration.ZERO.apply(new Vector3f(.02f,.03f,-.1f),base);
        assertEquals(.036f,result.x,1e-6);assertEquals(.06f,result.y,1e-6);assertEquals(-.84f,result.z,1e-6);
        assertEquals(base.zones().sight(),SightCalibration.ZERO.dimensions(base));
    }
    @Test void attachmentOffsetAddsBeforeGunScaleWithoutMutatingGeometry(){
        var base=gun(new InteractionOffsets.Point(.01f,.02f,-.6f),1.2f);var model=new Vector3f(.02f,.03f,-.1f);
        var extra=new SightCalibration(.03f,-.01f,-.2f,null);
        var result=extra.apply(model,base);
        assertEquals(.072f,result.x,1e-6);assertEquals(.048f,result.y,1e-6);assertEquals(-1.08f,result.z,1e-6);
        assertEquals(new Vector3f(.02f,.03f,-.1f),model);
    }
    @Test void attachmentDimensionsOverrideButDoNotResizeBaseline(){
        var box=new ZoneSizes.Box(.12f,.10f,.4f);var extra=new SightCalibration(0,0,0,box);
        assertEquals(box,extra.dimensions(Calibration.ZERO));assertEquals(ZoneSizes.DEFAULT.sight(),Calibration.ZERO.zones().sight());
    }
    @Test void widerSightLimitDoesNotWidenOtherInteractionOffsets(){
        assertTrue(gun(new InteractionOffsets.Point(0,0,-1.2f),1f).valid());
        assertFalse(gun(new InteractionOffsets.Point(0,0,-2.01f),1f).valid());
        var offsets=new InteractionOffsets(new InteractionOffsets.Point(0,0,-.6f),null,null,null,null,null);
        assertFalse(offsets.valid());assertFalse(new SightCalibration(Float.NaN,0,0,null).valid());
        assertFalse(new SightCalibration(0,0,-2.01f,null).valid());assertTrue(new SightCalibration(0,0,-1.2f,null).valid());
    }
    @Test void selectedHybridViewUsesZoomMappingRatherThanAlwaysFirstView(){
        assertEquals(1,OpticMath.scopeView(0,new int[]{2,1}));assertEquals(0,OpticMath.scopeView(1,new int[]{2,1}));
        assertEquals(1,OpticMath.scopeView(2,new int[]{2,1}));assertEquals(0,OpticMath.scopeView(0,null));
    }
    @Test void hipHeightEyesDoNotEnterSightEvenWhenLookingForward(){
        assertFalse(OpticMath.aligned(new Vector3f(0,-.4f,.2f),new Vector3f(0,0,-1),new Vector3f(),false));
    }
    private static Calibration gun(InteractionOffsets.Point sight,float scale){
        return new Calibration(0,0,0,0,0,0,0,0,0,new InteractionOffsets(null,null,null,null,null,sight),scale,ZoneSizes.DEFAULT);
    }
}
