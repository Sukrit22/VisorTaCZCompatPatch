package dev.visorcompat.tacz.fabric;
import net.fabricmc.api.ClientModInitializer;
import dev.visorcompat.tacz.client.ClientAddon;
import dev.visorcompat.tacz.client.CompatSettings;
import org.vmstudio.visor.api.VisorAPI;
public final class FabricClient implements ClientModInitializer {
 @Override public void onInitializeClient(){CompatSettings.register();dev.visorcompat.tacz.fabric.network.ClientNetwork.register(dev.visorcompat.tacz.network.CompatNetwork.CHANNEL);FabricEvents.register();VisorAPI.registerAddon(new ClientAddon());}
}
