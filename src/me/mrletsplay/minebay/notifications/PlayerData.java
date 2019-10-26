package me.mrletsplay.minebay.notifications;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

import me.mrletsplay.minebay.Config;
import me.mrletsplay.minebay.Main;
import me.mrletsplay.mrcore.bukkitimpl.config.BukkitCustomConfig;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;
import me.mrletsplay.mrcore.misc.Complex;

public class PlayerData {

	public static File dataFile = new File(Main.pl.getDataFolder(), "data.yml");
	private static FileCustomConfig data = ConfigLoader.loadConfigFromFile(new BukkitCustomConfig(dataFile), dataFile, true);
	
	static {
		data.registerMapper(JSONObjectMapper.create(OfflineNotification.class));
	}
	
	public static void addOfflineNotification(OfflinePlayer player, OfflineNotification notification) {
		if(player.isOnline()) {
			notification.send(player.getPlayer());
			return;
		}
		List<OfflineNotification> ns = getOfflineNotifications(player);
		ns.add(notification);
		data.set(Config.useUUIDs ? player.getUniqueId().toString() : player.getName() + ".notifications", ns);
		data.saveToFile();
	}
	
	public static List<OfflineNotification> getOfflineNotifications(OfflinePlayer player) {
		return data.getComplex(Config.useUUIDs ? player.getUniqueId().toString() : player.getName() + ".notifications", Complex.list(OfflineNotification.class), new ArrayList<>(), false);
	}
	
	public static void resetOfflineNotifications(OfflinePlayer player) {
		data.unset(Config.useUUIDs ? player.getUniqueId().toString() : player.getName() + ".notifications");
		data.saveToFile();
	}

}
