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
        if((!profile.pump()&&!profile.cylinder())||(phase!=Handling.Phase.SHELL&&phase!=Handling.Phase.LOADER))return;
        var ammo=TimelessAPI.getClientAmmoIndex(new ResourceLocation(profile.cylinder()?PistolProfiles.get(profile).ammo():"tacz:12g")).orElse(null);if(ammo==null)return;
        // The projectile entity model is not a physical cartridge (often a generic bullet).
        var model=ammo.getShellModel();var texture=ammo.getShellTextureLocation();
        var worldGun=new Matrix4f().translation(gun.hand()).rotate(gun.rotation()).scale(gun.worldScale());
        var offhand=new Matrix4f().translation(pose.getOffhand().getPosition()).mul(pose.getOffhand().getRotation()).scale(gun.worldScale());
        var correction=worldGun.invert().mul(offhand);
        matrices.pushPose();
        try{
            matrices.mulPoseMatrix(correction);matrices.last().normal().mul(correction.get3x3(new org.joml.Matrix3f()).invert().transpose());
            matrices.scale(size,size,size);
            if(phase==Handling.Phase.LOADER){
                var descriptor=DescriptorProfiles.details(profile);var id=new ResourceLocation(descriptor.id());
                var display=TimelessAPI.getClientGunIndex(id).map(i->i.getDefaultDisplay()).orElse(null);
                if(display!=null&&display.getGunModel()!=null)PhysicalModel.loosePart(matrices,display.getGunModel(),descriptor.visual().loader(),profile.scale(),RenderType.entityCutout(display.getModelTexture()),light);
                // Cosmetic round ring; insertion authoritatively consumes only available rounds.
                for(int i=0;i<descriptor.visual().rounds().size();i++){
                    matrices.pushPose();try{double a=2*Math.PI*i/descriptor.visual().rounds().size();matrices.translate(Math.cos(a)*.022,Math.sin(a)*.022,-.018);
                        if(model!=null&&texture!=null){matrices.translate(0,-1.5,0);model.render(matrices,ItemDisplayContext.GROUND,RenderType.entityCutout(texture),light,OverlayTexture.NO_OVERLAY);}
                    }finally{matrices.popPose();}
                }
            }else if(model!=null&&texture!=null){matrices.translate(0,-1.5,0);model.render(matrices,ItemDisplayContext.GROUND,RenderType.entityCutout(texture),light,OverlayTexture.NO_OVERLAY);}
            else HandlingEffects.LIVE_ROUND.render(matrices,Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutout(new ResourceLocation("minecraft","textures/block/red_concrete.png"))),light,OverlayTexture.NO_OVERLAY);
        } finally{matrices.popPose();}
    }
}
