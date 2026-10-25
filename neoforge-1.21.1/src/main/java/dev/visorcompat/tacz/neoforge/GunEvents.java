package dev.visorcompat.tacz.neoforge;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
/** Preserve shared cancellation semantics; NeoForge post returns the event. */
public final class GunEvents {
 public static <T extends Event> boolean post(T event){NeoForge.EVENT_BUS.post(event);return event instanceof ICancellableEvent cancellable&&cancellable.isCanceled();}
}
