package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.Profiles;
import com.tacz.guns.api.item.IGun;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid="visor_tacz",value=Dist.CLIENT)
public final class GunSupportTooltip {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event){append(event.getItemStack(),event.getToolTip());}
    public static void append(ItemStack stack,List<Component> lines){
        if(IGun.getIGunOrNull(stack)==null)return;
        var profile=Profiles.get(stack);
        lines.add(Component.literal("VR: ").withStyle(ChatFormatting.GRAY)
            .append(badge("B",profile!=null)).append(Component.literal(" · ").withStyle(ChatFormatting.GRAY))
            .append(badge("P",profile!=null&&profile.physical())).append(Component.literal(" · ").withStyle(ChatFormatting.GRAY))
            .append(badge("AH",profile!=null)));
        var catalog=GunReviewStore.get();var entry=catalog.guns().get(Profiles.key(stack));
        if(entry!=null)lines.add(Component.literal(catalog.packs().get(entry.pack())).withStyle(ChatFormatting.GRAY));
        boolean calibrated=entry!=null&&entry.calibrated()&&!GunReviewStore.failed();
        lines.add(Component.literal(GunReviewStore.failed()?"Calibration review file error":"Calibration: "+(calibrated?"verified":"unverified"))
            .withStyle(calibrated?ChatFormatting.GREEN:ChatFormatting.RED));
        if(entry!=null&&!entry.notes().isBlank())lines.add(Component.literal(entry.notes()).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal("B: buttons  P: physical  AH: advanced holding").withStyle(ChatFormatting.DARK_GRAY));
    }
    private static Component badge(String name,boolean supported){return Component.literal(name+(supported?" ✓":" ✗")).withStyle(supported?ChatFormatting.GREEN:ChatFormatting.RED);}
}
