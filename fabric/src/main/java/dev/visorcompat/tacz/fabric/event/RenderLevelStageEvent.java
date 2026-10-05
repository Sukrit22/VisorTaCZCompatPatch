package dev.visorcompat.tacz.fabric.event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
public record RenderLevelStageEvent(PoseStack poseStack, Camera camera, float partialTick) {
 public enum Stage { AFTER_ENTITIES }
 public Stage getStage(){return Stage.AFTER_ENTITIES;}
 public PoseStack getPoseStack(){return poseStack;} public Camera getCamera(){return camera;} public float getPartialTick(){return partialTick;}
}
