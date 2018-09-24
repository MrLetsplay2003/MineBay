package me.mrletsplay.minebay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class UpdateChecker {

	public static void checkForUpdate(Player... pls){
		try {
			URL updUrl = new URL("https://graphite-official.com/api/plugin-data/MineBay/version.txt");
			BufferedReader r = new BufferedReader(new InputStreamReader(updUrl.openStream(), StandardCharsets.UTF_8));
			String ver = r.readLine();
			if(!ver.equalsIgnoreCase(Main.PLUGIN_VERSION)){
				for(Player p : pls){
					p.sendMessage("§aThere's an update available for MineBay");
					p.sendMessage(ver+":");
				}
				String ln;
				while((ln = r.readLine()) != null){
					for(Player p : pls){
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', ln));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
