package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
/** A visual calibration sandbox: it never ejects or consumes inventory ammo. */
public final class PhysicsScreen extends Screen {
    public enum Preview {SEATED,EXIT,SIMULATE,SLIDE_CLOSED,SLIDE_REAR}
    private final String key;private final WeaponProfile profile;private final float[] v=new float[11];private PhysicsCalibration lastSaved;
    private int page,token;private boolean coarse;private Preview mode=Preview.SEATED;private String message="";
    public PhysicsScreen(String key){super(Component.literal("Physics: "+key.split("\\|")[0]));this.key=key;profile=Profiles.get(net.minecraft.client.Minecraft.getInstance().player.getMainHandItem());set(PhysicsStore.get(key,profile));}
    public String key(){return key;}public Preview previewMode(){return mode;}public int resetToken(){return token;}
    private void set(PhysicsCalibration p){float[] a={p.offsetX(),p.offsetY(),p.offsetZ(),p.pitch(),p.roll(),p.travel(),p.width(),p.height(),p.depth(),p.friction(),p.slideTravel()};System.arraycopy(a,0,v,0,11);preview();}
    private PhysicsCalibration value(){return new PhysicsCalibration(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10]);}
    private void preview(){PhysicsStore.previewKey=key;PhysicsStore.preview=value();token++;}
    @Override protected void init(){int x=width/2-150,y=Math.max(24,height/2-95);String[] titles={"Magwell / seated origin","Exit axis and clearance","Magazine measuring bounds","Sliding resistance","Slide visual travel"};
        addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.floorMod(page-1,5);rebuildWidgets();}).bounds(x,y,25,20).build());
        addRenderableWidget(Button.builder(Component.literal(titles[page]),b->{page=(page+1)%5;rebuildWidgets();}).bounds(x+29,y,242,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{page=(page+1)%5;rebuildWidgets();}).bounds(x+275,y,25,20).build());
        int start=new int[]{0,3,6,9,10}[page],count=page<3?3:1;String[] labels={"X right","Y up","Z back","Exit pitch (deg)","Exit roll (deg)","Clearance (m)","Width (m)","Height (m)","Depth (m)","Friction (m/s2)","Slide stroke (m)"};
        for(int i=0;i<count;i++){final int n=start+i;int row=y+25+i*24;
            addRenderableWidget(Button.builder(Component.literal("-"),b->adjust(n,-1)).bounds(x,row,30,20).build());
            var read=addRenderableWidget(Button.builder(Component.literal(labels[n]+": "+String.format(java.util.Locale.ROOT,"%.3f",v[n])),b->{}).bounds(x+34,row,232,20).build());read.active=false;
            addRenderableWidget(Button.builder(Component.literal("+"),b->adjust(n,1)).bounds(x+270,row,30,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Preview: "+mode),b->{var a=Preview.values();mode=a[(mode.ordinal()+1)%a.length];token++;rebuildWidgets();}).bounds(x,y+101,220,20).build());
        addRenderableWidget(Button.builder(Component.literal("Restart"),b->token++).bounds(x+224,y+101,76,20).build());
        addRenderableWidget(Button.builder(Component.literal(coarse?"Coarse":"Fine"),b->{coarse=!coarse;rebuildWidgets();}).bounds(x,y+125,145,20).build());
        addRenderableWidget(Button.builder(Component.literal("Defaults"),b->{set(PhysicsCalibration.defaults(profile));rebuildWidgets();}).bounds(x+155,y+125,145,20).build());
        boolean saved=value().equals(lastSaved);
        addRenderableWidget(Button.builder(Component.literal(saved?"Close":"Save"),b->{if(value().equals(lastSaved)){onClose();return;}try{lastSaved=value();PhysicsStore.save(key,lastSaved);CompatSettings.forceSync();message="Saved visor_tacz-physics.json";rebuildWidgets();}catch(java.io.IOException ex){lastSaved=null;message=ex.getMessage();}}).bounds(x,y+150,saved?300:145,20).build());
        if(!saved)addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(x+155,y+150,145,20).build());ClientControls.clearInput();
    }
    private void adjust(int n,int sign){float old=v[n];v[n]+=sign*(n==3||n==4?(coarse?10:1):n==9?(coarse?1:.1f):(coarse?.01f:.001f));if(!value().valid())v[n]=old;preview();rebuildWidgets();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void removed(){PhysicsStore.preview=null;PhysicsStore.previewKey=null;}
    @Override public void onClose(){minecraft.setScreen(new ControlsScreen());}
    @Override public void render(GuiGraphics g,int x,int y,float dt){g.drawCenteredString(font,title,width/2,8,0xffffff);super.render(g,x,y,dt);String hint=page==4?"Visual stroke only; gesture completion stays unchanged.":page==2?"White: magazine bounds. Cyan: exit path. Not a grab box.":"Clearance is how far it must slide before leaving the magwell.";g.drawCenteredString(font,message.isEmpty()?hint:message,width/2,height-13,0xffffff);}
}
