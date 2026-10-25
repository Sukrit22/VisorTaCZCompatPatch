package dev.visorcompat.tacz;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.bedrock.*;
import dev.visorcompat.tacz.client.OpticAperture;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OpticApertureTest {
    @Test void default552PhysicalWindowDoesNotUseDesktopCameraAtZ16(){
        // TaCZ default sight_552_geo: ocular cube origin/size; scope_view pivot Z=16.
        var ocular=new BedrockPart("ocular");
        ocular.cubes.add(new BedrockCubeBox(0,0,-.87638f,1.02427f,-.169f,1.74963f,1.33741f,.02463f,0,false,64,64));
        var mount=new PoseStack();mount.scale(.65f,.65f,.65f);mount.translate(0,.2,-.3);
        var aperture=OpticAperture.center(mount,List.of(ocular));
        var eyeAnchor=mount.last().pose().transformPosition(new Vector3f(0,1.7f/16,1));
        assertNotNull(aperture);assertEquals(.65f*(-.3f+(-.169f+.02463f/2)/16),aperture.z,1e-6);
        assertTrue(eyeAnchor.z-aperture.z>.65f);
    }
    @Test void parentMountAndChildTransformsAreAppliedOnce(){
        var parent=new BedrockPart("parent");parent.setPos(0,0,16);
        var child=new BedrockPart("ocular");child.setPos(0,0,8);
        child.cubes.add(new BedrockCubeBox(0,0,-1,-1,0,2,2,0,0,false,16,16));
        var mount=new PoseStack();mount.translate(0,0,-.4);mount.scale(.5f,.5f,.5f);
        assertEquals(.35f,OpticAperture.center(mount,List.of(parent,child)).z,1e-6);
        assertEquals(-.4f,mount.last().pose().m32(),1e-6);
    }
    @Test void unknownOrEmptyOcularFallsBackWithoutInventedPosition(){
        var part=new BedrockPart("ocular");assertNull(OpticAperture.center(new PoseStack(),List.of(part)));
        part.cubes.add((pose,consumer,light,overlay,r,g,b,a)->{});
        assertNull(OpticAperture.center(new PoseStack(),List.of(part)));
    }
}
