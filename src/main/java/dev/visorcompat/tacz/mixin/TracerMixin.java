package dev.visorcompat.tacz.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.renderer.entity.EntityBulletRenderer;
import com.tacz.guns.entity.EntityKineticBullet;
import dev.visorcompat.tacz.client.ClientControls;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityBulletRenderer.class, remap = false)
public abstract class TracerMixin {
    @Inject(method = "renderTracerAmmo", at = @At("HEAD"))
    private void visorTacz$worldOrigin(EntityKineticBullet bullet, float[] color, float partialTicks,
                                      PoseStack stack, int light, CallbackInfo ci) {
        if (ClientControls.vrActive() && bullet.getOwner() == Minecraft.getInstance().player) {
            // The server already spawned this bullet at the muzzle. Do not add the
            // desktop view-model offset a second time.
            bullet.setFirstPersonRenderOffset(new Vector3f());
        }
    }
}
