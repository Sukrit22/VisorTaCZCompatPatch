package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import java.util.Locale;

/** Six-axis detached-magazine grip editor with a headset-independent native preview. */
public final class MagazineGripScreen extends Screen {
    private final String key;
    private final float[] values=new float[6];
    private final DesktopCalibrationPreview preview=new DesktopCalibrationPreview();
    private InspectionCalibration lastSaved;
    private boolean coarse,previewOnly;
    private int panelLeft,top;
    private String message="";
    public MagazineGripScreen(String key){
        super(Component.literal("Magazine hand grip"));this.key=key;set(MagazineGripStore.get(key));
        if(MagazineGripStore.error()!=null)message=MagazineGripStore.error();
    }
    private InspectionCalibration value(){return new InspectionCalibration(values[0],values[1],values[2],values[3],values[4],values[5]);}
    private void set(InspectionCalibration c){values[0]=c.x();values[1]=c.y();values[2]=c.z();values[3]=c.pitch();values[4]=c.yaw();values[5]=c.roll();publish();}
    private void publish(){MagazineGripStore.previewKey=key;MagazineGripStore.preview=value();}
    @Override protected void init(){
        panelLeft=width>=620?width-310:width/2-150;top=Math.max(30,height/2-100);
        preview.bounds(width>=620?8:4,4,width>=620?panelLeft-16:width-8,height-8);
        if(width<620)addRenderableWidget(Button.builder(Component.literal(previewOnly?"Back to edits":"Show magazine preview"),b->{previewOnly=!previewOnly;rebuildWidgets();}).bounds(panelLeft,top-24,300,20).build());
        if(previewOnly&&width<620){ClientControls.clearInput();return;}
        String[] names={"X right","Y up","Z back","Pitch","Yaw","Roll"};
        for(int i=0;i<6;i++){
            int axis=i,row=top+i*23;
            addRenderableWidget(Button.builder(Component.literal("-"),b->adjust(axis,-1)).bounds(panelLeft,row,28,20).build());
            var label=addRenderableWidget(Button.builder(Component.literal(names[i]+": "+String.format(Locale.ROOT,i<3?"%.1f mm":"%.0f deg",values[i]*(i<3?1000:1))),b->{}).bounds(panelLeft+32,row,236,20).build());label.active=false;
            addRenderableWidget(Button.builder(Component.literal("+"),b->adjust(axis,1)).bounds(panelLeft+272,row,28,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Default grip"),b->{set(MagazineGripFiles.DEFAULT);message="";rebuildWidgets();}).bounds(panelLeft,top+141,140,20).build());
        addRenderableWidget(Button.builder(Component.literal(coarse?"Coarse: 1 cm / 10 deg":"Fine: 1 mm / 1 deg"),b->{coarse=!coarse;rebuildWidgets();}).bounds(panelLeft+145,top+141,155,20).build());
        boolean saved=value().equals(lastSaved);
        addRenderableWidget(Button.builder(Component.literal(saved?"Close":"Save"),b->{
            if(value().equals(lastSaved)){onClose();return;}
            try{var c=value();MagazineGripStore.save(key,c);lastSaved=c;message="Saved magazine hand grip";rebuildWidgets();}
            catch(java.io.IOException ex){message=ex.getMessage();}
        }).bounds(panelLeft,top+165,saved?300:145,20).build());
        if(!saved)addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).tooltip(Tooltip.create(Component.literal("Discard edits since the last save. Saved changes stay on disk."))).bounds(panelLeft+155,top+165,145,20).build());
        ClientControls.clearInput();
    }
    private void adjust(int axis,int sign){float old=values[axis];values[axis]+=sign*(axis<3?(coarse?.01f:.001f):(coarse?10:1));if(!value().valid())values[axis]=old;else message="";publish();rebuildWidgets();}
    private boolean previewVisible(){return width>=620||previewOnly;}
    @Override public boolean keyPressed(int key,int scan,int mods){if(previewVisible()&&preview.key(key,hasShiftDown()))return true;return super.keyPressed(key,scan,mods);}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        if(previewVisible()&&minecraft.player!=null){var stack=minecraft.player.getMainHandItem();var p=Profiles.get(stack);if(p!=null)preview.renderMagazine(g,stack,p,value(),CalibrationStore.get(key).gunScale());}
        if(previewOnly&&width<620){g.fill(panelLeft,top-24,panelLeft+300,top-4,0xE0101010);g.drawCenteredString(font,"Back to edits",panelLeft+150,top-18,0xFFFFFF);return;}
        g.fill(panelLeft-8,top-27,panelLeft+308,top+189,0xD0101010);
        g.drawCenteredString(font,title,panelLeft+150,top-23,0xFFFFFF);
        super.render(g,x,y,partial);
        g.drawCenteredString(font,message.isEmpty()?"Move the magazine relative to its holding hand.":message,width/2,height-12,0xFFFFFF);
    }
    @Override public boolean mouseClicked(double x,double y,int button){
        if(previewOnly&&width<620&&x>=panelLeft&&x<=panelLeft+300&&y>=top-24&&y<=top-4){previewOnly=false;rebuildWidgets();return true;}
        if(previewVisible()&&preview.press(x,y,button))return true;return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){if(previewVisible()&&preview.drag(dx,dy))return true;return super.mouseDragged(x,y,button,dx,dy);}
    @Override public boolean mouseReleased(double x,double y,int button){if(preview.release())return true;return super.mouseReleased(x,y,button);}
    @Override public boolean mouseScrolled(double x,double y,double delta){if(previewVisible()&&preview.scroll(x,y,delta))return true;return super.mouseScrolled(x,y,delta);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void removed(){MagazineGripStore.preview=null;MagazineGripStore.previewKey=null;ClientControls.clearInput();}
    @Override public void onClose(){minecraft.setScreen(new ControlsScreen());}
}
