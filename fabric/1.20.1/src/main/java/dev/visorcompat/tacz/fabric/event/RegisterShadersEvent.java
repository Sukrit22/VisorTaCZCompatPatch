package dev.visorcompat.tacz.fabric.event;
import java.io.IOException;
import java.util.function.Consumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
public record RegisterShadersEvent(CoreShaderRegistrationCallback.RegistrationContext context) {
 public void registerShader(ResourceLocation id,VertexFormat format,Consumer<ShaderInstance> loaded)throws IOException{context.register(id,format,loaded);}
}
