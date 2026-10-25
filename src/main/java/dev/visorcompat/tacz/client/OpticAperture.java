package dev.visorcompat.tacz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.bedrock.*;
import org.joml.Vector3f;
import java.util.List;

/** Physical ocular bounds through the same parent transforms used to render the lens. */
public final class OpticAperture {
    private OpticAperture(){}
    public static Vector3f center(PoseStack mounted,List<BedrockPart> path){
        if(path==null||path.isEmpty())return null;
        Vector3f min=new Vector3f(Float.POSITIVE_INFINITY),max=new Vector3f(Float.NEGATIVE_INFINITY);
        mounted.pushPose();
        try {
            for(int i=0;i<path.size()-1;i++)path.get(i).translateAndRotateAndScale(mounted);
            bounds(mounted,path.get(path.size()-1),min,max);
        }finally{mounted.popPose();}
        return Float.isFinite(min.x+min.y+min.z+max.x+max.y+max.z)?min.add(max).mul(.5f):null;
    }
    private static void bounds(PoseStack pose,BedrockPart part,Vector3f min,Vector3f max){
        pose.pushPose();
        try {
            part.translateAndRotateAndScale(pose);
            for(var cube:part.cubes){
                float x0,y0,z0,x1,y1,z1;
                if(cube instanceof BedrockCubeBox box){x0=box.minX;y0=box.minY;z0=box.minZ;x1=box.maxX;y1=box.maxY;z1=box.maxZ;}
                else if(cube instanceof BedrockCubePerFace box){x0=box.minX;y0=box.minY;z0=box.minZ;x1=box.maxX;y1=box.maxY;z1=box.maxZ;}
                else continue;
                for(int i=0;i<8;i++){
                    var point=new Vector3f((i&1)==0?x0:x1,(i&2)==0?y0:y1,(i&4)==0?z0:z1).div(16);
                    pose.last().pose().transformPosition(point);min.min(point);max.max(point);
                }
            }
            for(var child:part.children)bounds(pose,child,min,max);
        }finally{pose.popPose();}
    }
}
