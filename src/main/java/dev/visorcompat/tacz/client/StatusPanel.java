package dev.visorcompat.tacz.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.entity.IGunOperator;
import dev.visorcompat.tacz.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.joml.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;

/** Small world-anchored overlay panel. No framebuffer, stencil or camera movement. */
public final class StatusPanel extends VRHandEffect {
    public StatusPanel(VisorAddon owner){super(owner);}
    @Override public String getId(){return "tacz_status";}
    @Override public boolean isGlobal(){return true;}
    @Override public boolean isVisible(VRDecorator d,HandType hand,boolean gui){
        return !gui && ClientControls.vrActive() && (DualClient.active()?DualClient.assigned(hand):ClientControls.supported())
            && Minecraft.getInstance().screen==null && (CompatSettings.display()==CompatSettings.Display.GUN || CompatSettings.display()==CompatSettings.Display.WRIST)
            && (DualClient.active()||hand==HandType.MAIN) // Dual panels belong to their own weapon/hand.
            && !com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow();
    }
    @Override public void render(HandType hand,VRRenderPass pass,PoseStack matrices,boolean gui,float partial){
        if(!gui && pass.isEye() && !LateGunRenderer.drawing && !com.tacz.guns.compat.oculus.OculusCompat.isUsingRenderPack()){LateGunRenderer.defer(this,hand,pass,matrices,partial);return;}
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        boolean dual=DualClient.active();
        var stack=dual?DualClient.stack(hand):mc.player.getMainHandItem();var item=IGun.getIGunOrNull(stack);if(item==null)return;
        var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        var gun=dual?DualClient.pose(hand,PlayerPoseType.RENDER):GunPose.resolve(pose,Profiles.get(stack),mc.player.getOffhandItem().isEmpty()&&PhysicalClient.supporting(),CalibrationStore.get(Profiles.key(stack)),PhysicalClient.anchor());
        if(!dual)gun=AdvancedClient.renderPose(gun);
        if(gun==null)return;
        Vector3f anchor;
        if(CompatSettings.display()==CompatSettings.Display.GUN)anchor=gun.rotation().transform(new Vector3f(item.getGunId(stack).toString().equals("tacz:m700")?.18f:.10f,.13f,.03f).mul(gun.worldScale())).add(gun.hand());
        else {var wrist=dual?pose.getHand(hand):pose.getOffhand();anchor=wrist.getRotation().transformDirection(new Vector3f(0,.10f,.10f).mul(gun.worldScale())).add(wrist.getPosition());}
        var body=pose.getBody().getHand(hand).getPose();
        var old=new Matrix4f().translation(body.getPosition()).mul(body.getRotation()).scale(gun.worldScale());
        var panel=new Matrix4f().translation(anchor).mul(pose.getHmd().getRotation()).scale(gun.worldScale());
        matrices.pushPose();
        try {
            matrices.mulPoseMatrix(old.invert().mul(panel));matrices.scale(.0016f,-.0016f,.0016f);
            String[] lines=dual?DualStatus.lines(hand):StatusText.lines();
            var buffer=mc.renderBuffers().bufferSource();
            int width=0;for(String line:lines)width=java.lang.Math.max(width,mc.font.width(line));
            // One opaque plate and one full-bright text pass. No per-line transparent depth overlap.
            var type=net.minecraft.client.renderer.RenderType.textBackgroundSeeThrough();
            var plate=buffer.getBuffer(type);var matrix=matrices.last().pose();
            float left=-width/2f-5,right=width/2f+5,top=-4,bottom=lines.length*11;
            plate.vertex(matrix,left,bottom,.01f).color(12,16,20,255).uv2(0xF000F0).endVertex();
            plate.vertex(matrix,right,bottom,.01f).color(12,16,20,255).uv2(0xF000F0).endVertex();
            plate.vertex(matrix,right,top,.01f).color(12,16,20,255).uv2(0xF000F0).endVertex();
            plate.vertex(matrix,left,top,.01f).color(12,16,20,255).uv2(0xF000F0).endVertex();
            buffer.endBatch(type);
            for(int i=0;i<lines.length;i++)if(!lines[i].isEmpty())mc.font.drawInBatch(lines[i],-mc.font.width(lines[i])/2f,i*11,
                i==1&&AutoAds.aiming()?0xFF66FF88:0xFFFFFFFF,false,matrices.last().pose(),buffer,Font.DisplayMode.SEE_THROUGH,0,0xF000F0);
            buffer.endBatch();
        } finally {matrices.popPose();}
    }
}
