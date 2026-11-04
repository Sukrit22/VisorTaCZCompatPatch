package dev.visorcompat.tacz.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class HipFireScreen extends Screen {
    public HipFireScreen(){super(Component.literal("VR hip-fire accuracy"));}
    @Override protected void init(){int x=width/2-140,y=height/2-45;
        addRenderableWidget(Button.builder(Component.literal("Native hip-fire spread"),b->set(false)).bounds(x,y,280,20).build());
        addRenderableWidget(Button.builder(Component.literal("ADS-equivalent hip-fire spread"),b->set(true)).bounds(x,y+24,280,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(x,y+75,280,20).build());ClientControls.clearInput();
    }
    private void set(boolean ads){if(minecraft.getConnection()!=null)minecraft.getConnection().sendCommand("visor_tacz_accuracy "+(ads?"ads":"native"));}
    @Override public void render(GuiGraphics g,int x,int y,float partial){g.fill(width/2-150,height/2-80,width/2+150,height/2+65,0xE0101010);g.drawCenteredString(font,title,width/2,height/2-70,0xFFFFFF);super.render(g,x,y,partial);g.drawCenteredString(font,"Recoil unchanged. Server permission required.",width/2,height/2+8,0xFFFFFF);g.drawCenteredString(font,"Single-player owner allowed; result appears in chat.",width/2,height/2+20,0xBBBBBB);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(new ControlsScreen());}
}
