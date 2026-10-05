package dev.visorcompat.tacz.fabric.event;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
public record RegisterClientCommandsEvent(CommandDispatcher<FabricClientCommandSource> dispatcher) {public CommandDispatcher<FabricClientCommandSource> getDispatcher(){return dispatcher;}}
