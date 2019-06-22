package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.minebay.economy.MineBayEconomy.MineBayEconomyResponse;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ClickAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUI;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIActionEvent;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuildEvent;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuildPageItemEvent;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilder;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilderMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIDragDropEvent;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIDragDropListener;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElement;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElementAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElementActionEvent;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIHolder;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ItemSupplier;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.StaticGUIElement;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import me.mrletsplay.mrcore.bukkitimpl.versioned.VersionedDyeColor;
import me.mrletsplay.mrcore.bukkitimpl.versioned.VersionedMaterial;
import me.mrletsplay.mrcore.misc.QuickMap;

public class GUIs {
	
	public static final GUIMultiPage<AuctionRoom> AUCTION_ROOMS_GUI = buildAuctionRoomsGUI();
	public static final GUI CONFIRM_GUI = buildConfirmGUI();
	public static final GUIMultiPage<SellItem> AUCTION_ROOM_GUI = buildAuctionRoomGUI();
	public static final GUI AUCTION_ROOM_SETTINGS_GUI = buildAuctionRoomSettingsGUI();
	public static final GUI AUCTION_ROOM_CUSTOM_ICON_GUI = buildAuctionRoomCustomIconGUI();
	
	private static GUIMultiPage<AuctionRoom> buildAuctionRoomsGUI(){
		GUIBuilderMultiPage<AuctionRoom> builder = new GUIBuilderMultiPage<>(Config.prefix, 6);
		builder.addPreviousPageItem(52, ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.previous-page")));
		builder.addNextPageItem(53, ItemUtils.createItem(ItemUtils.arrowRight(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.next-page")));
		builder.addPageSlotsInRange(0, 44);
		builder.addElement(49, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.LIME_STAINED_CLAY, 1, Config.getMessage("minebay.gui.rooms.create-room")))
				.setAction(new GUIElementAction() {
					
					@Override
					public void onAction(GUIElementActionEvent e) {
						if(!Config.createPermission.equalsIgnoreCase("none") && !e.getPlayer().hasPermission(Config.createPermission)) {
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.permission-missing.create"));
							e.setCancelled(true);
							return;
						}
						if(Config.config.getBoolean("minebay.general.enable-user-rooms") && (Config.config.getBoolean("minebay.general.allow-room-creation") || e.getPlayer().hasPermission("minebay.user-rooms.create.when-disallowed"))){
							if(MineBay.hasPermissionToCreateRoom(e.getPlayer())){
								Inventory inv = buyRoomGUI(e.getPlayer());
								e.getPlayer().openInventory(inv);
							}else{
								e.getPlayer().sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.room-create.error.too-many-rooms")));
							}
						}else{
							e.getPlayer().sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.user-rooms-disabled")));
						}
						e.setCancelled(true);
					}
				}));
		builder.addElement(50, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.GREEN_BANNER, 1, Config.getMessage("minebay.gui.rooms.list-all")))
				.setAction(new GUIElementAction() {
					
					@Override
					public void onAction(GUIElementActionEvent e) {
						e.getPlayer().openInventory(getAuctionRoomsGUI(e.getPlayer(), null));
						e.setCancelled(true);
					}
				}));
		builder.addElement(51, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.RED_BANNER, 1, Config.getMessage("minebay.gui.rooms.list-self")))
				.setAction(new GUIElementAction() {
					
					@Override
					public void onAction(GUIElementActionEvent e) {
						e.getPlayer().openInventory(getAuctionRoomsGUI(e.getPlayer(), e.getPlayer().getName()));
						e.setCancelled(true);
					}
				}));
		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		builder.addElement(45, gPane);
		builder.addElement(46, gPane);
		builder.addElement(47, gPane);
		builder.addElement(48, gPane);
		builder.setSupplier(new ItemSupplier<AuctionRoom>() {
			
			@Override
			public GUIElement toGUIElement(GUIBuildPageItemEvent<AuctionRoom> event, AuctionRoom room) {
				boolean isSellMode = (boolean) event.getGUIHolder().getProperty(Main.pl, "sell_mode");
				Player p = event.getPlayer();
				if(!isSellMode) {
					return new StaticGUIElement(room.getSelectItemStack(p))
							.setAction(new GUIElementAction() {
								
								@Override
								public void onAction(GUIElementActionEvent e) {
									if(e.getButton().equals(ClickAction.LEFT_CLICK) || e.getButton().equals(ClickAction.SHIFT_LEFT_CLICK)){
										p.openInventory(room.getMineBayInv(0, p));
									}else if((e.getButton().equals(ClickAction.RIGHT_CLICK) || e.getButton().equals(ClickAction.SHIFT_RIGHT_CLICK))&& room.canEdit(p)){
										if(!Config.createPermission.equalsIgnoreCase("none") && !p.hasPermission(Config.createPermission)) {
											p.sendMessage(Config.getMessage("minebay.info.permission-missing.create"));
											e.setCancelled(true);
											return;
										}
										p.openInventory(getAuctionRoomSettingsGUI(e.getPlayer(), room.getID()));
									}
									e.setCancelled(true);
								}
							});
				}else {
					BigDecimal price = (BigDecimal) event.getGUIHolder().getProperty(Main.pl, "price");
					return new StaticGUIElement(room.getSelectItemStack(p))
							.setAction(new GUIElementAction() {
								
								@SuppressWarnings("deprecation")
								@Override
								public void onAction(GUIElementActionEvent e) {
									if(room.getOccupiedSlots() < room.getSlots() || room.getSlots() == -1){
										if(room.getSoldItemsBySeller(p).size() < Config.config.getInt("minebay.user-rooms.offers-per-slot")){
											if(p.getItemInHand()!=null && !p.getItemInHand().getType().equals(Material.AIR)){
												SellItem it = new SellItem(((Player)p).getItemInHand(), room, (Config.use_uuids?p.getUniqueId().toString():p.getName()), price, room.getNewItemID());
												room.addSellItem(it);
												p.setItemInHand(new ItemStack(Material.AIR));
												p.closeInventory();
												p.sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.sell.success"), it, room));
											}else{
												p.closeInventory();
												p.sendMessage(Config.getMessage("minebay.info.sell.error.noitem"));
											}
										}else{
											p.closeInventory();
											p.sendMessage(Config.getMessage("minebay.info.sell.error.too-many-sold"));
										}
									}else{
										p.closeInventory();
										p.sendMessage(Config.getMessage("minebay.info.sell.error.no-slots"));
									}
									e.setCancelled(true);
								}
							});
				}
			}
			
			@Override
			public List<AuctionRoom> getItems(GUIBuildEvent event) {
				String owner = (String) event.getGUIHolder().getProperty(Main.pl, "owner");
				if(owner == null) {
					return AuctionRooms.getAuctionRooms();
				}
				return AuctionRooms.getAuctionRoomsByOwner(owner);
			}
		});
		
		HashMap<String, Object> props = new HashMap<>();
		props.put("minebay_type", "auction rooms");
		builder.setDefaultProperties(props);
		return builder.build();
	}

	public static Inventory getAuctionRoomsGUI(Player forPlayer, String owner) {
		return AUCTION_ROOMS_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("owner", owner).put("sell_mode", false).makeHashMap());
	}
	
	public static Inventory getAuctionRoomsSellGUI(Player forPlayer, String owner, BigDecimal price){
		return AUCTION_ROOMS_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("owner", owner).put("sell_mode", true).put("price", price).makeHashMap());
	}
	
	public static Inventory getConfirmGUI(Player forPlayer, ItemStack baseItem, GUIElementAction confirm){
		return CONFIRM_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("base_item", baseItem).put("confirm_action", confirm).makeHashMap());
	}
	
	private static GUI buildConfirmGUI() {
		GUIBuilder builder = new GUIBuilder(Config.prefix, 3);

		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		for(int i = 0; i < 27; i++) {
			builder.addElement(i, gPane);
		}
		
		builder.addElement(10, new GUIElement() {
			
			@Override
			public ItemStack getItem(GUIBuildEvent event) {
				ItemStack baseItem = (ItemStack) event.getGUIHolder().getProperty(Main.pl, "base_item");
				return baseItem;
			}
		});
		ItemStack confirmItem = ItemUtils.createItem(ItemUtils.createBanner(null, VersionedDyeColor.GREEN),
				Config.getMessage("minebay.gui.confirm.confirm.name"),
				Config.getMessageList("minebay.gui.confirm.confirm.lore"));
		builder.addElement(14, new StaticGUIElement(confirmItem).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent event) {
				GUIElementAction a = (GUIElementAction) event.getGUIHolder().getProperty(Main.pl, "confirm_action");
				a.onAction(event);
			}
		}));
		ItemStack cancelItem = ItemUtils.createItem(ItemUtils.createBanner(null, VersionedDyeColor.RED),
				Config.getMessage("minebay.gui.confirm.cancel.name"),
				Config.getMessageList("minebay.gui.confirm.cancel.lore"));
		builder.addElement(16, new StaticGUIElement(cancelItem).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				e.getPlayer().closeInventory();
				e.setCancelled(true);
			}
		}));
		
		return builder.build();
	}
	
	public static Inventory buyRoomGUI(Player forPlayer) {
		ItemStack baseItem = ItemUtils.createItem(VersionedMaterial.GRASS_BLOCK, 1,
				Config.getMessage("minebay.gui.confirm.room-create.name"),
				Config.getMessageList("minebay.gui.confirm.room-create.lore", "price", ""+Config.config.getInt("minebay.user-rooms.room-price")));
		return getConfirmGUI(forPlayer, baseItem, new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), Config.config.getInt("minebay.user-rooms.room-price"));
				if(re.isTransactionSuccess()){
					CancelTask.cancelForPlayer(e.getPlayer());
					AuctionRoom r = AuctionRooms.createAuctionRoom(e.getPlayer(), AuctionRooms.getNewRoomID(), false);
					MineBay.updateRoomSelection();
					e.getPlayer().openInventory(getAuctionRoomSettingsGUI(e.getPlayer(), r.getID()));
					e.getPlayer().sendMessage(Config.replaceForAuctionRoom(Config.getMessage("minebay.info.room-created"), r));
				}else{
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.room-create.error.general").replace("%error%", re.getError()));
					e.getPlayer().closeInventory();
				}
				e.setCancelled(true);
			}
		});
	}
	
	public static Inventory buySlotsGUI(Player forPlayer, AuctionRoom r, int amount) {
		ItemStack baseItem = ItemUtils.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.slots-buy.name"),
				Config.getMessageList("minebay.gui.confirm.slots-buy.lore",
						"price", ""+(amount*Config.config.getInt("minebay.user-rooms.slot-price")),
						"amount", ""+amount,
						"room-id", ""+r.getID()));
		return getConfirmGUI(forPlayer, baseItem, new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), Config.config.getInt("minebay.user-rooms.slot-price")*amount);
				if(re.isTransactionSuccess()){
					r.setSlots(r.getSlots()+amount);
					r.saveAllSettings();
					r.updateSettings();
					MineBay.updateRoomSelection();
					if(Config.config.getBoolean("minebay.general.user-rooms-settings.slot-notify")){
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-buy.success", "slotamount", ""+amount, "price", ""+Config.config.getInt("minebay.user-rooms.slot-price")*amount));
					}
				}else{
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.room-create.error.general").replace("%error%", re.getError()));
					e.getPlayer().closeInventory();
				}
				e.getPlayer().openInventory(getAuctionRoomSettingsGUI(e.getPlayer(), r.getID()));
				e.setCancelled(true);
			}
		});
	}
	
	public static Inventory sellSlotsGUI(Player forPlayer, AuctionRoom r, int amount) {
		ItemStack baseItem = ItemUtils.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.slots-sell.name"),
				Config.getMessageList("minebay.gui.confirm.slots-sell.lore",
						"price", ""+(amount*Config.config.getInt("minebay.user-rooms.slot-sell-price")),
						"amount", ""+amount,
						"room-id", ""+r.getID()));
		return getConfirmGUI(forPlayer, baseItem, new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				MineBayEconomyResponse re = Main.econ.depositPlayer(e.getPlayer(), Config.config.getInt("minebay.user-rooms.slot-sell-price")*amount);
				if(re.isTransactionSuccess()){
					r.setSlots(r.getSlots()-amount);
					r.saveAllSettings();
					r.updateSettings();
					MineBay.updateRoomSelection();
					if(Config.config.getBoolean("minebay.general.user-rooms-settings.slot-notify")){
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.success", "slotamount", ""+amount, "price", ""+Config.config.getInt("minebay.user-rooms.slot-sell-price")*amount));
					}
				}
				e.getPlayer().openInventory(getAuctionRoomSettingsGUI(e.getPlayer(), r.getID()));
				e.setCancelled(true);
			}
		});
	}
	
	public static Inventory sellRoomGUI(Player forPlayer, AuctionRoom r) {
		int sl = (r.getSlots() - Config.config.getInt("minebay.user-rooms.default-slot-number"))*Config.config.getInt("minebay.user-rooms.slot-sell-price");
		int pr = Config.config.getInt("minebay.user-rooms.room-sell-price");
		
		ItemStack baseItem = ItemUtils.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.room-sell.name"),
				Config.getMessageList("minebay.gui.confirm.room-sell.lore",
						"price", ""+(sl+pr),
						"room-id", ""+r.getID()));
		return getConfirmGUI(forPlayer, baseItem, new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				int worth = r.getWorth();
				MineBayEconomyResponse re = Main.econ.depositPlayer(e.getPlayer(), worth);
				if(re.isTransactionSuccess()){
					AuctionRooms.deleteAuctionRoom(r.getID());
					for(Player pl : Bukkit.getOnlinePlayers()){
						Inventory oI = MineBay.getOpenInv(pl);
						if(oI == null) continue;
						GUI gui2 = GUIUtils.getGUI(oI);
						if(gui2 == null) continue;
						GUIHolder holder = (GUIHolder) oI.getHolder();
						String t = (String) holder.getProperty("minebay_type");
						if(t == null) continue;
						if(t.equals("auction room")) {
							if(((int) holder.getProperty("minebay_auctionroom_id")) == r.getID()) {
								pl.closeInventory();
							}
						}
					}
					MineBay.updateRoomSelection();
					e.getPlayer().closeInventory();
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell-room.success").replace("%price%", ""+worth));
				}else{
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell-room.error").replace("%error%", re.getError()));
				}
				e.setCancelled(true);
			}
		});
	}
	
	public static Inventory purchaseItemGUI(Player forPlayer, SellItem sellIt) {
		Inventory base = getConfirmGUI(forPlayer, sellIt.getItem(), new GUIElementAction() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onAction(GUIElementActionEvent e) {
				e.setCancelled(true);
				AuctionRoom r = sellIt.getRoom();
				MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), sellIt.getPrice().doubleValue());
				
				if(!re.isTransactionSuccess()) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.purchase.error", "error", re.getError()));
					return;
				}
				
				OfflinePlayer seller;
				if(Config.use_uuids) {
					seller = Bukkit.getOfflinePlayer(UUID.fromString(sellIt.getSeller()));
				}else{
					seller = Bukkit.getOfflinePlayer(sellIt.getSeller());
				}
				OfflinePlayer owner = null;
				if(r.getOwner()!=null){
					if(Config.use_uuids) {
						owner = Bukkit.getOfflinePlayer(UUID.fromString(r.getOwner()));
					}else {
						owner = Bukkit.getOfflinePlayer(r.getOwner());
					}
				}
				double sellerAm = round((double)((100-r.getTaxshare())*0.01)*sellIt.getPrice().doubleValue(),5);
				double ownerAm = round((double)(r.getTaxshare()*0.01)*sellIt.getPrice().doubleValue(),5);
				MineBayEconomyResponse r2 = Main.econ.depositPlayer(seller, sellerAm);
				
				if(!r2.isTransactionSuccess()) {
					Main.econ.depositPlayer(e.getPlayer(), sellIt.getPrice().doubleValue()); // Refund money
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.purchase.error", "error", re.getError()));
					return;
				}
				
				MineBayEconomyResponse r3 = null;
				if(owner!=null){
					r3 = Main.econ.depositPlayer(owner, ownerAm);
				}
				
				if(!r3.isTransactionSuccess()) {
					Main.econ.depositPlayer(e.getPlayer(), sellIt.getPrice().doubleValue()); // Refund money
					Main.econ.withdrawPlayer(owner, ownerAm); // Revoke money
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.purchase.error", "error", re.getError()));
					return;
				}
				
				if(re.isTransactionSuccess() && r2.isTransactionSuccess()){
					if((owner!=null && r3!=null && r3.isTransactionSuccess()) || owner==null){
						e.getPlayer().sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.success"), sellIt, r));
						r.removeSellItem(sellIt.getID());
						r.updateMineBay();
						ItemUtils.addItemOrDrop(e.getPlayer(), sellIt.getItem());
						e.getPlayer().closeInventory();
						if(seller.isOnline()){
							((Player)seller).sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.seller.success"), sellIt, r).replace("%buyer%", e.getPlayer().getName()).replace("%price2%", ""+sellerAm));
						}
						if(owner!=null && owner.isOnline() && r.getTaxshare() > 0){
							((Player) owner).sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.room-owner.success"), sellIt, r).replace("%buyer%", e.getPlayer().getName()).replace("%price2%", ""+ownerAm));
						}
					}
				}
			}
		});
//		base.getBuilder().addElement(1, new StaticGUIElement(sellIt.getConfirmItemStack()));
		return base;
	}
	
	private static double round (double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    return (double) Math.round(value * scale) / scale;
	}
	
	private static GUIMultiPage<SellItem> buildAuctionRoomGUI(){
		GUIBuilderMultiPage<SellItem> builder = new GUIBuilderMultiPage<>(Config.prefix, 6);
		builder.addPageSlotsInRange(0, 44);
		builder.addPreviousPageItem(52, ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.previous-page")));
		builder.addNextPageItem(53, ItemUtils.createItem(ItemUtils.arrowRight(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.next-page")));
		
		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		if(Config.config.getBoolean("minebay.general.enable-user-rooms")) {
			builder.addElement(45, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back")))
					.setAction(new GUIElementAction() {
						
						@Override
						public void onAction(GUIElementActionEvent e) {
							e.getPlayer().openInventory(GUIs.getAuctionRoomsGUI(e.getPlayer(), null));
							e.setCancelled(true);
						}
					}));
		}else {
			builder.addElement(45, gPane);
		}
		builder.addElement(46, gPane);
		builder.addElement(47, gPane);
		builder.addElement(48, gPane);
		builder.addElement(49, gPane);
		builder.addElement(50, gPane);
		builder.addElement(51, gPane);
		builder.setSupplier(new ItemSupplier<SellItem>() {
			
			@Override
			public GUIElement toGUIElement(GUIBuildPageItemEvent<SellItem> event, SellItem item) {
				Player p = event.getPlayer();
				return new StaticGUIElement(item.getSellItemStack(p))
						.setAction(new GUIElementAction() {
							
							@Override
							public void onAction(GUIElementActionEvent e) {
								AuctionRoom room = item.getRoom();
								if(item.getSeller()!=null && !item.isSeller(p)){
									p.closeInventory();
									p.openInventory(GUIs.purchaseItemGUI(p, item));
								}else{
									if(!Config.sellPermission.equalsIgnoreCase("none") && !e.getPlayer().hasPermission(Config.sellPermission)) {
										e.getPlayer().sendMessage(Config.getMessage("minebay.info.permission-missing.sell"));
										e.setCancelled(true);
										return;
									}
									HashMap<Integer,ItemStack> excess = p.getInventory().addItem(item.getItem());
									for(Map.Entry<Integer, ItemStack> me : excess.entrySet()){
										p.getWorld().dropItem(p.getLocation(), me.getValue());
									}
									room.removeSellItem(item.getID());
									p.sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.retract-sale.success")));
								}
								e.setCancelled(true);
							}
						});
			}
			
			@Override
			public List<SellItem> getItems(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				return r.getSoldItems();
			}
		});
		builder.setDragDropListener(new GUIDragDropListener() {
			
			@Override
			public void onDragDrop(GUIDragDropEvent e) {
				if(!Config.sellPermission.equalsIgnoreCase("none") && !e.getPlayer().hasPermission(Config.sellPermission)) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.permission-missing.sell"));
					e.setCancelled(true);
					return;
				}

				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(r.isPrivateRoom() && !r.isOwner(e.getPlayer())) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.private-room"));
					e.setCancelled(true);
					return;
				}
				e.setCancelled(!Config.config.getBoolean("minebay.general.allow-drag-and-drop"));
			}
		});
		builder.setActionListener(new GUIAction() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onAction(GUIActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(e.getItemClickedWith() != null && !e.getItemClickedWith().getType().equals(Material.AIR) && e.getElementClicked() == null) {
					if(r.getOccupiedSlots() < r.getSlots() || r.getSlots() == -1){
						if(r.getSoldItemsBySeller(e.getPlayer()).size() < Config.config.getInt("minebay.user-rooms.offers-per-slot")){
								int roomID = (int) e.getGUIHolder().getProperty(Main.pl, "room_id");
								Events.sellItem.put(e.getPlayer().getUniqueId(), new Object[]{roomID, e.getItemClickedWith()});
								int maxTime = Config.config.getInt("minebay.general.max-type-time-seconds");
								if(maxTime>0){
									Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask(e.getPlayer()), maxTime * 20);
								}
								e.getEvent().setCursor(new ItemStack(Material.AIR));
								e.getPlayer().sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.sell.type-in-price")));
								e.getPlayer().closeInventory();
						}else{
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.too-many-sold"));
						}
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.no-slots"));
					}
				}
				e.setCancelled(true);
			}
		});
		return builder.build();
	}
	
	private static GUI buildAuctionRoomSettingsGUI() {
		GUIBuilder builder = new GUIBuilder(Config.prefix, 6);
		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		for(int i = 0; i < 9*6; i++) {
			builder.addElement(i, gPane);
		}
		
		builder.addElement(8, new GUIElement() {
			
			@Override
			public ItemStack getItem(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				return ItemUtils.createItem(r.isPrivateRoom() ? VersionedMaterial.RED_BANNER : VersionedMaterial.GREEN_BANNER, 1,
						Config.getMessage(r.isPrivateRoom() ? "minebay.gui.room-settings.private-room.private.name" : "minebay.gui.room-settings.private-room.public.name"),
						Config.getMessageList(r.isPrivateRoom() ? "minebay.gui.room-settings.private-room.private.lore" : "minebay.gui.room-settings.private-room.public.lore"));
			}
		}.setAction(event -> {
			AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
			r.setPrivateRoom(!r.isPrivateRoom());
			r.saveAllSettings();
			r.updateSettings();
			MineBay.updateRoomSelection();
			event.setCancelled(true);
		}));
		
		builder.addElement(10, new GUIElement() {

			@Override
			public ItemStack getItem(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				List<String> l1 = Config.getMessageList("minebay.gui.room-settings.name-desc.name-lore", "name", r.getName(), "description", r.getDescription()!=null?r.getDescription():Config.getMessage("minebay.gui.misc.none"));
				String lbC = Config.getMessage("minebay.gui.room-settings.name-desc.name-lore-linebreak-color");
				List<String> l1f = new ArrayList<>();
				for(String s : l1) {
					for(String s2 : WordUtils.wrap(s, 50).split(System.getProperty("line.separator"))) {
						l1f.add(lbC+s2);
					}
				}
				return ItemUtils.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.name-desc.name"), l1f.toArray(new String[l1f.size()]));
			}
			
		});
		
		builder.addElement(14, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.YELLOW_STAINED_CLAY, 1, Config.getMessage("minebay.gui.room-settings.name-desc.change-name"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				int roomID = (int) e.getGUIHolder().getProperty(Main.pl, "room_id");
				Events.changeName.put(e.getPlayer().getUniqueId(), roomID);
				int maxTime = Config.config.getInt("minebay.general.max-type-time-seconds");
				if(maxTime>0){
					Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask(e.getPlayer()), maxTime*20);
				}
				e.getPlayer().closeInventory();
				e.getPlayer().sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.newname")));
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(15, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.YELLOW_STAINED_CLAY, 1, Config.getMessage("minebay.gui.room-settings.name-desc.change-description"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				int roomID = (int) e.getGUIHolder().getProperty(Main.pl, "room_id");
				Events.changeDescription.put(e.getPlayer().getUniqueId(), roomID);
				int maxTime = Config.config.getInt("minebay.general.max-type-time-seconds");
				if(maxTime>0){
					Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask(e.getPlayer()), maxTime*20);
				}
				e.getPlayer().closeInventory();
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.newdescription"));
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(19, new GUIElement() {
			
			@Override
			public ItemStack getItem(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				return ItemUtils.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.block.name"), Config.getMessageList("minebay.gui.room-settings.block.lore", "type", Config.getFriendlyTypeName(r.getIcon().getType())));
			}
		});
		
		builder.addElement(23, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.YELLOW_STAINED_CLAY, 1, Config.getMessage("minebay.gui.room-settings.block-change.name"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				e.getPlayer().openInventory(getAuctionRoomCustomIconGUI(e.getPlayer(), r.getID())); //TODO
				r.saveAllSettings();
				r.updateSettings();
				MineBay.updateRoomSelection();
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(28, new GUIElement() {
			
			@Override
			public ItemStack getItem(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				return ItemUtils.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.slots.name"), Config.getMessageList("minebay.gui.room-settings.slots.lore",
						"slots", ""+(r.getSlots()==-1?Config.getMessage("minebay.gui.rooms.room-item.slots-unlimited"):""+r.getSlots())));
			}
		});
		
		builder.addElement(32, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(), Config.getMessage("minebay.gui.room-settings.slots-buy.name"), Config.getMessageList("minebay.gui.room-settings.slots-buy.lore"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(!r.isDefaultRoom()){
					if(e.getButton().equals(ClickAction.LEFT_CLICK)){
						if(r.getSlots() < Config.config.getInt("minebay.user-rooms.max-slots")){
							e.getPlayer().openInventory(GUIs.buySlotsGUI(e.getPlayer(), r, 1));
						}else{
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-buy.toomanyslots"));
						}
					}else if(e.getButton().equals(ClickAction.SHIFT_LEFT_CLICK)){
						if(r.getSlots() + 5 <= Config.config.getInt("minebay.user-rooms.max-slots")){
							e.getPlayer().openInventory(GUIs.buySlotsGUI(e.getPlayer(), r, 5));
						}else if(r.getSlots() < Config.config.getInt("minebay.user-rooms.max-slots")){
							e.getPlayer().openInventory(GUIs.buySlotsGUI(e.getPlayer(), r, Config.config.getInt("minebay.user-rooms.max-slots") - r.getSlots()));
						}else{
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-buy.toomanyslots"));
						}
					}
				}else{
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-buy.is-default"));
				}
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(33, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowRight(), Config.getMessage("minebay.gui.room-settings.slots-sell.name"), Config.getMessageList("minebay.gui.room-settings.slots-sell.lore"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(!r.isDefaultRoom()){
					if(Config.config.getBoolean("minebay.general.allow-slot-selling")){
						if(e.getButton().equals(ClickAction.LEFT_CLICK)){
							if(r.getSlots() > Config.config.getInt("minebay.user-rooms.default-slot-number")){
								if(r.getOccupiedSlots() <= r.getSlots() - 1){
									e.getPlayer().openInventory(GUIs.sellSlotsGUI(e.getPlayer(), r, 1));
								}else{
									e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.all-slots-occupied"));
								}
							}else{
								e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.notenoughslots"));
							}
						}else if(e.getButton().equals(ClickAction.SHIFT_LEFT_CLICK)){
							if(r.getSlots() - 5 >= Config.config.getInt("minebay.user-rooms.default-slot-number")){
								if(r.getOccupiedSlots() <= r.getSlots() - 5){
									e.getPlayer().openInventory(GUIs.sellSlotsGUI(e.getPlayer(), r, 5));
								}else{
									e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.all-slots-occupied"));
								}
							}else if(r.getSlots() > Config.config.getInt("minebay.user-rooms.default-slot-number")){
								int slotsToSell = r.getSlots()-Config.config.getInt("minebay.user-rooms.default-slot-number");
								if(r.getOccupiedSlots() <= r.getSlots()-slotsToSell){
									e.getPlayer().openInventory(GUIs.sellSlotsGUI(e.getPlayer(), r, slotsToSell));
								}
							}else{
								e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.notenoughslots"));
							}
						}
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.not-allowed"));
					}
				}else{
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.is-default"));
				}
				e.setCancelled(true);
			}
		}));
		
		
		builder.addElement(37, new GUIElement() {

			@Override
			public ItemStack getItem(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				return ItemUtils.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.tax.name"), Config.getMessageList("minebay.gui.room-settings.tax.lore", "tax", ""+r.getTaxshare()));
			}
			
		});
		
		builder.addElement(41, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(), Config.getMessage("minebay.gui.room-settings.tax-increase.name"), Config.getMessageList("minebay.gui.room-settings.tax-increase.lore", "tax-changing-disabled", Config.allow_tax_change ? "" : Config.getMessage("minebay.gui.room-settings.tax-increase.disabled")))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(!Config.allow_tax_change && !e.getPlayer().hasPermission("minebay.change-tax-when-disabled")) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax-changing-disabled"));
					e.setCancelled(true);
					return;
				}
				if(e.getButton().equals(ClickAction.LEFT_CLICK)){
					if(r.getTaxshare()<Config.config.getInt("minebay.user-rooms.max-tax-percent")){
						r.setTaxshare(r.getTaxshare() + 1);
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+r.getTaxshare()));
						}
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.toohigh"));
					}
				}else if(e.getButton().equals(ClickAction.SHIFT_LEFT_CLICK)){
					if(r.getTaxshare()+10<=Config.config.getInt("minebay.user-rooms.max-tax-percent")){
						r.setTaxshare(r.getTaxshare()+10);
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+r.getTaxshare()));
						}
					}else if(r.getTaxshare()<Config.config.getInt("minebay.user-rooms.max-tax-percent")){
						r.setTaxshare(Config.config.getInt("minebay.user-rooms.max-tax-percent"));
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+r.getTaxshare()));
						}
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.toohigh"));
					}
				}
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(42, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowRight(), Config.getMessage("minebay.gui.room-settings.tax-decrease.name"), Config.getMessageList("minebay.gui.room-settings.tax-decrease.lore", "tax-changing-disabled", Config.allow_tax_change ? "" : Config.getMessage("minebay.gui.room-settings.tax-increase.disabled")))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(!Config.allow_tax_change && !e.getPlayer().hasPermission("minebay.change-tax-when-disabled")) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax-changing-disabled"));
					e.setCancelled(true);
					return;
				}
				if(e.getButton().equals(ClickAction.LEFT_CLICK)){
					if(r.getTaxshare()>0){
						r.setTaxshare(r.getTaxshare()-1);
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+r.getTaxshare()));
						}
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.toolow"));
					}
				}else if(e.getButton().equals(ClickAction.SHIFT_LEFT_CLICK)){
					if(r.getTaxshare()>9){
						r.setTaxshare(r.getTaxshare()-10);
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+r.getTaxshare()));
						}
					}else if(r.getTaxshare()>0){
						r.setTaxshare(0);
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+r.getTaxshare()));
						}
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax.toolow"));
					}
				}
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(45, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				Inventory newInv = GUIs.getAuctionRoomsGUI(e.getPlayer(), null);
				e.getPlayer().openInventory(newInv);
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(53, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.RED_STAINED_CLAY, 1, Config.getMessage("minebay.gui.room-settings.delete"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(Config.config.getBoolean("minebay.general.allow-room-selling")){
					if(r.getID() != 0){
						if(r.getSoldItems().isEmpty()){
							e.getPlayer().openInventory(GUIs.sellRoomGUI(e.getPlayer(), r));
						}else{
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell-room.not-empty"));
						}
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell-room.is-default"));
						e.getPlayer().closeInventory();
					}
				}else{
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell-room.not-allowed"));
				}
				e.setCancelled(true);
			}
		}));

		return builder.build();
	}
	
	private static GUI buildAuctionRoomCustomIconGUI() {
		GUIBuilder builder = new GUIBuilder(Config.prefix, 1);
		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		for(int i = 0; i < 9; i++) {
			builder.addElement(i, gPane);
		}
		
		builder.addElement(0, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				e.getPlayer().openInventory(getAuctionRoomSettingsGUI(e.getPlayer(), r.getID()));//TODO
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(4, new StaticGUIElement(ItemUtils.createItem(ItemUtils.createBanner(null, VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.room-settings.custom-icon.item-drop.name"), Config.getMessageList("minebay.gui.room-settings.custom-icon.item-drop.lore"))).setAction(new GUIElementAction() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(e.getItemClickedWith() != null && !e.getItemClickedWith().getType().equals(Material.AIR)) {
					int price = r.isDefaultRoom()?0:Config.config.getInt("minebay.user-rooms.custom-icon-price");
					MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), price);
					if(re.isTransactionSuccess()){
						r.setIcon(e.getItemClickedWith());
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(!Config.config.getBoolean("minebay.general.user-rooms-settings.change-icon-remove-item")){
							Tools.addItem(e.getPlayer(), e.getItemClickedWith());
						}
						e.getEvent().setCursor(new ItemStack(Material.AIR));
						e.getPlayer().openInventory(getAuctionRoomSettingsGUI(e.getPlayer(), r.getID()));
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.buy-icon.success").replace("%type%", Config.getFriendlyTypeName(r.getIcon().getType())).replace("%price%", ""+price));
					}else{
						e.getPlayer().sendMessage(Config.getMessage("minebay.info.buy-icon.error").replace("%error%", re.getError()));
					}
				}
				e.setCancelled(true);
			}
		}));
		
		builder.setDragDropListener(new GUIDragDropListener() {
			
			@Override
			public void onDragDrop(GUIDragDropEvent e) {
				e.setCancelled(false);
			}
		});
		
		return builder.build();
	}
	
	public static Inventory getAuctionRoomGUI(Player forPlayer, int roomID, int page) {
		return AUCTION_ROOM_GUI.getForPlayer(forPlayer, page, Main.pl, new QuickMap<String, Object>().put("room_id", roomID).makeHashMap());
	}
	
	public static Inventory getAuctionRoomSettingsGUI(Player forPlayer, int roomID) {
		return AUCTION_ROOM_SETTINGS_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("room_id", roomID).makeHashMap());
	}
	
	public static Inventory getAuctionRoomCustomIconGUI(Player forPlayer, int roomID) {
		return AUCTION_ROOM_CUSTOM_ICON_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("room_id", roomID).makeHashMap());
	}
	
}
