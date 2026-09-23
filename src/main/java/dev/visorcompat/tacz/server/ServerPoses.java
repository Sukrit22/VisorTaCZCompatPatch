package dev.visorcompat.tacz.server;

import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

public final class ServerPoses {
    private ServerPoses() {}
    public static boolean isVr(ServerPlayer player) {
        // Require live client activation AND Visor's server-side VR identity.
        // Visor 0.5.0 establishes that identity at login, not on each state change.
        return CompatNetwork.enabled(player) && VisorAPI.getVRPlayer(player) instanceof VRServerPlayer;
    }
    public static GunPose validated(ServerPlayer player) {
        if(!ServerAdvanced.canInteract(player))return null;
        if (!(VisorAPI.getVRPlayer(player) instanceof VRServerPlayer vr) || !vr.hasPoseData()) return null;
        if (!(vr instanceof FreshPose fresh) || fresh.visorTacz$lastPoseNanos() == 0
                || System.nanoTime() - fresh.visorTacz$lastPoseNanos() > 500_000_000L) return null;
        WeaponProfile profile = Profiles.get(player.getMainHandItem());
        if (profile == null || !player.isAlive() || player.isSpectator()) return null;
        GunPose pose = GunPose.resolve(vr.getPoseData(), profile, player.getOffhandItem().isEmpty() && (!ServerPhysical.enabled(player) || ServerPhysical.supporting(player)), CompatNetwork.calibration(player),ServerPhysical.anchor(player));
        pose=ServerAdvanced.mountedPose(player,pose);
        if (pose == null) return null;
        Vec3 head = vr.getPoseData().getHmd().getPositionVec3();
        Vec3 hand = new Vec3(pose.hand());
        Vec3 muzzle = new Vec3(pose.muzzle());
        double scale = pose.worldScale();
        if (!Double.isFinite(head.lengthSqr()) || head.distanceTo(player.position()) > 3 * scale
                || head.distanceTo(hand) > 1.5 * scale || hand.distanceTo(muzzle) > 3 * scale) return null;
        // Both segments matter: the whole hand can be on the far side of a wall.
        if (blocked(player, player.getEyePosition(), head) || blocked(player, head, hand)
                || blocked(player, hand, muzzle)) return null;
        return pose;
    }
    private static boolean blocked(ServerPlayer player, Vec3 from, Vec3 to) {
        return player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS;
    }
}
