package dev.visorcompat.tacz.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
public final class AdvancedScreen extends Screen {
    private int page;
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
        addRenderableWidget(Button.builder(Component.literal("Independent dual wield: "+(CompatSettings.dualWield()?"ON":"OFF")),b->{CompatSettings.setDualWield(!CompatSettings.dualWield());rebuildWidgets();}).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Experimental: assign two guns with Visor. Grip draws; release holsters; Trigger fires; Use reloads. Physical dual Glock is experimental; other guns use Button handling."))).bounds(x,y+192,300,20).build());
        addRenderableWidget(Button.builder(Component.literal("Four holsters / assignment rules"),b->minecraft.setScreen(new FourHolsterScreen())).bounds(x,y+216,300,20).build());
        // Visor can provide a shorter GUI canvas than the desktop. Paginate rows,
        // reserving navigation and Back at the bottom rather than letting them overflow.
        var buttons=children().stream().filter(v->v instanceof Button).map(v->(Button)v).toList();
        var rows=buttons.stream().map(Button::getY).distinct().sorted().toList();
        int capacity=Math.max(1,(height-84)/24),count=Math.max(1,(rows.size()+capacity-1)/capacity);
        page=Math.min(page,count-1);int panelWidth=Math.min(300,width-24),left=(width-panelWidth)/2;
        for(var button:buttons){int row=rows.indexOf(button.getY());button.visible=row/capacity==page;
            button.setX(left+(button.getX()-x)*panelWidth/300);button.setWidth(button.getWidth()*panelWidth/300);
            button.setY(28+(row%capacity)*24);
        }
        var previous=addRenderableWidget(Button.builder(Component.literal("< Previous"),b->{page--;rebuildWidgets();}).bounds(left,height-50,panelWidth/2-2,20).build());previous.active=page>0;
        var next=addRenderableWidget(Button.builder(Component.literal("Next > "+(page+1)+"/"+count),b->{page++;rebuildWidgets();}).bounds(left+panelWidth/2+2,height-50,panelWidth/2-2,20).build());next.active=page<count-1;
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(new ControlsScreen())).bounds(left,height-26,panelWidth,20).build());
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int x,int y,float dt){
        renderBackground(g);g.drawCenteredString(font,title,width/2,8,0xffffff);
        super.render(g,x,y,dt);
    }
}
