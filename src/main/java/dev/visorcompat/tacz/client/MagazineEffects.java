package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.MagazineMotion;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.compat.oculus.OculusCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayDeque;
/** Small bounded cosmetic simulation; no inventory entities, old velocity history or per-frame packets. */
@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT)
public final class MagazineEffects {
    private static final class Prop {final CompatNetwork.DroppedMagazine data;final long start=System.nanoTime();long last=start,frame=-1;MagazineMotion motion;Prop(CompatNetwork.DroppedMagazine d){data=d;}}
    private static final ArrayDeque<Prop> PROPS=new ArrayDeque<>();private static long frame;private static Prop preview;private static PhysicsScreen previewScreen;private static int previewToken;private static Object world;
    private static void checkWorld(){var current=Minecraft.getInstance().level;if(world!=current){PROPS.clear();preview=null;previewScreen=null;world=current;}}
    @SubscribeEvent public static void frame(net.minecraftforge.event.TickEvent.RenderTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.START)frame++;}
    public static void receive(CompatNetwork.DroppedMagazine m){checkWorld();if(Minecraft.getInstance().level==null||Profiles.get(m.stack())==null||!m.physics().valid()||m.descriptors()&&!m.profileDigest().equals(DescriptorProfiles.digest()))return;if(PROPS.size()>=16)PROPS.removeFirst();PROPS.addLast(new Prop(m));}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){PROPS.clear();preview=null;previewScreen=null;world=null;}
    private static GunPose gun(AbstractClientPlayer p,float partial){
        var mc=Minecraft.getInstance();var profile=Profiles.get(p.getMainHandItem());if(profile==null||!(VisorAPI.getVRPlayer(p) instanceof VRClientPlayer vr))return null;var pose=vr.getPoseData(PlayerPoseType.RENDER);if(pose==null)return null;
        if(p==mc.player){if(!ClientControls.vrActive())return null;return ClientRecoil.apply(p,AdvancedClient.renderPose(GunPose.resolve(pose,profile,p.getOffhandItem().isEmpty()&&PhysicalClient.supporting(),CalibrationStore.render(Profiles.key(p.getMainHandItem())),PhysicalClient.anchor())),partial);}
        var state=RemoteGuns.state(p);if(state==null)return null;var base=GunPose.resolve(pose,profile,p.getOffhandItem().isEmpty()&&state.phase()==dev.visorcompat.tacz.physical.Handling.Phase.SUPPORT,state.calibration(),state.anchor());return ClientRecoil.apply(p,AdvancedRemote.pose(p.getUUID(),Profiles.key(p.getMainHandItem()),base,profile,state.calibration()),partial);
    }
    private static Vector3f origin(CompatNetwork.DroppedMagazine m,GunPose g){return new Quaternionf(g.rotation()).transform(m.physics().origin(Profiles.get(m.stack())).mul(m.scale()*m.modelScale())).add(g.hand());}
    private static GunPose fallback(CompatNetwork.DroppedMagazine m){return new GunPose(m.position(),m.rotation(),m.position(),new Vector3f(0,0,-1),m.scale());}
    private static void advance(Prop p,float partial,boolean simulation){
        if(p.frame==frame)return;p.frame=frame;long now=System.nanoTime();float dt=(now-p.last)/1e9f;p.last=now;var m=p.data;var mc=Minecraft.getInstance();var owner=mc.level.getPlayerByUUID(m.owner());
        boolean bound=owner instanceof AbstractClientPlayer&&Profiles.key(owner.getMainHandItem()).equals(Profiles.key(m.stack()))&&(owner!=mc.player||owner.getInventory().selected==m.slot());
        var current=bound?gun((AbstractClientPlayer)owner,partial):null;
        if(p.motion==null){var g=current==null?fallback(m):current;p.motion=new MagazineMotion(origin(m,g),g.rotation(),m.physics().exitDirection(),m.physics().travel()*m.modelScale()*m.scale(),m.physics().friction(),m.scale());return;}
        if(current==null)p.motion.detach();
        else if(!simulation&&owner==mc.player&&!dev.visorcompat.tacz.physical.Handling.magazineOut(PhysicalClient.phase()))p.motion.detach();
        var o=current==null?p.motion.position():origin(m,current);var q=current==null?p.motion.rotation():current.rotation();p.motion.advance(o,q,dt);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES||OculusCompat.isRenderShadow())return;
        checkWorld();var mc=Minecraft.getInstance();if(mc.level==null)return;long now=System.nanoTime();PROPS.removeIf(p->now-p.start>10_000_000_000L);
        for(var p:PROPS){advance(p,e.getPartialTick(),false);draw(e,p,p.motion.position(),p.motion.rotation(),false);}
        if(mc.screen instanceof PhysicsScreen s&&mc.player!=null&&s.key().equals(Profiles.key(mc.player.getMainHandItem()))){
            var g=gun(mc.player,e.getPartialTick());if(g!=null){var profile=Profiles.get(mc.player.getMainHandItem());var c=PhysicsStore.get(s.key(),profile);float size=CalibrationStore.render(s.key()).gunScale();
                if(preview==null||previewScreen!=s||previewToken!=s.resetToken()){preview=new Prop(new CompatNetwork.DroppedMagazine(mc.player.getUUID(),mc.player.getInventory().selected,mc.player.getMainHandItem().copy(),g.worldScale(),size,g.hand(),g.rotation(),c,true,false,""));previewScreen=s;previewToken=s.resetToken();}
                if(s.previewMode()==PhysicsScreen.Preview.SIMULATE){advance(preview,e.getPartialTick(),true);draw(e,preview,preview.motion.position(),preview.motion.rotation(),true);}
                else if(s.previewMode()==PhysicsScreen.Preview.SEATED||s.previewMode()==PhysicsScreen.Preview.EXIT){var o=origin(preview.data,g);if(s.previewMode()==PhysicsScreen.Preview.EXIT)o.add(new Quaternionf(g.rotation()).transform(c.exitDirection()).mul(c.travel()*size*g.worldScale()));draw(e,preview,o,g.rotation(),true);}
            }
        }else{preview=null;previewScreen=null;}
        if(!PROPS.isEmpty()||preview!=null)mc.renderBuffers().bufferSource().endBatch();
    }
    private static void draw(RenderLevelStageEvent e,Prop p,Vector3f position,Quaternionf rotation,boolean guide){
        var m=p.data;var profile=Profiles.get(m.stack());var display=TimelessAPI.getGunDisplay(m.stack()).orElse(null);if(profile==null||display==null||display.getGunModel()==null)return;
        var matrices=e.getPoseStack();var camera=e.getCamera().getPosition();matrices.pushPose();
        try{matrices.translate(position.x-camera.x,position.y-camera.y,position.z-camera.z);matrices.mulPose(rotation);matrices.scale(m.scale(),m.scale(),m.scale());
            // Origin calibration moves the rail reference, not the native seated model.
            var off=m.physics();matrices.pushPose();matrices.translate(-off.offsetX()*m.modelScale(),-off.offsetY()*m.modelScale(),-off.offsetZ()*m.modelScale());
            PhysicalModel.looseMagazine(matrices,display.getGunModel(),profile,m.modelScale(),display.enablesTransparency()?RenderType.entityTranslucent(display.getModelTexture()):RenderType.entityCutout(display.getModelTexture()),0xF000F0,m.loaded());matrices.popPose();
            if(guide){float k=m.modelScale();var c=m.physics();var buf=Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());LevelRenderer.renderLineBox(matrices,buf,-c.width()*k/2,-c.height()*k/2,-c.depth()*k/2,c.width()*k/2,c.height()*k/2,c.depth()*k/2,1,1,1,1);var a=c.exitDirection().mul(c.travel()*k);LevelRenderer.renderLineBox(matrices,buf,Math.min(0,a.x)-.002,Math.min(0,a.y)-.002,Math.min(0,a.z)-.002,Math.max(0,a.x)+.002,Math.max(0,a.y)+.002,Math.max(0,a.z)+.002,0,1,1,1);}
        }finally{matrices.popPose();}
    }
}
