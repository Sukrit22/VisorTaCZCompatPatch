package dev.visorcompat.tacz.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import dev.visorcompat.tacz.Profiles;

public final class ControlsScreen extends Screen {
    public ControlsScreen(){super(Component.literal("TaCZ VR controls"));}
    @Override protected void init(){
        int x=width/2-130,y=height/2-96;
        addRenderableWidget(Button.builder(Component.literal("Grab: "+CompatSettings.grab()),b->{
            CompatSettings.setGrab(CompatSettings.grab()==CompatSettings.Grab.USE?CompatSettings.Grab.TRIGGER:CompatSettings.Grab.USE);rebuildWidgets();
        }).bounds(x,y,260,20).build());
        addRenderableWidget(Button.builder(Component.literal("Ammo / ADS panel: "+CompatSettings.display()),b->{
            var all=CompatSettings.Display.values();CompatSettings.setDisplay(all[(CompatSettings.display().ordinal()+1)%all.length]);rebuildWidgets();
        }).bounds(x,y+24,260,20).build());
        addRenderableWidget(Button.builder(Component.literal("Calibrate gun"),b->{
            if(minecraft.player!=null && Profiles.get(minecraft.player.getMainHandItem())!=null)
                minecraft.setScreen(new CalibrationScreen(Profiles.key(minecraft.player.getMainHandItem())));
        }).bounds(x,y+48,128,20).build());
        addRenderableWidget(Button.builder(Component.literal("Reload calibration"),b->{
            b.setMessage(Component.literal(CompatSettings.reloadCalibration()?"Reloaded":"Reload failed"));
        }).bounds(x+132,y+48,128,20).build());
        addRenderableWidget(Button.builder(Component.literal("Handling: "+(CompatSettings.physical()?"PHYSICAL":"BUTTONS")),b->{
            CompatSettings.setPhysical(!CompatSettings.physical());rebuildWidgets();
        }).bounds(x,y+72,260,20).build());
        addRenderableWidget(Button.builder(Component.literal("Auto ADS: "+CompatSettings.autoAds()),b->{
            CompatSettings.setAutoAds(!CompatSettings.autoAds());rebuildWidgets();
        }).bounds(x,y+96,260,20).build());
        addRenderableWidget(Button.builder(Component.literal("VR optics: "+CompatSettings.optics()),b->{
            CompatSettings.setOptics(!CompatSettings.optics());rebuildWidgets();
        }).bounds(x,y+120,260,20).build());
        addRenderableWidget(Button.builder(Component.literal("Debug cubes: "+(CompatSettings.debugCubes()?"ON":"OFF")),b->{
            CompatSettings.setDebugCubes(!CompatSettings.debugCubes());rebuildWidgets();
        }).bounds(x,y+144,260,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(x,y+172,260,20).build());
        ClientControls.clearInput();
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(width/2-140,height/2-118,width/2+140,height/2+104,0xD0101010);
        g.drawCenteredString(font,title,width/2,height/2-111,0xFFFFFF);super.render(g,x,y,partial);
    }
}
