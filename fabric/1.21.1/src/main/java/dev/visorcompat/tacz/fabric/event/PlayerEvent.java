package dev.visorcompat.tacz.fabric.event;
import net.minecraft.world.entity.player.Player;
public class PlayerEvent {
 private final Player player; public PlayerEvent(Player p){player=p;} public Player getEntity(){return player;}
 public static final class PlayerLoggedInEvent extends PlayerEvent {public PlayerLoggedInEvent(Player p){super(p);}}
 public static final class PlayerLoggedOutEvent extends PlayerEvent {public PlayerLoggedOutEvent(Player p){super(p);}}
}
