package dev.visorcompat.tacz.fabric;
import com.tacz.guns.api.event.common.GunReloadEvent;
/** Common-side native TaCZ event forwarding, safe to load on dedicated servers. */
public final class FabricGunEvents {
 private FabricGunEvents(){}
 public static boolean post(GunReloadEvent event){GunReloadEvent.CALLBACK.invoker().post(event);return event.isCanceled();}
}
