package me.mrletsplay.minebay;

import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MineBayCommandExecutor implements CommandExecutor {
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
							p.openInventory(GUIs.getAuctionRoomsGUI(p, null));
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
							Bukkit.getPluginManager().disablePlugin(Main.pl);
							Bukkit.getPluginManager().enablePlugin(Main.pl);
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
							if(p.getItemInHand()!=null && !p.getItemInHand().getType().equals(Material.AIR)){
								ItemStack item = p.getItemInHand();
								BigDecimal price = new BigDecimal(args[1]);
								BigDecimal minPrice = Config.getMinimumPrice(item);
								if(minPrice.compareTo(BigDecimal.ZERO) == 1 && price.compareTo(minPrice.multiply(BigDecimal.valueOf(item.getAmount()))) == -1) { // minPrice > 0 && pr < x * minPrice
									p.sendMessage(Config.getMessage("minebay.info.sell.error.below-min-price", "min-price", minPrice.toString(), "total-min-price", minPrice.multiply(BigDecimal.valueOf(item.getAmount())).toString()));
									return true;
								}
								BigDecimal maxPrice = Config.getMaximumPrice(item);
								if(maxPrice.compareTo(BigDecimal.ZERO) == 1 && price.compareTo(maxPrice.multiply(BigDecimal.valueOf(item.getAmount()))) == 1) { // maxPrice > 0 && pr > x * maxPrice
									p.sendMessage(Config.getMessage("minebay.info.sell.error.above-max-price", "max-price", maxPrice.toString(), "total-max-price", maxPrice.multiply(BigDecimal.valueOf(item.getAmount())).toString()));
									return true;
								}
								if(price.compareTo(new BigDecimal("0")) == 1){ // > 0
									if(Config.config.getBoolean("minebay.general.enable-user-rooms")){
										CancelTask.cancelForPlayer(p);
										p.openInventory(GUIs.getAuctionRoomsSellGUI(p, null, price));
									}else{
										CancelTask.cancelForPlayer(p);
										AuctionRoom main = MineBay.getMainAuctionRoom();
										if(main.getSoldItemsBySeller(p).size() < Config.config.getInt("minebay.user-rooms.offers-per-slot")){
											SellItem it = new SellItem(item, main, (Config.useUUIDs?p.getUniqueId().toString():p.getName()), price, main.getNewItemID());
											main.addSellItem(it);
											p.setItemInHand(new ItemStack(Material.AIR));
											p.sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.sell.success"), it, main));
										}else {
											p.sendMessage(Config.getMessage("minebay.info.sell.error.too-many-sold"));
										}
									}
									return true;
								}else{
									p.sendMessage(Config.getMessage("minebay.info.sell.error.toocheap"));
								}
							}else{
								p.sendMessage(Config.getMessage("minebay.info.sell.error.noitem"));
								return true;
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
							p.openInventory(GUIs.buyRoomGUI(p));

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
						p.openInventory(GUIs.getAuctionRoomSettingsGUI(p, r.getID()));
						p.sendMessage(Config.replaceForAuctionRoom(Config.getMessage("minebay.info.room-created"), r));
					}else{
						sendCommandHelp(p);
					}
				}else if(args[0].equalsIgnoreCase("version")){
					if(p.hasPermission("minebay.version")){
						p.sendMessage("Current MineBay version: §7" + Main.pluginVersion);
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
				}else if(args[0].equalsIgnoreCase("spawnnpc")) {
					if(!Config.enableNPCs) {
						p.sendMessage(Config.getMessage("minebay.info.npcs-disabled"));
						return true;
					}
					p.openInventory(GUIs.getAuctionRoomsSpawnNPCGUI(p, p.getName()));
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
		return false;
	}

	private void sendCommandHelp(Player p) {
		p.sendMessage(Config.prefix+" §cHelp");
		p.sendMessage("§7/minebay open §8- Opens the MineBay auction room selection menu");
		p.sendMessage("§7/minebay sell <Price> §8- Put an item for sale on MineBay");
		p.sendMessage("§7/minebay create §8- Create an auction room");
		p.sendMessage("§7/minebay spawnnpc [room id] §8- Spawn an auction room npc");
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
