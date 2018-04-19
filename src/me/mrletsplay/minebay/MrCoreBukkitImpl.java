package me.mrletsplay.minebay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MrCoreBukkitImpl {
	
	public static final String MRCORE_PLUGIN_NAME = "MrCore_BukkitImpl";

	public static void loadMrCore(JavaPlugin plugin) {
		if(Bukkit.getPluginManager().isPluginEnabled(MRCORE_PLUGIN_NAME)) {
			return;
		}
		plugin.getLogger().info("Couldn't find "+MRCORE_PLUGIN_NAME+", so it seems like we need to download it from GitHub...");
		try {
//			String oldProtocol = System.getProperty("http.protocols");
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			JSONObject release = (JSONObject) new JSONParser().parse(new InputStreamReader(new URL("https://api.github.com/repos/MrLetsplay2003/MrCore/releases/latest").openStream()));
			JSONArray assets = (JSONArray) release.get("assets");
			JSONObject asset = (JSONObject) assets.get(0);
			String downloadL = (String) asset.get("browser_download_url");
			File mrCoreFile = new File("plugins/"+MRCORE_PLUGIN_NAME+".jar");
			if(mrCoreFile.exists()) {
				plugin.getLogger().info("A file named \""+mrCoreFile.getName()+"\" already exists, assuming that MrCore was already loaded");
				return;
			}
			plugin.getLogger().info("Downloading from "+downloadL+"...");
			download(new URL(downloadL), mrCoreFile);
			Bukkit.getPluginManager().loadPlugin(mrCoreFile);
			plugin.getLogger().info("Down-/loaded MrCore successfully");
//			System.setProperty("https.protocols", oldProtocol);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	private static void download(URL url, File file) {
		try {
			file.getParentFile().mkdirs();
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(file);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
