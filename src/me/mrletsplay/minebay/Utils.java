package me.mrletsplay.minebay;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.io.IOUtils;

public class Utils {
	
	public static void addItem(Player p, ItemStack item){
		HashMap<Integer,ItemStack> excess = p.getInventory().addItem(item);
		for(Map.Entry<Integer, ItemStack> me : excess.entrySet()){
			p.getWorld().dropItem(p.getLocation(), me.getValue());
		}
	}
	
	public static String imgBase64(String url) {
		try(InputStream in = new URL(url).openStream()) {
			byte[] bytes = IOUtils.readAllBytes(in);
			return Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static boolean isUUID(String s) {
		return s.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
	}
	
}
