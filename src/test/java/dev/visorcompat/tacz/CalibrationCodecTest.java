package dev.visorcompat.tacz;
import dev.visorcompat.tacz.network.CalibrationCodec;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalibrationCodecTest {
    @Test void networkPreservesScaleAndIndependentBoxes(){
        var zones=new ZoneSizes(new ZoneSizes.Box(.2f,.3f,.4f),null,null,null,null,null,null);
        var c=new Calibration(.01f,.02f,.03f,13,2,1,.02f,0,0,InteractionOffsets.ZERO,.75f,zones,false);
        var b=new FriendlyByteBuf(Unpooled.buffer());
        try {CalibrationCodec.write(b,c);assertEquals(c,CalibrationCodec.read(b));assertEquals(0,b.readableBytes());}
        finally{b.release();}
    }
}
