package dev.visorcompat.tacz.mixin;

import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Binary bridge for optional addons (including gundb 2.2.2) built against TaCZ 1.1.8.
 * JVM method descriptors include the return type, so this coexists with the 1.2 SoundData API.
 */
@Mixin(value = GunDisplayInstance.class, remap = false)
public abstract class LegacyGunSoundMixin {
    @Unique
    public ResourceLocation getSounds(String name) {
        var sound = ((GunDisplayInstance) (Object) this).getSounds(name);
        return sound == null ? null : sound.getSound();
    }
}
