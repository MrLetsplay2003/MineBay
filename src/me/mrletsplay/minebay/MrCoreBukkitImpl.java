package me.mrletsplay.minebay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MrCoreBukkitImpl {
	
	public static final String MRCORE_PLUGIN_NAME = "MrCore_BukkitImpl";

	/**
	 * This will load (or download) the MrCore lib<br>
	 * Version "latest" is to be used with caution, because i'm horrible at sustaining backwards-compatability
	 * @param plugin The plugin to load it for
	 */
	public static void loadMrCore(JavaPlugin plugin) {
		if(Bukkit.getPluginManager().isPluginEnabled(MRCORE_PLUGIN_NAME)) {
			return;
		}
		plugin.getLogger().info("Updating/Downloading MrCore from GitHub...");
		try {
			String oldProtocol = System.getProperty("http.protocols");
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			
			File mrCoreFile = new File("plugins/"+MRCORE_PLUGIN_NAME+".jar");
			if(mrCoreFile.exists()) {
				plugin.getLogger().info("A file named \""+mrCoreFile.getName()+"\" already exists, assuming that MrCore was already loaded");
				return;
			}
			JSONObject release = (JSONObject) new JSONParser().parse(new InputStreamReader(new URL("https://api.github.com/repos/MrLetsplay2003/MrCore/releases/latest").openStream()));
			JSONArray assets = (JSONArray) release.get("assets");
			JSONObject asset = (JSONObject) assets.get(0); // The attached MrCore.jar file
			String downloadL = (String) asset.get("browser_download_url");
			plugin.getLogger().info("Downloading from "+downloadL+"...");
			download(new URL(downloadL), mrCoreFile);
			plugin.getLogger().info("Downloaded MrCore jar");
			Plugin p = Bukkit.getPluginManager().loadPlugin(mrCoreFile);
			Bukkit.getPluginManager().enablePlugin(p);
			plugin.getLogger().info("Loaded MrCore successfully");
			
			if(oldProtocol != null) System.setProperty("https.protocols", oldProtocol);
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
