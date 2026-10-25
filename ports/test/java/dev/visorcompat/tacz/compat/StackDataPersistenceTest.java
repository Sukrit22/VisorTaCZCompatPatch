package dev.visorcompat.tacz.compat;

import net.minecraft.SharedConstants;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Run with the target's real Minecraft runtime, without a running game or test framework. */
public final class StackDataPersistenceTest {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        PoseStack poses = new PoseStack();
        Matrix4f correction = new Matrix4f().rotateY((float)Math.PI / 2);
        poses.mulPose(correction);
        check(poses.last().normal().equals(new Matrix3f(correction), 0.00001f),
            "matrix pose multiplication applies normal correction exactly once");
        ItemStack fresh = new ItemStack(Items.STONE);
        check(!StackData.has(fresh), "new stack has no custom data");
        StackData.putBoolean(fresh, "visor_tacz_open", true);
        check(StackData.has(fresh) && StackData.read(fresh).getBoolean("visor_tacz_open"), "mutation creates missing component");
        StackData.remove(fresh, "visor_tacz_open");
        check(!StackData.has(fresh), "last custom key removal persists");
        ItemStack original = new ItemStack(Items.STONE);
        CompoundTag seed = new CompoundTag();
        seed.putString("other_mod", "preserved");
        original.set(DataComponents.CUSTOM_DATA, CustomData.of(seed));
        original.set(DataComponents.MAX_STACK_SIZE, 16);
        StackData.putBoolean(original, "visor_tacz_open", true);
        StackData.putInt(original, "visor_tacz_spent", 3);
        StackData.putString(original, "visor_tacz_jam", "STOVEPIPE");
        check(StackData.read(original).getBoolean("visor_tacz_open"), "boolean persisted");
        check(StackData.read(original).getInt("visor_tacz_spent") == 3, "integer persisted");
        check(StackData.read(original).getString("visor_tacz_jam").equals("STOVEPIPE"), "string persisted");
        check(StackData.read(original).getString("other_mod").equals("preserved"), "unrelated custom data retained");
        check(original.get(DataComponents.MAX_STACK_SIZE) == 16, "unrelated component retained");
        ItemStack copy = original.copy();
        StackData.putBoolean(copy, "visor_tacz_open", false);
        StackData.remove(copy, "visor_tacz_spent");
        check(StackData.read(original).getBoolean("visor_tacz_open"), "copy mutation leaves original unchanged");
        check(!StackData.read(copy).getBoolean("visor_tacz_open"), "copy boolean persisted");
        check(!StackData.read(copy).contains("visor_tacz_spent"), "copy removal persisted");
        check(StackData.read(original).getInt("visor_tacz_spent") == 3, "copy removal leaves original unchanged");
        StackData.read(original).putInt("visor_tacz_spent", 99);
        check(StackData.read(original).getInt("visor_tacz_spent") == 3, "read snapshots cannot mutate components");
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        try {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, original);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, ItemStack.EMPTY);
            ItemStack decoded = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            check(ItemStack.isSameItemSameComponents(original, decoded), "packet preserves custom and unrelated components");
            check(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer).isEmpty(), "packet supports empty offhand stack");
            check(!buffer.isReadable(), "packet codec consumes the full payload");
        } finally {
            buffer.release();
        }
        System.out.println("StackData component persistence checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
