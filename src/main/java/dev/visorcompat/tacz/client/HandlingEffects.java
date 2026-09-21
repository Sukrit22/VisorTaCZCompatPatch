package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.VisorTacz;
import dev.visorcompat.tacz.network.CompatNetwork;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.compat.oculus.OculusCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.sounds.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.math.Axis;
import java.util.*;

@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT)
public final class HandlingEffects {
    static final net.minecraft.client.model.geom.ModelPart LIVE_ROUND=liveRound();
    private static net.minecraft.client.model.geom.ModelPart liveRound(){
        var mesh=new net.minecraft.client.model.geom.builders.MeshDefinition();
        mesh.getRoot().addOrReplaceChild("case",net.minecraft.client.model.geom.builders.CubeListBuilder.create()
            .texOffs(0,0).addBox(-.08f,-.08f,-.3f,.16f,.16f,.6f),net.minecraft.client.model.geom.PartPose.ZERO);
        return net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,16,16).bakeRoot();
    }
    // 0 remove, 1 insert, 2 rack empty, 3 eject live round, 4 eject spent casing.
    private record Round(CompatNetwork.Feedback data,long start){}
    private static final ArrayDeque<Round> ROUNDS=new ArrayDeque<>();
    public static void receive(CompatNetwork.Feedback m){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(m.kind()!=4 || m.gun().getPath().equals("m870") || m.gun().getPath().equals("m700")){
            var entity=mc.level.getEntity(m.entityId());
            String folder=m.gun().getPath();
            String clip=switch(m.kind()) {
                case 0 -> folder.equals("glock_17")?"glock17_reload_magout":"m4a1_reload_tactical_magout";
                case 1 -> folder.equals("glock_17")?"glock17_reload_magin":"m4a1_reload_tactical_magin";
                case 5,6 -> folder.equals("glock_17")?"glock17_inspect_rackhit":"m4a1_inspect_rackrelase";
                default -> folder.equals("glock_17")?"glock17_inspect_rackon":"m4a1_inspect_rackon";
            };
            if(folder.equals("m870"))clip=switch(m.kind()) {
                case 1 -> "m870_reload_loop_shellin_01";
                case 5 -> "m870_bolt_out";
                default -> "m870_bolt_in";
            };
            if(folder.equals("m700"))clip=switch(m.kind()) {
                case 0 -> "m700_reload_magout";case 1 -> "m700_reload_magin";
                case 5 -> "m700_bolt_out";default -> "m700_bolt_in";
            };
            if(folder.equals("hk_mp5a5"))clip=switch(m.kind()) {
                case 0 -> "hk_mp5a5_reload_magout";case 1 -> "hk_mp5a5_reload_magin";
                case 5 -> "hk_mp5a5_inspect_boltrelease";default -> "hk_mp5a5_inspect_boltback";
            };
            var pistol=dev.visorcompat.tacz.PistolProfiles.get(dev.visorcompat.tacz.Profiles.byId(m.gun().toString()));
            String sound=pistol==null?folder+"/"+clip:pistol.sound(m.kind());
            if(entity!=null)com.tacz.guns.client.sound.SoundPlayManager.playAnimationSound(entity,
                new net.minecraft.resources.ResourceLocation("tacz",sound),m.kind()==6?.55f:.8f,m.kind()==6?.65f:1f,12);
        }
        if(m.kind()==3 || m.kind()==4){
            if(ROUNDS.size()>=64)ROUNDS.removeFirst();ROUNDS.addLast(new Round(m,System.nanoTime()));
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){ROUNDS.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES || OculusCompat.isRenderShadow())return;
        long now=System.nanoTime();ROUNDS.removeIf(r->now-r.start()>1_250_000_000L);
        var mc=Minecraft.getInstance();if(mc.level==null || ROUNDS.isEmpty())return;
        var camera=e.getCamera().getPosition();var matrices=e.getPoseStack();
        for(var r:ROUNDS){
            var m=r.data();var index=TimelessAPI.getClientAmmoIndex(m.ammo()).orElse(null);if(index==null)continue;
            boolean liveModel=false; // Physical cartridges share casing geometry; projectile entities have unrelated scale.
            var model=liveModel?index.getAmmoEntityModel():index.getShellModel();
            var texture=liveModel?index.getAmmoEntityTextureLocation():index.getShellTextureLocation();
            boolean generic=m.kind()==3 && (model==null || texture==null);
            if(!generic && (model==null || texture==null))continue;
            float t=(now-r.start())/1_000_000_000f;
            matrices.pushPose();
            try {
                matrices.translate(m.x()+m.vx()*t-camera.x,m.y()+m.vy()*t-2.4*t*t*m.scale()-camera.y,m.z()+m.vz()*t-camera.z);
                matrices.mulPose(Axis.XP.rotationDegrees(t*560));matrices.mulPose(Axis.ZP.rotationDegrees(t*300));
                float visual=m.scale()*m.gunScale();matrices.scale(visual,visual,visual);
                if(generic) {
                    var type=RenderType.entityCutout(new net.minecraft.resources.ResourceLocation("minecraft","textures/block/gold_block.png"));
                    LIVE_ROUND.render(matrices,mc.renderBuffers().bufferSource().getBuffer(type),0xF000F0,OverlayTexture.NO_OVERLAY);
                } else {
                    matrices.translate(0,-1.5,0);
                    model.render(matrices,ItemDisplayContext.GROUND,RenderType.entityCutout(texture),0xF000F0,OverlayTexture.NO_OVERLAY);
                }
            } finally {matrices.popPose();}
        }
        mc.renderBuffers().bufferSource().endBatch();
    }
}
