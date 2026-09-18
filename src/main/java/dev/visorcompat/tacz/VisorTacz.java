package dev.visorcompat.tacz;

import dev.visorcompat.tacz.client.ClientAddon;
import dev.visorcompat.tacz.server.ServerEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import dev.visorcompat.tacz.network.CompatNetwork;
import org.vmstudio.visor.api.VisorAPI;

@Mod(VisorTacz.ID)
public final class VisorTacz {
    public static final String ID = "visor_tacz";

    public VisorTacz() {
        // Require the same addon protocol on both ends; no second shooting packet is sent.
        CompatNetwork.register();
        MinecraftForge.EVENT_BUS.register(ServerEvents.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientRegistration::register);
    }

    private static final class ClientRegistration {
        static void register() {
            dev.visorcompat.tacz.client.CompatSettings.register();
            VisorAPI.registerAddon(new ClientAddon());
        }
    }
}
