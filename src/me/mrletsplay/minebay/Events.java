package me.mrletsplay.minebay;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class Events implements Listener{
	
	public static HashMap<Player,Integer> changeName = new HashMap<>();
	public static HashMap<Player,Object[]> sellItem = new HashMap<>();
	

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW)
	public void onInvClick(InventoryClickEvent e){
		if(e.getInventory().getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix")))){
			try{
				if(e.getInventory().getSize() == 9*6){
					String mode = e.getInventory().getItem(46).getItemMeta().getDisplayName();
					if(mode.equals("§8Auction Room")){
						int page = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(0)));
						int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(1)));
						AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
						if(Config.Config.getBoolean("minebay.general.allow-drag-and-drop")){
							if(e.getClickedInventory()!=null && e.getClickedInventory().getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix")))){
								if(e.getCursor()!=null && !e.getCursor().getType().equals(Material.AIR) && (e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))){
									sellItem.put((Player)e.getWhoClicked(), new Object[]{roomID,e.getCursor()});
									int maxTime = Config.Config.getInt("minebay.general.max-type-time-seconds");
									if(maxTime>0){
										Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask((Player) e.getWhoClicked()), maxTime*20);
									}
									e.setCursor(new ItemStack(Material.AIR));
									e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell.type-in-price")));
									e.getWhoClicked().closeInventory();
									return;
								}
							}else if(e.getClickedInventory()!=null){
								return;
							}
						}
						if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta()){
							if(e.getCurrentItem().getItemMeta().hasDisplayName()){
								String name = e.getCurrentItem().getItemMeta().getDisplayName();
								if(name.equals("§0")){
									e.setCancelled(true);
									return;
								}else if(name.equals("§7Previous page")){
									Inventory newInv = r.getMineBayInv(page-1, (Player)e.getWhoClicked());
									if(newInv!=null){
										MineBay.changeInv(e.getInventory(), newInv);
									}
									e.setCancelled(true);
									return;
								}else if(name.equals("§7Next page")){
									Inventory newInv = r.getMineBayInv(page+1, (Player)e.getWhoClicked());
									if(newInv!=null){
										MineBay.changeInv(e.getInventory(), newInv);
									}
									e.setCancelled(true);
									return;
								}else if(name.equals("§6Back")){
									Inventory newInv = MineBay.getRoomSelectionMenu(0, "all", (Player)e.getWhoClicked());
									MineBay.changeInv(e.getInventory(), newInv);
									e.setCancelled(true);
									return;
								}
							}
							if(e.getCurrentItem().getItemMeta().hasLore() && (e.getCurrentItem().getItemMeta().getLore().size() == 3 || e.getCurrentItem().getItemMeta().getLore().size() == 4)){
								int id = Integer.parseInt(Config.onlyDigitsNoColor(e.getCurrentItem().getItemMeta().getLore().get(2)));
								SellItem it = r.getItemByID(id);
								if(it!=null){
									if(it.getSeller()!=null && !it.getSeller().equals(e.getWhoClicked().getName())){
										e.getWhoClicked().closeInventory();
										MineBay.showPurchaseConfirmDialog((Player)e.getWhoClicked(), it);
									}else{
										HashMap<Integer,ItemStack> excess = e.getWhoClicked().getInventory().addItem(it.getItem());
										for(Map.Entry<Integer, ItemStack> me : excess.entrySet()){
											e.getWhoClicked().getWorld().dropItem(e.getWhoClicked().getLocation(), me.getValue());
										}
										r.removeSellItem(id);
										e.getWhoClicked().closeInventory();
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.retract-sale.success")));
									}
								}
							}
						}
					}else if(mode.equals("§8Auction Rooms")){
						if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
							String name = e.getCurrentItem().getItemMeta().getDisplayName();
							int page = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(0)));
							String search = e.getInventory().getItem(46).getItemMeta().getLore().get(1).replace("§8Owner: §7", "");
							if(name.equals("§7Previous page")){
								Inventory newInv = MineBay.getRoomSelectionMenu(page-1, search, (Player)e.getWhoClicked());
								if(newInv!=null){
									MineBay.changeInv(e.getInventory(), newInv);
								}
								e.setCancelled(true);
								return;
							}else if(name.equals("§7Next page")){
								Inventory newInv = MineBay.getRoomSelectionMenu(page+1, search, (Player)e.getWhoClicked());
								if(newInv!=null){
									MineBay.changeInv(e.getInventory(), newInv);
								}
								e.setCancelled(true);
								return;
							}else if(name.equals("§7Your Rooms")){
								MineBay.changeInv(e.getInventory(), MineBay.getRoomSelectionMenu(0, e.getWhoClicked().getName(), (Player)e.getWhoClicked()));
							}else if(name.equals("§7All Rooms")){
								MineBay.changeInv(e.getInventory(), MineBay.getRoomSelectionMenu(0, "all", (Player)e.getWhoClicked()));
							}else if(name.equals("§aCreate new room")){
								if(Config.Config.getBoolean("minebay.general.enable-user-rooms") && (Config.Config.getBoolean("minebay.general.allow-room-creation") || e.getWhoClicked().hasPermission("minebay.user-rooms.create.when-disallowed"))){
									if(MineBay.hasPermissionToCreateRoom((Player)e.getWhoClicked())){
										e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.GRASS, 1, 0, "§8Buy Auction Room", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.room-price"))));
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.room-create.error.too-many-rooms")));
									}
								}else{
									e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.user-rooms-disabled")));
								}
							}else if(e.getCurrentItem().getItemMeta().hasLore() && e.getCurrentItem().getItemMeta().getLore().size() >= 4){
								if(e.getClick().equals(ClickType.LEFT)){
									int clRoomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getCurrentItem().getItemMeta().getLore().get(3)));
									AuctionRoom r = AuctionRooms.getAuctionRoomByID(clRoomID);
									MineBay.changeInv(e.getInventory(), r.getMineBayInv(0, (Player)e.getWhoClicked()));
								}else if(e.getClick().equals(ClickType.RIGHT) && e.getCurrentItem().getItemMeta().getLore().size() == 5){
									int clRoomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getCurrentItem().getItemMeta().getLore().get(3)));
									AuctionRoom r = AuctionRooms.getAuctionRoomByID(clRoomID);
									MineBay.changeInv(e.getInventory(), r.getSettingsMenu());
								}
							}
						}
					}else if(mode.equals("§8Settings")){
						if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
							String name = e.getCurrentItem().getItemMeta().getDisplayName();
							int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(0)));
							AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
							if(name.equals("§7Change Name")){
								Events.changeName.put((Player)e.getWhoClicked(), roomID);
								int maxTime = Config.Config.getInt("minebay.general.max-type-time-seconds");
								if(maxTime>0){
									Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask((Player) e.getWhoClicked()), maxTime*20);
								}
								e.getWhoClicked().closeInventory();
								e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.newname")));
							}else if(name.equals("§7Change Block")){
								MineBay.changeInv(e.getInventory(), r.getBlockSelectionInv());
								r.saveAllSettings();
								r.updateSettings();
								MineBay.updateRoomSelection();
							}else if(name.equals("§7Buy slot/s")){
								if(e.getClick().equals(ClickType.LEFT)){
									if(r.getSlots() < Config.Config.getInt("minebay.user-rooms.max-slots")){
										e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Buy Slot/s", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.slot-price"), "§8Room ID: §7"+r.getRoomID(), "§8Count: §71")));
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-buy.toomanyslots")));
									}
								}else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
									if(r.getSlots()+5 <= Config.Config.getInt("minebay.user-rooms.max-slots")){
										e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Buy Slot/s", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.slot-price"), "§8Room ID: §7"+r.getRoomID(), "§8Count: §75")));
									}else if(r.getSlots() < Config.Config.getInt("minebay.user-rooms.max-slots")){
										e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Buy Slot/s", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.slot-price"), "§8Room ID: §7"+r.getRoomID(), "§8Count: §7"+(Config.Config.getInt("minebay.user-rooms.max-slots")-r.getSlots()))));
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-buy.toomanyslots")));
									}
								}
							}else if(name.equals("§7Sell slot/s")){
								if(Config.Config.getBoolean("minebay.general.allow-slot-selling")){
									if(e.getClick().equals(ClickType.LEFT)){
										if(r.getSlots() > Config.Config.getInt("minebay.user-rooms.default-slot-number")){
											if(r.getOccupiedSlots() <= r.getSlots()-1){
												e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Sell Slot/s", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.slot-sell-price"), "§8Room ID: §7"+r.getRoomID(), "§8Count: §71")));
											}else{
												e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-sell.all-slots-occupied")));
											}
										}else{
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-sell.notenoughslots")));
										}
									}else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
										if(r.getSlots()-5 >= Config.Config.getInt("minebay.user-rooms.default-slot-number")){
											if(r.getOccupiedSlots() <= r.getSlots()-5){
												e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Sell Slot/s", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.slot-sell-price"), "§8Room ID: §7"+r.getRoomID(), "§8Count: §75")));
											}else{
												e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-sell.all-slots-occupied")));
											}
										}else if(r.getSlots() > Config.Config.getInt("minebay.user-rooms.default-slot-number")){
											int slotsToSell = r.getSlots()-Config.Config.getInt("minebay.user-rooms.default-slot-number");
											if(r.getOccupiedSlots() <= r.getSlots()-slotsToSell){
												e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Sell Slot/s", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.slot-sell-price"), "§8Room ID: §7"+r.getRoomID(), "§8Count: §7"+slotsToSell)));
											}
										}else{
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-sell.notenoughslots")));
										}
									}
								}else{
									e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-sell.not-allowed")));
								}
							}else if(name.equals("§7Increase Tax")){
								if(e.getClick().equals(ClickType.LEFT)){
									if(r.getTaxshare()<Config.Config.getInt("minebay.user-rooms.max-tax-percent")){
										r.setTaxshare(r.getTaxshare()+1);
										r.saveAllSettings();
										r.updateSettings();
										MineBay.updateRoomSelection();
										if(Config.Config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.success")).replace("%newtax%", ""+r.getTaxshare()));
										}
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.toohigh")));
									}
								}else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
									if(r.getTaxshare()+10<=Config.Config.getInt("minebay.user-rooms.max-tax-percent")){
										r.setTaxshare(r.getTaxshare()+10);
										r.saveAllSettings();
										r.updateSettings();
										MineBay.updateRoomSelection();
										if(Config.Config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.success")).replace("%newtax%", ""+r.getTaxshare()));
										}
									}else if(r.getTaxshare()<Config.Config.getInt("minebay.user-rooms.max-tax-percent")){
										r.setTaxshare(Config.Config.getInt("minebay.user-rooms.max-tax-percent"));
										r.saveAllSettings();
										r.updateSettings();
										MineBay.updateRoomSelection();
										if(Config.Config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.success")).replace("%newtax%", ""+r.getTaxshare()));
										}
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.toohigh")));
									}
								}
							}else if(name.equals("§7Decrease Tax")){
								if(e.getClick().equals(ClickType.LEFT)){
									if(r.getTaxshare()>0){
										r.setTaxshare(r.getTaxshare()-1);
										r.saveAllSettings();
										r.updateSettings();
										MineBay.updateRoomSelection();
										if(Config.Config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.success")).replace("%newtax%", ""+r.getTaxshare()));
										}
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.toolow")));
									}
								}else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
									if(r.getTaxshare()>9){
										r.setTaxshare(r.getTaxshare()-10);
										r.saveAllSettings();
										r.updateSettings();
										MineBay.updateRoomSelection();
										if(Config.Config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.success")).replace("%newtax%", ""+r.getTaxshare()));
										}
									}else if(r.getTaxshare()>0){
										r.setTaxshare(0);
										r.saveAllSettings();
										r.updateSettings();
										MineBay.updateRoomSelection();
										if(Config.Config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.success")).replace("%newtax%", ""+r.getTaxshare()));
										}
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.tax.toolow")));
									}
								}
							}else if(name.equals("§cDelete Room")){
								if(Config.Config.getBoolean("minebay.general.allow-room-selling")){
									if(r.getSoldItems().isEmpty()){
										int sl = (r.getSlots() - Config.Config.getInt("minebay.user-rooms.default-slot-number"))*Config.Config.getInt("minebay.user-rooms.slot-sell-price");
										int pr = Config.Config.getInt("minebay.user-rooms.room-sell-price");
										e.getWhoClicked().openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Delete Room", "§8Price: §7"+(sl+pr), "§8Room ID: §7"+r.getRoomID())));
									}else{
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell-room.not-empty")));
									}
								}else{
									e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell-room.not-allowed")));
								}
							}else if(name.equals("§6Back")){
								Inventory newInv = MineBay.getRoomSelectionMenu(0, "all", (Player)e.getWhoClicked());
								MineBay.changeInv(e.getInventory(), newInv);
								e.setCancelled(true);
								return;
							}
						}
					}else if(mode.equals("§8Sell Item")){
						if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
							String name = e.getCurrentItem().getItemMeta().getDisplayName();
							int page = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(0)));
							String search = e.getInventory().getItem(46).getItemMeta().getLore().get(1).replace("§8Owner: §7", "");
							if(name.equals("§7Previous page")){
								int price = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(2)));
								Inventory newInv = MineBay.getSellRoomSelectionMenu(page-1, search, price);
								if(newInv!=null){
									MineBay.changeInv(e.getInventory(), newInv);
								}
								e.setCancelled(true);
								return;
							}else if(name.equals("§7Next page")){
								int price = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(2)));
								Inventory newInv = MineBay.getSellRoomSelectionMenu(page+1, search, price);
								if(newInv!=null){
									MineBay.changeInv(e.getInventory(), newInv);
								}
								e.setCancelled(true);
								return;
							}else if(name.equals("§7Your Rooms")){
								int price = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(2)));
								MineBay.changeInv(e.getInventory(), MineBay.getSellRoomSelectionMenu(0, e.getWhoClicked().getName(), price));
							}else if(name.equals("§7All Rooms")){
								int price = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(2)));
								MineBay.changeInv(e.getInventory(), MineBay.getSellRoomSelectionMenu(0, "all", price));
							}else if(e.getCurrentItem().getItemMeta().hasLore() && e.getCurrentItem().getItemMeta().getLore().size() >= 4){
								int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getCurrentItem().getItemMeta().getLore().get(3)));
								AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
								if(r.getOccupiedSlots() < r.getSlots() || r.getSlots() == -1){
									if(r.getSoldItemsBySeller(e.getWhoClicked().getName()).size() < Config.Config.getInt("minebay.user-rooms.offers-per-slot")){
										if(e.getWhoClicked().getItemInHand()!=null && !e.getWhoClicked().getItemInHand().getType().equals(Material.AIR)){
											SellItem it = new SellItem(((Player)e.getWhoClicked()).getItemInHand(), r, e.getWhoClicked().getName(), Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(2))), r.getNewItemID());
											r.addSellItem(it);
											((Player)e.getWhoClicked()).setItemInHand(new ItemStack(Material.AIR));
											e.getWhoClicked().closeInventory();
											e.getWhoClicked().sendMessage(Config.replaceForSellItem(Config.simpleReplace(Config.Config.getString("minebay.info.sell.success")), it, r));
										}else{
											e.getWhoClicked().closeInventory();
											e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell.error.noitem")));
										}
									}else{
										e.getWhoClicked().closeInventory();
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell.error.too-many-sold")));
									}
								}else{
									e.getWhoClicked().closeInventory();
									e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell.error.no-slots")));
								}
							}
						}
					}else if(mode.equals("§8Change Block")){
						if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
							String name = e.getCurrentItem().getItemMeta().getDisplayName();
							int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(46).getItemMeta().getLore().get(0)));
							AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
							if(name.contains("§7Block | ") || name.contains("§7Item | ")){
								r.setIcon(e.getCurrentItem());
								e.getWhoClicked().closeInventory();
								r.saveAllSettings();
								r.updateSettings();
								r.updateMineBay();
								MineBay.updateRoomSelection();
								e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.newicon-applied")).replace("%type%", e.getCurrentItem().getType().name().toLowerCase().replace("_", " ")));
							}else if(name.equals("§6Back")){
								Inventory newInv = r.getSettingsMenu();
								MineBay.changeInv(e.getInventory(), newInv);
								e.setCancelled(true);
								return;
							}else if(name.equals("§6Custom block/item")){
								Inventory newInv = r.getIconChangeMenu();
								e.getWhoClicked().openInventory(newInv);
								e.setCancelled(true);
								return;
							}
						}
					}
				}
				e.setCancelled(true);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}else if(e.getInventory().getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §8Confirm")){
			try{
				if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getInventory().getItem(1).getItemMeta().hasLore() && e.getInventory().getItem(1).getItemMeta().getLore().size() == 4){
					String name = e.getCurrentItem().getItemMeta().getDisplayName();
					if(name.equals("§aConfirm")){
						int id = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(1).getItemMeta().getLore().get(2)));
						int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(1).getItemMeta().getLore().get(3)));
						AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
						SellItem it = r.getItemByID(id);
						EconomyResponse re = Main.econ.withdrawPlayer((OfflinePlayer)e.getWhoClicked(), it.getPrice());
						OfflinePlayer seller = Bukkit.getOfflinePlayer(it.getSeller());
						OfflinePlayer owner = null;
						if(r.getOwner()!=null){
							owner = Bukkit.getOfflinePlayer(r.getOwner());
						}
						double sellerAm = round((double)((100-r.getTaxshare())*0.01)*it.getPrice(),5);
						double ownerAm = round((double)(r.getTaxshare()*0.01)*it.getPrice(),5);
						EconomyResponse r2 = Main.econ.depositPlayer(seller, sellerAm);
						EconomyResponse r3 = null;
						if(owner!=null){
							r3 = Main.econ.depositPlayer(owner, ownerAm);
						}
						if(re.transactionSuccess() && r2.transactionSuccess()){
							if((owner!=null && r3!=null && r3.transactionSuccess()) || owner==null){
								e.getWhoClicked().sendMessage(Config.replaceForSellItem(Config.simpleReplace(Config.Config.getString("minebay.info.purchase.success")), it, r));
								r.removeSellItem(id);
								r.updateMineBay();
								Player p = (Player)e.getWhoClicked();
								HashMap<Integer,ItemStack> excess = p.getInventory().addItem(it.getItem());
								for(Map.Entry<Integer, ItemStack> me : excess.entrySet()){
									p.getWorld().dropItem(p.getLocation(), me.getValue());
								}
								e.getWhoClicked().closeInventory();
								if(seller.isOnline()){
									((Player)seller).sendMessage(Config.replaceForSellItem(Config.simpleReplace(Config.Config.getString("minebay.info.purchase.seller.success")), it, r).replace("%buyer%", e.getWhoClicked().getName()).replace("%price2%", ""+sellerAm));
								}
								if(owner!=null && owner.isOnline() && r.getTaxshare() > 0){
									((Player)owner).sendMessage(Config.replaceForSellItem(Config.simpleReplace(Config.Config.getString("minebay.info.purchase.room-owner.success")), it, r).replace("%buyer%", e.getWhoClicked().getName()).replace("%price2%", ""+ownerAm));
								}
							}
						}else{
							e.getWhoClicked().sendMessage(Config.replaceForSellItem(Config.simpleReplace(Config.Config.getString("minebay.info.purchase.error")), it, r).replace("%error%", re.errorMessage));
							e.getWhoClicked().closeInventory();
						}
					}else if(name.equals("§cCancel")){
						e.getWhoClicked().closeInventory();
					}
				}
				e.setCancelled(true);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			e.setCancelled(true);
		}else if(e.getInventory().getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §7Confirm")){
			if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getInventory().getItem(0)!=null && e.getInventory().getItem(0).hasItemMeta() && e.getInventory().getItem(0).getItemMeta().hasDisplayName()){
				String name = e.getCurrentItem().getItemMeta().getDisplayName();
				String mode = e.getInventory().getItem(0).getItemMeta().getDisplayName();
				if(name.equals("§aConfirm")){
					if(mode.equals("§8Buy Auction Room")){
						EconomyResponse re = Main.econ.withdrawPlayer((Player)e.getWhoClicked(), Config.Config.getInt("minebay.user-rooms.room-price"));
						if(re.transactionSuccess()){
							CancelTask.cancelForPlayer((Player)e.getWhoClicked());
							AuctionRoom r = AuctionRooms.createAuctionRoom(e.getWhoClicked().getName(), AuctionRooms.getNewRoomID());
							MineBay.updateRoomSelection();
							e.getWhoClicked().openInventory(r.getSettingsMenu());
							e.getWhoClicked().sendMessage(Config.replaceForAuctionRoom(Config.simpleReplace(Config.Config.getString("minebay.info.room-created")), r));
						}else{
							e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.room-create.error.general")).replace("%error%", re.errorMessage));
							e.getWhoClicked().closeInventory();
						}
					}else if(mode.equals("§8Buy Slot/s")){
						if(e.getInventory().getItem(0).getItemMeta().hasLore() && e.getInventory().getItem(0).getItemMeta().getLore().size() == 3){
							try{
								int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(0).getItemMeta().getLore().get(1)));
								int slotcount = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(0).getItemMeta().getLore().get(2)));
								AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
								EconomyResponse re = Main.econ.withdrawPlayer((OfflinePlayer)e.getWhoClicked(), Config.Config.getInt("minebay.user-rooms.slot-price")*slotcount);
								if(re.transactionSuccess()){
									r.setSlots(r.getSlots()+slotcount);
									r.saveAllSettings();
									r.updateSettings();
									MineBay.updateRoomSelection();
									if(Config.Config.getBoolean("minebay.general.user-rooms-settings.slot-notify")){
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-buy.success")).replace("%slotamount%", ""+slotcount).replace("%price%", ""+Config.Config.getInt("minebay.user-rooms.slot-price")*slotcount));
									}
								}
								e.getWhoClicked().closeInventory();
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
					}else if(mode.equals("§8Sell Slot/s")){
						if(e.getInventory().getItem(0).getItemMeta().hasLore() && e.getInventory().getItem(0).getItemMeta().getLore().size() == 3){
							try{
								int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(0).getItemMeta().getLore().get(1)));
								int slotcount = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(0).getItemMeta().getLore().get(2)));
								AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
								EconomyResponse re = Main.econ.depositPlayer((OfflinePlayer)e.getWhoClicked(), Config.Config.getInt("minebay.user-rooms.slot-sell-price")*slotcount);
								if(re.transactionSuccess()){
									r.setSlots(r.getSlots()-slotcount);
									r.saveAllSettings();
									r.updateSettings();
									MineBay.updateRoomSelection();
									if(Config.Config.getBoolean("minebay.general.user-rooms-settings.slot-notify")){
										e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.slot-sell.success")).replace("%slotamount%", ""+slotcount).replace("%price%", ""+Config.Config.getInt("minebay.user-rooms.slot-sell-price")*slotcount));
									}
								}
								e.getWhoClicked().closeInventory();
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
					}else if(mode.equals("§8Delete Room")){
						if(e.getInventory().getItem(0).getItemMeta().hasLore() && e.getInventory().getItem(0).getItemMeta().getLore().size() == 2){
							try{
								int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(0).getItemMeta().getLore().get(1)));
								AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
								int sl = (r.getSlots() - Config.Config.getInt("minebay.user-rooms.default-slot-number"))*Config.Config.getInt("minebay.user-rooms.slot-sell-price");
								int pr = Config.Config.getInt("minebay.user-rooms.room-sell-price");
								EconomyResponse re = Main.econ.depositPlayer((Player)e.getWhoClicked(), sl+pr);
								if(re.transactionSuccess()){
									AuctionRooms.deleteAuctionRoom(roomID);
									for(Player p : Bukkit.getOnlinePlayers()){
										String t = MineBay.getInvType(p);
										if(t.equals("auction room")){
											int plRoomID = Integer.parseInt(Config.onlyDigitsNoColor(p.getOpenInventory().getTopInventory().getItem(46).getItemMeta().getLore().get(1)));
											if(plRoomID == roomID){
												p.closeInventory();
											}
										}
									}
									MineBay.updateRoomSelection();
									e.getWhoClicked().closeInventory();
									e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell-room.success")).replace("%price%", ""+(sl+pr)));
								}else{
									e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell-room.error")).replace("%error%", re.errorMessage));
								}
								e.getWhoClicked().closeInventory();
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
					}
				}else if(name.equals("§cCancel")){
					e.getWhoClicked().closeInventory();
				}
			}
			e.setCancelled(true);
		}else if(e.getInventory().getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §8Custom icon")){
			if(e.getClickedInventory()!=null && e.getClickedInventory().getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §8Custom icon")){
				if(e.getCurrentItem()!=null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getInventory().getItem(0)!=null && e.getInventory().getItem(0).hasItemMeta() && e.getInventory().getItem(0).getItemMeta().hasDisplayName()){
					String name = e.getCurrentItem().getItemMeta().getDisplayName();
					int roomID = Integer.parseInt(Config.onlyDigitsNoColor(e.getInventory().getItem(1).getItemMeta().getLore().get(0)));
					AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
					if(name.equals("§8Drop item here")){
						if(e.getCursor()!=null && !e.getCursor().getType().equals(Material.AIR)){
							EconomyResponse re = Main.econ.withdrawPlayer((Player)e.getWhoClicked(), Config.Config.getInt("minebay.user-rooms.custom-icon-price"));
							if(re.transactionSuccess()){
								r.setIcon(e.getCursor());
								r.saveAllSettings();
								r.updateSettings();
								MineBay.updateRoomSelection();
								e.setCursor(new ItemStack(Material.AIR));
								e.setCancelled(true);
								e.getWhoClicked().closeInventory();
								e.getWhoClicked().openInventory(r.getSettingsMenu());
								e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.buy-icon.success")).replace("%type%", r.getIcon().getType().name().toLowerCase().replace("_", " ")).replace("%price%", ""+Config.Config.getInt("minebay.user-rooms.custom-icon-price")));
							}else{
								e.getWhoClicked().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.buy-icon.error")).replace("%error%", re.errorMessage));
							}
						}
					}
				}
			}else if(e.getClickedInventory()!=null && !e.getClickedInventory().getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §8Custom icon")){
				return;
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		if(changeName.containsKey(e.getPlayer())){
			String nName = e.getMessage();
			int room = changeName.get(e.getPlayer());
			if(nName.length()<=Config.Config.getInt("minebay.user-rooms.max-name-length")){
				changeName.remove(e.getPlayer());
				AuctionRoom r = AuctionRooms.getAuctionRoomByID(room);
				if(MineBay.hasPermissionForColoredNames(e.getPlayer())){
					nName = ChatColor.translateAlternateColorCodes('&', nName);
				}
				r.setName(nName);
				r.saveAllSettings();
				r.updateSettings();
				MineBay.updateRoomSelection();
				e.getPlayer().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.newname-applied")).replace("%newname%", nName));
			}else{
				e.getPlayer().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.error.name-too-long")));
			}
			e.setCancelled(true);
		}else if(sellItem.containsKey(e.getPlayer())){
			String price = e.getMessage();
			try{
				int pr = Integer.parseInt(price);
				Object[] objs = sellItem.get(e.getPlayer());
				int roomID = (int) objs[0];
				ItemStack item = (ItemStack) objs[1];
				AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
				SellItem it = new SellItem(item, r, e.getPlayer().getName(), pr, r.getNewItemID());
				r.addSellItem(it);
				e.getPlayer().sendMessage(Config.replaceForSellItem(Config.simpleReplace(Config.Config.getString("minebay.info.sell.success")), it, r));
				sellItem.remove(e.getPlayer());
			}catch(NumberFormatException ex){
				e.getPlayer().sendMessage(Config.simpleReplace(Config.Config.getString("minebay.info.sell.error.invalid-price")));
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if(e.getPlayer().hasPermission("minebay.notify-update")){
			if(Config.Config.getBoolean("minebay.general.enable-update-check") && Config.Config.getBoolean("minebay.general.update-check-on-join")){
				UpdateChecker.checkForUpdate(e.getPlayer());
			}
		}
	}
	
	private static double round (double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    return (double) Math.round(value * scale) / scale;
	}
	
}
