package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.TossMotion;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.compat.oculus.OculusCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import java.util.ArrayDeque;

/** Bounded cosmetic props: no entities, inventory, physics engine or per-frame networking. */
@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT)
public final class MagazineEffects {
    private record Prop(CompatNetwork.DroppedMagazine data,long start){}
    private static final ArrayDeque<Prop> PROPS=new ArrayDeque<>();
    public static void receive(CompatNetwork.DroppedMagazine m){
        if(Minecraft.getInstance().level==null)return;
        if(m.descriptors()&&!m.profileDigest().equals(DescriptorProfiles.digest()))return;
        if(PROPS.size()>=16)PROPS.removeFirst();PROPS.addLast(new Prop(m,System.nanoTime()));
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){PROPS.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES||OculusCompat.isRenderShadow())return;
        long now=System.nanoTime();PROPS.removeIf(p->now-p.start()>1_500_000_000L);
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        var camera=e.getCamera().getPosition();var matrices=e.getPoseStack();
        for(var prop:PROPS){
            var m=prop.data();var profile=Profiles.get(m.stack());var display=TimelessAPI.getGunDisplay(m.stack()).orElse(null);
            if(profile==null||display==null||display.getGunModel()==null)continue;
            float dt=(now-prop.start())/1_000_000_000f;
            var position=TossMotion.advance(m.position(),m.velocity(),9.81f*m.scale(),dt);
            matrices.pushPose();
            try{
                matrices.translate(position.x-camera.x,position.y-camera.y,position.z-camera.z);
                // Release spin is world-space, so pre-multiply the original orientation.
                matrices.mulPose(new Quaternionf().integrate(dt,m.spin().x,m.spin().y,m.spin().z).mul(m.rotation()));
                matrices.scale(m.scale(),m.scale(),m.scale());
                PhysicalModel.looseMagazine(matrices,display.getGunModel(),profile,m.modelScale(),
                    display.enablesTransparency()?RenderType.entityTranslucent(display.getModelTexture()):RenderType.entityCutout(display.getModelTexture()),0xF000F0,m.loaded());
            }finally{matrices.popPose();}
        }
        if(!PROPS.isEmpty())mc.renderBuffers().bufferSource().endBatch();
    }
}
