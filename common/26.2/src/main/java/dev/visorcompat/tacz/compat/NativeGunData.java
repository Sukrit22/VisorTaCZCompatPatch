package dev.visorcompat.tacz.compat;

import com.tacz.guns.api.item.IGun;
import dev.visorcompat.tacz.core.GunSnapshot;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

/** Minecraft 26.2 / pinned Refabricated API. Does not perform reload or inventory mutations. */
public final class NativeGunData {
    private NativeGunData() {}
    public static Optional<GunSnapshot> read(ItemStack stack) {
        var gun=IGun.getIGunOrNull(stack);
        if(gun==null)return Optional.empty();
        return Optional.of(new GunSnapshot(gun.getGunId(stack).toString(),gun.getGunDisplayId(stack).toString(),
                gun.getCurrentAmmoCount(stack),gun.hasBulletInBarrel(stack),gun.getFireMode(stack).name()));
    }
}
