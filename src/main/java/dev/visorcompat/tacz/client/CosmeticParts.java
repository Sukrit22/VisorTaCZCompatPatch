package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import java.util.*;
/** Scoped mutations: shared TaCZ models must be restored for every eye and player. */
final class CosmeticParts implements AutoCloseable {
    private record Saved(BedrockPart part,boolean visible,float x,float z,float offset){}
    private final List<Saved> saved=new ArrayList<>();
    private BedrockPart remember(BedrockPart root,String name){if(name.isEmpty())return null;var p=find(root,name);if(p!=null&&saved.stream().noneMatch(s->s.part()==p))saved.add(new Saved(p,p.visible,p.xRot,p.zRot,p.offsetZ));return p;}
    private static BedrockPart find(BedrockPart p,String n){if(p==null)return null;if(n.equals(p.name))return p;for(var c:p.children){var found=find(c,n);if(found!=null)return found;}return null;}
    CosmeticParts(BedrockPart root,WeaponProfile profile,ItemStack stack,Calibration calibration,boolean physical,boolean manualBusy){
        var data=DescriptorProfiles.details(profile);if(data==null)return;var v=data.visual();var gun=IGun.getIGunOrNull(stack);if(gun==null)return;
        if(!manualBusy&&!dev.visorcompat.tacz.physical.ManualCycle.enabled(stack)){var shot=remember(root,v.shotNode());if(shot!=null)shot.offsetZ+=ShotVisual.cycle(stack)*v.shotTravel()/profile.scale();}
        if(v.rounds().isEmpty())return;
        int live=Math.max(0,gun.getCurrentAmmoCount(stack));int spent=physical&&stack.hasTag()?Math.max(0,stack.getTag().getInt(dev.visorcompat.tacz.server.ServerCylinder.SPENT)):0;
        for(int i=0;i<v.rounds().size();i++){
            var round=remember(root,v.rounds().get(i));if(round!=null)round.visible=i<live+spent;
            if(!v.heads().isEmpty()){var head=remember(root,v.heads().get(i));if(head!=null)head.visible=i<live;}
        }
        for(String name:v.hidden()){var hidden=remember(root,name);if(hidden!=null)hidden.visible=false;}
        var loader=remember(root,v.loader());if(loader!=null)loader.visible=false;
        float charge=dev.visorcompat.tacz.server.ServerCylinder.open(stack)?0:ShotVisual.charge(stack);
        var hammer=remember(root,v.hammer());if(hammer!=null)hammer.xRot+=(float)Math.toRadians(v.hammerDegrees())*charge;
        var cylinder=remember(root,v.cylinder());if(cylinder!=null)cylinder.zRot-=(float)(2*Math.PI/v.rounds().size())*(spent+charge);
    }
    public void close(){for(var s:saved){s.part().visible=s.visible();s.part().xRot=s.x();s.part().zRot=s.z();s.part().offsetZ=s.offset();}}
}
