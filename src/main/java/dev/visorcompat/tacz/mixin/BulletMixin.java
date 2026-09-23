package dev.visorcompat.tacz.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import dev.visorcompat.tacz.GunPose;
import dev.visorcompat.tacz.server.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityKineticBullet.class, remap = false)
public abstract class BulletMixin extends Projectile implements BulletPose {
    protected BulletMixin(EntityType<? extends Projectile> type,Level level) {super(type,level);}
    // Override the inherited scalar route: Visor's generic Projectile mixin otherwise
    // replaces TaCZ's pitch/yaw with the last active hand after our spread hook.
    @Override public void shootFromRotation(Entity shooter,float pitch,float yaw,float roll,float speed,float spread) {
        if(visorTacz$pose==null) {super.shootFromRotation(shooter,pitch,yaw,roll,speed,spread);return;}
        var direction=visorTacz$pose.direction();
        this.shoot(direction.x,direction.y,direction.z,speed,spread);
        Vec3 motion=shooter.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(motion.x,shooter.onGround()?0:motion.y,motion.z));
    }
    @Shadow private Vec3 startPos;
    @Unique private GunPose visorTacz$pose;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;ZLcom/tacz/guns/resource/pojo/data/gun/GunData;Lcom/tacz/guns/resource/pojo/data/gun/BulletData;)V", at = @At("RETURN"))
    private void visorTacz$origin(EntityType<? extends Projectile> type, Level level, LivingEntity shooter,
                                  ItemStack stack, ResourceLocation ammo, ResourceLocation gun,
                                  ResourceLocation display, boolean tracer, GunData data, BulletData bulletData,
                                  CallbackInfo ci) {
        if (!(shooter instanceof ServerPlayer player) || !ServerPoses.isVr(player)) return;
        EntityKineticBullet self = (EntityKineticBullet) (Object) this;
        visorTacz$pose = ServerPoses.validated(player);
        if (visorTacz$pose == null) {
            self.discard(); // Delayed burst rounds also fail closed when tracking/geometry becomes invalid.
            return;
        }
        startPos = new Vec3(visorTacz$pose.muzzle());
        self.setPos(startPos);
        self.xOld = startPos.x;
        self.yOld = startPos.y;
        self.zOld = startPos.z;
        if(dev.visorcompat.tacz.Profiles.cylinder(stack) && ServerPhysical.enabled(player))dev.visorcompat.tacz.server.ServerCylinder.shot(player,stack);
        else if(dev.visorcompat.tacz.Profiles.pump(stack) && ServerPhysical.enabled(player))ServerPump.shot(player,stack);
        else if(dev.visorcompat.tacz.Profiles.bolt(stack) && ServerPhysical.enabled(player))stack.getOrCreateTag().putBoolean(dev.visorcompat.tacz.physical.BoltState.SPENT,true);
        else {if(!ServerPhysical.enabled(player)||!dev.visorcompat.tacz.physical.ManualCycle.enabled(stack))HandlingFeedback.emit(player,4,visorTacz$pose);if(ServerPhysical.enabled(player))ServerPhysical.shot(player);}
    }
    @Override public GunPose visorTacz$pose() { return visorTacz$pose; }
}
