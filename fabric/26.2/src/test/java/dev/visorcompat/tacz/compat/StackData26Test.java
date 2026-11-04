package dev.visorcompat.tacz.compat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import io.netty.buffer.Unpooled;

/** Uses real 26.2 ItemStack components and codecs; no fake Minecraft data containers. */
class StackData26Test {
    static {
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        // Isolated stone fixture with vanilla common defaults. 26.2 normally binds
        // registry components at resource load, including tag-dependent values.
        // This tests real stack/component codecs, not that full loading lifecycle.
        Items.STONE.builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
    }
    @Test void mutationAndCopyPreserveUnrelatedData(){
        var original=new ItemStack(Items.STONE);
        StackData.putString(original,"other_mod","preserved");
        original.set(DataComponents.MAX_STACK_SIZE,16);
        StackData.putInt(original,"visor_tacz_rounds",7);
        StackData.putBoolean(original,"visor_tacz_open",true);
        var copy=original.copy();StackData.remove(copy,"visor_tacz_rounds");
        assertEquals(7,StackData.intValue(original,"visor_tacz_rounds"));
        assertEquals(0,StackData.intValue(copy,"visor_tacz_rounds"));
        assertEquals("preserved",StackData.stringValue(copy,"other_mod"));
        assertEquals(16,copy.get(DataComponents.MAX_STACK_SIZE));
        assertTrue(StackData.booleanValue(copy,"visor_tacz_open"));
        StackData.read(original).putInt("visor_tacz_rounds",99);
        assertEquals(7,StackData.intValue(original,"visor_tacz_rounds"));
    }
    @Test void absentAndWrongTypedValuesUseDefaults(){
        var stack=new ItemStack(Items.STONE);assertFalse(StackData.has(stack));
        assertFalse(StackData.booleanValue(stack,"missing"));assertEquals(0,StackData.intValue(stack,"missing"));
        StackData.putString(stack,"visor_tacz_rounds","invalid");assertEquals(0,StackData.intValue(stack,"visor_tacz_rounds"));
        StackData.remove(stack,"visor_tacz_rounds");assertFalse(StackData.has(stack));
    }
    @Test void packetRoundTripKeepsCustomStateAndEmptyHand(){
        var stack=new ItemStack(Items.STONE);StackData.putInt(stack,"visor_tacz_rounds",9);
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        try{
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer,stack);ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer,ItemStack.EMPTY);
            var decoded=ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            assertTrue(ItemStack.isSameItemSameComponents(stack,decoded));
            assertTrue(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer).isEmpty());assertFalse(buffer.isReadable());
        }finally{buffer.release();}
    }
}
