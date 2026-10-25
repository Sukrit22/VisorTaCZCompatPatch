package dev.visorcompat.tacz.mixin;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import dev.visorcompat.tacz.server.DualGunOperator;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value={AttachmentPropertyManager.class,com.tacz.guns.resource.pojo.data.gun.InaccuracyType.class},remap=false)
public abstract class DualCacheContextMixin {
 @Group(name="dualOperator",min=1)
 @Redirect(method="*",require=0,at=@At(value="INVOKE",target="Lcom/tacz/guns/api/entity/IGunOperator;fromLivingEntity(Lnet/minecraft/world/entity/LivingEntity;)Lcom/tacz/guns/api/entity/IGunOperator;"))
 private static IGunOperator visorTacz$operator(LivingEntity entity){var op=DualGunOperator.current(entity);return op==null?(IGunOperator)entity:op;}
 @Group(name="dualOperator",min=1)
 @Redirect(method="*",require=0,at=@At(value="INVOKE",target="Lcom/tacz/guns/api/entity/IGunOperator;fromLivingEntity(Lnet/minecraft/class_1309;)Lcom/tacz/guns/api/entity/IGunOperator;"))
 private static IGunOperator visorTacz$intermediary(LivingEntity entity){var op=DualGunOperator.current(entity);return op==null?(IGunOperator)entity:op;}
}
