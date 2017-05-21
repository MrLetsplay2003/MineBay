package me.mrletsplay.minebay;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{

	public static Economy econ;
	public static Plugin pl;
	
	@Override
	public void onEnable() {
		pl = this;
		initConfig();
		Bukkit.getPluginManager().registerEvents(new Events(), this);
		if(!setupEconomy()){
			getLogger().info("Failed to register economy! Disabling...");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}else{
			getLogger().info("Enabled");
		}
		if(!AuctionRooms.getAuctionRoomIDs().contains(0) || Config.Config.getBoolean("minebay.default-auction-room.applySettings")){
			System.out.println("Creating default room...");
			AuctionRoom defRoom = AuctionRooms.createAuctionRoom(null, 0);
			defRoom.setSlots(Config.Config.getInt("minebay.default-auction-room.slots"));
			defRoom.setTaxshare(Config.Config.getInt("minebay.default-auction-room.taxshare"));
			defRoom.setName(Config.Config.getString("minebay.default-auction-room.name"));
			defRoom.setIconMaterial(Material.getMaterial(Config.Config.getString("minebay.default-auction-room.icon-material")));
			defRoom.saveAllSettings();
			Config.Config.set("minebay.default-auction-room.applySettings", false);
			Config.save();
			System.out.println("Created!");
		}
		new Metrics(this);
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}
	
	private boolean setupEconomy()
    {
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
		if(label.equalsIgnoreCase("minebay")){
			if(sender instanceof Player){
				Player p = (Player)sender;
				if(args.length>=1){
					if(args[0].equalsIgnoreCase("open")){
						p.openInventory(MineBay.getRoomSelectionMenu(0, "all", p));
						return true;
					}else if(args[0].equalsIgnoreCase("sell")){
						if(args.length==2){
							try{
								int price = Integer.parseInt(args[1]);
								if(price > 0){
									if(p.getItemInHand()!=null && !p.getItemInHand().getType().equals(Material.AIR)){
										p.openInventory(MineBay.getSellRoomSelectionMenu(0, "all", price));
										return true;
									}else{
										p.sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell.error.noitem")));
										return true;
									}
								}else{
									p.sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell.error.toocheap")));
								}
							}catch(Exception e){
								sendCommandHelp(p);
								return true;
							}
						}else{
							sendCommandHelp(p);
							return true;
						}
					}else if(args[0].equalsIgnoreCase("create")){
						int mRooms = Config.Config.getInt("minebay.user-rooms.max-rooms");
						if(p.hasPermission("minebay.user-rooms.create.unlimited")){
							mRooms = -1;
						}else{
							for(String perm : Config.Config.getStringList("room-perms")){
								if(p.hasPermission(perm)){
									int r = Config.Config.getInt("room-perm."+perm+".max-rooms");
									if(r>mRooms){
										mRooms = r;
									}
								}
							}
						}
						List<AuctionRoom> rooms = AuctionRooms.getAuctionRoomsByOwner(p.getName());
						if(rooms.size() < mRooms || mRooms == -1){
							AuctionRoom r = AuctionRooms.createAuctionRoom(p.getName(), AuctionRooms.getNewRoomID());
							MineBay.updateRoomSelection();
							p.openInventory(r.getSettingsMenu());
							p.sendMessage(Config.replaceForAuctionRoom(Config.simpleReplace(Config.Config.getString("minebay.info.room-created")), r));
						}else{
							p.sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.room-create.error.too-many-rooms")));
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
		p.sendMessage(Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §cHelp");
		p.sendMessage("§7/minebay open §8- Opens the MineBay auction room selection menu");
		p.sendMessage("§7/minebay sell <Price> §8- Put an item for sale on MineBay");
		p.sendMessage("§7/minebay create §8- Create an auction room");
	}
	
}
