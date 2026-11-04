package dev.visorcompat.tacz.neoforge.network;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
public final class PacketDistributor {
 public record Target(int kind, Entity entity) {}
 public record Distribution(int kind) { public Target with(java.util.function.Supplier<? extends Entity> entity) { return new Target(kind,entity.get()); } }
 public static final Distribution PLAYER=new Distribution(0),TRACKING_ENTITY=new Distribution(1),TRACKING_ENTITY_AND_SELF=new Distribution(2);
}
