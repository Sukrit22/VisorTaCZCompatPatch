package dev.visorcompat.tacz.mixin;

import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import com.tacz.guns.api.client.animation.ObjectAnimationSoundChannel;
import com.tacz.guns.api.item.IGun;
import dev.visorcompat.tacz.client.PhysicalClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** MK23 shot clips contain an automatic hand-rack sound, independent of LocalPlayerBolt. */
@Mixin(value=ObjectAnimationRunner.class,remap=false)
public abstract class PhysicalShotSoundMixin {
    @Redirect(method={"update","updateSoundOnly"},at=@At(value="INVOKE",target="Lcom/tacz/guns/api/client/animation/ObjectAnimation;getSoundChannel()Lcom/tacz/guns/api/client/animation/ObjectAnimationSoundChannel;"))
    private ObjectAnimationSoundChannel physicalShotSound(ObjectAnimation animation){
        // DualStatus owns reload/bolt playback for both inventory slots. The native
        // animation runner also advances the selected slot, otherwise doubling it.
        if(dev.visorcompat.tacz.client.DualClient.active()&&dev.visorcompat.tacz.client.DualReloadSounds.ownsAnimation(animation.name))return null;
        var player=Minecraft.getInstance().player;
        if(player!=null && PhysicalClient.active() && animation.name.startsWith("shoot")){
            var stack=player.getMainHandItem();var gun=IGun.getIGunOrNull(stack);
            if(gun!=null && gun.getGunId(stack).toString().equals("tacz:hk_mk23"))return null;
        }
        return animation.getSoundChannel();
    }
}
