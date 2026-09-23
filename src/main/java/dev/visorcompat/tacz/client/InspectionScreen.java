package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
public final class InspectionScreen extends Screen {
    private final String key;private final boolean pistol;private final float[] values=new float[6];private String message="";
    private boolean coarse;
    private boolean partial=true;
    public boolean partial(){return partial;}
    private InspectionCalibration lastSaved;
    public InspectionScreen(String key,boolean pistol){super(Component.literal("Inspection: "+key.split("\\|")[0]));this.key=key;this.pistol=pistol;set(InspectionStore.get(key,pistol));}
    private void set(InspectionCalibration c){values[0]=c.x();values[1]=c.y();values[2]=c.z();values[3]=c.pitch();values[4]=c.yaw();values[5]=c.roll();preview();}
    private InspectionCalibration value(){return new InspectionCalibration(values[0],values[1],values[2],values[3],values[4],values[5]);}
    private void preview(){InspectionStore.previewKey=key;InspectionStore.preview=value();}
    @Override protected void init(){int x=width/2-150,y=Math.max(24,height/2-100);String[] names={"X right","Y up","Z back","Pitch","Yaw","Roll"};
        for(int i=0;i<6;i++){final int n=i;int row=y+i*23;
            addRenderableWidget(Button.builder(Component.literal("-"),b->adjust(n,-1)).bounds(x,row,28,20).build());
            addRenderableWidget(Button.builder(Component.literal(names[i]+": "+String.format(java.util.Locale.ROOT,i<3?"%.3f m":"%.0f deg",values[i])),b->{}).bounds(x+32,row,236,20).build());
            addRenderableWidget(Button.builder(Component.literal("+"),b->adjust(n,1)).bounds(x+272,row,28,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Defaults"),b->{set(InspectionCalibration.defaults(pistol));message="";rebuildWidgets();}).bounds(x,y+141,65,20).build());
        addRenderableWidget(Button.builder(Component.literal(partial?"Slide: back":"Slide: closed"),b->{partial=!partial;rebuildWidgets();}).bounds(x+69,y+141,72,20).build());
        addRenderableWidget(Button.builder(Component.literal(coarse?"Coarse: 1 cm / 10 deg":"Fine: 1 mm / 1 deg"),b->{coarse=!coarse;rebuildWidgets();}).bounds(x+145,y+141,155,20).build());
        addRenderableWidget(Button.builder(Component.literal(value().equals(lastSaved)?"Close":"Save"),b->{
            if(value().equals(lastSaved)){onClose();return;}
            try{var next=value();InspectionStore.save(key,next);lastSaved=next;message="Saved to visor_tacz-inspections.json";rebuildWidgets();}catch(java.io.IOException ex){message=ex.getMessage();}
        }).bounds(x,y+165,value().equals(lastSaved)?300:145,20).build());
        if(!value().equals(lastSaved))addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Discard edits since the last save. Already saved changes remain on disk."))).bounds(x+155,y+165,145,20).build());
    }
    private void adjust(int i,int direction){float old=values[i];values[i]+=direction*(i<3?(coarse?.01f:.001f):(coarse?10f:1f));if(!value().valid())values[i]=old;else message="";preview();rebuildWidgets();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void removed(){InspectionStore.preview=null;InspectionStore.previewKey=null;}
    @Override public void onClose(){minecraft.setScreen(new AdvancedScreen());}
    @Override public void render(GuiGraphics g,int x,int y,float dt){g.drawCenteredString(font,title,width/2,8,0xffffff);super.render(g,x,y,dt);g.drawCenteredString(font,message.isEmpty()?"Move the gun relative to your hand; normal grip calibration is unchanged.":message,width/2,height-12,0xffffff);}
}
