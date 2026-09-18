package dev.visorcompat.tacz.mixin;
import dev.visorcompat.tacz.client.StencilScope;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.gui.GunRefitScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.opengl.GL11;

@Mixin(value=BedrockGunModel.class,remap=false)
public abstract class GunStencilMixin {
    @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",at=@At("HEAD"))
    private void visorTacz$begin(PoseStack p,ItemStack item,ItemDisplayContext context,RenderType type,int light,int overlay,CallbackInfo ci){
        StencilScope.begin(!context.firstPerson() || Minecraft.getInstance().screen instanceof GunRefitScreen);
    }
    @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",at=@At("RETURN"))
    private void visorTacz$end(CallbackInfo ci){StencilScope.end();}
    @Redirect(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",
        at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
    private void visorTacz$keepWorldStencil(int mask,boolean osx){
        if(StencilScope.active())mask&=~GL11.GL_STENCIL_BUFFER_BIT;
        if(mask!=0)RenderSystem.clear(mask,osx);
    }
}
