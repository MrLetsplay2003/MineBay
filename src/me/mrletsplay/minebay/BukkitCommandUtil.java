package me.mrletsplay.minebay;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpMap;

import me.mrletsplay.mrcore.bukkitimpl.versioned.NMSVersion;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class BukkitCommandUtil {
	
	public static void registerCommand(Command command, String fallbackPrefix) {
		try {
			Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			f.setAccessible(true);
			CommandMap map = (CommandMap) f.get(Bukkit.getServer());

			map.register(command.getLabel(), fallbackPrefix, command);
			
			if(NMSVersion.getCurrentServerVersion().isNewerThanOrEqualTo(NMSVersion.V1_13_R1)) {
				final Field f3 = Bukkit.getServer().getClass().getDeclaredField("helpMap");
				f3.setAccessible(true);
				HelpMap m = (HelpMap) f3.get(Bukkit.getServer());
				m.getClass().getDeclaredMethod("initializeCommands").invoke(m);
				
				Method sync = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
				sync.setAccessible(true);
				sync.invoke(Bukkit.getServer());
				
				Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
			}
		} catch (Exception e) {
			throw new FriendlyException(e);
		}
	}

}
