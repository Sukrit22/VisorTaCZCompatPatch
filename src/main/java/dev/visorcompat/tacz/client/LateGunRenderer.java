package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import java.util.ArrayList;
import java.util.List;

/** Render after world particles so scope capture contains decals, before GUI postprocessing. */
public final class LateGunRenderer extends VRGameEffect {
    private record Pending(VRHandEffect renderer,HandType hand,VRRenderPass pass,PoseStack pose,Matrix4f view,float partial){}
    private static final List<Pending> pending=new ArrayList<>();
    static boolean drawing;
    public LateGunRenderer(VisorAddon owner){super(owner);}
    @Override public String getId(){return "tacz_late_gun";}
    @Override public boolean isGlobal(){return true;}
    @Override public boolean isVisible(VRDecorator decorator){return !pending.isEmpty();}
    static void defer(VRHandEffect renderer,HandType hand,VRRenderPass pass,PoseStack source,float partial){
        var copy=new PoseStack();copy.last().pose().set(source.last().pose());copy.last().normal().set(source.last().normal());
        pending.removeIf(work->work.pass()!=pass || (work.renderer()==renderer && work.hand()==hand));
        pending.add(new Pending(renderer,hand,pass,copy,new Matrix4f(RenderSystem.getModelViewMatrix()),partial));
    }
    @Override public void render(VRRenderPass pass,PoseStack pose,float partial){
        var workList=new ArrayList<>(pending);pending.clear();
        // Both hands survive deferral. Panels follow all gun draws and translucent water.
        workList.sort(java.util.Comparator.comparingInt(work->work.renderer() instanceof StatusPanel?1:0));
        if(!ClientControls.vrActive())return;
        for(var work:workList){
            if(work.pass()!=pass)continue;
            var view=RenderSystem.getModelViewStack();view.pushPose();
            try {view.last().pose().set(work.view());RenderSystem.applyModelViewMatrix();drawing=true;
                work.renderer().render(work.hand(),pass,work.pose(),false,work.partial());
            } finally {drawing=false;view.popPose();RenderSystem.applyModelViewMatrix();}
        }
    }
}
