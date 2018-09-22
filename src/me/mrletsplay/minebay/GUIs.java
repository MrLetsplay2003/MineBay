package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuildEvent;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuildPageItemEvent;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilder;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilderMultiPage;
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

public class GUIs {
	
	private static GUIBuilderMultiPage<AuctionRoom> getAuctionRoomsBuilder(String owner){
		GUIBuilderMultiPage<AuctionRoom> builder = new GUIBuilderMultiPage<>(Config.prefix, 6);
		builder.addPreviousPageItem(52, ItemUtils.createItem(ItemUtils.arrowLeft(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.previous-page")));
		builder.addNextPageItem(53, ItemUtils.createItem(ItemUtils.arrowRight(VersionedDyeColor.WHITE), Config.getMessage("minebay.gui.misc.next-page")));
		builder.addPageSlotsInRange(0, 44);
		builder.addElement(49, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.LIME_STAINED_CLAY, 1, Config.getMessage("minebay.gui.rooms.create-room")))
				.setAction(new GUIElementAction() {
					
					@Override
					public void onAction(GUIElementActionEvent e) {
						if(Config.config.getBoolean("minebay.general.enable-user-rooms") && (Config.config.getBoolean("minebay.general.allow-room-creation") || e.getPlayer().hasPermission("minebay.user-rooms.create.when-disallowed"))){
							if(MineBay.hasPermissionToCreateRoom(e.getPlayer())){
								Inventory inv = buyRoomGUI().getForPlayer(e.getPlayer());
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
						e.getPlayer().openInventory(getAuctionRoomsGUI(null).getForPlayer(e.getPlayer()));
						e.setCancelled(true);
					}
				}));
		builder.addElement(51, new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.RED_BANNER, 1, Config.getMessage("minebay.gui.rooms.list-self")))
				.setAction(new GUIElementAction() {
					
					@Override
					public void onAction(GUIElementActionEvent e) {
						e.getPlayer().openInventory(getAuctionRoomsGUI(e.getPlayer().getName()).getForPlayer(e.getPlayer()));
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
			public GUIElement toGUIElement(GUIBuildPageItemEvent event, AuctionRoom room) {
				Player p = event.getPlayer();
				return new StaticGUIElement(room.getSelectItemStack(p))
						.setAction(new GUIElementAction() {
							
							@Override
							public void onAction(GUIElementActionEvent e) {
								if(e.getButton().equals(ClickAction.LEFT_CLICK) || e.getButton().equals(ClickAction.SHIFT_LEFT_CLICK)){
									p.openInventory(room.getMineBayInv(0, p));
								}else if((e.getButton().equals(ClickAction.RIGHT_CLICK) || e.getButton().equals(ClickAction.SHIFT_RIGHT_CLICK))&& room.canEdit(p)){
									p.openInventory(room.getSettingsGUI().getForPlayer(p));
								}
								e.setCancelled(true);
							}
						});
			}
			
			@Override
			public List<AuctionRoom> getItems(GUIBuildEvent event) {
				if(owner == null) {
					return AuctionRooms.getAuctionRooms();
				}
				return AuctionRooms.getAuctionRoomsByOwner(owner);
			}
		});
		
		HashMap<String, Object> props = new HashMap<>();
		props.put("minebay_type", "auction rooms");
		builder.setDefaultProperties(props);
		return builder;
	}

	public static GUIMultiPage<AuctionRoom> getAuctionRoomsGUI(String owner) {
		return getAuctionRoomsBuilder(owner).build();
	}
	
	public static GUIMultiPage<AuctionRoom> getAuctionRoomsSellGUI(String owner, BigDecimal price){
		GUIBuilderMultiPage<AuctionRoom> builderBase = getAuctionRoomsBuilder(owner);
		builderBase.setSupplier(new ItemSupplier<AuctionRoom>() {
			
			@Override
			public GUIElement toGUIElement(GUIBuildPageItemEvent event, AuctionRoom room) {
				Player p = event.getPlayer();
				return new StaticGUIElement(room.getSelectItemStack(p))
						.setAction(new GUIElementAction() {
							
							@SuppressWarnings("deprecation")
							@Override
							public void onAction(GUIElementActionEvent e) {
								if(room.getOccupiedSlots() < room.getSlots() || room.getSlots() == -1){
									if(room.getSoldItemsBySeller((Player) p).size() < Config.config.getInt("minebay.user-rooms.offers-per-slot")){
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
			
			@Override
			public List<AuctionRoom> getItems(GUIBuildEvent event) {
				if(owner == null) {
					return AuctionRooms.getAuctionRooms();
				}
				return AuctionRooms.getAuctionRoomsByOwner(owner);
			}
		});
		return builderBase.build();
	}
	
	public static GUI getConfirmGUI(ItemStack baseItem, GUIElementAction confirm){
		GUIBuilder builder = new GUIBuilder(Config.prefix, 3);

		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
		for(int i = 0; i < 27; i++) {
			builder.addElement(i, gPane);
		}
		
		builder.addElement(10, new StaticGUIElement(baseItem));
		ItemStack confirmItem = ItemUtils.createItem(ItemUtils.createBanner(null, VersionedDyeColor.GREEN),
				Config.getMessage("minebay.gui.confirm.confirm.name"),
				Config.getMessageList("minebay.gui.confirm.confirm.lore"));
		builder.addElement(14, new StaticGUIElement(confirmItem).setAction(confirm));
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
	
	public static GUI buyRoomGUI() {
		ItemStack baseItem = ItemUtils.createItem(Material.GRASS, 1, 0,
				Config.getMessage("minebay.gui.confirm.room-create.name"),
				Config.getMessageList("minebay.gui.confirm.room-create.lore", "price", ""+Config.config.getInt("minebay.user-rooms.room-price")));
		return getConfirmGUI(baseItem, new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), Config.config.getInt("minebay.user-rooms.room-price"));
				if(re.isTransactionSuccess()){
					CancelTask.cancelForPlayer(e.getPlayer());
					AuctionRoom r = AuctionRooms.createAuctionRoom(e.getPlayer(), AuctionRooms.getNewRoomID(), false);
					MineBay.updateRoomSelection();
					e.getPlayer().openInventory(r.getSettingsGUI().getForPlayer(e.getPlayer()));
					e.getPlayer().sendMessage(Config.replaceForAuctionRoom(Config.getMessage("minebay.info.room-created"), r));
				}else{
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.room-create.error.general").replace("%error%", re.getError()));
					e.getPlayer().closeInventory();
				}
				e.setCancelled(true);
			}
		});
	}
	
	public static GUI buySlotsGUI(AuctionRoom r, int amount) {
		ItemStack baseItem = ItemUtils.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.slots-buy.name"),
				Config.getMessageList("minebay.gui.confirm.slots-buy.lore",
						"price", ""+(amount*Config.config.getInt("minebay.user-rooms.slot-price")),
						"amount", ""+amount,
						"room-id", ""+r.getRoomID()));
		return getConfirmGUI(baseItem, new GUIElementAction() {
			
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
				e.getPlayer().openInventory(r.getSettingsGUI().getForPlayer(e.getPlayer()));
				e.setCancelled(true);
			}
		});
	}
	
	public static GUI sellSlotsGUI(AuctionRoom r, int amount) {
		ItemStack baseItem = ItemUtils.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.slots-sell.name"),
				Config.getMessageList("minebay.gui.confirm.slots-sell.lore",
						"price", ""+(amount*Config.config.getInt("minebay.user-rooms.slot-sell-price")),
						"amount", ""+amount,
						"room-id", ""+r.getRoomID()));
		return getConfirmGUI(baseItem, new GUIElementAction() {
			
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
				e.getPlayer().openInventory(r.getSettingsGUI().getForPlayer(e.getPlayer()));
				e.setCancelled(true);
			}
		});
	}
	
	public static GUI sellRoomGUI(AuctionRoom r) {
		int sl = (r.getSlots() - Config.config.getInt("minebay.user-rooms.default-slot-number"))*Config.config.getInt("minebay.user-rooms.slot-sell-price");
		int pr = Config.config.getInt("minebay.user-rooms.room-sell-price");
		
		ItemStack baseItem = ItemUtils.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.room-sell.name"),
				Config.getMessageList("minebay.gui.confirm.room-sell.lore",
						"price", ""+(sl+pr),
						"room-id", ""+r.getRoomID()));
		return getConfirmGUI(baseItem, new GUIElementAction() {
			
			@Override
			public void onAction(GUIElementActionEvent e) {
				int worth = r.getWorth();
				MineBayEconomyResponse re = Main.econ.depositPlayer(e.getPlayer(), worth);
				if(re.isTransactionSuccess()){
					AuctionRooms.deleteAuctionRoom(r.getRoomID());
					for(Player pl : Bukkit.getOnlinePlayers()){
						Inventory oI = MineBay.getOpenInv(pl);
						if(oI == null) continue;
						GUI gui2 = GUIUtils.getGUI(oI);
						if(gui2 == null) continue;
						GUIHolder holder = (GUIHolder) oI.getHolder();
						String t = (String) holder.getProperty("minebay_type");
						if(t == null) continue;
						if(t.equals("auction room")) {
							if(((int) holder.getProperty("minebay_auctionroom_id")) == r.getRoomID()) {
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
	
	public static GUI purchaseItemGUI(SellItem sellIt) {
		GUI base = getConfirmGUI(sellIt.getItem(), new GUIElementAction() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onAction(GUIElementActionEvent e) {
				AuctionRoom r = sellIt.getRoom();
				MineBayEconomyResponse re = Main.econ.withdrawPlayer(e.getPlayer(), sellIt.getPrice().doubleValue());
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
				MineBayEconomyResponse r3 = null;
				if(owner!=null){
					r3 = Main.econ.depositPlayer(owner, ownerAm);
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
				e.setCancelled(true);
			}
		});
		base.getBuilder().addElement(1, new StaticGUIElement(sellIt.getConfirmItemStack()));
		return base;
	}
	
	private static double round (double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    return (double) Math.round(value * scale) / scale;
	}
	
}
