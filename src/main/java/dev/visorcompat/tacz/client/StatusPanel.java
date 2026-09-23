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
        return !gui && ClientControls.vrActive() && ClientControls.supported()
            && Minecraft.getInstance().screen==null && (CompatSettings.display()==CompatSettings.Display.GUN || CompatSettings.display()==CompatSettings.Display.WRIST)
            && hand==HandType.MAIN // Keep wrist text alive when Visor hides the offhand world effect.
            && !com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow();
    }
    @Override public void render(HandType hand,VRRenderPass pass,PoseStack matrices,boolean gui,float partial){
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        var stack=mc.player.getMainHandItem();var item=IGun.getIGunOrNull(stack);if(item==null)return;
        var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        var gun=GunPose.resolve(pose,Profiles.get(stack),mc.player.getOffhandItem().isEmpty()&&PhysicalClient.supporting(),CalibrationStore.get(Profiles.key(stack)),PhysicalClient.anchor());
        gun=AdvancedClient.renderPose(gun);
        if(gun==null)return;
        Vector3f anchor;
        if(CompatSettings.display()==CompatSettings.Display.GUN)anchor=gun.rotation().transform(new Vector3f(.10f,.13f,.03f).mul(gun.worldScale())).add(gun.hand());
        else anchor=pose.getOffhand().getRotation().transformDirection(new Vector3f(0,.10f,.10f).mul(gun.worldScale())).add(pose.getOffhand().getPosition());
        var body=pose.getBody().getHand(hand).getPose();
        var old=new Matrix4f().translation(body.getPosition()).mul(body.getRotation()).scale(gun.worldScale());
        var panel=new Matrix4f().translation(anchor).mul(pose.getCameraPose(pass).getRotation()).scale(gun.worldScale());
        matrices.pushPose();
        try {
            matrices.mulPoseMatrix(old.invert().mul(panel));matrices.scale(.0016f,-.0016f,.0016f);
            String[] lines=StatusText.lines();
            var buffer=mc.renderBuffers().bufferSource();
            for(int i=0;i<lines.length;i++)if(!lines[i].isEmpty())mc.font.drawInBatch(lines[i],-mc.font.width(lines[i])/2f,i*11,
                i==1&&AutoAds.aiming()?0xFF66FF88:0xFFFFFFFF,false,matrices.last().pose(),buffer,Font.DisplayMode.SEE_THROUGH,0xD0101010,0xF000F0);
            buffer.endBatch();
        } finally {matrices.popPose();}
    }
}
