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
        if(jam.remaining()==0 || jam.kind()==Jam.Kind.NONE || jam.kind()==Jam.Kind.DUD)return;
        var item=IGun.getIGunOrNull(stack);if(item==null)return;
        var common=TimelessAPI.getCommonGunIndex(item.getGunId(stack)).orElse(null);if(common==null)return;
        var ammo=TimelessAPI.getClientAmmoIndex(common.getGunData().getAmmoId()).orElse(null);if(ammo==null)return;
        boolean spent=jam.kind()==Jam.Kind.STOVEPIPE;
        var model=spent?ammo.getShellModel():ammo.getAmmoEntityModel();
        var texture=spent?ammo.getShellTextureLocation():ammo.getAmmoEntityTextureLocation();
        var anchor=phase==Handling.Phase.PLUCKING?Handling.local(gun,pose.getOffhand().getPosition()):JamProfile.of(profile,calibration).port();
        for(int i=0;i<jam.remaining();i++) {
            matrices.pushPose();
            try {
                matrices.translate(anchor.x,anchor.y+i*.012f,anchor.z-i*.014f);
                matrices.mulPose(Axis.ZP.rotationDegrees(spent?65:12*i));
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
