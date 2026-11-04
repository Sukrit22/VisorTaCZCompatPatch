package dev.visorcompat.tacz.probe;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.player.Player;
import org.vmstudio.visor.api.VisorAPI;

/** Compile-time surface checks only; no mod entrypoint or runtime compatibility claim. */
public final class ApiSurfaceProbe {
    public static Object player(Player player){return VisorAPI.getVRPlayer(player);}
    public static void modelPart(BedrockPart part,PoseStack poses,VertexConsumer vertices){
        part.translateAndRotateAndScale(poses);
        part.render(poses,ItemDisplayContext.GROUND,vertices,0,0);
    }
}
