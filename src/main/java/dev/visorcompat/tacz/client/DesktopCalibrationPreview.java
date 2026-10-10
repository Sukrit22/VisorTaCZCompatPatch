package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.Handling;
import dev.visorcompat.tacz.physical.JamProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** An orthographic GUI preview: no world camera, controller pose or VR render pass. */
final class DesktopCalibrationPreview {
    private float yaw=65,pitch=-15,zoom=1;
    private int left,top,width,height;
    private boolean dragging;
    private boolean highlight;
    private float targetYaw=65,targetPitch=-15;
    private boolean moving;
    private long frameTime;
    private static final int[][] EDGES={{0,1},{0,2},{0,4},{1,3},{1,5},{2,3},{2,6},{3,7},{4,5},{4,6},{5,7},{6,7}};
    void bounds(int x,int y,int w,int h){left=x;top=y;width=w;height=h;}
    boolean contains(double x,double y){return x>=left&&x<left+width&&y>=top&&y<top+height;}
    boolean press(double x,double y,int button){
        if(button==0&&y>=top+23&&y<top+41){int i=(int)(x-left-7)/26;if(x>=left+7&&i>=0&&i<6){axis(i/2,i%2==1);return true;}}
        return dragging=button==0&&contains(x,y)&&y>=top+43;
    }
    boolean key(int key,boolean reverse){int axis=key==88?0:key==89?1:key==90?2:-1;if(axis<0)return false;axis(axis,reverse);return true;}
    private void axis(int axis,boolean reverse){
        targetPitch=axis==1?(reverse?-90:90):0;
        targetYaw=axis==0?(reverse?-90:90):axis==2&&reverse?180:0;
        targetYaw=yaw+(float)Math.IEEEremainder(targetYaw-yaw,360);moving=true;dragging=false;
    }
    private void animate(){long now=System.nanoTime();float dt=frameTime==0?.016f:Math.min(.1f,(now-frameTime)/1e9f);frameTime=now;
        if(moving){float blend=1-(float)Math.exp(-14*dt);yaw+=(targetYaw-yaw)*blend;pitch+=(targetPitch-pitch)*blend;
            if(Math.abs(yaw-targetYaw)+Math.abs(pitch-targetPitch)<.05f){yaw=targetYaw;pitch=targetPitch;moving=false;}}
    }
    private void viewButtons(GuiGraphics g){String[] names={"X","-X","Y","-Y","Z","-Z"};for(int i=0;i<6;i++){int x=left+7+i*26;g.fill(x,top+23,x+24,top+41,0xFF384652);g.drawString(Minecraft.getInstance().font,names[i],x+4,top+28,0xFFFFFF);}}
    boolean drag(double dx,double dy){if(!dragging)return false;moving=false;yaw+=(float)dx*.6f;pitch=Math.max(-90,Math.min(90,pitch+(float)dy*.6f));return true;}
    boolean release(){boolean old=dragging;dragging=false;return old;}
    boolean scroll(double x,double y,double delta){if(!contains(x,y))return false;zoom=Math.max(.25f,Math.min(4,zoom*(float)Math.pow(1.12,delta)));return true;}
    private Quaternionf view(){return new Quaternionf().rotationXYZ((float)Math.toRadians(pitch),(float)Math.toRadians(yaw),0);}
    private float pixels(){return Math.min(width,height)*.8f*zoom;}
    private Vector3f projected(Vector3f p,Calibration c,boolean mounted){
        if(mounted)p=c.rotation().transform(p).add(c.translation());
        return view().transform(p).mul(pixels()).add(left+width*.5f,0,0);
    }
    private void line(GuiGraphics g,Vector3f a,Vector3f b,int color){
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(a.x-b.x),Math.abs(a.y-b.y))));
        for(int i=0;i<=steps;i++){float t=(float)i/steps;int x=Math.round(a.x+(b.x-a.x)*t),y=Math.round(top+height*.48f-a.y-(b.y-a.y)*t);
            if(highlight)g.fill(RenderType.guiOverlay(),x-1,y-1,x+2,y+2,0xFFFFFFFF);
            g.fill(RenderType.guiOverlay(),x,y,x+1,y+1,color|0xFF000000);}
    }
    private void box(GuiGraphics g,Vector3f center,ZoneSizes.Box box,Calibration c,boolean mounted,int color){
        Vector3f[] points=new Vector3f[8];
        for(int i=0;i<8;i++)points[i]=projected(new Vector3f(center).add((i&1)==0?-box.width()/2:box.width()/2,(i&2)==0?-box.height()/2:box.height()/2,(i&4)==0?-box.depth()/2:box.depth()/2),c,mounted);
        for(var edge:EDGES)line(g,points[edge[0]],points[edge[1]],color);
    }
    void renderMagazine(GuiGraphics g,ItemStack stack,WeaponProfile profile,InspectionCalibration c,float modelScale){
        animate();var mc=Minecraft.getInstance();g.fill(left,top,left+width,top+height,0xE0182028);
        g.drawString(mc.font,"Drag to rotate | Scroll to zoom",left+7,top+7,0xCCCCCC);
        viewButtons(g);g.enableScissor(left,top+43,left+width,top+height-20);
        try{
            g.flush();
            TimelessAPI.getGunDisplay(stack).ifPresent(display->{
                var model=display.getGunModel();if(model==null)return;var pose=g.pose();pose.pushPose();
                try{
                    pose.translate(left+width*.5,top+height*.48,150);pose.scale(pixels(),-pixels(),pixels());pose.mulPose(view());
                    pose.translate(c.x(),c.y(),c.z());
                    float r=(float)Math.PI/180;pose.mulPose(new Quaternionf().rotateY(c.yaw()*r).rotateX(c.pitch()*r).rotateZ(c.roll()*r));
                    Lighting.setupFor3DItems();model.cleanAnimationTransform();
                    var type=display.enablesTransparency()?RenderType.entityTranslucent(display.getModelTexture()):RenderType.entityCutout(display.getModelTexture());
                    PhysicalModel.looseMagazine(pose,model,profile,modelScale,type,0xF000F0,true);
                    mc.renderBuffers().bufferSource().endBatch();
                }finally{model.cleanAnimationTransform();pose.popPose();Lighting.setupForFlatItems();}
            });
            box(g,new Vector3f(0,-.025f,.025f),new ZoneSizes.Box(.075f,.10f,.065f),Calibration.ZERO,false,0xDFB58B);
            box(g,new Vector3f(.042f,.01f,-.012f),new ZoneSizes.Box(.026f,.045f,.07f),Calibration.ZERO,false,0xDFB58B);
        }finally{g.disableScissor();}
        g.drawString(mc.font,"Tan outline: magazine hand placeholder",left+7,top+height-14,0xDFB58B);
    }
    void render(GuiGraphics g,ItemStack stack,WeaponProfile profile,Calibration c,String key,CalibrationLayout.Page selected){
        animate();var mc=Minecraft.getInstance();g.fill(left,top,left+width,top+height,0xE0182028);
        g.drawString(mc.font,"Drag to rotate | Scroll to zoom",left+7,top+7,0xCCCCCC);
        viewButtons(g);g.enableScissor(left,top+43,left+width,top+height-20);
        try{
            g.flush();
            TimelessAPI.getGunDisplay(stack).ifPresent(display->{
                var model=display.getGunModel();if(model==null)return;
                var pose=g.pose();boolean hands=model.getRenderHand(),flash=MuzzleFlashRender.isSelf,shell=ShellRender.isSelf;
                pose.pushPose();
                try{
                    pose.translate(left+width*.5,top+height*.48,150);
                    pose.scale(pixels(),-pixels(),pixels());pose.mulPose(view());
                    pose.translate(c.x(),c.y(),c.z());pose.mulPose(c.rotation());
                    pose.scale(c.gunScale(),c.gunScale(),c.gunScale());
                    var grip=profile.grip();pose.translate(-grip.x,-grip.y,-grip.z);
                    pose.scale(profile.scale(),profile.scale(),profile.scale());pose.translate(0,1.5,0);pose.mulPose(Axis.ZP.rotationDegrees(180));
                    model.cleanAnimationTransform();model.setRenderHand(false);MuzzleFlashRender.isSelf=false;ShellRender.isSelf=false;
                    Lighting.setupFor3DItems();
                    var type=display.enablesTransparency()?RenderType.entityTranslucent(display.getModelTexture()):RenderType.entityCutout(display.getModelTexture());
                    model.render(pose,stack,ItemDisplayContext.FIXED,type,0xF000F0,OverlayTexture.NO_OVERLAY);
                    mc.renderBuffers().bufferSource().endBatch();
                }finally{model.setRenderHand(hands);MuzzleFlashRender.isSelf=flash;ShellRender.isSelf=shell;model.cleanAnimationTransform();pose.popPose();Lighting.setupForFlatItems();}
            });
            // The placeholder stays at the controller origin while grip offsets move the gun.
            box(g,new Vector3f(0,-.025f,.025f),new ZoneSizes.Box(.075f,.10f,.065f),c,false,0xDFB58B);
            box(g,new Vector3f(.042f,.01f,-.012f),new ZoneSizes.Box(.026f,.045f,.07f),c,false,0xDFB58B);
            for(var page:CalibrationLayout.pages(key,profile)){
                if(page.zone()==null||page.zone()==ZoneSizes.Zone.POUCH)continue;
                Vector3f center=switch(page.zone()){
                    case SUPPORT->Handling.support(profile,c);case MAGAZINE->Handling.magazine(profile,c);
                    case RACK->Handling.rack(profile,c);case SELECTOR->Handling.selector(profile,c);
                    case RELEASE->Handling.release(profile,c);case PORT->JamProfile.of(profile,c).port();
                    case SIGHT->{var p=OpticGeometry.sight(stack,profile);yield p==null?null:p.add(c.interactions().sight().vector()).mul(c.gunScale()).add(0,0,.015f+c.zones().sight().depth()/2);}
                    default->null;
                };
                if(center==null)continue;
                int color=CalibrationHints.get(key,page.id()).color();highlight=page.id().equals(selected.id());box(g,center,c.zones().get(page.zone()),c,true,color);highlight=false;
                if(page.id().equals(selected.id())){var p=projected(new Vector3f(center),c,true);g.drawString(mc.font,CalibrationHints.get(key,page.id()).title(),Math.round(p.x)+4,Math.round(top+height*.48f-p.y),color);}
            }
            highlight=selected.id().equals("muzzle");box(g,c.muzzleOffset(profile.muzzleOffset()),new ZoneSizes.Box(.012f,.012f,.012f),c,true,CalibrationHints.get(key,"muzzle").color());highlight=false;
        }finally{g.disableScissor();}
        g.drawString(mc.font,selected.zone()==ZoneSizes.Zone.POUCH?"Pouch uses head coordinates; check in VR":"Tan outline: grip hand placeholder",left+7,top+height-14,0xDFB58B);
    }
}
