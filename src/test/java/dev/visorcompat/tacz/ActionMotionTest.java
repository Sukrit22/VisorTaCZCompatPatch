package dev.visorcompat.tacz;

import com.tacz.guns.client.model.bedrock.BedrockPart;
import dev.visorcompat.tacz.client.ActionMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActionMotionTest {
    @Test void nestedBoltInheritsItsParentWithoutAnExtraStroke() {
        var handle = new BedrockPart("handle");
        var intermediate = new BedrockPart("group");
        var bolt = new BedrockPart("bolt");
        handle.addChild(intermediate); intermediate.addChild(bolt);
        handle.offsetZ = .2f; bolt.offsetZ = .03f;
        ActionMotion.follow(handle, bolt, .2f, 1);
        assertEquals(.03f, bolt.offsetZ, 1e-6);
        assertEquals(.23f, handle.offsetZ + bolt.offsetZ, 1e-6);
    }
    @Test void separateChargingHandleCanDriveAnOppositeBoltIndicator() {
        var handle = new BedrockPart("charge_handle");
        var bolt = new BedrockPart("bolt"); bolt.offsetZ = .1f;
        ActionMotion.follow(handle, bolt, .07f, -.5707143f);
        assertEquals(.06005f, bolt.offsetZ, 1e-6);
        ActionMotion.follow(handle, bolt, -.07f, -.5707143f);
        assertEquals(.1f, bolt.offsetZ, 1e-6);
    }
    @Test void siblingBoltFollowsRegularHandle() {
        var handle = new BedrockPart("handle"); var bolt = new BedrockPart("bolt");
        ActionMotion.follow(handle, bolt, .12f, 1);
        assertEquals(.12f, bolt.offsetZ, 1e-6);
        ActionMotion.follow(handle, null, .12f, 1);
    }
}
