package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class CalibrationScreen extends Screen {
    private final String key;
    private final float[] values=new float[52];
    private final List<CalibrationLayout.Page> pages;
    private final Button[] readouts=new Button[3];
    private boolean coarse,size,scaleLock;
    private int page,top;
    private String error="";
    public CalibrationScreen(String key) {
        super(Component.literal("TaCZ VR calibration"));this.key=key;pages=CalibrationLayout.pages(key);
        if(CalibrationStore.error()!=null)error=CalibrationStore.error();
        var c=CalibrationStore.get(key);scaleLock=c.scaleLock();
        float[] base={c.x(),c.y(),c.z(),c.pitch(),c.yaw(),c.roll(),c.muzzleX(),c.muzzleY(),c.muzzleZ()};
        System.arraycopy(base,0,values,0,9);int n=9;
        for(var p:c.interactions().points()){values[n++]=p.x();values[n++]=p.y();values[n++]=p.z();}
        values[30]=c.gunScale();n=31;
        for(var box:c.zones().boxes()){values[n++]=box.width();values[n++]=box.height();values[n++]=box.depth();}
    }
    private CalibrationLayout.Page current(){return pages.get(page);}
    private int start(){return size?31+current().zone().ordinal()*3:current().offset();}
    private int count(){return size?3:current().count();}
    private Calibration value(){
        var p=new InteractionOffsets.Point[7];var b=new ZoneSizes.Box[7];
        for(int i=0;i<7;i++){
            p[i]=new InteractionOffsets.Point(values[9+i*3],values[10+i*3],values[11+i*3]);
            b[i]=new ZoneSizes.Box(values[31+i*3],values[32+i*3],values[33+i*3]);
        }
        return new Calibration(values[0],values[1],values[2],values[3],values[4],values[5],values[6],values[7],values[8],
            new InteractionOffsets(p[0],p[1],p[2],p[3],p[4],p[5],p[6]),values[30],new ZoneSizes(b[0],b[1],b[2],b[3],b[4],b[5],b[6]),scaleLock);
    }
    private void changePage(int direction){page=Math.floorMod(page+direction,pages.size());size=false;rebuildWidgets();}
    @Override protected void init(){
        int left=width/2-150;top=Math.max(8,height/2-108);Arrays.fill(readouts,null);
        var hint=CalibrationHints.get(key,current().id());
        addRenderableWidget(Button.builder(Component.literal("<"),b->changePage(-1)).bounds(left,top+14,24,20).build());
        addRenderableWidget(Button.builder(Component.literal(hint.title()),b->changePage(1))
            .tooltip(Tooltip.create(Component.literal(hint.text()))).bounds(left+28,top+14,244,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->changePage(1)).bounds(left+276,top+14,24,20).build());
        if(current().zone()!=null)addRenderableWidget(Button.builder(Component.literal(size?"Editing: box width / height / depth":"Editing: position (click for box size)"),b->{size=!size;rebuildWidgets();}).bounds(left,top+37,300,20).build());
        if(current().offset()==30)addRenderableWidget(Button.builder(Component.literal(scaleLock?"Scale link: LOCKED":"Scale link: UNLOCKED"),b->{scaleLock=!scaleLock;rebuildWidgets();}).tooltip(Tooltip.create(Component.literal("When locked, changing gun scale also resizes gun interaction boxes. Pouch stays independent."))).bounds(left,top+37,300,20).build());
        for(int i=0;i<count();i++){
            int axis=start()+i,y=top+60+i*23;
            addRenderableWidget(Button.builder(Component.literal("-"),b->adjust(axis,-1)).bounds(left,y,35,20).build());
            readouts[i]=addRenderableWidget(Button.builder(Component.empty(),b->{}).bounds(left+40,y,220,20).build());readouts[i].active=false;
            addRenderableWidget(Button.builder(Component.literal("+"),b->adjust(axis,1)).bounds(left+265,y,35,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal(coarse?"Coarse steps":"Fine steps"),b->{coarse=!coarse;rebuildWidgets();}).bounds(left,top+169,145,20).build());
        addRenderableWidget(Button.builder(Component.literal(size?"Default box size":current().offset()==30?"Reset to 100%":"Zero this page"),b->{
            if(size){var box=ZoneSizes.DEFAULT.get(current().zone());values[start()]=box.width();values[start()+1]=box.height();values[start()+2]=box.depth();}
            else if(current().offset()==30)scaleTo(1);else Arrays.fill(values,start(),start()+count(),0);update();
        }).bounds(left+155,top+169,145,20).build());
        addRenderableWidget(Button.builder(Component.literal("Save & close"),b->{try{CalibrationStore.save(key,value());CompatSettings.syncState();onClose();}catch(Exception e){error="Save failed: "+e.getMessage();}}).bounds(left,top+193,145,20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(left+155,top+193,145,20).build());
        update();ClientControls.clearInput();
    }
    private void adjust(int axis,int sign){
        boolean rotation=axis>=3&&axis<6,scale=axis==30;
        float step=scale?(coarse?.05f:.01f):rotation?(coarse?10:1):(coarse?.01f:.001f);
        float min=scale?.5f:axis>=31?.01f:rotation?-180:axis>=9?-.5f:-.25f;
        float max=scale?1.5f:axis>=31?1.5f:rotation?180:axis>=9?.5f:.25f;
        float next=Math.max(min,Math.min(max,values[axis]+step*sign));
        if(scale)scaleTo(next);else values[axis]=next;update();
    }
    private void scaleTo(float next){
        if(scaleLock){int i=31;for(var box:value().zones().resizeMounted(next/values[30]).boxes()){
            values[i++]=box.width();values[i++]=box.height();values[i++]=box.depth();
        }}
        values[30]=next;
    }
    private void update(){
        CalibrationStore.previewKey=key;CalibrationStore.preview=value();
        for(int i=0;i<count();i++)if(readouts[i]!=null){
            int axis=start()+i;boolean rotation=axis>=3&&axis<6,scale=axis==30;
            String label=size?new String[]{"Width X","Height Y","Depth Z"}[i]:scale?"Gun size":rotation?new String[]{"Pitch","Yaw","Roll"}[i]:new String[]{"X right","Y up","Z back"}[i];
            readouts[i].setMessage(Component.literal(String.format(Locale.ROOT,"%s: %.1f %s",label,values[axis]*(scale?100:rotation?1:1000),scale?"%":rotation?"deg":"mm")));
        }
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(width/2-158,top-5,width/2+158,top+218,0xB0101010);
        g.drawCenteredString(font,title,width/2,top+1,0xFFFFFF);
        var hint=CalibrationHints.get(key,current().id());var lines=font.split(Component.literal(hint.text()),294);
        for(int i=0;i<Math.min(3,lines.size());i++)g.drawString(font,lines.get(i),width/2-147,top+132+i*10,hint.color());
        if(!error.isEmpty())g.drawString(font,error,5,Math.max(0,top-15),0xFF6666);
        super.render(g,x,y,partial);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void removed(){CalibrationStore.previewKey=null;CalibrationStore.preview=null;ClientControls.clearInput();}
}
