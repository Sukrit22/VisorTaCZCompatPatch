package dev.visorcompat.tacz.mixin;
import com.tacz.guns.compat.ar.ARCompat;
import dev.visorcompat.tacz.client.StencilScope;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ARCompat.class,remap=false)
public abstract class ScopedAccelerationMixin {
    // Deferred acceleration callbacks would escape the stencil save/restore scope.
    @Inject(method="shouldAccelerate",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$immediateWorldGun(CallbackInfoReturnable<Boolean> ci){if(StencilScope.active())ci.setReturnValue(false);}
}
