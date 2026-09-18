package dev.visorcompat.tacz.mixin;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.visorcompat.tacz.client.OpticRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=BedrockAttachmentModel.class,remap=false)
public abstract class OpticLensMixin {
    @Inject(method="render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",at=@At("HEAD"))
    private void visorTacz$clearGlass(CallbackInfo ci){dev.visorcompat.tacz.client.OpticVisibility.begin((BedrockAttachmentModel)(Object)this);}
    // TaCZ passes null for both stacks when drawing a loose attachment. Its
    // first-person scope path nevertheless enables/resizes the main stencil
    // target, invalidating shader-pack depth copies. A loose optic is a model,
    // not an aimed gun: select the ordinary body/ring path without changing
    // the outer item-in-hand transform or attached-gun optics.
    @ModifyVariable(method="render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",
        at=@At("HEAD"),argsOnly=true,ordinal=0)
    private ItemDisplayContext visorTacz$looseOptic(ItemDisplayContext original,ItemStack attachment,ItemStack gun,
                                                  PoseStack matrices,ItemDisplayContext context,RenderType type,int light,int overlay) {
        if((gun==null || gun.isEmpty() || dev.visorcompat.tacz.client.StencilScope.active()) && original.firstPerson())
            return original==ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        return original;
    }
    @Inject(method="render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",at=@At("TAIL"))
    private void visorTacz$lens(ItemStack attachment,ItemStack gun,PoseStack matrices,ItemDisplayContext context,RenderType type,int light,int overlay,CallbackInfo ci) {
        try {OpticRenderer.draw((BedrockAttachmentModel)(Object)this,gun,matrices,context,light,overlay);}
        finally {dev.visorcompat.tacz.client.OpticVisibility.end();}
    }
}
