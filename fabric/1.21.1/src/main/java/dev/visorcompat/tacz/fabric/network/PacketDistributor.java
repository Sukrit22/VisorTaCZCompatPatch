package dev.visorcompat.tacz.fabric.network;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import java.util.*;
public final class PacketDistributor {
 public interface Target {Collection<ServerPlayer> players();}
 public static final Distribution PLAYER=new Distribution(0),TRACKING_ENTITY=new Distribution(1),TRACKING_ENTITY_AND_SELF=new Distribution(2);
 public record Distribution(int kind){public Target with(java.util.function.Supplier<? extends Entity> supplier){return ()->{Entity e=supplier.get();if(kind==0)return List.of((ServerPlayer)e);var players=new HashSet<>(net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(e));if(kind==2&&e instanceof ServerPlayer p)players.add(p);return players;};}}
}
