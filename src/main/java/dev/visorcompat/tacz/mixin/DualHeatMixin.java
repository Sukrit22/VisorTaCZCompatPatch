package dev.visorcompat.tacz.mixin;

import com.tacz.guns.entity.shooter.LivingEntityHeat;
import dev.visorcompat.tacz.server.ServerDual;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntityHeat.class, remap = false)
public abstract class DualHeatMixin {
    @Shadow @Final private LivingEntity shooter;
    @Inject(method = "tickHeat", at = @At("HEAD"), cancellable = true)
    private void visorTacz$singleHeatOwner(CallbackInfo ci) {
        if (shooter instanceof ServerPlayer player && ServerDual.active(player)) ci.cancel();
    }
}
