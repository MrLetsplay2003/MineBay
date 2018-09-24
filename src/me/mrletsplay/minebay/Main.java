package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrletsplay.minebay.economy.MineBayEconomy;
import me.mrletsplay.minebay.economy.ReserveEconomy;
import me.mrletsplay.minebay.economy.TokenEnchantEconomy;
import me.mrletsplay.minebay.economy.VaultEconomy;

public class Main extends JavaPlugin{

	public static MineBayEconomy econ;
	public static JavaPlugin pl;
	
	public static String PLUGIN_VERSION;
	
	/**
	 * Sell button
	 * Open room cmd
	 * Edit name fix
	 */
	
	@Override
	public void onEnable() {
		pl = this;
		MrCoreBukkitImpl.loadMrCore(this);
		PLUGIN_VERSION = getDescription().getVersion();
		initConfig();
		Bukkit.getPluginManager().registerEvents(new Events(), this);
		getCommand("minebay").setTabCompleter(new MineBayTabCompleter());
		if(!setupEconomy()){
			getLogger().info("Failed to register economy! Disabling...");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}else{
			getLogger().info("Enabled");
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
		new Metrics(this);
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}
	
	private boolean setupEconomy() {
		String economy = Config.economy;
		getLogger().info("Using "+economy+" economy");
		switch(economy.toLowerCase()) {
			case "tokenenchant":
				econ = new TokenEnchantEconomy();
				return true;
			case "vault":
				RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
				if (economyProvider != null) {
					econ = new VaultEconomy(economyProvider.getProvider());
				}
				return (econ != null);
			case "reserve":
				econ = new ReserveEconomy();
				return true;
			default:
				throw new IllegalArgumentException("Invalid economy \""+economy+"\" provided");
		}
    }
	
	private void initConfig(){
		Config.init();
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//		((Player)sender).getInventory().addItem(ItemUtils.createItem(VersionedMaterial.GOLDEN_AXE, 1, "§cTest", "§6Test!"));
		if(command.getName().equalsIgnoreCase("minebay")){
			if(sender instanceof Player){
				Player p = (Player)sender;
				if(args.length>=1){
					if(args[0].equalsIgnoreCase("open")){
						if(args.length == 1){
							if(!Config.openPermission.equalsIgnoreCase("none") && !p.hasPermission(Config.openPermission)) {
								p.sendMessage(Config.getMessage("minebay.info.permission-missing.open"));
								return true;
							}
							if(Config.config.getBoolean("minebay.general.enable-user-rooms")){
								p.openInventory(GUIs.getAuctionRoomsGUI(null).getForPlayer(p));
								CancelTask.cancelForPlayer(p);
							}else{
								p.openInventory(MineBay.getMainAuctionRoom().getMineBayInv(0, p));
							}
						}else{
							sendCommandHelp(p);
						}
						return true;
					}else if(args[0].equalsIgnoreCase("reload")){
						if(p.hasPermission("minebay.reload")){
							if(args.length == 1) {
								Config.reload();
								Bukkit.getPluginManager().disablePlugin(this);
								Bukkit.getPluginManager().enablePlugin(this);
								p.sendMessage(Config.getMessage("minebay.info.reload-complete"));
							}else{
								sendCommandHelp(p);
							}
						}else{
							p.sendMessage(Config.getMessage("minebay.info.reload-no-permission"));
						}
						return true;
					}else if(args[0].equalsIgnoreCase("sell")){
						if(args.length==2){
							if(!Config.sellPermission.equalsIgnoreCase("none") && !p.hasPermission(Config.sellPermission)) {
								p.sendMessage(Config.getMessage("minebay.info.permission-missing.sell"));
								return true;
							}
							try{
								BigDecimal price = new BigDecimal(args[1]);
								if(price.compareTo(new BigDecimal("0")) == 1){ // > 0
									if(p.getItemInHand()!=null && !p.getItemInHand().getType().equals(Material.AIR)){
										if(Config.config.getBoolean("minebay.general.enable-user-rooms")){
											CancelTask.cancelForPlayer(p);
											p.openInventory(GUIs.getAuctionRoomsSellGUI(null, price).getForPlayer(p));
										}else{
											CancelTask.cancelForPlayer(p);
											AuctionRoom main = MineBay.getMainAuctionRoom();
											SellItem it = new SellItem(p.getItemInHand(), main, (Config.use_uuids?p.getUniqueId().toString():p.getName()), price, main.getNewItemID());
											main.addSellItem(it);
											p.setItemInHand(new ItemStack(Material.AIR));
											p.sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.sell.success"), it, main));
										}
										return true;
									}else{
										p.sendMessage(Config.getMessage("minebay.info.sell.error.noitem"));
										return true;
									}
								}else{
									p.sendMessage(Config.getMessage("minebay.info.sell.error.toocheap"));
								}
							}catch(NumberFormatException e){
								sendCommandHelp(p);
								return true;
							}
						}else{
							sendCommandHelp(p);
							return true;
						}
					}else if(args[0].equalsIgnoreCase("create")){
						if(!Config.createPermission.equalsIgnoreCase("none") && !p.hasPermission(Config.createPermission)) {
							p.sendMessage(Config.getMessage("minebay.info.permission-missing.create"));
							return true;
						}
						if(Config.config.getBoolean("minebay.general.enable-user-rooms") && (Config.config.getBoolean("minebay.general.allow-room-creation") || p.hasPermission("minebay.user-rooms.create.when-disallowed"))){
							if(MineBay.hasPermissionToCreateRoom(p)){
								p.openInventory(GUIs.buyRoomGUI().getForPlayer(p));

							}else{
								p.sendMessage(Config.getMessage("minebay.info.room-create.error.too-many-rooms"));
							}
						}else{
							p.sendMessage(Config.getMessage("minebay.info.user-rooms-disabled"));
						}
					}else if(args[0].equalsIgnoreCase("createdefault")){
						if(p.hasPermission("minebay.default-rooms.create")){
							AuctionRoom r = AuctionRooms.createAuctionRoom(null, AuctionRooms.getNewRoomID(), true);
							r.setSlots(-1);
							r.saveAllSettings();
							MineBay.updateRoomSelection();
							p.openInventory(r.getSettingsGUI().getForPlayer(p));
							p.sendMessage(Config.replaceForAuctionRoom(Config.getMessage("minebay.info.room-created"), r));
						}else{
							sendCommandHelp(p);
						}
					}else if(args[0].equalsIgnoreCase("version")){
						if(p.hasPermission("minebay.version")){
							p.sendMessage("Current MineBay version: §7"+PLUGIN_VERSION);
							if(Config.config.getBoolean("minebay.general.update-check-on-command")){
								UpdateChecker.checkForUpdate(p);
							}
						}else{
							sendCommandHelp(p);
						}
					}else if(args[0].equalsIgnoreCase("filter")) {
						if(args.length == 1) {
							MineBayFilter.filterUI.sendToPlayer(p);
						}else if(args[1].equalsIgnoreCase("remove")) {
							
						}else if(args[1].equalsIgnoreCase("add")) {
							
						}else {
							sendCommandHelp(p);
						}
					}else{
						sendCommandHelp(p);
						return true;
					}
				}else{
					sendCommandHelp(p);
					return true;
				}
			}else{
				sender.sendMessage("§cThe console can't use MineBay");
				return true;
			}
		}
		return false;
	}

	private void sendCommandHelp(Player p) {
		p.sendMessage(Config.prefix+" §cHelp");
		p.sendMessage("§7/minebay open §8- Opens the MineBay auction room selection menu");
		p.sendMessage("§7/minebay sell <Price> §8- Put an item for sale on MineBay");
		p.sendMessage("§7/minebay create §8- Create an auction room");
		if(p.hasPermission("minebay.reload")){
			p.sendMessage("§7/minebay reload §8- Reload the MineBay config");
		}
		if(p.hasPermission("minebay.default-rooms.create")){
			p.sendMessage("§7/minebay createdefault §8- Create a default auction room (Auction room with no owner)");
		}
		if(p.hasPermission("minebay.version")){
			p.sendMessage("§7/minebay version §8- Shows the MineBay version and checks for an update (if enabled)");
		}
		if(p.hasPermission("minebay.filter")) {
			p.sendMessage("§7/minebay filter §8- Shows all the items on the filter");
			p.sendMessage("§7/minebay filter add [Excluded comparison parameters...] §8- Adds an item to the filter");
			p.sendMessage("§7/minebay filter remove <index> §8- Removed the item at the specified index from the filter");
		}
	}
	
}
