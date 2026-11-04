package dev.visorcompat.tacz.fabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.tacz.guns.api.event.common.*;
import dev.visorcompat.tacz.fabric.event.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.server.ServerEvents;
public final class FabricMain implements ModInitializer {
 @Override public void onInitialize(){
  CompatNetwork.register();
  CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->ServerEvents.commands(new RegisterCommandsEvent(dispatcher)));
  ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->CompatNetwork.login(new PlayerEvent.PlayerLoggedInEvent(handler.player)));
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->CompatNetwork.logout(new PlayerEvent.PlayerLoggedOutEvent(handler.player)));
  ServerLifecycleEvents.SERVER_STOPPED.register(server->CompatNetwork.stopped(new ServerStoppedEvent()));
  cn.sh1rocu.tacz.api.event.PlayerTickEvent.END.register(event->ServerEvents.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,event.getEntity())));
  GunShootEvent.CALLBACK.register(ServerEvents::beforeShot);
  GunFireEvent.CALLBACK.register(ServerEvents::beforeRound);
  GunReloadEvent.CALLBACK.register(ServerEvents::reload);
 }
}
