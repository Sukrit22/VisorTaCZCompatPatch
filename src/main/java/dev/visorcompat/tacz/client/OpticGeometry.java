package dev.visorcompat.tacz.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.*;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import dev.visorcompat.tacz.*;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import java.util.List;

public final class OpticGeometry {
    private OpticGeometry() {}
    public static PoseStack base(WeaponProfile profile) {
        var matrices=new PoseStack();var grip=profile.grip();
        matrices.translate(-grip.x,-grip.y,-grip.z);
        matrices.scale(profile.scale(),profile.scale(),profile.scale());
        matrices.translate(0,1.5,0);matrices.mulPose(Axis.ZP.rotationDegrees(180));return matrices;
    }
    public static void path(PoseStack matrices,List<BedrockPart> path) { if(path!=null)for(var p:path)p.translateAndRotateAndScale(matrices); }
    public static Vector3f sight(ItemStack stack,WeaponProfile profile) {
        var display=TimelessAPI.getGunDisplay(stack).orElse(null);
        if(display==null || display.getGunModel()==null)return null;
        var model=display.getGunModel();var matrices=base(profile);
        IGun gun=IGun.getIGunOrNull(stack);
        ItemStack attachment=gun.getAttachment(stack,AttachmentType.SCOPE);
        if(attachment.isEmpty())attachment=gun.getBuiltinAttachment(stack,AttachmentType.SCOPE);
        IAttachment item=IAttachment.getIAttachmentOrNull(attachment);
        if(item!=null && model.getScopePosPath()!=null) {
            var index=TimelessAPI.getClientAttachmentIndex(item.getAttachmentId(attachment)).orElse(null);
            if(index!=null && index.getAttachmentModel()!=null) {
                var view=index.getAttachmentModel().getScopeViewPath(0);
                if(view!=null) {
                    path(matrices,model.getScopePosPath());matrices.translate(0,-1.5,0);path(matrices,view);
                    return matrices.last().pose().getTranslation(new Vector3f());
                }
            }
        }
        if(model.getIronSightPath()==null)return null;
        path(matrices,model.getIronSightPath());return matrices.last().pose().getTranslation(new Vector3f());
    }
}
