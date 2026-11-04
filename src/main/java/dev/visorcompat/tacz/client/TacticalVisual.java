package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.BedrockGunModel;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import org.joml.*;
import org.vmstudio.visor.api.common.player.VRPlayerPose;

/** Both magazines use their own round count, pose and insertion candidate. */
public final class TacticalVisual {
    private TacticalVisual(){}
    public static boolean render(PoseStack matrices,BedrockGunModel model,WeaponProfile profile,GunPose gun,VRPlayerPose pose,RenderType type,int light){
        if(!TacticalClient.active())return false;
        var player=Minecraft.getInstance().player;var stack=player.getMainHandItem();String key=Profiles.key(stack);
        var c=TacticalStore.get(key.split("\\|",2)[0],key);var calibration=CalibrationStore.render(key);
        var hand=AdvancedClient.freeMain()?pose.getMainHand():pose.getOffhand();var rotation=hand.getRotation().getNormalizedRotation(new Quaternionf());
        var draft=TacticalScreen.preview(key.split("\\|",2)[0]);
        if(CompatSettings.debugCubes()||draft!=null){
            var guide=draft==null?c:draft;var entry=TacticalPose.entry(profile,calibration,guide);
            matrices.pushPose();try{matrices.translate(entry.x,entry.y,entry.z);matrices.mulPose(TacticalPose.rotation(guide.magwellEntry()));
                var lines=Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());float radius=guide.entryRadius();
                net.minecraft.client.renderer.LevelRenderer.renderLineBox(matrices,lines,-radius,-.002,-radius,radius,.002,radius,.3f,.85f,1,1);
                net.minecraft.client.renderer.LevelRenderer.renderLineBox(matrices,lines,-.002,0,-.002,.002,guide.seatedDepth(),.002,1,1,.1f,1);
                net.minecraft.client.renderer.LevelRenderer.renderLineBox(matrices,lines,-.015,guide.seatedDepth()-.002,-.015,.015,guide.seatedDepth()+.002,.015,.2f,1,.4f,1);
            }finally{matrices.popPose();}
        }
        for(boolean pinch:new boolean[]{false,true}){
            if(TacticalState.count(stack,pinch)<0)continue;
            var at=TacticalPose.magazine(stack,pinch,c,gun,new Vector3f(hand.getPosition()),rotation);
            boolean engaged=TacticalState.guide(stack)==(pinch?2:1);
            var guide=MagazineGuide.solve(at.position(),at.rotation(),TacticalPose.entry(profile,calibration,c),TacticalPose.rotation(c.magwellEntry()),c,engaged);
            var position=engaged&&guide.engaged()?guide.position():at.position();var q=engaged&&guide.engaged()?guide.rotation():at.rotation();
            matrices.pushPose();try{matrices.translate(position.x,position.y,position.z);matrices.mulPose(q);
                PhysicalModel.looseMagazine(matrices,model,profile,calibration.gunScale(),type,light,TacticalState.count(stack,pinch)>0);
            }finally{matrices.popPose();}
        }
        return true;
    }
}
