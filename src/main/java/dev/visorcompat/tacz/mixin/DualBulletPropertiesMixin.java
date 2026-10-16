package dev.visorcompat.tacz.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import dev.visorcompat.tacz.server.DualGunOperator;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserve the firing weapon for native scripted damage modifiers during flight. */
@Mixin(value = EntityKineticBullet.class, remap = false)
public abstract class DualBulletPropertiesMixin {
    @Unique private DualGunOperator visorTacz$operator;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void visorTacz$capture(CallbackInfo ci) {
        var bullet = (EntityKineticBullet) (Object) this;
        if (visorTacz$operator == null && bullet.getOwner() instanceof LivingEntity shooter) {
            var context = DualGunOperator.current(shooter);
            if (context != null) visorTacz$operator = context.bulletProperties();
        }
    }
    @Inject(method = "modifyProperty(Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private <T> void visorTacz$property(String id, Class<T> type, T original, CallbackInfoReturnable<T> ci) {
        if (visorTacz$operator != null) ci.setReturnValue(visorTacz$operator.modifyBulletProperty(id, type, original));
    }
}
