package dev.visorcompat.tacz.client;

import com.tacz.guns.client.model.bedrock.BedrockPart;

/** Apply only translation not already inherited through the native bone tree. */
public final class ActionMotion {
    private ActionMotion() {}
    public static void follow(BedrockPart action, BedrockPart bolt, float pull, float ratio) {
        if (bolt != null && !contains(action, bolt)) bolt.offsetZ += pull * ratio;
    }
    private static boolean contains(BedrockPart root, BedrockPart target) {
        if (root == null) return false;
        if (root == target) return true;
        for (var child : root.children) if (contains(child, target)) return true;
        return false;
    }
}
