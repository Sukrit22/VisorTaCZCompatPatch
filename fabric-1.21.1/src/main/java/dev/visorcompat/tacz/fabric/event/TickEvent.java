package dev.visorcompat.tacz.fabric.event;
import net.minecraft.world.entity.player.Player;
public final class TickEvent {
 public enum Phase { START, END }
 public static class ClientTickEvent { public final Phase phase; public ClientTickEvent(Phase p){phase=p;} }
 public static class RenderTickEvent { public final Phase phase; public final float renderTickTime; public RenderTickEvent(Phase p,float t){phase=p;renderTickTime=t;} }
 public static class PlayerTickEvent { public final Phase phase; public final Player player; public PlayerTickEvent(Phase p,Player player){phase=p;this.player=player;} }
}
