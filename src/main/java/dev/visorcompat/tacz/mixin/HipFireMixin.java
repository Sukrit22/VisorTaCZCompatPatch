package dev.visorcompat.tacz.mixin;

import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Select only the native accuracy category; retain attachments, heat, scripts and pellet calculation. */
@Mixin(value=InaccuracyType.class,remap=false)
public abstract class HipFireMixin {
    @Inject(method="getInaccuracyType",at=@At("HEAD"),cancellable=true)
    private static void visorTacz$accuracy(LivingEntity shooter,CallbackInfoReturnable<InaccuracyType> cir){
        if(dev.visorcompat.tacz.server.HipFirePolicy.applies(shooter))cir.setReturnValue(InaccuracyType.AIM);
    }
}
