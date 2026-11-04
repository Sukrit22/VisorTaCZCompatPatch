package dev.visorcompat.tacz.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Compatibility state lives in CUSTOM_DATA; each mutation persists a fresh component. */
public final class StackData {
    private StackData() {}
    public static boolean has(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }
    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
    public static boolean booleanValue(ItemStack stack,String key){return read(stack).getBooleanOr(key,false);}
    public static int intValue(ItemStack stack,String key){return read(stack).getIntOr(key,0);}
    public static String stringValue(ItemStack stack,String key){return read(stack).getStringOr(key,"");}
    public static void putBoolean(ItemStack stack, String key, boolean value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(key, value));
    }
    public static void putInt(ItemStack stack, String key, int value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(key, value));
    }
    public static void putString(ItemStack stack, String key, String value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(key, value));
    }
    public static void remove(ItemStack stack, String key) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(key));
    }
}
