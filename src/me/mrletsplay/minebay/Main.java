package me.mrletsplay.minebay;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrletsplay.minebay.economy.MineBayEconomy;
import me.mrletsplay.minebay.economy.ReserveEconomy;
import me.mrletsplay.minebay.economy.TokenEnchantEconomy;
import me.mrletsplay.minebay.economy.VaultEconomy;

public class Main extends JavaPlugin{

	public static MineBayEconomy econ;
	public static JavaPlugin pl;
	
	public static MineBayCommandExecutor commandExecutor = new MineBayCommandExecutor();
	public static MineBayTabCompleter tabCompleter = new MineBayTabCompleter();
	
	public static String pluginVersion;
	
	/**
	 * Sell button
	 * Open room cmd
	 * Edit name fix
	 */
	
	@Override
	public void onEnable() {
		pl = this;
		MrCoreBukkitImpl.loadMrCore(this);
		pluginVersion = getDescription().getVersion();
		initConfig();
		Bukkit.getPluginManager().registerEvents(new Events(), this);
		getCommand("minebay").setTabCompleter(tabCompleter);
		getCommand("minebay").setExecutor(commandExecutor);
		
		if(!setupEconomy()){
			getLogger().info("Failed to register economy! Disabling...");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		}else{
			getLogger().info("Enabled");
		}
		
		if(Config.enableNPCs) {
			if(!Bukkit.getPluginManager().isPluginEnabled("Citizens")) throw new RuntimeException("Citizens is required in order for NPCs to function");
			MineBayNPCs.init();
		}
		
		if(!AuctionRooms.getAuctionRoomIDs().contains(0)){
			getLogger().info("Creating default room...");
			AuctionRoom r = AuctionRooms.createAuctionRoom(null, 0, true);
			r.setSlots(-1);
			r.saveAllSettings();
			getLogger().info("Created!");
		}
		if(Config.config.getBoolean("minebay.general.enable-update-check")){
			getLogger().info("Checking for update...");
			List<Player> pls = new ArrayList<>();
			for(Player pl : Bukkit.getOnlinePlayers()){
				if(pl.hasPermission("minebay.notify-update")){
					pls.add(pl);
				}
			}
			UpdateChecker.checkForUpdate(pls.toArray(new Player[pls.size()]));
			getLogger().info("Finished!");
		}
		for(String a : Config.config.getStringList("minebay.general.command-aliases")) {
			DynamicCommand c = new DynamicCommand(a);
			BukkitCommandUtil.registerCommand(c, getName());
		}
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}
	
	private boolean setupEconomy() {
		String economy = Config.economy;
		getLogger().info("Using " + economy + " economy");
		switch(economy.toLowerCase()) {
			case "tokenenchant":
				if(!Bukkit.getPluginManager().isPluginEnabled("TokenEnchant")) throw new RuntimeException("TokenEnchant economy is selected, but TokenEnchant is not present");
				econ = new TokenEnchantEconomy();
				return true;
			case "vault":
				if(!Bukkit.getPluginManager().isPluginEnabled("Vault")) throw new RuntimeException("Vault economy is selected, but Vault is not present");
				RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
				if (economyProvider != null) {
					econ = new VaultEconomy(economyProvider.getProvider());
				}
				return (econ != null);
			case "reserve":
				if(!Bukkit.getPluginManager().isPluginEnabled("Reserve")) throw new RuntimeException("Reserve economy is selected, but Reserve is not present");
				econ = new ReserveEconomy();
				return true;
			default:
				throw new IllegalArgumentException("Invalid economy \""+economy+"\" provided");
		}
    }
	
	private void initConfig(){
		Config.init();
	}
	
}
