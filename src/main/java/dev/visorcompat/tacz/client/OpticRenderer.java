package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.compat.oculus.OculusCompat;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.mixin.ScopeModelAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import java.io.IOException;
import java.nio.ByteBuffer;

@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class OpticRenderer {
    private static final ResourceLocation SCENE=new ResourceLocation(VisorTacz.ID,"scope_scene");
    private static ShaderInstance shader;
    private static SceneTexture texture;
    private static boolean local;
    private static boolean gunPass;
    public static boolean gunPass(){return gunPass;}
    private static float scale=1;
    private static final int[] viewport=new int[4];
    @SubscribeEvent public static void shaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation(VisorTacz.ID,"optic"),DefaultVertexFormat.NEW_ENTITY),s->shader=s);
    }
    public static void begin(VRRenderPass pass,float worldScale) {
        local=false;
        gunPass=ClientControls.vrActive() && pass.isEye();
        if(!ClientControls.vrActive() || !CompatSettings.optics() || shader==null || !pass.isEye() || OculusCompat.isUsingRenderPack())return;
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.screen!=null)return;
        var stack=mc.player.getMainHandItem();var gun=IGun.getIGunOrNull(stack);if(gun==null)return;
        var scope=com.tacz.guns.api.item.attachment.AttachmentType.SCOPE;
        if(gun.getAttachment(stack,scope).isEmpty() && gun.getBuiltinAttachment(stack,scope).isEmpty())return;
        mc.renderBuffers().bufferSource().endBatch();
        GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        if(viewport[2]<=0 || viewport[3]<=0)return;
        if(texture==null) {texture=new SceneTexture();mc.getTextureManager().register(SCENE,texture);}
        texture.capture(viewport);scale=worldScale;local=true;
    }
    public static void end() {local=false;gunPass=false;}
    public static void draw(BedrockAttachmentModel model,ItemStack stack,PoseStack matrices,ItemDisplayContext context,int light,int overlay) {
        if(!local || !ClientControls.vrActive() || shader==null || stack==null || stack.isEmpty()
            || IGun.getIGunOrNull(stack)==null || (!model.isSight() && !model.isScope()))return;
        var access=(ScopeModelAccess)model;
        var paths=access.visorTacz$oculars();var scopes=access.visorTacz$scopeOculars();
        if(paths==null)return;
        float magnification=IGun.getIGunOrNull(stack).getAimingZoom(stack);
        magnification=Float.isFinite(magnification)?java.lang.Math.max(1,java.lang.Math.min(16,magnification)):1;
        var buffers=Minecraft.getInstance().renderBuffers().bufferSource();
        buffers.endBatch();
        boolean stencil=GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        try {
            for(int i=0;i<paths.size();i++) {
                var path=paths.get(i);if(path==null || path.isEmpty())continue;
                boolean scope=model.isScope() && (!model.isSight() || scopes.get(i));
                matrices.pushPose();
                try {
                    OpticGeometry.path(matrices,path);
                    Matrix4f view=new Matrix4f(RenderSystem.getModelViewMatrix()).mul(matrices.last().pose());
                    Vector3f center=view.getTranslation(new Vector3f());
                    Vector3f forward=view.transformDirection(new Vector3f(0,0,-1)).normalize();
                    Vector3f target=new Vector3f(forward).mul(100*scale).add(center);
                    Vector2f aim=OpticMath.project(target,RenderSystem.getProjectionMatrix());
                    if(aim==null)continue;
                    float relief=center.dot(forward);
                    float lateral=new Vector3f(center).sub(new Vector3f(forward).mul(relief)).length();
                    boolean valid=relief>.025f*scale && relief<.65f*scale && lateral<.04f*scale;
                    shader.safeGetUniform("Viewport").set((float)viewport[0],(float)viewport[1],(float)viewport[2],(float)viewport[3]);
                    shader.safeGetUniform("AimUV").set(aim.x,aim.y);
                    shader.safeGetUniform("Zoom").set(scope?magnification:1);
                    shader.safeGetUniform("Scope").set(scope?1f:0f);
                    shader.safeGetUniform("EyeValid").set(valid?1f:0f);
                } finally {matrices.popPose();}
                // Use the optic's actual ocular mesh as the aperture; no global stencil clear.
                matrices.pushPose();
                try {
                    for(int j=0;j<path.size()-1;j++)path.get(j).translateAndRotateAndScale(matrices);
                    var part=path.get(path.size()-1);boolean visible=part.visible;
                    try {part.visible=true;part.render(matrices,context,buffers.getBuffer(LensType.TYPE),light,overlay);}
                    finally {part.visible=visible;}
                    buffers.endBatch(LensType.TYPE);
                } finally {matrices.popPose();}
            }
        } finally {if(stencil)GL11.glEnable(GL11.GL_STENCIL_TEST);}
    }
    private static final class SceneTexture extends AbstractTexture {
        private int width,height;
        @Override public void load(ResourceManager resources) {}
        void capture(int[] view) {
            int previous=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GlStateManager._bindTexture(getId());
            try {
                if(width!=view[2] || height!=view[3]) {
                    width=view[2];height=view[3];
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,width,height,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,(ByteBuffer)null);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
                }
                GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,view[0],view[1],width,height);
            } finally {GlStateManager._bindTexture(previous);}
        }
    }
    private static final class LensType extends RenderType {
        private LensType() {super("unused",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,false,()->{},()->{});}
        static final RenderType TYPE=create("visor_tacz_optic",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,false,
            CompositeState.builder().setShaderState(new ShaderStateShard(()->shader))
                .setTextureState(new TextureStateShard(SCENE,false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    }
}
