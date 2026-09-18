package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class CalibrationScreen extends Screen {
    private final String key;
    private final float[] values;
    private final Button[] readouts = new Button[6];
    private boolean coarse;
    private int page;
    private static final String[] PAGES={"Grip","Bullet origin","Foregrip","Magazine","Slide / charging handle","Fire selector","Reload pouch","Sight alignment","Ejection port / casing grab"};
    private int start(){return page==0?0:page==1?6:9+(page-2)*3;}
    private int count(){return page==0?6:3;}
    private String error = "";
    private static final String[] LABELS = {"X right", "Y up", "Z back", "Pitch X", "Yaw Y", "Roll Z", "Muzzle X right", "Muzzle Y up", "Muzzle Z back"};
    public CalibrationScreen(String key) {
        super(Component.literal("TaCZ VR grip calibration"));
        this.key = key;
        if (CalibrationStore.error() != null) error = CalibrationStore.error();
        Calibration c = CalibrationStore.get(key);
        values = new float[30];float[] base={c.x(),c.y(),c.z(),c.pitch(),c.yaw(),c.roll(),c.muzzleX(),c.muzzleY(),c.muzzleZ()};
        System.arraycopy(base,0,values,0,9);int n=9;for(var point:c.interactions().points()){values[n++]=point.x();values[n++]=point.y();values[n++]=point.z();}
    }
    private Calibration value() {
        var p=new InteractionOffsets.Point[7];for(int i=0;i<7;i++)p[i]=new InteractionOffsets.Point(values[9+i*3],values[10+i*3],values[11+i*3]);
        return new Calibration(values[0],values[1],values[2],values[3],values[4],values[5],values[6],values[7],values[8],new InteractionOffsets(p[0],p[1],p[2],p[3],p[4],p[5],p[6]));
    }
    private void preview() { CalibrationStore.previewKey = key; CalibrationStore.preview = value(); }
    @Override protected void init() {
        int left = width/2-150, top = height/2-75;
        java.util.Arrays.fill(readouts,null);
        addRenderableWidget(Button.builder(Component.literal((key.startsWith("tacz:m870|") && page==3?"Shell loading port":key.startsWith("tacz:m870|") && page==4?"Pump grip":PAGES[page])+" (click for next)"), b -> {
            page=(page+1)%PAGES.length; rebuildWidgets();
        }).bounds(left,top-20,300,18).build());
        for (int i=0;i<count();i++) {
            final int axis=start()+i;
            int y=top+i*23;
            addRenderableWidget(Button.builder(Component.literal("-"), b -> adjust(axis,-1)).bounds(left,y,35,20).build());
            readouts[i] = addRenderableWidget(Button.builder(Component.empty(), b -> {}).bounds(left+40,y,220,20).build());
            readouts[i].active=false;
            addRenderableWidget(Button.builder(Component.literal("+"), b -> adjust(axis,1)).bounds(left+265,y,35,20).build());
        }
        int y=top+142;
        addRenderableWidget(Button.builder(Component.literal("Fine / coarse"), b -> {coarse=!coarse; update();}).bounds(left,y,145,20).build());
        addRenderableWidget(Button.builder(Component.literal("Zero this page"), b -> {java.util.Arrays.fill(values,start(),start()+count(),0);update();}).bounds(left+155,y,145,20).build());
        addRenderableWidget(Button.builder(Component.literal("Save & close"), b -> {
            try { CalibrationStore.save(key,value()); CompatSettings.syncState(); onClose(); }
            catch (Exception e) { error="Save failed: " + e.getMessage(); }
        }).bounds(left,y+24,145,20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose()).bounds(left+155,y+24,145,20).build());
        update();
        ClientControls.clearInput();
    }
    private void adjust(int axis, int sign) {
        float step = (axis<3 || axis>=6) ? (coarse?.01f:.001f) : (coarse?10:1);
        float limit = axis>=9?.5f:(axis<3 || axis>=6)?.25f:180;
        values[axis]=Math.max(-limit,Math.min(limit,values[axis]+step*sign));
        update();
    }
    private void update() {
        preview();
        for (int i=0;i<6;i++) if (readouts[i]!=null) {
            int axis=start()+i;
            boolean translation=axis<3 || axis>=6;
            readouts[i].setMessage(Component.literal(String.format(Locale.ROOT,"%s: %.1f %s",
                (axis<9?LABELS[axis]:new String[]{"X right","Y up","Z back"}[(axis-9)%3]),translation?values[axis]*1000:values[axis],translation?"mm":"deg")));
        }
    }
    @Override public void render(GuiGraphics graphics,int x,int y,float partial) {
        // Keep the world visible for headset alignment. Use the offhand to operate UI.
        graphics.fill(width/2-158,height/2-126,width/2+158,height/2+115,0xB0101010);
        graphics.drawCenteredString(font,title,width/2,height/2-119,0xFFFFFF);
        graphics.drawCenteredString(font,key,width/2,height/2-106,0xAAAAAA);
        if (page==1) {
            graphics.drawCenteredString(font,"Cyan marker = bullet origin",width/2,height/2+5,0x55FFFF);
            graphics.drawCenteredString(font,"Negative Z moves toward barrel tip",width/2,height/2+20,0xFFFFFF);
            graphics.drawCenteredString(font,"Gun position and aim stay unchanged",width/2,height/2+35,0xAAAAAA);
        }
        graphics.drawCenteredString(font,coarse?"Step: 10 mm / 10 degrees":"Step: 1 mm / 1 degree",width/2,height/2+95,0xFFFFAA);
        if (!error.isEmpty()) graphics.drawString(font,error,5,5,0xFF6666);
        super.render(graphics,x,y,partial);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void removed() { CalibrationStore.previewKey=null; CalibrationStore.preview=null; ClientControls.clearInput(); }
}
