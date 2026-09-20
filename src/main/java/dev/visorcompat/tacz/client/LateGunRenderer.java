package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;

/** Render after world particles so scope capture contains decals, before GUI postprocessing. */
public final class LateGunRenderer extends VRGameEffect {
    private record Pending(GunRenderer renderer,HandType hand,VRRenderPass pass,PoseStack pose,Matrix4f view,float partial){}
    private static Pending pending;
    static boolean drawing;
    public LateGunRenderer(VisorAddon owner){super(owner);}
    @Override public String getId(){return "tacz_late_gun";}
    @Override public boolean isGlobal(){return true;}
    @Override public boolean isVisible(VRDecorator decorator){return pending!=null;}
    static void defer(GunRenderer renderer,HandType hand,VRRenderPass pass,PoseStack source,float partial){
        var copy=new PoseStack();copy.last().pose().set(source.last().pose());copy.last().normal().set(source.last().normal());
        pending=new Pending(renderer,hand,pass,copy,new Matrix4f(RenderSystem.getModelViewMatrix()),partial);
    }
    @Override public void render(VRRenderPass pass,PoseStack pose,float partial){
        var work=pending;pending=null;if(work==null || work.pass()!=pass || !ClientControls.supported())return;
        var view=RenderSystem.getModelViewStack();view.pushPose();
        try {view.last().pose().set(work.view());RenderSystem.applyModelViewMatrix();drawing=true;
            work.renderer().render(work.hand(),pass,work.pose(),false,work.partial());
        } finally {drawing=false;view.popPose();RenderSystem.applyModelViewMatrix();}
    }
}
