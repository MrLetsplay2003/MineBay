package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.mrletsplay.minebay.economy.MineBayEconomy.MineBayEconomyResponse;
import me.mrletsplay.minebay.notifications.CancelledOffer;
import me.mrletsplay.minebay.notifications.PlayerData;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUI;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIBuilder;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIBuilderMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIDragDropListener;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIElement;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIElementAction;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIHolder;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIItemSupplier;
import me.mrletsplay.mrcore.bukkitimpl.gui.GUIMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.gui.StaticGUIElement;
import me.mrletsplay.mrcore.bukkitimpl.gui.event.GUIBuildEvent;
import me.mrletsplay.mrcore.bukkitimpl.gui.event.GUIBuildPageItemEvent;
import me.mrletsplay.mrcore.bukkitimpl.gui.event.GUIDragDropEvent;
import me.mrletsplay.mrcore.bukkitimpl.gui.event.GUIElementActionEvent;
import me.mrletsplay.mrcore.bukkitimpl.versioned.NMSVersion;
import me.mrletsplay.mrcore.bukkitimpl.versioned.VersionedDyeColor;
import me.mrletsplay.mrcore.bukkitimpl.versioned.VersionedMaterial;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.mrcore.misc.QuickMap;

public class GUIs {
	
	public static final GUIMultiPage<AuctionRoom> AUCTION_ROOMS_GUI = buildAuctionRoomsGUI();
	public static final GUI CONFIRM_GUI = buildConfirmGUI();
	public static final GUIMultiPage<SellItem> AUCTION_ROOM_GUI = buildAuctionRoomGUI();
	public static final GUI AUCTION_ROOM_SETTINGS_GUI = buildAuctionRoomSettingsGUI();
	public static final GUI AUCTION_ROOM_CUSTOM_ICON_GUI = buildAuctionRoomCustomIconGUI();
	public static final GUIMultiPage<OfflinePlayer> AUCTION_ROOM_PLAYER_LIST_GUI = buildAuctionRoomPlayerListGUI();
	
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
						e.getGUIHolder().setProperty(Main.pl, "owner", null);
						e.getPlayer().openInventory(getAuctionRoomsGUIRaw(e.getPlayer(), e.getGUIHolder().getProperties()));
						e.setCancelled(true);
					}
				}));
		builder.addElement(51, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.RED_BANNER, 1, Config.getMessage("minebay.gui.rooms.list-self")))
				.setAction(new GUIElementAction() {
					
					@Override
					public void onAction(GUIElementActionEvent e) {
						e.getGUIHolder().setProperty(Main.pl, "owner", e.getPlayer().getName());
						e.getPlayer().openInventory(getAuctionRoomsGUIRaw(e.getPlayer(), e.getGUIHolder().getProperties()));
						e.setCancelled(true);
					}
				}));
		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		builder.addElement(45, gPane);
		builder.addElement(46, gPane);
		builder.addElement(47, gPane);
		builder.addElement(48, gPane);
		builder.setSupplier(new GUIItemSupplier<AuctionRoom>() {
			
			@Override
			public GUIElement toGUIElement(GUIBuildPageItemEvent<AuctionRoom> event, AuctionRoom room) {
				String mode = (String) event.getGUIHolder().getProperty(Main.pl, "mode");
				Player p = event.getPlayer();
				if("none".equals(mode)) {
					return new StaticGUIElement(room.getSelectItemStack(p))
							.setAction(new GUIElementAction() {
								
								@Override
								public void onAction(GUIElementActionEvent e) {
									if(e.getClickType().isLeftClick()){
										p.openInventory(room.getMineBayInv(0, p));
									}else if(e.getClickType().isRightClick() && room.canEdit(p)){
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
				}else if("sell".equals(mode)){
					BigDecimal price = (BigDecimal) event.getGUIHolder().getProperty(Main.pl, "price");
					return new StaticGUIElement(room.getSelectItemStack(p))
							.setAction(new GUIElementAction() {
								
								@SuppressWarnings("deprecation")
								@Override
								public void onAction(GUIElementActionEvent e) {
									if(room.getOccupiedSlots() < room.getSlots() || room.getSlots() == -1){
										if(room.getSoldItemsBySeller(p).size() < Config.config.getInt("minebay.user-rooms.offers-per-slot")){
											if(p.getItemInHand()!=null && !p.getItemInHand().getType().equals(Material.AIR)){
												SellItem it = new SellItem(((Player)p).getItemInHand(), room, (Config.useUUIDs?p.getUniqueId().toString():p.getName()), price, room.getNewItemID());
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
				}else if("spawnnpc".equals(mode)) {
					return new StaticGUIElement(room.getSelectItemStack(p))
							.setAction(e -> {
								e.setCancelled(true);
								int npcPrice = Config.config.getInt("minebay.user-rooms.npc-price");
								MineBayEconomyResponse r = Main.econ.withdrawPlayer(e.getPlayer(), new BigDecimal(npcPrice));
								if(!r.isTransactionSuccess()) {
									e.getPlayer().sendMessage(Config.getMessage("minebay.info.spawn-npc.error.general", "error", r.getError()));
									return;
								}
								MineBayNPCs.spawnAuctionRoomNPC(room, e.getPlayer().getLocation());
								e.getPlayer().sendMessage(Config.getMessage("minebay.info.spawn-npc.success"));
							});
				}else {
					throw new FriendlyException("Invalid mode");
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
		return builder.create();
	}

	public static Inventory getAuctionRoomsGUI(Player forPlayer, String owner) {
		return AUCTION_ROOMS_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("owner", owner).put("mode", "none").makeHashMap());
	}
	
	public static Inventory getAuctionRoomsSellGUI(Player forPlayer, String owner, BigDecimal price){
		return AUCTION_ROOMS_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("owner", owner).put("mode", "sell").put("price", price).makeHashMap());
	}
	
	public static Inventory getAuctionRoomsSpawnNPCGUI(Player forPlayer, String owner) {
		return AUCTION_ROOMS_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("owner", owner).put("mode", "spawnnpc").makeHashMap());
	}

	public static Inventory getAuctionRoomsGUIRaw(Player forPlayer, Map<String, Object> properties) {
		return AUCTION_ROOMS_GUI.getForPlayer(forPlayer, properties);
	}
	
	public static Inventory getConfirmGUI(Player forPlayer, ItemStack baseItem, GUIElementAction confirm){
		return CONFIRM_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("base_item", baseItem).put("confirm_action", confirm).makeHashMap());
	}
	
	public static Inventory getPurchaseConfirmGUI(Player forPlayer, SellItem sellItem, GUIElementAction confirm){
		return CONFIRM_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("base_item", sellItem.getItem()).put("confirm_action", confirm).put("sell_item", sellItem).makeHashMap());
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
		
		return builder.create();
	}
	
	public static Inventory buyRoomGUI(Player forPlayer) {
		ItemStack baseItem = ItemUtils.createItem(VersionedMaterial.GRASS_BLOCK, 1,
				Config.getMessage("minebay.gui.confirm.room-create.name"),
				Config.getMessageList("minebay.gui.confirm.room-create.lore", "price", ""+Config.config.getInt("minebay.user-rooms.room-price")));
		return getConfirmGUI(forPlayer, baseItem, new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), new BigDecimal(Config.config.getInt("minebay.user-rooms.room-price")));
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
				MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), new BigDecimal(Config.config.getInt("minebay.user-rooms.slot-price") * amount));
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
				MineBayEconomyResponse re = Main.econ.depositPlayer(e.getPlayer(), new BigDecimal(Config.config.getInt("minebay.user-rooms.slot-sell-price") * amount));
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
				MineBayEconomyResponse re = Main.econ.depositPlayer(e.getPlayer(), new BigDecimal(worth));
				if(re.isTransactionSuccess()){
					AuctionRooms.deleteAuctionRoom(r.getID());
					for(Player pl : Bukkit.getOnlinePlayers()){
						Inventory oI = MineBay.getOpenInv(pl);
						if(oI == null) continue;
						GUI gui2 = GUI.getGUI(oI);
						if(gui2 == null) continue;
						GUIHolder holder = (GUIHolder) oI.getHolder();
						String t = (String) holder.getProperty("minebay_type");
						if(t == null) continue;
						if(t.equals("auction room")) {
							int roomID = (int) holder.getProperty("minebay_auctionroom_id");
							if(roomID == r.getID()) {
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
		Inventory base = getPurchaseConfirmGUI(forPlayer, sellIt, e -> {
			e.setCancelled(true);
			AuctionRoom r = sellIt.getRoom();
			MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), sellIt.getPrice());
			
			if(!re.isTransactionSuccess()) {
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.purchase.error", "error", re.getError()));
				return;
			}
			
			OfflinePlayer seller;
			if(Config.useUUIDs) {
				seller = Bukkit.getOfflinePlayer(UUID.fromString(sellIt.getSeller()));
			}else{
				seller = Bukkit.getOfflinePlayer(sellIt.getSeller());
			}
			OfflinePlayer owner = null;
			if(r.getOwner()!=null){
				if(Config.useUUIDs) {
					owner = Bukkit.getOfflinePlayer(UUID.fromString(r.getOwner()));
				}else {
					owner = Bukkit.getOfflinePlayer(r.getOwner());
				}
			}
			BigDecimal sellerAm = sellIt.getPrice().multiply(new BigDecimal((100 - r.getTaxshare()) * 0.01D)).setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal ownerAm = sellIt.getPrice().multiply(new BigDecimal(r.getTaxshare() * 0.01D)).setScale(2, RoundingMode.HALF_DOWN);
			MineBayEconomyResponse r2 = Main.econ.depositPlayer(seller, sellerAm);
			
			if(!r2.isTransactionSuccess()) {
				Main.econ.depositPlayer(e.getPlayer(), sellIt.getPrice()); // Refund money
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.purchase.error", "error", r2.getError()));
				return;
			}
			
			MineBayEconomyResponse r3 = null;
			if(owner!=null) r3 = Main.econ.depositPlayer(owner, ownerAm);
			
			if(r3 != null && !r3.isTransactionSuccess()) {
				Main.econ.depositPlayer(e.getPlayer(), sellIt.getPrice()); // Refund money
				Main.econ.withdrawPlayer(owner, ownerAm); // Revoke money
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.purchase.error", "error", r3.getError()));
				return;
			}
			
			e.getPlayer().sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.success"), sellIt, r));
			r.removeSellItem(sellIt.getID());
			r.updateMineBay();
			ItemUtils.addItemOrDrop(e.getPlayer(), sellIt.getItem());
			if(Config.enableTransactionLog) MineBayTransactionLogger.logTransaction(seller, e.getPlayer(), r, sellIt.getPrice(), sellIt.getItem());
			e.getPlayer().closeInventory();
			if(seller.isOnline()){
				((Player)seller).sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.seller.success"), sellIt, r).replace("%buyer%", e.getPlayer().getName()).replace("%price2%", ""+sellerAm));
			}
			if(owner!=null && owner.isOnline() && r.getTaxshare() > 0){
				((Player) owner).sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.room-owner.success"), sellIt, r).replace("%buyer%", e.getPlayer().getName()).replace("%price2%", ""+ownerAm));
			}
		});
//		base.getBuilder().addElement(1, new StaticGUIElement(sellIt.getConfirmItemStack()));
		return base;
	}
	
	@SuppressWarnings("deprecation")
	private static GUIMultiPage<SellItem> buildAuctionRoomGUI(){
		GUIBuilderMultiPage<SellItem> builder = new GUIBuilderMultiPage<>(Config.prefix, 6);
		builder.addPageSlotsInRange(0, 44);
		builder.addPreviousPageItem(52, ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.previous-page")));
		builder.addNextPageItem(53, ItemUtils.createItem(ItemUtils.arrowRight(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.next-page")));
		
		ItemStack gPaneItem = ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0");
		GUIElement gPane = new StaticGUIElement(gPaneItem);
		if(Config.config.getBoolean("minebay.general.enable-user-rooms")) {
			builder.addElement(45, new GUIElement() {
				
				@Override
				public ItemStack getItem(GUIBuildEvent event) {
					if((boolean) event.getGUIHolder().getProperty(Main.pl, "disable_back_button")) return gPaneItem;
					return ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"));
				}
			}.setAction(new GUIElementAction() {
						
						@Override
						public void onAction(GUIElementActionEvent e) {
							e.setCancelled(true);
							if((boolean) e.getGUIHolder().getProperty(Main.pl, "disable_back_button")) return;
							e.getPlayer().openInventory(GUIs.getAuctionRoomsGUI(e.getPlayer(), null));
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
		builder.setSupplier(new GUIItemSupplier<SellItem>() {
			
			@Override
			public GUIElement toGUIElement(GUIBuildPageItemEvent<SellItem> event, SellItem item) {
				Player p = event.getPlayer();
				return new StaticGUIElement(item.getSellItemStack(p))
						.setAction(new GUIElementAction() {
							
							@Override
							public void onAction(GUIElementActionEvent e) {
								AuctionRoom room = item.getRoom();
								if(e.getClickType().isRightClick() && room.canEdit(p) && !item.isSeller(p)) {
									PlayerData.addOfflineNotification(item.getSellerPlayer(), new CancelledOffer(item));
									room.removeSellItem(item.getID());
									p.sendMessage(Config.getMessage("minebay.info.offer-cancelled", "seller", item.getSellerName()));
								}else {
									if(item.getSeller() != null && !item.isSeller(p)){
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
				if(!r.canSell(e.getPlayer())) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.missing-access"));
					e.setCancelled(true);
					return;
				}
				e.setCancelled(!Config.config.getBoolean("minebay.general.allow-drag-and-drop"));
			}
		});
		builder.setActionListener(e -> {
			AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
			if(e.getItemClickedWith() != null && !e.getItemClickedWith().getType().equals(Material.AIR) && e.getElementClicked() == null) {
				if(r.getOccupiedSlots() < r.getSlots() || r.getSlots() == -1){
					if(r.isDefaultRoom() ?
							MineBay.hasPermissionToCreateDefaultRoomSale(r, e.getPlayer())
							: (r.getSoldItemsBySeller(e.getPlayer()).size() < Config.config.getInt("minebay.user-rooms.offers-per-slot"))){
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
		});
		return builder.create();
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
		
		builder.addElement(7, new GUIElement() {
			
			@Override
			public ItemStack getItem(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				return ItemUtils.createItem(r.isPrivateRoom() ? VersionedMaterial.SKELETON_SKULL : VersionedMaterial.WITHER_SKELETON_SKULL, 1,
						Config.getMessage(r.isPrivateRoom() ? "minebay.gui.room-settings.private-room.whitelist.name" : "minebay.gui.room-settings.private-room.blacklist.name"),
						Config.getMessageList(r.isPrivateRoom() ? "minebay.gui.room-settings.private-room.whitelist.lore" : "minebay.gui.room-settings.private-room.blacklist.lore"));
			}
		}.setAction(event -> {
			event.getPlayer().openInventory(getAuctionRoomPlayerListGUI(event.getPlayer(), (int) event.getGUIHolder().getProperty(Main.pl, "room_id"), 0));
			event.setCancelled(true); // TODO
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
					if(e.getClickType().equals(ClickType.LEFT)){
						if(r.getSlots() < Config.config.getInt("minebay.user-rooms.max-slots")){
							e.getPlayer().openInventory(GUIs.buySlotsGUI(e.getPlayer(), r, 1));
						}else{
							e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-buy.toomanyslots"));
						}
					}else if(e.getClickType().equals(ClickType.SHIFT_LEFT)){
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
						if(e.getClickType().equals(ClickType.LEFT)){
							if(r.getSlots() > Config.config.getInt("minebay.user-rooms.default-slot-number")){
								if(r.getOccupiedSlots() <= r.getSlots() - 1){
									e.getPlayer().openInventory(GUIs.sellSlotsGUI(e.getPlayer(), r, 1));
								}else{
									e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.all-slots-occupied"));
								}
							}else{
								e.getPlayer().sendMessage(Config.getMessage("minebay.info.slot-sell.notenoughslots"));
							}
						}else if(e.getClickType().equals(ClickType.SHIFT_LEFT)){
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
		
		builder.addElement(41, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(), Config.getMessage("minebay.gui.room-settings.tax-increase.name"), Config.getMessageList("minebay.gui.room-settings.tax-increase.lore", "tax-changing-disabled", Config.allowTaxChange ? "" : Config.getMessage("minebay.gui.room-settings.tax-increase.disabled")))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(!Config.allowTaxChange && !e.getPlayer().hasPermission("minebay.change-tax-when-disabled")) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax-changing-disabled"));
					e.setCancelled(true);
					return;
				}
				if(e.getClickType().equals(ClickType.LEFT)){
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
				}else if(e.getClickType().equals(ClickType.SHIFT_LEFT)){
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
		
		builder.addElement(42, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowRight(), Config.getMessage("minebay.gui.room-settings.tax-decrease.name"), Config.getMessageList("minebay.gui.room-settings.tax-decrease.lore", "tax-changing-disabled", Config.allowTaxChange ? "" : Config.getMessage("minebay.gui.room-settings.tax-increase.disabled")))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				if(!Config.allowTaxChange && !e.getPlayer().hasPermission("minebay.change-tax-when-disabled")) {
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.tax-changing-disabled"));
					e.setCancelled(true);
					return;
				}
				if(e.getClickType().equals(ClickType.LEFT)){
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
				}else if(e.getClickType().equals(ClickType.SHIFT_LEFT)){
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

		return builder.create();
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
					MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), new BigDecimal(price));
					if(re.isTransactionSuccess()){
						r.setIcon(e.getItemClickedWith());
						r.saveAllSettings();
						r.updateSettings();
						MineBay.updateRoomSelection();
						if(!Config.config.getBoolean("minebay.general.user-rooms-settings.change-icon-remove-item")){
							Utils.addItem(e.getPlayer(), e.getItemClickedWith());
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
		
		return builder.create();
	}
	
	private static GUIMultiPage<OfflinePlayer> buildAuctionRoomPlayerListGUI() {
		GUIBuilderMultiPage<OfflinePlayer> builder = new GUIBuilderMultiPage<>(Config.prefix, 4);
		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		for(int i = 27; i < 36; i++) {
			builder.addElement(i, gPane);
		}
		
		builder.addElement(27, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"))).setAction(new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
				e.getPlayer().openInventory(getAuctionRoomSettingsGUI(e.getPlayer(), r.getID()));
				e.setCancelled(true);
			}
		}));
		
		builder.addElement(28, new StaticGUIElement(ItemUtils.plus(VersionedDyeColor.WHITE)).setAction(event -> {
			int roomID = (int) event.getGUIHolder().getProperty(Main.pl, "room_id");
			Events.addPlayer.put(event.getPlayer().getUniqueId(), roomID);
			int maxTime = Config.config.getInt("minebay.general.max-type-time-seconds");
			if(maxTime>0){
				Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask(event.getPlayer()), maxTime * 20);
			}
			event.getPlayer().closeInventory();
			event.getPlayer().sendMessage(Config.getMessage("minebay.info.addplayer"));
		}));

		builder.addPreviousPageItem(34, ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.previous-page")));
		builder.addNextPageItem(35, ItemUtils.createItem(ItemUtils.arrowRight(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.next-page")));
		
		builder.setSupplier(new GUIItemSupplier<OfflinePlayer>() {

			@SuppressWarnings("deprecation")
			@Override
			public GUIElement toGUIElement(GUIBuildPageItemEvent<OfflinePlayer> event, OfflinePlayer item) {
				ItemStack it = ItemUtils.createItem(VersionedMaterial.PLAYER_HEAD, 1,
						Config.getMessage("minebay.gui.player-list.item.name", "player", item.getName()),
						Config.getMessageList("minebay.gui.player-list.item.lore"));
				SkullMeta s = (SkullMeta) it.getItemMeta();
				if(NMSVersion.getCurrentServerVersion().isNewerThanOrEqualTo(NMSVersion.V1_12_R1)) {
					s.setOwningPlayer(item);
				}else {
					s.setOwner(item.getName());
				}
				it.setItemMeta(s);
				return new StaticGUIElement(it).setAction(e -> {
					AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) e.getGUIHolder().getProperty(Main.pl, "room_id"));
					r.removePlayerFromList(item);
					r.saveAllSettings();
					r.updatePlayerList();
				});
			}
			
			@Override
			public List<OfflinePlayer> getItems(GUIBuildEvent event) {
				AuctionRoom r = AuctionRooms.getAuctionRoomByID((int) event.getGUIHolder().getProperty(Main.pl, "room_id"));
				return r.getPlayerList();
			}
		});
		
		builder.addPageSlotsInRange(0, 26);
		
		return builder.create();
	}
	
	public static Inventory getAuctionRoomGUI(Player forPlayer, int roomID, int page) {
		return getAuctionRoomGUI(forPlayer, roomID, page, false);
	}
	
	public static Inventory getAuctionRoomGUI(Player forPlayer, int roomID, int page, boolean disableBackButton) {
		return AUCTION_ROOM_GUI.getForPlayer(forPlayer, page, Main.pl, new QuickMap<String, Object>().put("room_id", roomID).put("disable_back_button", disableBackButton).makeHashMap());
	}
	
	public static Inventory getAuctionRoomSettingsGUI(Player forPlayer, int roomID) {
		return AUCTION_ROOM_SETTINGS_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("room_id", roomID).makeHashMap());
	}
	
	public static Inventory getAuctionRoomCustomIconGUI(Player forPlayer, int roomID) {
		return AUCTION_ROOM_CUSTOM_ICON_GUI.getForPlayer(forPlayer, Main.pl, new QuickMap<String, Object>().put("room_id", roomID).makeHashMap());
	}
	
	public static Inventory getAuctionRoomPlayerListGUI(Player forPlayer, int roomID, int page) {
		return AUCTION_ROOM_PLAYER_LIST_GUI.getForPlayer(forPlayer, page, Main.pl, new QuickMap<String, Object>().put("room_id", roomID).makeHashMap());
	}
	
}
