package dev.visorcompat.tacz.fabric.event;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
public final class RegisterGuiOverlaysEvent {
 @FunctionalInterface public interface Overlay {void render(Object gui,GuiGraphics graphics,float partial,int width,int height);}
 public void registerAboveAll(String id,Overlay overlay){HudRenderCallback.EVENT.register((graphics,partial)->{var mc=Minecraft.getInstance();overlay.render(mc.gui,graphics,partial.getGameTimeDeltaPartialTick(false),mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());});}
}

