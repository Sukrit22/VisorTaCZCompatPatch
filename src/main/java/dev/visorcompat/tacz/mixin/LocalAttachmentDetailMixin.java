package dev.visorcompat.tacz.mixin;
import com.tacz.guns.util.RenderDistance;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.visorcompat.tacz.client.OpticRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=RenderDistance.class,remap=false)
public abstract class LocalAttachmentDetailMixin {
    @Inject(method="inRenderHighPolyModelDistance",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$detail(PoseStack stack,CallbackInfoReturnable<Boolean> ci) {
        if(OpticRenderer.gunPass())ci.setReturnValue(true);
    }
}
