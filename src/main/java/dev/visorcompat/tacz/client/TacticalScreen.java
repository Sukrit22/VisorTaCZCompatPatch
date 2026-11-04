package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.Locale;

/** Explicit-save tactical editor; drafts never alter live or server calibration. */
public final class TacticalScreen extends Screen {
    private final String gunId,returnKey;
    private final float[] values=new float[24];
    private int page;
    private boolean coarse,previewOnly,alternative;
    private final DesktopCalibrationPreview desktop=new DesktopCalibrationPreview();
    private TacticalCalibration lastSaved;
    private String message="";
    public TacticalScreen(String gunId,String returnKey){super(Component.literal("Tactical magazine calibration"));this.gunId=gunId;this.returnKey=returnKey;set(TacticalStore.get(gunId,returnKey));if(TacticalStore.error()!=null)message=TacticalStore.error();}
    private void set(TacticalCalibration c){
        alternative=c.alternativeControls();
        InspectionCalibration[] poses={c.normalGrip(),c.pinchGrip(),c.magwellEntry()};
        for(int i=0;i<3;i++){var p=poses[i];float[] a={p.x(),p.y(),p.z(),p.pitch(),p.yaw(),p.roll()};System.arraycopy(a,0,values,i*6,6);}
        values[18]=c.pitchTolerance();values[19]=c.yawTolerance();values[20]=c.rollTolerance();values[21]=c.entryRadius();values[22]=c.seatedDepth();values[23]=c.seatedTolerance();
    }
    private InspectionCalibration pose(int n){int i=n*6;return new InspectionCalibration(values[i],values[i+1],values[i+2],values[i+3],values[i+4],values[i+5]);}
    public TacticalCalibration value(){return new TacticalCalibration(pose(0),pose(1),pose(2),values[18],values[19],values[20],values[21],values[22],values[23],alternative);}
    /** Draft for local guide rendering only; never used for gameplay or synchronization. */
    public String gunId(){return gunId;}
    public static TacticalCalibration preview(String id){var screen=net.minecraft.client.Minecraft.getInstance().screen;return screen instanceof TacticalScreen tactical&&tactical.gunId.equals(id)?tactical.value():null;}
    private boolean metres(int i){return i<18?i%6<3:i>=21;}
    @Override protected void init(){
        boolean compact=height<310;int rows=compact?3:6,pagesCount=compact?8:4;page=Math.min(page,pagesCount-1);
        int group=compact?page/2:page,first=compact?(page%2)*3:0;
        int x=width>=620?width-310:width/2-150,y=Math.max(54,height/2-(compact?85:120)),tail=28+rows*23;
        desktop.bounds(width>=620?8:4,4,width>=620?x-16:width-8,height-8);
        if(width<620)addRenderableWidget(Button.builder(Component.literal(previewOnly?"Back to edits":"Show tactical preview"),b->{previewOnly=!previewOnly;rebuildWidgets();}).bounds(x,y-24,300,20).build());
        if(previewOnly&&width<620){ClientControls.clearInput();return;}
        String[] pages={"Normal magazine grip","Pinch magazine grip","Magwell entry pose","Pose range / seating guide"};
        addRenderableWidget(Button.builder(Component.literal("<"),b->{page=(page+pagesCount-1)%pagesCount;rebuildWidgets();}).bounds(x,y,28,20).build());
        var titleButton=addRenderableWidget(Button.builder(Component.literal(pages[group]+(compact?" "+(page%2+1)+"/2":"")),b->{}).bounds(x+32,y,236,20).build());titleButton.active=false;
        addRenderableWidget(Button.builder(Component.literal(">"),b->{page=(page+1)%pagesCount;rebuildWidgets();}).bounds(x+272,y,28,20).build());
        String[] poseNames={"X right","Y up","Z back","Pitch","Yaw","Roll"};
        String[] guideNames={"Pitch tolerance","Yaw tolerance","Roll tolerance","Entry radius","Seated depth","Seated tolerance"};
        for(int row=0;row<rows;row++){
            int i=group*6+first+row,top=y+25+row*23;
            addRenderableWidget(Button.builder(Component.literal("-"),b->adjust(i,-1)).bounds(x,top,28,20).build());
            String text=(group==3?guideNames[first+row]:poseNames[first+row])+": "+String.format(Locale.ROOT,metres(i)?"%.1f mm":"%.0f deg",values[i]*(metres(i)?1000:1));
            var label=addRenderableWidget(Button.builder(Component.literal(text),b->{}).bounds(x+32,top,236,20).build());label.active=false;
            addRenderableWidget(Button.builder(Component.literal("+"),b->adjust(i,1)).bounds(x+272,top,28,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Defaults (draft)"),b->{set(TacticalCalibration.DEFAULT);rebuildWidgets();}).bounds(x,y+tail,145,20).build());
        addRenderableWidget(Button.builder(Component.literal(coarse?"Coarse: 1 cm / 10 deg":"Fine: 1 mm / 1 deg"),b->{coarse=!coarse;rebuildWidgets();}).bounds(x+150,y+tail,150,20).build());
        addRenderableWidget(Button.builder(Component.literal(alternative?"Alternative: Grab new / Use old":"Standard: Use new / Grab old"),b->{alternative=!alternative;rebuildWidgets();}).bounds(x,y+tail+24,300,20).build());
        boolean saved=value().equals(lastSaved);
        addRenderableWidget(Button.builder(Component.literal(saved?"Close":"Save"),b->{
            if(value().equals(lastSaved)){onClose();return;}
            try{TacticalStore.save(gunId,value());lastSaved=value();CompatSettings.forceSync();message="Saved tactical calibration";rebuildWidgets();}catch(java.io.IOException ex){message=ex.getMessage();}
        }).bounds(x,y+tail+48,saved?300:145,20).build());
        if(!saved)addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(x+150,y+tail+48,150,20).build());ClientControls.clearInput();
    }
    private void adjust(int i,int sign){float old=values[i];values[i]+=sign*(metres(i)?(coarse?.01f:.001f):(coarse?10:1));if(!value().valid())values[i]=old;rebuildWidgets();}
    private boolean previewVisible(){return width>=620||previewOnly;}
    @Override public boolean keyPressed(int key,int scan,int mods){if(previewVisible()&&desktop.key(key,hasShiftDown()))return true;return super.keyPressed(key,scan,mods);}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        renderBackground(g);
        if(previewVisible()&&minecraft.player!=null){
            var stack=minecraft.player.getMainHandItem();var profile=Profiles.get(stack);
            if(profile!=null){var c=CalibrationStore.get(returnKey);int group=height<310?page/2:page;if(group<2)desktop.renderMagazine(g,stack,profile,group==0?value().normalGrip():value().pinchGrip(),c.gunScale());else desktop.renderTactical(g,stack,profile,c,returnKey,value());}
        }
        super.render(g,x,y,partial);
        if(!previewOnly||width>=620){g.drawCenteredString(font,title,width>=620?width-160:width/2,12,0xFFFFFF);g.drawCenteredString(font,message.isEmpty()?"Tolerance = alignment at entry; distances = mm.":message,width/2,height-12,0xFFFFFF);}
    }
    @Override public boolean mouseClicked(double x,double y,int button){if(super.mouseClicked(x,y,button))return true;if(previewVisible()&&desktop.press(x,y,button))return true;return false;}
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){if(previewVisible()&&desktop.drag(dx,dy))return true;return super.mouseDragged(x,y,button,dx,dy);}
    @Override public boolean mouseReleased(double x,double y,int button){if(desktop.release())return true;return super.mouseReleased(x,y,button);}
    @Override public boolean mouseScrolled(double x,double y,double delta){if(previewVisible()&&desktop.scroll(x,y,delta))return true;return super.mouseScrolled(x,y,delta);}
    @Override public void removed(){ClientControls.clearInput();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(new MagazineGripScreen(returnKey));}
}
