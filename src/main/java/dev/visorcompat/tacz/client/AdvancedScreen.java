package dev.visorcompat.tacz.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
public final class AdvancedScreen extends Screen {
    public AdvancedScreen(){super(Component.literal("Advanced Handling (experimental)"));}
    @Override protected void init(){int x=width/2-150,y=Math.max(26,height/2-126);
        addRenderableWidget(Button.builder(Component.literal("Handling: "+CompatSettings.handlingMode()),b->{CompatSettings.cycleHandling();rebuildWidgets();}).bounds(x,y,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Release: "+(CompatSettings.toss()?"Toss / catch, then auto-holster":"Auto-holster")),b->{CompatSettings.setToss(!CompatSettings.toss());rebuildWidgets();}).bounds(x,y+24,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Inspection grace: "+CompatSettings.inspectWindow()+" ms"),b->{int n=CompatSettings.inspectWindow();CompatSettings.setInspectWindow(n==50?100:n==100?250:50);rebuildWidgets();}).bounds(x,y+48,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Catch grace: "+CompatSettings.catchWindow()+" ms"),b->{int n=CompatSettings.catchWindow();CompatSettings.setCatchWindow(n==0?100:n==100?250:n==250?500:0);rebuildWidgets();}).bounds(x,y+72,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Return gun to holster"),b->{DualClient.reset();AdvancedClient.reset();}).bounds(x,y+96,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Calibrate holster"),b->{if(CompatSettings.dualWield()){minecraft.setScreen(new DualHolsterScreen());return;}if(minecraft.player!=null){var p=dev.visorcompat.tacz.Profiles.get(minecraft.player.getMainHandItem());if(p!=null)minecraft.setScreen(new HolsterScreen(dev.visorcompat.tacz.Profiles.key(minecraft.player.getMainHandItem()),p.supportDistance()==0));}}).bounds(x,y+120,198,20).build());
        addRenderableWidget(Button.builder(Component.literal("Recenter"),b->AdvancedClient.recenterHolster()).bounds(x+202,y+120,98,20).build());
        addRenderableWidget(Button.builder(Component.literal("Apply bundled calibration to this gun"),b->{
            if(minecraft.player==null)return;String key=dev.visorcompat.tacz.Profiles.key(minecraft.player.getMainHandItem());
            var c=dev.visorcompat.tacz.CalibrationDefaults.load().get(key);if(c==null)return;
            try{CalibrationStore.save(key,c);CompatSettings.forceSync();b.setMessage(Component.literal("Applied: bundled calibration"));}catch(java.io.IOException ex){b.setMessage(Component.literal("Failed: "+ex.getMessage()));}
        }).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Replaces this gun's saved calibration with the new bundled profile. Other guns are unchanged."))).bounds(x,y+144,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Calibrate inspection grip"),b->{
            if(minecraft.player!=null){var p=dev.visorcompat.tacz.Profiles.get(minecraft.player.getMainHandItem());if(p!=null&&p.supportDistance()==0&&!p.manualAction())minecraft.setScreen(new InspectionScreen(dev.visorcompat.tacz.Profiles.key(minecraft.player.getMainHandItem()),true));}
        }).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Magazine pistols: adjust inspection position and rotation with live preview."))).bounds(x,y+168,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Independent dual wield: "+(CompatSettings.dualWield()?"ON":"OFF")),b->{CompatSettings.setDualWield(!CompatSettings.dualWield());rebuildWidgets();}).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Experimental: assign two guns with Visor. Grip draws; release holsters; Trigger fires; Use reloads. Physical reloads and charged guns are not supported here yet."))).bounds(x,y+192,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Four holsters / assignment rules"),b->minecraft.setScreen(new FourHolsterScreen())).bounds(x,y+216,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(new ControlsScreen())).bounds(x,y+240,300,20).build());
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int x,int y,float dt){
        renderBackground(g);g.drawCenteredString(font,title,width/2,8,0xffffff);
        super.render(g,x,y,dt);
    }
}
