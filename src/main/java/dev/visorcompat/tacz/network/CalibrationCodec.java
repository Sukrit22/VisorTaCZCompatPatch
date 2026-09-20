package dev.visorcompat.tacz.network;
import dev.visorcompat.tacz.Calibration;

public final class CalibrationCodec {
    public static void write(net.minecraft.network.FriendlyByteBuf b,Calibration c) {
        b.writeFloat(c.x());b.writeFloat(c.y());b.writeFloat(c.z());b.writeFloat(c.pitch());b.writeFloat(c.yaw());b.writeFloat(c.roll());
        b.writeFloat(c.muzzleX());b.writeFloat(c.muzzleY());b.writeFloat(c.muzzleZ());
        for(var point:c.interactions().points()){b.writeFloat(point.x());b.writeFloat(point.y());b.writeFloat(point.z());}
        b.writeBoolean(c.scaleLock());b.writeFloat(c.gunScale());b.writeFloat(c.casingScale());for(var box:c.zones().boxes()){b.writeFloat(box.width());b.writeFloat(box.height());b.writeFloat(box.depth());}
    }
    public static Calibration read(net.minecraft.network.FriendlyByteBuf b) {
        float[] v=new float[9];for(int i=0;i<9;i++)v[i]=b.readFloat();
        var p=new dev.visorcompat.tacz.InteractionOffsets.Point[8];
        for(int i=0;i<8;i++)p[i]=new dev.visorcompat.tacz.InteractionOffsets.Point(b.readFloat(),b.readFloat(),b.readFloat());
        boolean locked=b.readBoolean();float scale=b.readFloat(),casing=b.readFloat();var boxes=new dev.visorcompat.tacz.ZoneSizes.Box[8];
        for(int i=0;i<8;i++)boxes[i]=new dev.visorcompat.tacz.ZoneSizes.Box(b.readFloat(),b.readFloat(),b.readFloat());
        return new Calibration(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],
            new dev.visorcompat.tacz.InteractionOffsets(p[0],p[1],p[2],p[3],p[4],p[5],p[6],p[7]),scale,new dev.visorcompat.tacz.ZoneSizes(boxes[0],boxes[1],boxes[2],boxes[3],boxes[4],boxes[5],boxes[6],boxes[7]),locked,casing);
    }
}
