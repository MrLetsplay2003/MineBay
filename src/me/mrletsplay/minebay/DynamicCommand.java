package me.mrletsplay.minebay;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DynamicCommand extends Command {

	protected DynamicCommand(String name) {
		super(name);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		return Main.commandExecutor.onCommand(sender, this, commandLabel, args);
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		return Main.tabCompleter.onTabComplete(sender, this, alias, args);
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
		return Main.tabCompleter.onTabComplete(sender, this, alias, args);
	}

}
