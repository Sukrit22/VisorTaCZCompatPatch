package dev.visorcompat.tacz.client;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.Handling.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import java.util.*;

@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT)
public final class RemoteGuns {
    private record Entry(CompatNetwork.RemoteState state,long time) {}
    private static final Map<UUID,Entry> STATES=new HashMap<>();
    public static void receive(CompatNetwork.RemoteState state) {
        if(state.active() && state.calibration().valid())STATES.put(state.player(),new Entry(state,System.nanoTime()));
        else STATES.remove(state.player());
    }
    private static CompatNetwork.RemoteState state(AbstractClientPlayer p) {
        var mc=Minecraft.getInstance();var entry=STATES.get(p.getUUID());
        if(p==mc.player || entry==null || System.nanoTime()-entry.time()>2_500_000_000L || !p.isAlive()
            || p.isSpectator() || p.isInvisibleTo(mc.player) || !entry.state().key().equals(Profiles.key(p.getMainHandItem()))
            || Profiles.get(p.getMainHandItem())==null || !(VisorAPI.getVRPlayer(p) instanceof VRClientPlayer))return null;
        return entry.state();
    }
    public static boolean replaces(ItemStack stack,ItemDisplayContext context) {
        if(com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow())return false;
        var mc=Minecraft.getInstance();if(mc.level==null || (context!=ItemDisplayContext.THIRD_PERSON_LEFT_HAND && context!=ItemDisplayContext.THIRD_PERSON_RIGHT_HAND))return false;
        for(var p:mc.level.players())if(p.getMainHandItem()==stack) {
            var state=state(p);if(state==null)continue;
            var vr=(VRClientPlayer)VisorAPI.getVRPlayer(p);
            var display=TimelessAPI.getGunDisplay(stack).orElse(null);
            if(display!=null && display.getGunModel()!=null && GunPose.resolve(vr.getPoseData(PlayerPoseType.RENDER),Profiles.get(stack),
                p.getOffhandItem().isEmpty() && (!state.physical() || (state.phase()==Phase.SUPPORT || state.phase()==Phase.PUMP_HOLD && state.pull()<4)),state.calibration(),state.anchor())!=null)return true;
        }
        return false;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {STATES.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES
            || com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow())return;
        var mc=Minecraft.getInstance();if(mc.level==null || mc.player==null)return;
        var matrices=event.getPoseStack();var camera=event.getCamera().getPosition();
        for(var player:mc.level.players()) {
            var state=state(player);if(state==null)continue;
            var vr=(VRClientPlayer)VisorAPI.getVRPlayer(player);
            var pose=vr.getPoseData(PlayerPoseType.RENDER);var stack=player.getMainHandItem();var profile=Profiles.get(stack);
            var gun=GunPose.resolve(pose,profile,player.getOffhandItem().isEmpty() && (!state.physical() || (state.phase()==Phase.SUPPORT || state.phase()==Phase.PUMP_HOLD && state.pull()<4)),state.calibration(),state.anchor());
            var display=TimelessAPI.getGunDisplay(stack).orElse(null);
            if(gun==null || display==null || display.getGunModel()==null)continue;
            var model=display.getGunModel();boolean hands=model.getRenderHand(),flash=MuzzleFlashRender.isSelf;
            boolean oldShell=com.tacz.guns.client.model.functional.ShellRender.isSelf;
            com.tacz.guns.client.model.functional.ShellRender.isSelf=false;
            matrices.pushPose();
            try {
                matrices.translate(gun.hand().x-camera.x,gun.hand().y-camera.y,gun.hand().z-camera.z);
                matrices.mulPose(gun.rotation());matrices.scale(gun.worldScale(),gun.worldScale(),gun.worldScale());
                matrices.pushPose();
                float modelScale=state.calibration().gunScale();matrices.scale(modelScale,modelScale,modelScale);
                var grip=profile.grip();matrices.translate(-grip.x,-grip.y,-grip.z);
                matrices.scale(profile.scale(),profile.scale(),profile.scale());matrices.translate(0,1.5,0);matrices.mulPose(Axis.ZP.rotationDegrees(180));
                model.cleanAnimationTransform();model.setRenderHand(false);MuzzleFlashRender.isSelf=false;
                var type=display.enablesTransparency()?RenderType.entityTranslucent(display.getModelTexture()):RenderType.entityCutout(display.getModelTexture());
                int light=mc.getEntityRenderDispatcher().getPackedLightCoords(player,event.getPartialTick());
                try(var physical=new PhysicalModel(model,profile,state.physical(),state.phase(),state.pull()*.005f,stack,state.calibration())) {
                    model.render(matrices,stack,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,type,light,OverlayTexture.NO_OVERLAY);
                } finally {matrices.popPose();}
                PhysicalModel.detached(matrices,model,profile,gun,pose,type,light,state.physical(),state.phase(),state.calibration().gunScale(),state.magazineLoaded());
                if(state.physical())PumpVisual.render(matrices,profile,gun,pose,state.phase(),light,state.calibration().gunScale());
                if(state.physical())JamVisual.render(matrices,stack,profile,state.calibration(),gun,pose,state.phase(),light);
                mc.renderBuffers().bufferSource().endBatch();
            } finally {model.setRenderHand(hands);MuzzleFlashRender.isSelf=flash;com.tacz.guns.client.model.functional.ShellRender.isSelf=oldShell;model.cleanAnimationTransform();matrices.popPose();}
        }
    }
}
