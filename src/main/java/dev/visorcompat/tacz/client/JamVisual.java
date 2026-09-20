package dev.visorcompat.tacz.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.*;
import dev.visorcompat.tacz.server.ServerJams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import org.vmstudio.visor.api.common.player.VRPlayerPose;

/** Attached cosmetic obstructions. Authoritative counts live on the server gun stack. */
public final class JamVisual {
    public static void render(PoseStack matrices,ItemStack stack,WeaponProfile profile,Calibration calibration,
                              GunPose gun,VRPlayerPose pose,Handling.Phase phase,int light) {
        var jam=ServerJams.read(stack);
        boolean preview=Minecraft.getInstance().screen instanceof CalibrationScreen screen && screen.casingPreview() && Minecraft.getInstance().player!=null && stack==Minecraft.getInstance().player.getMainHandItem();
        if(!preview && (jam.remaining()==0 || jam.kind()==Jam.Kind.NONE || jam.kind()==Jam.Kind.DUD))return;
        var item=IGun.getIGunOrNull(stack);if(item==null)return;
        var common=TimelessAPI.getCommonGunIndex(item.getGunId(stack)).orElse(null);if(common==null)return;
        var ammo=TimelessAPI.getClientAmmoIndex(common.getGunData().getAmmoId()).orElse(null);if(ammo==null)return;
        boolean spent=preview || jam.kind()==Jam.Kind.STOVEPIPE;
        var model=ammo.getShellModel();
        var texture=ammo.getShellTextureLocation();
        var anchor=phase==Handling.Phase.PLUCKING?Handling.local(gun,pose.getOffhand().getPosition()):JamProfile.of(profile,calibration).port();
        for(int i=0;i<(preview?1:jam.remaining());i++) {
            matrices.pushPose();
            try {
                matrices.translate(anchor.x,anchor.y+i*.012f,anchor.z-i*.014f);
                // Shell model runs along local Z. Turn it across the bore, tilted upward 22.5 degrees.
                if(spent){matrices.mulPose(Axis.ZP.rotationDegrees(22.5f));matrices.mulPose(Axis.YP.rotationDegrees(90));}
                else matrices.mulPose(Axis.YP.rotationDegrees(i==0?-8:8));
                matrices.mulPose(Axis.YP.rotationDegrees(180)); // Case base faces inward, narrow/open end outward.
                float visual=calibration.gunScale()*calibration.casingScale();matrices.scale(visual,visual,visual);
                if(model!=null && texture!=null) {
                    matrices.translate(0,-1.5,0);
                    model.render(matrices,ItemDisplayContext.GROUND,RenderType.entityCutout(texture),light,OverlayTexture.NO_OVERLAY);
                } else {
                    HandlingEffects.LIVE_ROUND.render(matrices,Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(
                        RenderType.entityCutout(new ResourceLocation("minecraft","textures/block/gold_block.png"))),light,OverlayTexture.NO_OVERLAY);
                }
            } finally {matrices.popPose();}
        }
    }
}
