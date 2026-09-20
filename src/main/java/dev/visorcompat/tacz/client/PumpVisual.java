package dev.visorcompat.tacz.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.Handling;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.common.player.VRPlayerPose;

public final class PumpVisual {
    public static void render(PoseStack matrices,WeaponProfile profile,GunPose gun,VRPlayerPose pose,Handling.Phase phase,int light){
        render(matrices,profile,gun,pose,phase,light,CalibrationStore.render(Profiles.key(Minecraft.getInstance().player.getMainHandItem())).gunScale()*CalibrationStore.render(Profiles.key(Minecraft.getInstance().player.getMainHandItem())).casingScale());
    }
    public static void render(PoseStack matrices,WeaponProfile profile,GunPose gun,VRPlayerPose pose,Handling.Phase phase,int light,float size){
        if(!profile.pump()||phase!=Handling.Phase.SHELL)return;
        var ammo=TimelessAPI.getClientAmmoIndex(new ResourceLocation("tacz","12g")).orElse(null);if(ammo==null)return;
        // The projectile entity model is not a physical cartridge (often a generic bullet).
        var model=ammo.getShellModel();var texture=ammo.getShellTextureLocation();
        var worldGun=new Matrix4f().translation(gun.hand()).rotate(gun.rotation()).scale(gun.worldScale());
        var offhand=new Matrix4f().translation(pose.getOffhand().getPosition()).mul(pose.getOffhand().getRotation()).scale(gun.worldScale());
        var correction=worldGun.invert().mul(offhand);
        matrices.pushPose();
        try{
            matrices.mulPoseMatrix(correction);matrices.last().normal().mul(correction.get3x3(new org.joml.Matrix3f()).invert().transpose());
            matrices.scale(size,size,size);
            if(model!=null&&texture!=null){matrices.translate(0,-1.5,0);model.render(matrices,ItemDisplayContext.GROUND,RenderType.entityCutout(texture),light,OverlayTexture.NO_OVERLAY);}
            else HandlingEffects.LIVE_ROUND.render(matrices,Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutout(new ResourceLocation("minecraft","textures/block/red_concrete.png"))),light,OverlayTexture.NO_OVERLAY);
        } finally{matrices.popPose();}
    }
}
