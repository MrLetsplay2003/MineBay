package me.mrletsplay.minebay;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUI;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIMultiPage;
import me.mrletsplay.mrcore.config.CustomConfig.InvalidConfigException;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{

	public static Economy econ;
	public static Plugin pl;
	
	public static String PLUGIN_VERSION;
	
	/**
	 * Sell button
	 * Open room cmd
	 * Edit name fix
	 */
	
	public static GUI createRoomGUI;
	
	@Override
	public void onEnable() {
		pl = this;
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
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			econ = economyProvider.getProvider();
		}
		return (econ != null);
    }
	
	private void initConfig(){
		Config.init();
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("minebay")){
			if(sender instanceof Player){
				Player p = (Player)sender;
				p.openInventory(AuctionRoomsGUI.getGUI(null).getForPlayer(p));
				if(args.length>=1){
					if(args[0].equalsIgnoreCase("open")){
						if(args.length == 1){
							if(Config.config.getBoolean("minebay.general.enable-user-rooms")){
								p.openInventory(MineBay.getRoomSelectionMenu(0, "all", p));
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
							if(args.length == 1){
								try {
									Config.config.reloadConfig(false);
								} catch (InvalidConfigException | IOException e) {
									e.printStackTrace();
								}
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
							try{
								BigDecimal price = new BigDecimal(args[1]);
								if(price.compareTo(new BigDecimal("0")) == 1){ // > 0
									if(p.getItemInHand()!=null && !p.getItemInHand().getType().equals(Material.AIR)){
										if(Config.config.getBoolean("minebay.general.enable-user-rooms")){
											CancelTask.cancelForPlayer(p);
											p.openInventory(MineBay.getSellRoomSelectionMenu(0, "all", price));
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
						if(Config.config.getBoolean("minebay.general.enable-user-rooms") && (Config.config.getBoolean("minebay.general.allow-room-creation") || p.hasPermission("minebay.user-rooms.create.when-disallowed"))){
							if(MineBay.hasPermissionToCreateRoom(p)){
								p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.GRASS, 1, 0, "§8Buy Auction Room", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.room-price"))));
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
							p.openInventory(r.getSettingsMenu());
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
	}
	
}
