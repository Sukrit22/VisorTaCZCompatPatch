package dev.visorcompat.tacz;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
/** Gun handle position relative to Visor's room floor; VR metres/degrees. */
public record HolsterCalibration(float x,float y,float z,float pitch,float yaw,float roll){
    public static HolsterCalibration defaults(boolean pistol){
        return pistol?new HolsterCalibration(.25f,.94f,-.14f,-60,0,0)
            :new HolsterCalibration(0,1.04f,-.36f,0,90,45);
    }
    public boolean valid(){return Float.isFinite(x+y+z+pitch+yaw+roll)&&Math.abs(x)<=1.5&&y>=0&&y<=2.5&&Math.abs(z)<=1.5&&Math.abs(pitch)<=180&&Math.abs(yaw)<=180&&Math.abs(roll)<=180;}
    public Vector3f position(Vector3f feet,float heading,float scale,float heightRatio,float side){
        return new Quaternionf().rotateY(heading).transform(new Vector3f(x*side,y*heightRatio,z).mul(scale)).add(feet);
    }
    public Vector3f fromRoomFloor(Vector3fc origin,Vector3fc head,float heading,float scale,float side){
        // Never use the camera/player entity's Y: Visor temporarily sets it to the eye pose.
        return position(new Vector3f(head.x(),origin.y(),head.z()),heading,scale,1,side);
    }
    public Vector3f fromHead(Vector3fc head,float heading,float scale,float side){
        // Preserve saved Y numbers as nominal standing heights (Minecraft eyes: 1.62 m).
        return position(new Vector3f(head).sub(0,1.62f*scale,0),heading,scale,1,side);
    }
    public Vector3f followingCrouch(Vector3fc origin,Vector3fc head,float heading,float scale,float side){
        var adjusted=new Vector3f(head);
        float standingEyes=origin.y()+1.62f*scale;
        if(adjusted.y<standingEyes)adjusted.y=standingEyes+(adjusted.y-standingEyes)*(2f/3f);
        return fromHead(adjusted,heading,scale,side);
    }
    public Quaternionf rotation(float heading,float side){float r=(float)Math.PI/180;
        return new Quaternionf().rotateY(heading).rotateZ(roll*side*r).rotateY(yaw*side*r).rotateX(pitch*r);
    }
}
