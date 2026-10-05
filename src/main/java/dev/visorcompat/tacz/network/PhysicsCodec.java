package dev.visorcompat.tacz.network;
import dev.visorcompat.tacz.PhysicsCalibration;
import net.minecraft.network.FriendlyByteBuf;
public final class PhysicsCodec {
    public static void write(FriendlyByteBuf b,PhysicsCalibration p){b.writeFloat(p.offsetX());b.writeFloat(p.offsetY());b.writeFloat(p.offsetZ());b.writeFloat(p.pitch());b.writeFloat(p.roll());b.writeFloat(p.travel());b.writeFloat(p.width());b.writeFloat(p.height());b.writeFloat(p.depth());b.writeFloat(p.friction());b.writeFloat(p.slideTravel());}
    public static PhysicsCalibration read(FriendlyByteBuf b){return new PhysicsCalibration(b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat());}
}
