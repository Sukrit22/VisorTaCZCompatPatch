package dev.visorcompat.tacz.client;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import dev.visorcompat.tacz.mixin.ScopeModelAccess;
import java.util.*;

/** Hide ordinary opaque ocular geometry only inside the local VR gun draw. */
public final class OpticVisibility {
    private record Saved(BedrockPart part,boolean visible){}
    private static final ArrayDeque<List<Saved>> STACK=new ArrayDeque<>();
    public static void begin(BedrockAttachmentModel model){
        List<Saved> saved=new ArrayList<>();STACK.push(saved);
        if(!OpticRenderer.gunPass())return;
        var paths=((ScopeModelAccess)model).visorTacz$oculars();
        if(paths==null)return;
        for(var path:paths)if(path!=null && !path.isEmpty()){
            var part=path.get(path.size()-1);saved.add(new Saved(part,part.visible));part.visible=false;
        }
    }
    public static void end(){if(!STACK.isEmpty())for(var s:STACK.pop())s.part().visible=s.visible();}
}
