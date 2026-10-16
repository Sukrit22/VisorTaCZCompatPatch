package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.Profiles;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.vmstudio.visor.api.common.HandType;
public final class DualHolsterScreen extends Screen {
 public DualHolsterScreen(){super(Component.literal("Dual holster calibration"));}
 @Override protected void init(){int x=width/2-150,y=height/2-35;
  for(var hand:HandType.values()){
   var stack=DualClient.stack(hand);var profile=Profiles.get(stack);boolean off=hand==HandType.OFFHAND;
   var button=Button.builder(Component.literal((off?"Offhand: ":"Main: ")+(profile==null?"Assign a gun in Visor":stack.getHoverName().getString())),b->minecraft.setScreen(new HolsterScreen(Profiles.key(stack),profile.supportDistance()==0,off))).bounds(x,y,300,20).build();button.active=profile!=null;addRenderableWidget(button);y+=26;
  }
  addRenderableWidget(Button.builder(Component.literal("Four holsters / assignment rules"),b->minecraft.setScreen(new FourHolsterScreen())).bounds(x,y+8,300,20).build());y+=26;
  addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(new AdvancedScreen())).bounds(x,y+8,300,20).build());
 }
 @Override public boolean isPauseScreen(){return false;}
 @Override public void render(GuiGraphics g,int x,int y,float dt){g.drawCenteredString(font,title,width/2,20,0xffffff);super.render(g,x,y,dt);}
}
