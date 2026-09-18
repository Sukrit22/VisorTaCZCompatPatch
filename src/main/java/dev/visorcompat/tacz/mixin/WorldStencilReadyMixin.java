package dev.visorcompat.tacz.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prepare before LevelRenderer/Oculus capture the depth format for this frame. */
@Mixin(value=GameRenderer.class,remap=false)
public abstract class WorldStencilReadyMixin {
    @Inject(method={"renderLevel", "m_109089_"},at=@At("HEAD"))
    private void visorTacz$prepareStencil(CallbackInfo ci) {
        RenderTarget target=Minecraft.getInstance().getMainRenderTarget();
        if(target.useDepth && target.frameBufferId>=0 && !target.isStencilEnabled()) {
            target.enableStencil();
            target.bindWrite(true);
        }
    }
}
