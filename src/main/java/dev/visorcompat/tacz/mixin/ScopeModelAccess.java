package dev.visorcompat.tacz.mixin;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;
@Mixin(value=BedrockAttachmentModel.class,remap=false)
public interface ScopeModelAccess {
    @Accessor("ocularNodePaths") List<List<BedrockPart>> visorTacz$oculars();
    @Accessor("isScopeOcular") List<Boolean> visorTacz$scopeOculars();
}
