package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import dev.visorcompat.tacz.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;

/** Stack-specific native models, without singleton handling or camera animation state. */
public final class DualGunRenderer extends VRHandEffect {
    public DualGunRenderer(VisorAddon owner){super(owner);}
    @Override public String getId(){return "tacz_dual_gun";}
    @Override public boolean isGlobal(){return true;}
    @Override public boolean isVisible(VRDecorator d,HandType hand,boolean gui){return !gui&&(DualClient.assigned(hand)||(FourHolsters.active()&&hand==HandType.MAIN))&&(Minecraft.getInstance().screen==null||Minecraft.getInstance().screen instanceof HolsterScreen||Minecraft.getInstance().screen instanceof DualHolsterScreen||Minecraft.getInstance().screen instanceof FourHolsterScreen)&&!com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow();}
    @Override public void render(HandType hand,VRRenderPass pass,PoseStack matrices,boolean gui,float partial){
        if(FourHolsters.active()){
            if(DualClient.held(hand))draw(hand,DualClient.stack(hand),DualClient.pose(hand,PlayerPoseType.RENDER),matrices,partial);
            if(hand==HandType.MAIN)for(int i=0;i<4;i++)if(!FourHolsters.heldSlot(FourHolsters.slot(i)))draw(hand,FourHolsters.stack(i),FourHolsters.pose(i,PlayerPoseType.RENDER),matrices,partial);
        }else draw(hand,DualClient.stack(hand),DualClient.pose(hand,PlayerPoseType.RENDER),matrices,partial);
    }
    private void draw(HandType hand,net.minecraft.world.item.ItemStack stack,GunPose gun,PoseStack matrices,float partial){
        var mc=Minecraft.getInstance();var profile=Profiles.get(stack);if(profile==null||gun==null)return;
        var display=TimelessAPI.getGunDisplay(stack).orElse(null);if(display==null||display.getGunModel()==null)return;
        var model=display.getGunModel();var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        var body=pose.getBody().getHand(hand).getPose();
        var correction=new Matrix4f().translation(body.getPosition()).mul(body.getRotation()).scale(gun.worldScale()).invert().mul(new Matrix4f().translation(gun.hand()).rotate(gun.rotation()).scale(gun.worldScale()));
        boolean hands=model.getRenderHand();boolean flash=com.tacz.guns.client.model.functional.MuzzleFlashRender.isSelf;
        boolean shell=com.tacz.guns.client.model.functional.ShellRender.isSelf;
        matrices.pushPose();
        try{
            matrices.mulPoseMatrix(correction);matrices.last().normal().mul(correction.get3x3(new Matrix3f()).invert().transpose());
            var c=CalibrationStore.render(Profiles.key(stack));float scale=c.gunScale();matrices.scale(scale,scale,scale);
            var grip=profile.grip();matrices.translate(-grip.x,-grip.y,-grip.z);matrices.scale(profile.scale(),profile.scale(),profile.scale());
            matrices.translate(0,1.5,0);matrices.mulPose(Axis.ZP.rotationDegrees(180));
            model.cleanAnimationTransform();model.setRenderHand(false);
            com.tacz.guns.client.model.functional.MuzzleFlashRender.isSelf=false;
            com.tacz.guns.client.model.functional.ShellRender.isSelf=false;
            mc.gameRenderer.lightTexture().turnOnLightLayer();
            var type=display.enablesTransparency()?RenderType.entityTranslucent(display.getModelTexture()):RenderType.entityCutout(display.getModelTexture());
            model.render(matrices,stack,hand==HandType.MAIN?ItemDisplayContext.THIRD_PERSON_RIGHT_HAND:ItemDisplayContext.THIRD_PERSON_LEFT_HAND,type,mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player,partial),OverlayTexture.NO_OVERLAY);
            mc.renderBuffers().bufferSource().endBatch();
        }finally{model.setRenderHand(hands);model.cleanAnimationTransform();com.tacz.guns.client.model.functional.MuzzleFlashRender.isSelf=flash;com.tacz.guns.client.model.functional.ShellRender.isSelf=shell;mc.gameRenderer.lightTexture().turnOffLightLayer();matrices.popPose();}
    }
}
