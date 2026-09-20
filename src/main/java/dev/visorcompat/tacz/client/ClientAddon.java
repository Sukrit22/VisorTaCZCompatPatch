package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.VisorTacz;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;

public final class ClientAddon implements VisorAddon {
    @Override public void onAddonLoad() {
        ClientControls controls = new ClientControls();
        VisorAPI.eventBus().registerListener(this, controls);
        MinecraftForge.EVENT_BUS.register(controls);
        VisorAPI.addonManager().getRegistries().handEffects().registerComponent(new GunRenderer(this));
        VisorAPI.addonManager().getRegistries().handEffects().registerComponent(new StatusPanel(this));
        VisorAPI.addonManager().getRegistries().gameEffects().registerComponent(new LateGunRenderer(this));
    }
    @Override public String getAddonId() { return VisorTacz.ID; }
    @Override public String getModId() { return VisorTacz.ID; }
    @Override public Component getAddonName() { return Component.literal("TaCZ VR"); }
}
