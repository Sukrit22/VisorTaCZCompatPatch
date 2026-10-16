package dev.visorcompat.tacz.client;

import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.DualNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Server snapshots for remote independent weapon models. */
@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT)
public final class DualRemote {
    private record Entry(DualNetwork.Visual visual,long time){}
    private static final Map<UUID,Entry> STATES=new HashMap<>();
    public static void receive(DualNetwork.Visual visual){
        if(visual.enabled()&&visual.mainCal().valid()&&visual.offCal().valid())STATES.put(visual.player(),new Entry(visual,System.nanoTime()));
        else STATES.remove(visual.player());
    }
    private static DualNetwork.Visual state(UUID id){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.player.getUUID().equals(id))return null;
        var entry=STATES.get(id);if(entry==null)return null;
        if(System.nanoTime()-entry.time()>2_500_000_000L){STATES.remove(id);return null;}
        var p=mc.level.getPlayerByUUID(id);
        if(p==null||!p.isAlive()||p.isSpectator()||p.isInvisibleTo(mc.player))return null;
        return entry.visual();
    }
    public static boolean active(UUID id){return state(id)!=null;}
    private static boolean drawable(ItemStack stack,GunPose pose){
        var display=TimelessAPI.getGunDisplay(stack).orElse(null);
        return Profiles.get(stack)!=null&&pose!=null&&PoseMath.finite(pose.hand())&&display!=null&&display.getGunModel()!=null;
    }
    public static boolean replaces(ItemStack stack,ItemDisplayContext context){
        var mc=Minecraft.getInstance();if(mc.level==null||com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow()||(context!=ItemDisplayContext.THIRD_PERSON_RIGHT_HAND&&context!=ItemDisplayContext.THIRD_PERSON_LEFT_HAND))return false;
        for(var p:mc.level.players()){
            var v=state(p.getUUID());if(v==null)continue;
            if(stack==p.getMainHandItem()&&drawable(v.main(),v.mainPose()))return true;
            if(stack==p.getOffhandItem()&&drawable(v.off(),v.offPose()))return true;
        }return false;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){STATES.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES||com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow())return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        var camera=event.getCamera().getPosition();var matrices=event.getPoseStack();
        for(var player:mc.level.players()){
            var v=state(player.getUUID());if(v==null)continue;
            for(int i=0;i<2;i++){
                var stack=i==0?v.main():v.off();var gun=i==0?v.mainPose():v.offPose();var c=i==0?v.mainCal():v.offCal();
                if(!drawable(stack,gun))continue;
                var profile=Profiles.get(stack);var display=TimelessAPI.getGunDisplay(stack).orElseThrow();var model=display.getGunModel();
                boolean hands=model.getRenderHand(),flash=com.tacz.guns.client.model.functional.MuzzleFlashRender.isSelf,shell=com.tacz.guns.client.model.functional.ShellRender.isSelf;
                matrices.pushPose();
                try{
                    matrices.translate(gun.hand().x-camera.x,gun.hand().y-camera.y,gun.hand().z-camera.z);matrices.mulPose(gun.rotation());matrices.scale(gun.worldScale(),gun.worldScale(),gun.worldScale());
                    matrices.scale(c.gunScale(),c.gunScale(),c.gunScale());var grip=profile.grip();matrices.translate(-grip.x,-grip.y,-grip.z);
                    matrices.scale(profile.scale(),profile.scale(),profile.scale());matrices.translate(0,1.5,0);matrices.mulPose(Axis.ZP.rotationDegrees(180));
                    model.cleanAnimationTransform();model.setRenderHand(false);com.tacz.guns.client.model.functional.MuzzleFlashRender.isSelf=false;com.tacz.guns.client.model.functional.ShellRender.isSelf=false;
                    var type=display.enablesTransparency()?RenderType.entityTranslucent(display.getModelTexture()):RenderType.entityCutout(display.getModelTexture());
                    model.render(matrices,stack,i==0?ItemDisplayContext.THIRD_PERSON_RIGHT_HAND:ItemDisplayContext.THIRD_PERSON_LEFT_HAND,type,mc.getEntityRenderDispatcher().getPackedLightCoords(player,event.getPartialTick()),OverlayTexture.NO_OVERLAY);mc.renderBuffers().bufferSource().endBatch();
                }finally{model.setRenderHand(hands);model.cleanAnimationTransform();com.tacz.guns.client.model.functional.MuzzleFlashRender.isSelf=flash;com.tacz.guns.client.model.functional.ShellRender.isSelf=shell;matrices.popPose();}
            }
        }
    }
}
