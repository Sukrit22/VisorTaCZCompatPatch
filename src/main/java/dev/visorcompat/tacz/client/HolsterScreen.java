package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
public final class HolsterScreen extends Screen {
    private final String key;private final boolean pistol;private final float[] values=new float[6];private String message="";
    private boolean coarse;private boolean dual,off;private int mount=-1;
    private HolsterCalibration lastSaved;
    public HolsterScreen(String key,boolean pistol){super(Component.literal("Holster: "+key.split("\\|")[0]));this.key=key;this.pistol=pistol;set(HolsterStore.get(key,pistol));}
    public HolsterScreen(String key,boolean pistol,boolean off){this(key,pistol);this.dual=true;this.off=off;HolsterStore.preview=null;HolsterStore.previewKey=null;set(DualHolsterStore.get(key,pistol,off));}
    public HolsterScreen(String key,int mount){this(key,mount<2);this.mount=mount;HolsterStore.preview=null;HolsterStore.previewKey=null;set(FourMountStore.get(key,mount));}
    private void set(HolsterCalibration c){values[0]=c.x();values[1]=c.y();values[2]=c.z();values[3]=c.pitch();values[4]=c.yaw();values[5]=c.roll();preview();}
    private HolsterCalibration value(){return new HolsterCalibration(values[0],values[1],values[2],values[3],values[4],values[5]);}
    private void preview(){if(mount>=0){FourMountStore.previewKey=FourMountStore.key(key,mount);FourMountStore.preview=value();}else if(dual){DualHolsterStore.previewKey=DualHolsterStore.key(key,pistol,off);DualHolsterStore.preview=value();}else{HolsterStore.previewKey=key;HolsterStore.preview=value();}}
    @Override protected void init(){int x=width/2-150,y=Math.max(24,height/2-100);String[] names={mount>=0?(mount==1?"Side (+ left)":"Side (+ right)"):dual&&!pistol?"Side (+ dominant shoulder)":"Side (+ toward gun hand)","Y from eyes","Depth (- forward)","Pitch","Yaw","Roll"};
        for(int i=0;i<6;i++){final int n=i;int row=y+i*23;
            addRenderableWidget(Button.builder(Component.literal("-"),b->adjust(n,-1)).bounds(x,row,28,20).build());
            addRenderableWidget(Button.builder(Component.literal(names[i]+": "+String.format(java.util.Locale.ROOT,i<3?"%.2f m":"%.0f deg",i==1?values[i]-1.62f:values[i])),b->{}).bounds(x+32,row,236,20).build());
            addRenderableWidget(Button.builder(Component.literal("+"),b->adjust(n,1)).bounds(x+272,row,28,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Defaults"),b->{set(mount>=0?FourMountStore.defaults(key,mount):dual?DualHolsterStore.defaults(key,pistol,off):HolsterCalibration.defaults(pistol));message="";rebuildWidgets();}).bounds(x,y+141,65,20).build());
        addRenderableWidget(Button.builder(Component.literal("Recenter"),b->AdvancedClient.recenterHolster()).bounds(x+69,y+141,72,20).build());
        addRenderableWidget(Button.builder(Component.literal(coarse?"Coarse: 10 cm / 10 deg":"Fine: 1 cm / 1 deg"),b->{coarse=!coarse;rebuildWidgets();}).bounds(x+145,y+141,155,20).build());
        addRenderableWidget(Button.builder(Component.literal(value().equals(lastSaved)?"Close":"Save"),b->{
            if(value().equals(lastSaved)){onClose();return;}
            try{var next=value();if(mount>=0)FourMountStore.save(key,mount,next);else if(dual)DualHolsterStore.save(key,pistol,off,next);else HolsterStore.save(key,next);lastSaved=next;message=mount>=0?"Saved to visor_tacz-four-holsters.json":dual?"Saved to visor_tacz-dual-holsters.json":"Saved to visor_tacz-holsters.json";rebuildWidgets();}catch(java.io.IOException ex){message=ex.getMessage();}
        }).bounds(x,y+165,value().equals(lastSaved)?300:145,20).build());
        if(!value().equals(lastSaved))addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Discard edits since the last save. Already saved changes remain on disk."))).bounds(x+155,y+165,145,20).build());
    }
    private void adjust(int i,int direction){float old=values[i];values[i]+=direction*(i<3?(coarse?.1f:.01f):(coarse?10f:1f));if(!value().valid())values[i]=old;else message="";preview();rebuildWidgets();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void removed(){FourMountStore.preview=null;FourMountStore.previewKey=null;HolsterStore.preview=null;HolsterStore.previewKey=null;DualHolsterStore.preview=null;DualHolsterStore.previewKey=null;}
    @Override public void onClose(){minecraft.setScreen(mount>=0?new FourHolsterScreen():dual?new DualHolsterScreen():new AdvancedScreen());}
    @Override public void render(GuiGraphics g,int x,int y,float dt){g.drawCenteredString(font,mount>=0?HolsterAssignments.Mount.values()[mount]+" holster":dual?(off?"Offhand ":"Main ")+(pistol?"pistol holster":"long-gun holster"):title.getString(),width/2,8,0xffffff);super.render(g,x,y,dt);g.drawCenteredString(font,message.isEmpty()?"Look forward, Recenter; look down to inspect the holster.":message,width/2,height-12,0xffffff);}
}
