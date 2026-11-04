package dev.visorcompat.tacz;

import dev.visorcompat.tacz.physical.ChamberGuide;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChamberGuideTest {
    @Test void openingAndGuideConstrainTravel() {
        assertEquals(0,ChamberGuide.displacement(0,1));
        assertEquals(.004f,ChamberGuide.displacement(.004f,1),1e-7);
        assertEquals(.012f,ChamberGuide.displacement(.055f,1),1e-7);
        assertEquals(.006f,ChamberGuide.displacement(.055f,.5f),1e-7);
        assertEquals(0,ChamberGuide.displacement(.055f,-1));
    }
    @Test void repeatedEyesAndFrameRatesDoNotAdvanceMotion() {
        for(int rate:new int[]{30,72,90,144})for(int frame=0;frame<rate;frame++) {
            assertEquals(.006f,ChamberGuide.displacement(.02f,.5f),1e-7);
            assertEquals(.006f,ChamberGuide.displacement(.02f,.5f),1e-7);
        }
    }
    @Test void invalidInputsNeverEscapeBounds() {
        assertEquals(0,ChamberGuide.displacement(Float.NaN,1));
        assertEquals(0,ChamberGuide.displacement(.02f,Float.POSITIVE_INFINITY));
        assertEquals(0,ChamberGuide.displacement(-1,1));
        assertEquals(.012f,ChamberGuide.displacement(1,4),1e-7);
    }
}
