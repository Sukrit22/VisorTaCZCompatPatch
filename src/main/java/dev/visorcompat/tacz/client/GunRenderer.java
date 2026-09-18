package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import dev.visorcompat.tacz.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;

public final class GunRenderer extends VRHandEffect {
    public GunRenderer(VisorAddon owner) { super(owner); }
    @Override public String getId() { return "tacz_gun"; }
    @Override public boolean isGlobal() { return true; }
    @Override public boolean isVisible(VRDecorator decorator, HandType hand, boolean guiHand) {
        return !guiHand && hand == HandType.MAIN && ClientControls.vrActive()
                && !com.tacz.guns.compat.oculus.OculusCompat.isRenderShadow()
                && (Minecraft.getInstance().screen == null || Minecraft.getInstance().screen instanceof CalibrationScreen || Minecraft.getInstance().screen instanceof com.tacz.guns.client.gui.GunRefitScreen) && ClientControls.supported();
    }

    @Override public void render(HandType hand, VRRenderPass pass, PoseStack matrices,
                                 boolean guiHand, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        var stack = mc.player.getMainHandItem();
        WeaponProfile profile = Profiles.get(stack);
        if (profile == null) return;
        var pose = VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        GunPose gun = GunPose.resolve(pose, profile, mc.player.getOffhandItem().isEmpty() && PhysicalClient.supporting(), CalibrationStore.render(Profiles.key(stack)));
        if (gun == null) return;
        TimelessAPI.getGunDisplay(stack).ifPresent(display -> {
            var model = display.getGunModel();
            if (model == null) return;
            // Visor supplies a body-hand transform. Convert it to the actual controller aim
            // transform, retaining the current eye camera and world scaling.
            var body = pose.getBody().getHand(HandType.MAIN).getPose();
            Matrix4f bodyTransform = new Matrix4f().translation(body.getPosition())
                    .mul(body.getRotation()).scale(gun.worldScale());
            Matrix4f gunTransform = new Matrix4f().translation(gun.hand())
                    .rotate(gun.rotation()).scale(gun.worldScale());
            Matrix4f correction = bodyTransform.invert().mul(gunTransform);
            OpticRenderer.begin(pass,gun.worldScale());
            matrices.pushPose();
            boolean oldHands = model.getRenderHand();
            boolean oldFlash = MuzzleFlashRender.isSelf;
            boolean oldShell=com.tacz.guns.client.model.functional.ShellRender.isSelf;
            try {
                matrices.mulPoseMatrix(correction);
                matrices.last().normal().mul(correction.get3x3(new org.joml.Matrix3f()).invert().transpose());
                if (mc.screen instanceof CalibrationScreen) {
                    Vector3f origin = CalibrationStore.render(Profiles.key(stack)).muzzleOffset(profile.muzzleOffset());
                    var lines = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
                    net.minecraft.client.renderer.LevelRenderer.renderLineBox(matrices, lines,
                        origin.x-.006,origin.y-.006,origin.z-.006,
                        origin.x+.006,origin.y+.006,origin.z+.006,0,1,1,1);
                }
                PhysicalModel.guides(matrices,profile,gun,pose);
                matrices.pushPose();
                Vector3f grip = profile.grip();
                matrices.translate(-grip.x, -grip.y, -grip.z);
                matrices.scale(profile.scale(), profile.scale(), profile.scale());
                matrices.translate(0, 1.5, 0);
                matrices.mulPose(Axis.ZP.rotationDegrees(180));
                // Keep the receiver anchored; desktop reload/idle animations move entire guns
                // around the camera and cannot safely drive the controller-aligned model.
                model.cleanAnimationTransform();
                model.setRenderHand(false);
                MuzzleFlashRender.isSelf = true;
                com.tacz.guns.client.model.functional.ShellRender.isSelf=false; // world-space effects replace camera-cached shells
                mc.gameRenderer.lightTexture().turnOnLightLayer();
                RenderType type = display.enablesTransparency()
                        ? RenderType.entityTranslucent(display.getModelTexture())
                        : RenderType.entityCutout(display.getModelTexture());
                int light=mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player,partialTicks);
                try (var physical=new PhysicalModel(model,profile)) {
                    model.render(matrices, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, type,light,OverlayTexture.NO_OVERLAY);
                } finally { matrices.popPose(); }
                PhysicalModel.detached(matrices,model,profile,gun,pose,type,light);
                if(PhysicalClient.active())PumpVisual.render(matrices,profile,gun,pose,PhysicalClient.phase(),light);
                if(PhysicalClient.active())JamVisual.render(matrices,stack,profile,CalibrationStore.render(Profiles.key(stack)),gun,pose,PhysicalClient.phase(),light);
                mc.renderBuffers().bufferSource().endBatch();
            } finally {
                OpticRenderer.end();
                model.setRenderHand(oldHands);
                MuzzleFlashRender.isSelf = oldFlash;
                com.tacz.guns.client.model.functional.ShellRender.isSelf=oldShell;
                model.cleanAnimationTransform();
                mc.gameRenderer.lightTexture().turnOffLightLayer();
                matrices.popPose();
            }
        });
    }
}
