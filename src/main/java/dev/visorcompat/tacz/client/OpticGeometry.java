package dev.visorcompat.tacz.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.*;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.mixin.ScopeModelAccess;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import java.util.List;

public final class OpticGeometry {
    public record Ads(Vector3f sight,ZoneSizes.Box box){}
    public static String context(ItemStack stack){
        var gun=IGun.getIGunOrNull(stack);if(gun==null)return "";
        var id=gun.getAttachmentId(stack,AttachmentType.SCOPE);
        if(com.tacz.guns.api.DefaultAssets.isEmptyAttachmentId(id))id=gun.getBuiltInAttachmentId(stack,AttachmentType.SCOPE);
        return Profiles.key(stack)+"|sight="+id+"|view="+view(stack);
    }
    private static ItemStack attachment(ItemStack stack){
        var gun=IGun.getIGunOrNull(stack);if(gun==null)return ItemStack.EMPTY;
        var result=gun.getAttachment(stack,AttachmentType.SCOPE);
        return result.isEmpty()?gun.getBuiltinAttachment(stack,AttachmentType.SCOPE):result;
    }
    public static int view(ItemStack stack){
        var attachment=attachment(stack);var item=IAttachment.getIAttachmentOrNull(attachment);if(item==null)return 0;
        var index=TimelessAPI.getClientAttachmentIndex(item.getAttachmentId(attachment)).orElse(null);
        if(index==null||index.getViews()==null||index.getViews().length==0)return 0;
        return OpticMath.scopeView(item.getZoomNumber(attachment),index.getViews());
    }
    public static Ads ads(ItemStack stack,WeaponProfile profile,Calibration calibration,boolean preview){
        var sight=sight(stack,profile);if(sight==null)return null;
        String key=context(stack);var extra=preview?SightCalibrationStore.render(key):SightCalibrationStore.get(key);
        return new Ads(extra.apply(sight,calibration),extra.dimensions(calibration));
    }
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
        ItemStack attachment=attachment(stack);
        IAttachment item=IAttachment.getIAttachmentOrNull(attachment);
        if(item!=null && model.getScopePosPath()!=null) {
            var index=TimelessAPI.getClientAttachmentIndex(item.getAttachmentId(attachment)).orElse(null);
            if(index!=null && index.getAttachmentModel()!=null) {
                var attachmentModel=index.getAttachmentModel();int selected=view(stack);
                var access=(ScopeModelAccess)attachmentModel;var oculars=access.visorTacz$oculars();var scopeFlags=access.visorTacz$scopeOculars();
                boolean nonmagnifying=attachmentModel.isSight()&&(!attachmentModel.isScope()
                    ||scopeFlags!=null&&selected<scopeFlags.size()&&!Boolean.TRUE.equals(scopeFlags.get(selected)));
                if(nonmagnifying&&oculars!=null&&selected<oculars.size()){
                    matrices.pushPose();
                    Vector3f aperture;
                    try{path(matrices,model.getScopePosPath());matrices.translate(0,-1.5,0);aperture=OpticAperture.center(matrices,oculars.get(selected));}
                    finally{matrices.popPose();}
                    if(aperture!=null)return aperture;
                }
                // Magnifying scopes retain their authored eye-relief marker. Unknown
                // custom cube types also fall back to this native view path.
                var view=attachmentModel.getScopeViewPath(selected);
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
