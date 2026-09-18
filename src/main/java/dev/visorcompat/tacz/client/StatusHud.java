package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.VisorTacz;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** A named Forge overlay, allowing GUI capture addons to handle placement. */
@Mod.EventBusSubscriber(modid=VisorTacz.ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class StatusHud {
    private StatusHud() {}
    @SubscribeEvent public static void register(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("ammo_ads",(gui,graphics,partial,width,height)->{
            var mc=Minecraft.getInstance();
            if(CompatSettings.display()!=CompatSettings.Display.HUD || !ClientControls.vrActive()
                    || !ClientControls.supported() || mc.screen!=null || mc.options.hideGui
                    || com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow())return;
            String[] lines=StatusText.lines();
            int panelWidth=0,count=0;
            for(String line:lines)if(!line.isEmpty()){panelWidth=Math.max(panelWidth,mc.font.width(line));count++;}
            if(count==0)return;
            int x=8,y=8;
            graphics.fill(x-4,y-4,x+panelWidth+4,y+count*11+2,0xD0101010);
            for(int i=0;i<lines.length;i++)if(!lines[i].isEmpty()){
                graphics.drawString(mc.font,lines[i],x,y,i==1&&AutoAds.aiming()?0xFF66FF88:0xFFFFFFFF,false);
                y+=11;
            }
        });
    }
}
