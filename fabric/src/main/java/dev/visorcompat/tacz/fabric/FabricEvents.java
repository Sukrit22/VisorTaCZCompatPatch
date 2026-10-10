package dev.visorcompat.tacz.fabric;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import dev.visorcompat.tacz.client.*;
import dev.visorcompat.tacz.fabric.event.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
/** Explicit Fabric callback wiring; shared logic has no annotation-scanned event bus. */
public final class FabricEvents {
 private static ClientControls controls;
 public static void registerControls(ClientControls value){controls=value;}
 public static void register(){
  net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack,context,lines)->GunSupportTooltip.append(stack,lines));
  ClientCommandRegistrationCallback.EVENT.register((dispatcher,registry)->CompatSettings.commands(new RegisterClientCommandsEvent(dispatcher)));
  ClientPlayConnectionEvents.JOIN.register((handler,sender,client)->CompatSettings.login(new ClientPlayerNetworkEvent.LoggingIn()));
  ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{
   var event=new ClientPlayerNetworkEvent.LoggingOut();CompatSettings.logout(event);ClientRecoil.logout(event);HandlingEffects.logout(event);MagazineEffects.logout(event);RemoteGuns.logout(event);
  });
  var inputPhase=new net.minecraft.resources.ResourceLocation("visor_tacz","input");
  ClientTickEvents.START_CLIENT_TICK.addPhaseOrdering(inputPhase,net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE);
  ClientTickEvents.START_CLIENT_TICK.register(inputPhase,client->{
   var event=new TickEvent.ClientTickEvent(TickEvent.Phase.START);
   // Input ownership must be established before native TaCZ tick processing.
   if(controls!=null)controls.tick(event);CompatSettings.tick(event);ClientRecoil.tick(event);AutoAds.tick(event);
  });
  ClientTickEvents.END_CLIENT_TICK.register(client->{
   var event=new TickEvent.ClientTickEvent(TickEvent.Phase.END);
   CompatSettings.tick(event);ClientRecoil.tick(event);AutoAds.tick(event);
  });
  cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent.EVENT.register(nativeEvent->{
   if(nativeEvent.phase!=cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent.Phase.START)return;
   var event=new TickEvent.RenderTickEvent(TickEvent.Phase.START,nativeEvent.renderTickTime);
   MagazineEffects.frame(event);if(controls!=null)controls.animation(event);
  });
  WorldRenderEvents.AFTER_ENTITIES.register(context->{
   var event=new RenderLevelStageEvent(context.matrixStack(),context.camera(),context.tickDelta());
   RemoteGuns.render(event);HandlingEffects.render(event);MagazineEffects.render(event);
  });
  CoreShaderRegistrationCallback.EVENT.register(context->OpticRenderer.shaders(new RegisterShadersEvent(context)));
  StatusHud.register(new RegisterGuiOverlaysEvent());
  GunShootEvent.CALLBACK.register(event->{if(controls!=null)controls.beforeShot(event);});
  var feedbackPhase=new net.minecraft.resources.ResourceLocation("visor_tacz","feedback");
  GunFireEvent.CALLBACK.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE,feedbackPhase);
  GunFireEvent.CALLBACK.register(feedbackPhase,event->{if(!event.isCanceled()&&controls!=null)controls.fired(event);});
 }
}
