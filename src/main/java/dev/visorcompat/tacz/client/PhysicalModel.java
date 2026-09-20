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
    private final float slideZ, magazineY,boltZ,slideRot;
    public PhysicalModel(BedrockGunModel model,WeaponProfile profile) {
        this(model,profile,PhysicalClient.active(),PhysicalClient.phase(),PhysicalClient.pull(),Minecraft.getInstance().player.getMainHandItem(),CalibrationStore.render(Profiles.key(Minecraft.getInstance().player.getMainHandItem())));
    }
    public PhysicalModel(BedrockGunModel model,WeaponProfile profile,boolean active,Phase phase,float travel,net.minecraft.world.item.ItemStack stack,Calibration calibration) {
        loose=profile.pump()?find(model.getRootNode(),"bullet_and_lefthand"):null;looseVisible=loose!=null&&loose.visible;
        bolt=profile.boltNode()!=null?find(model.getRootNode(),profile.boltNode()):null;boltZ=bolt==null?0:bolt.offsetZ;
        magazine=find(model.getRootNode(),"magazine");
        slide=find(model.getRootNode(),profile.rackNode());slideRot=slide==null?0:slide.zRot;
        visible=magazine!=null && magazine.visible;magazineY=magazine==null?0:magazine.offsetY;slideZ=slide==null?0:slide.offsetZ;
        if(active) {
            if(loose!=null)loose.visible=false;
            if(magazine!=null && Handling.magazineOut(phase)) magazine.visible=false;
            if(magazine!=null && phase==Phase.REMOVING) magazine.offsetY+=travel/(profile.scale()*calibration.gunScale());
            if(slide!=null) {
                float pull=Handling.racking(phase) || phase==Phase.PUMP_HOLD || phase==Phase.PUMP_OPEN?travel:0;
                if(profile.pump() && phase!=Phase.PUMP_HOLD && dev.visorcompat.tacz.server.ServerPump.open(stack))pull=dev.visorcompat.tacz.physical.PumpCycle.TRAVEL;
                if(profile.bolt() && !Handling.racking(phase) && dev.visorcompat.tacz.physical.BoltState.open(stack))pull=dev.visorcompat.tacz.physical.PumpCycle.TRAVEL;
                var jam=dev.visorcompat.tacz.server.ServerJams.read(stack);
                if(profile.bolt()){if(dev.visorcompat.tacz.physical.ActionState.lifted(stack)||dev.visorcompat.tacz.physical.BoltState.open(stack))slide.zRot+=1.05f;else pull=0;}
                if(dev.visorcompat.tacz.physical.ActionState.locked(stack)){
                    if(profile.smg()){pull=java.lang.Math.max(pull,.055f);slide.zRot+=.6f;}
                    else if(bolt!=null)bolt.offsetZ+=.055f/(profile.scale()*calibration.gunScale());
                    else pull=java.lang.Math.max(pull,.055f);
                }
                if(jam.remaining()>0 && (jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE || jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.DOUBLE_FEED))
                    {if(bolt!=null)bolt.offsetZ+=java.lang.Math.max(0,.025f-pull)/(profile.scale()*calibration.gunScale());
                    else pull=java.lang.Math.max(pull,.025f);}
                if(profile.smg()&&bolt!=null)bolt.offsetZ+=pull/(profile.scale()*calibration.gunScale());
                slide.offsetZ+=pull/(profile.scale()*calibration.gunScale());
            }
        }
    }
    @Override public void close() { if(magazine!=null){magazine.visible=visible;magazine.offsetY=magazineY;}if(slide!=null){slide.offsetZ=slideZ;slide.zRot=slideRot;}if(bolt!=null)bolt.offsetZ=boltZ;if(loose!=null)loose.visible=looseVisible; }
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
        if(!CompatSettings.debugCubes() && !(Minecraft.getInstance().screen instanceof CalibrationScreen))return;
        boolean calibrating=Minecraft.getInstance().screen instanceof CalibrationScreen;
        if(!PhysicalClient.active()&&!calibrating)return;
        var stack=Minecraft.getInstance().player.getMainHandItem();String key=Profiles.key(stack);
        var c=CalibrationStore.render(key);var local=Handling.local(gun,PhysicalClient.anchor()!=null?pose.getMainHand().getPosition():pose.getOffhand().getPosition());
        guide(matrices,Handling.magazine(profile,c),c.zones().magazine(),key,"magazine",local);
        var rack=Handling.rack(profile,c,dev.visorcompat.tacz.physical.BoltState.open(stack));
        if(profile.pump()&&dev.visorcompat.tacz.server.ServerPump.open(stack))rack.add(0,0,dev.visorcompat.tacz.physical.PumpCycle.TRAVEL);
        if(profile.bolt()&&dev.visorcompat.tacz.physical.ActionState.lifted(stack))rack.add(0,.035f,0);
        guide(matrices,rack,c.zones().rack(),key,"rack",local);
        if(!profile.manualAction())guide(matrices,Handling.release(profile,c),c.zones().release(),key,"release",local);
        var port=dev.visorcompat.tacz.physical.JamProfile.of(profile,c).port();
        guide(matrices,port,profile.bolt()?new ZoneSizes.Box(.025f,.025f,.025f):c.zones().port(),key,"port",local);
        if(!profile.pump()&&profile.supportDistance()>0){
            if(profile.selector())guide(matrices,Handling.selector(profile,c),c.zones().selector(),key,"selector",local);
            guide(matrices,Handling.support(profile,c),c.zones().support(),key,"support",local);
        }
        Vector3f forward=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
        if(profile.pump()||PhysicalClient.phase()==Phase.NO_MAG||calibrating){
            var flat=new Vector3f(forward.x,0,forward.z);if(flat.lengthSquared()<.001f)flat.set(0,0,-1);else flat.normalize();
            var pouch=Handling.pouch(pose.getHmd().getPosition(),forward,gun.worldScale(),c);
            var worldGun=new Matrix4f().translation(gun.hand()).rotate(gun.rotation()).scale(gun.worldScale());
            var worldPouch=new Matrix4f().translation(pouch).rotateY((float)java.lang.Math.atan2(-flat.x,-flat.z)).scale(gun.worldScale());
            matrices.pushPose();
            try{var correction=worldGun.invert().mul(worldPouch);matrices.mulPoseMatrix(correction);matrices.last().normal().mul(correction.get3x3(new Matrix3f()).invert().transpose());
                guide(matrices,new Vector3f(),c.zones().pouch(),key,"pouch",Handling.pouchLocal(pose.getOffhand().getPosition(),pose.getHmd().getPosition(),forward,gun.worldScale(),c));
            }finally{matrices.popPose();}
        }
        if(calibrating){
            var sight=OpticGeometry.sight(stack,profile);
            if(sight!=null){
                sight.add(c.interactions().sight().vector()).mul(c.gunScale());
                var eye=Handling.local(gun,pose.getHmd().getPosition());
                if(pose instanceof org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient clientPose){
                    var left=Handling.local(gun,clientPose.getEyeLeft().getPosition());
                    var right=Handling.local(gun,clientPose.getEyeRight().getPosition());
                    var center=new Vector3f(sight).add(0,0,.015f+c.zones().sight().depth()/2);
                    eye=c.zones().sight().contains(left,center)?left:right;
                }
                guide(matrices,new Vector3f(sight).add(0,0,.015f+c.zones().sight().depth()/2),c.zones().sight(),key,"sight",eye);
                guide(matrices,sight,new ZoneSizes.Box(.01f,.01f,.01f),key,"sight",eye);
                // Thin forward marker makes the direction of the sight line explicit.
                guide(matrices,new Vector3f(sight).add(0,0,-.06f),new ZoneSizes.Box(.002f,.002f,.12f),key,"sight",eye);
            }
        }
    }
    private static void guide(PoseStack matrices,Vector3f pos,ZoneSizes.Box size,String key,String action,Vector3f hand){
        int color=CalibrationHints.get(key,action).color();boolean inside=size.contains(hand,pos);
        float r=((color>>16)&255)/255f,g=((color>>8)&255)/255f,b=(color&255)/255f;
        if(inside){r=(r+1)/2;g=(g+1)/2;b=(b+1)/2;}
        var buffer=Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(matrices,buffer,pos.x-size.width()/2,pos.y-size.height()/2,pos.z-size.depth()/2,
            pos.x+size.width()/2,pos.y+size.height()/2,pos.z+size.depth()/2,r,g,b,inside?1:.45f);
    }
    public static void detached(PoseStack matrices,BedrockGunModel model,WeaponProfile profile,
                                GunPose gun,VRPlayerPose pose,RenderType type,int light) {
        detached(matrices,model,profile,gun,pose,type,light,PhysicalClient.active(),PhysicalClient.phase(),CalibrationStore.render(Profiles.key(Minecraft.getInstance().player.getMainHandItem())).gunScale(),
            dev.visorcompat.tacz.physical.PouchAmmo.magazineLoaded(Minecraft.getInstance().player,PhysicalClient.phase()));
    }
    public static void detached(PoseStack matrices,BedrockGunModel model,WeaponProfile profile,
                                GunPose gun,VRPlayerPose pose,RenderType type,int light,boolean active,Phase phase,float modelScale,boolean loaded) {
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
            matrices.scale(modelScale,modelScale,modelScale);
            Vector3f anchor=profile.grip().add(Handling.magazine(profile));
            matrices.translate(-anchor.x,-anchor.y,-anchor.z);
            matrices.scale(profile.scale(),profile.scale(),profile.scale());
            matrices.translate(0,1.5,0);matrices.mulPose(Axis.ZP.rotationDegrees(180));
            for(int i=0;i<parts.size()-1;i++)parts.get(i).translateAndRotateAndScale(matrices);
            var mag=parts.get(parts.size()-1);
            boolean oldVisible=mag.visible;var bullet=find(mag,"bullet_in_mag");boolean oldBullet=bullet!=null&&bullet.visible;
            try {
                mag.visible=true;if(bullet!=null)bullet.visible=loaded;
                mag.render(matrices,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(type),light,OverlayTexture.NO_OVERLAY);
            } finally {mag.visible=oldVisible;if(bullet!=null)bullet.visible=oldBullet;}
        } finally {matrices.popPose();}
    }
}
