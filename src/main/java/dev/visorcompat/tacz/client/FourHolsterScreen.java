package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
public final class FourHolsterScreen extends Screen {
 public FourHolsterScreen(){super(Component.literal("Four holsters (experimental)"));}
 @Override protected void init(){int x=width/2-155,y=Math.max(28,height/2-108);
  addRenderableWidget(Button.builder(Component.literal("Four holsters: "+(FourHolsterSettings.enabled()?"ON":"OFF")),b->{FourHolsterSettings.enable(!FourHolsterSettings.enabled());if(FourHolsterSettings.enabled()&&!CompatSettings.dualWield())CompatSettings.setDualWield(true);rebuildWidgets();}).bounds(x,y,310,20).build());y+=23;
  String[] orders={"1 -> 9","9 -> 1","Manual"};addRenderableWidget(Button.builder(Component.literal("Assignment: "+orders[FourHolsterSettings.order()]),b->{FourHolsterSettings.cycleOrder();FourHolsters.tick();rebuildWidgets();}).bounds(x,y,310,20).build());y+=23;
  addRenderableWidget(Button.builder(Component.literal("Require empty hand: "+FourHolsterSettings.emptyRequired()),b->{FourHolsterSettings.toggleEmpty();rebuildWidgets();}).bounds(x,y,310,20).build());y+=23;
  addRenderableWidget(Button.builder(Component.literal("Lock other's selected gun: "+FourHolsterSettings.lockSelected()),b->{FourHolsterSettings.toggleLock();rebuildWidgets();}).bounds(x,y,310,20).build());y+=23;
  FourHolsters.tick();
  for(int i=0;i<4;i++){final int m=i;String name=HolsterAssignments.Mount.values()[i].name();int slot=FourHolsters.slot(i);
   var assign=Button.builder(Component.literal(name+": "+(FourHolsterSettings.order()==2?(FourHolsterSettings.binding(i)<0?"disabled":"slot "+(FourHolsterSettings.binding(i)+1)):(slot<0?"empty":"slot "+(slot+1)))),b->{FourHolsterSettings.cycleBinding(m);FourHolsters.tick();rebuildWidgets();}).bounds(x,y,195,20).build();assign.active=FourHolsterSettings.order()==2;addRenderableWidget(assign);
   var edit=Button.builder(Component.literal("Calibrate"),b->minecraft.setScreen(new HolsterScreen(Profiles.key(FourHolsters.stack(m)),m))).bounds(x+200,y,110,20).build();edit.active=slot>=0;addRenderableWidget(edit);y+=23;
  }
  addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(new AdvancedScreen())).bounds(x,y+2,310,20).build());
 }
 @Override public boolean isPauseScreen(){return false;}
 @Override public void render(GuiGraphics g,int x,int y,float dt){renderBackground(g);g.drawCenteredString(font,title,width/2,8,0xffffff);super.render(g,x,y,dt);}
}
