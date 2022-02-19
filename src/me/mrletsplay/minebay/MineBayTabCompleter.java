package me.mrletsplay.minebay;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MineBayTabCompleter implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> tabCompletions = new ArrayList<>();
		if(args.length == 1){
			if("open".startsWith(args[0])){
				tabCompletions.add("open");
			}
			
			if("sell".startsWith(args[0])){
				tabCompletions.add("sell");
			}
			
			if("create".startsWith(args[0])){
				tabCompletions.add("create");
			}
			
			if(Config.enableNPCs && "spawnnpc".startsWith(args[0])) {
				tabCompletions.add("spawnnpc");
			}
			
			if(sender.hasPermission("minebay.reload") && "reload".startsWith(args[0])){
				tabCompletions.add("reload");
			}
			
			if(sender.hasPermission("minebay.default-room.create") && "createdefault".startsWith(args[0])){
				tabCompletions.add("createdefault");
			}
			
			if(sender.hasPermission("minebay.version") && "version".startsWith(args[0])){
				tabCompletions.add("version");
			}
		}
		return tabCompletions;
	}

}
