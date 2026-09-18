package dev.visorcompat.tacz.mixin;

import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import dev.visorcompat.tacz.PoseMath;
import dev.visorcompat.tacz.server.BulletPose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ModernKineticGunItem.class, remap = false)
public abstract class BulletSpreadMixin {
    @ModifyVariable(method = "doBulletSpread", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float visorTacz$pitch(float original, ShooterDataHolder data, ItemStack stack,
                                 LivingEntity shooter, Projectile projectile, int count,
                                 float speed, float spread, float pitch, float yaw) {
        return projectile instanceof BulletPose bullet && bullet.visorTacz$pose() != null
                ? PoseMath.pitch(bullet.visorTacz$pose().direction()) : original;
    }
    @ModifyVariable(method = "doBulletSpread", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float visorTacz$yaw(float original, ShooterDataHolder data, ItemStack stack,
                               LivingEntity shooter, Projectile projectile, int count,
                               float speed, float spread, float pitch, float yaw) {
        return projectile instanceof BulletPose bullet && bullet.visorTacz$pose() != null
                ? PoseMath.yaw(bullet.visorTacz$pose().direction()) : original;
    }
}
