package dev.visorcompat.tacz.mixin;
import com.tacz.guns.entity.shooter.LivingEntityBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=LivingEntityBolt.class,remap=false)
public abstract class PumpBoltMixin {
    @Shadow @Final private LivingEntity shooter;
    @Inject(method="bolt",at=@At("HEAD"),cancellable=true)
    private void physicalPump(CallbackInfo ci){if(shooter instanceof ServerPlayer p && dev.visorcompat.tacz.Profiles.manualAction(p.getMainHandItem()) && dev.visorcompat.tacz.server.ServerPhysical.enabled(p))ci.cancel();}
}
