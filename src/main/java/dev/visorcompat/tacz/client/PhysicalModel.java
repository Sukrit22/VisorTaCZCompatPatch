package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.Handling;
import dev.visorcompat.tacz.physical.Handling.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.*;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
import java.util.*;

public final class PhysicalModel implements AutoCloseable {
    private final BedrockPart magazine,slide,bolt,loose;
    private final boolean looseVisible;
    private final boolean visible;
    private final float slideZ, magazineY,boltZ;
    public PhysicalModel(BedrockGunModel model,WeaponProfile profile) {
        this(model,profile,PhysicalClient.active(),PhysicalClient.phase(),PhysicalClient.pull(),Minecraft.getInstance().player.getMainHandItem());
    }
    public PhysicalModel(BedrockGunModel model,WeaponProfile profile,boolean active,Phase phase,float travel,net.minecraft.world.item.ItemStack stack) {
        loose=profile.pump()?find(model.getRootNode(),"bullet_and_lefthand"):null;looseVisible=loose!=null&&loose.visible;
        bolt=profile.supportDistance()>0?find(model.getRootNode(),"m4a1_bolt"):null;boltZ=bolt==null?0:bolt.offsetZ;
        magazine=find(model.getRootNode(),"magazine");
        slide=find(model.getRootNode(),profile.pump()?"slide2":profile.supportDistance()==0?"slide":"m4a1_pull");
        visible=magazine!=null && magazine.visible;magazineY=magazine==null?0:magazine.offsetY;slideZ=slide==null?0:slide.offsetZ;
        if(active) {
            if(loose!=null)loose.visible=false;
            if(magazine!=null && Handling.magazineOut(phase)) magazine.visible=false;
            if(magazine!=null && phase==Phase.REMOVING) magazine.offsetY+=travel/profile.scale();
            if(slide!=null) {
                float pull=Handling.racking(phase) || phase==Phase.PUMP_HOLD || phase==Phase.PUMP_OPEN?travel:0;
                if(profile.pump() && phase!=Phase.PUMP_HOLD && dev.visorcompat.tacz.server.ServerPump.open(stack))pull=dev.visorcompat.tacz.physical.PumpCycle.TRAVEL;
                var jam=dev.visorcompat.tacz.server.ServerJams.read(stack);
                if(profile.supportDistance()==0 && phase==Phase.NEED_RACK && jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.NONE)pull=.055f;
                if(jam.remaining()>0 && (jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE || jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.DOUBLE_FEED))
                    {if(bolt!=null)bolt.offsetZ+=java.lang.Math.max(0,.025f-pull)/profile.scale();
                    else pull=java.lang.Math.max(pull,.025f);}
                slide.offsetZ+=pull/profile.scale();
            }
        }
    }
    @Override public void close() { if(magazine!=null){magazine.visible=visible;magazine.offsetY=magazineY;}if(slide!=null)slide.offsetZ=slideZ;if(bolt!=null)bolt.offsetZ=boltZ;if(loose!=null)loose.visible=looseVisible; }
    private static BedrockPart find(BedrockPart node,String name) {
        if(node==null)return null;if(name.equals(node.name))return node;
        for(var child:node.children){var found=find(child,name);if(found!=null)return found;}return null;
    }
    private static boolean path(BedrockPart node,String name,List<BedrockPart> parts) {
        if(node==null)return false;parts.add(node);if(name.equals(node.name))return true;
        for(var child:node.children)if(path(child,name,parts))return true;
        parts.remove(parts.size()-1);return false;
    }
    public static void guides(PoseStack matrices,WeaponProfile profile,GunPose gun,VRPlayerPose pose) {
        if(!CompatSettings.debugCubes())return;
        if(!PhysicalClient.active() && !(Minecraft.getInstance().screen instanceof CalibrationScreen))return;
        var c=CalibrationStore.render(Profiles.key(Minecraft.getInstance().player.getMainHandItem()));
        box(matrices,Handling.magazine(profile,c),.023f,0,1,0);
        box(matrices,Handling.rack(profile,c),.023f,1,.6f,0);
        box(matrices,dev.visorcompat.tacz.physical.JamProfile.of(profile,c).port(),.015f,1,.2f,.6f);
        if(!profile.pump())box(matrices,Handling.selector(profile,c),.014f,1,1,0);
        // Pump grip provides support; the separate support point only calibrates aim.
        if(profile.supportDistance()>0 && (!profile.pump() || Minecraft.getInstance().screen instanceof CalibrationScreen))box(matrices,Handling.support(profile,c),.022f,0,.6f,1);
        Vector3f forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        Vector3f pouch=Handling.pouch(pose.getHmd().getPosition(),forward,gun.worldScale(),c);
        if(profile.pump() || PhysicalClient.phase()==Phase.NO_MAG || Minecraft.getInstance().screen instanceof CalibrationScreen)
            box(matrices,Handling.local(gun,pouch),.065f,0,1,1);
        if(Minecraft.getInstance().screen instanceof CalibrationScreen) {
            var sight=OpticGeometry.sight(Minecraft.getInstance().player.getMainHandItem(),profile);
            if(sight!=null)box(matrices,sight.add(c.interactions().sight().vector()),.01f,1,0,1);
        }
    }
    private static void box(PoseStack matrices,Vector3f pos,float radius,float r,float g,float b) {
        var buffer=Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(matrices,buffer,pos.x-radius,pos.y-radius,pos.z-radius,
            pos.x+radius,pos.y+radius,pos.z+radius,r,g,b,1);
    }
    public static void detached(PoseStack matrices,BedrockGunModel model,WeaponProfile profile,
                                GunPose gun,VRPlayerPose pose,RenderType type,int light) {
        detached(matrices,model,profile,gun,pose,type,light,PhysicalClient.active(),PhysicalClient.phase());
    }
    public static void detached(PoseStack matrices,BedrockGunModel model,WeaponProfile profile,
                                GunPose gun,VRPlayerPose pose,RenderType type,int light,boolean active,Phase phase) {
        if(!active || (phase!=Phase.OLD_MAG && phase!=Phase.NEW_MAG)) return;
        List<BedrockPart> parts=new ArrayList<>();
        if(!path(model.getRootNode(),"magazine",parts)) return;
        Matrix4f worldGun=new Matrix4f().translation(gun.hand()).rotate(gun.rotation()).scale(gun.worldScale());
        Matrix4f offhand=new Matrix4f().translation(pose.getOffhand().getPosition())
            .mul(pose.getOffhand().getRotation()).scale(gun.worldScale());
        Matrix4f correction=worldGun.invert().mul(offhand);
        matrices.pushPose();
        try {
            matrices.mulPoseMatrix(correction);
            matrices.last().normal().mul(correction.get3x3(new Matrix3f()).invert().transpose());
            Vector3f anchor=profile.grip().add(Handling.magazine(profile));
            matrices.translate(-anchor.x,-anchor.y,-anchor.z);
            matrices.scale(profile.scale(),profile.scale(),profile.scale());
            matrices.translate(0,1.5,0);matrices.mulPose(Axis.ZP.rotationDegrees(180));
            for(int i=0;i<parts.size()-1;i++)parts.get(i).translateAndRotateAndScale(matrices);
            var mag=parts.get(parts.size()-1);
            boolean oldVisible=mag.visible;
            try {
                mag.visible=true;
                mag.render(matrices,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(type),light,OverlayTexture.NO_OVERLAY);
            } finally {mag.visible=oldVisible;}
        } finally {matrices.popPose();}
    }
}
