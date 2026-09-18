package dev.visorcompat.tacz.mixin;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.visorcompat.tacz.client.RemoteGuns;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=GunItemRendererWrapper.class,remap=false)
public abstract class RemoteGunMixin {
    @Inject(method={"renderByItem","m_108829_"},at=@At("HEAD"),cancellable=true)
    private void visorTacz$remote(ItemStack stack,ItemDisplayContext context,PoseStack matrices,MultiBufferSource buffer,int light,int overlay,CallbackInfo ci) {
        if(RemoteGuns.replaces(stack,context))ci.cancel();
    }
}
