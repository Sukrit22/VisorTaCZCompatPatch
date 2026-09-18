package dev.visorcompat.tacz.mixin;
import com.tacz.guns.util.RenderHelper;
import dev.visorcompat.tacz.client.StencilScope;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=RenderHelper.class,remap=false)
public abstract class StencilEnableMixin {
    @Inject(method="enableItemEntityStencilTest",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$noWorldStencil(CallbackInfo ci){if(StencilScope.active())ci.cancel();}
}
