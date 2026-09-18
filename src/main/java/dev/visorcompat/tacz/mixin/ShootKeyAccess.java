package dev.visorcompat.tacz.mixin;
import com.tacz.guns.client.input.ShootKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=ShootKey.class,remap=false)
public interface ShootKeyAccess {
    @Accessor("lastTimeShootSuccess") static void visorTacz$success(boolean value) {throw new AssertionError();}
}
