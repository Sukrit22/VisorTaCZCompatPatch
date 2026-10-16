package dev.visorcompat.tacz.mixin;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import dev.visorcompat.tacz.server.DualGunOperator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=ModernKineticGunScriptAPI.class,remap=false)
public abstract class DualScriptWeaponMixin {
 // The native guard reads the stack twice (identity and emptiness). Group counts
 // matched call sites, not mapping aliases, so do not cap it at one invocation.
 @Group(name="dualWeaponGuard",min=1)
 @Redirect(method="*",require=0,at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;",remap=false))
 private ItemStack visorTacz$devWeapon(LivingEntity shooter){return visorTacz$weapon(shooter);}
 @Group(name="dualWeaponGuard",min=1)
 @Redirect(method="*",require=0,at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;m_21205_()Lnet/minecraft/world/item/ItemStack;",remap=false))
 private ItemStack visorTacz$productionWeapon(LivingEntity shooter){return visorTacz$weapon(shooter);}
 private ItemStack visorTacz$weapon(LivingEntity shooter){var context=DualGunOperator.current(shooter);return context==null?shooter.getMainHandItem():context.stack();}
}
