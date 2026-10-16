package dev.visorcompat.tacz.mixin;

import com.tacz.guns.util.CycleTaskHelper;
import dev.visorcompat.tacz.server.DualGunOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.function.BooleanSupplier;

@Mixin(value = CycleTaskHelper.class, remap = false)
public abstract class DualBurstContextMixin {
    @ModifyVariable(method = {"addCycleTask(Ljava/util/function/BooleanSupplier;JI)V", "addCycleTask(Ljava/util/function/BooleanSupplier;JJI)V"}, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static BooleanSupplier visorTacz$task(BooleanSupplier task) { return DualGunOperator.wrap(task); }
}
