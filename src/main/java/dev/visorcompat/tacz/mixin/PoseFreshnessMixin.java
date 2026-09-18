package dev.visorcompat.tacz.mixin;

import dev.visorcompat.tacz.server.FreshPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.server.player.VRServerPlayerImpl;

@Mixin(value = VRServerPlayerImpl.class, remap = false)
public abstract class PoseFreshnessMixin implements FreshPose {
    @Unique private long visorTacz$received;
    @Inject(method = "receivedPosePacket", at = @At("RETURN"))
    private void visorTacz$received(CallbackInfo ci) { visorTacz$received = System.nanoTime(); }
    @Override public long visorTacz$lastPoseNanos() { return visorTacz$received; }
}
