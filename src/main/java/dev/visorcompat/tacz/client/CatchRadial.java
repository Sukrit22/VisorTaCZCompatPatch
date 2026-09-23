package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.physical.CatchWindow;
import net.minecraft.client.Minecraft;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayRadialSelector;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;

/** Visor 0.5.0 adapter: show its radial without starting its slot-changing task yet. */
final class CatchRadial {
    private static final CatchWindow[] WINDOWS={new CatchWindow(),new CatchWindow()};
    private static final boolean[] consumed={false,false};
    private static int index(HandType h){return h==HandType.MAIN?0:1;}
    private static VROverlayRadialSelector overlay(HandType h){
        if(VisorAPI.client()==null||VisorAPI.client().getGuiManager()==null||VisorAPI.client().getGuiManager().getOverlayManager()==null)return null;
        return VisorAPI.client().getGuiManager().getOverlayManager().getOverlay(h==HandType.MAIN?"hotbar_mainhand":"hotbar_offhand",VROverlayRadialSelector.class);
    }
    static boolean pending(){return WINDOWS[0].pending()||WINDOWS[1].pending();}
    static boolean start(HandType h){
        if(CompatSettings.catchWindow()==0||TaskHotBar.getInstance()==null)return false;
        var radial=overlay(h);if(radial==null)return false;
        // Do not take over a radial that Visor already owns.
        if(radial.isEnabled())return false;
        int i=index(h);WINDOWS[i].start(System.nanoTime());consumed[i]=true;radial.setSelectedSlice(-1);radial.setEnabled(true);return true;
    }
    static boolean release(HandType h){
        int i=index(h);if(!consumed[i])return false;
        consumed[i]=false;boolean provisional=WINDOWS[i].pending();WINDOWS[i].cancel();var radial=overlay(h);
        if(radial!=null){
            int selected=radial.getSelectedSlice();radial.setEnabled(false);
            // Early release explicitly confirms the provisional selection, without replaying Grip.
            var mc=Minecraft.getInstance();
            if(provisional&&mc.player!=null&&mc.screen==null&&VisorAPI.clientState().stateMode().isFocused()&&selected>=0&&selected<9){
                if(h==HandType.MAIN)mc.player.getInventory().selected=selected;
                else VisorAPI.client().getVRLocalPlayer().setOffhandSlot(selected);
            }
        }return true;
    }
    static boolean poll(HandType h,boolean eligible,boolean near){
        int i=index(h);if(!WINDOWS[i].pending())return false;
        var result=WINDOWS[i].poll(System.nanoTime(),CompatSettings.catchWindow(),eligible,near);
        if(result==CatchWindow.Result.WAIT)return false;
        var radial=overlay(h);
        if(result==CatchWindow.Result.HOTBAR){
            consumed[i]=false;
            // The original press was intercepted. Begin Visor's real task now; its ordinary
            // release event will close it. No forced press/release or held-button re-trigger.
            var task=TaskHotBar.getInstance();if(task!=null){if(h==HandType.MAIN)task.setInputPressedMain(true);else task.setInputPressedOffhand(true);}
            else if(radial!=null)radial.setEnabled(false);
        }else{
            if(radial!=null)radial.setEnabled(false);
            if(result==CatchWindow.Result.CATCH){consumed[i]=false;return true;}
        }
        return false;
    }
    static void cancel(){
        for(var h:HandType.values()){
            int i=index(h);if(WINDOWS[i].pending()){WINDOWS[i].cancel();var radial=overlay(h);if(radial!=null)radial.setEnabled(false);}
        }
    }
    static void sessionReset(){
        for(var h:HandType.values()){
            WINDOWS[index(h)].cancel();consumed[index(h)]=false;
            var radial=overlay(h);if(radial!=null){radial.setSelectedSlice(-1);radial.setEnabled(false);}
        }
        var task=TaskHotBar.getInstance();if(task!=null){
            task.setInputPressedMain(false);task.setInputPressedOffhand(false);
            if(overlay(HandType.MAIN)!=null&&overlay(HandType.OFFHAND)!=null)task.clear(Minecraft.getInstance().player);
        }
    }
}
