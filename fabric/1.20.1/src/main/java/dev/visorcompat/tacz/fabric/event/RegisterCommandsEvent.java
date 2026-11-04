package dev.visorcompat.tacz.fabric.event;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
public record RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher) {public CommandDispatcher<CommandSourceStack> getDispatcher(){return dispatcher;}}
