package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.GUIUtils;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ClickAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUI;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilder;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilderMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElement;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElementAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ItemSupplier;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.StaticGUIElement;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import net.milkbowl.vault.economy.EconomyResponse;

public class GUIs {
	
	private static GUIBuilderMultiPage<AuctionRoom> getAuctionRoomsBuilder(String owner){
		GUIBuilderMultiPage<AuctionRoom> builder = new GUIBuilderMultiPage<>(Config.prefix, 6);
		builder.addPreviousPageItem(52, ItemUtils.createItem(ItemUtils.arrowLeft(DyeColor.WHITE), Config.getMessage("minebay.gui.misc.previous-page")));
		builder.addNextPageItem(53, ItemUtils.createItem(ItemUtils.arrowRight(DyeColor.WHITE), Config.getMessage("minebay.gui.misc.next-page")));
		builder.addPageSlotsInRange(0, 44);
		builder.addElement(49, new StaticGUIElement(Tools.createItem(Material.STAINED_CLAY, 1, 5, Config.getMessage("minebay.gui.rooms.create-room")))
				.setAction(new GUIElementAction() {
					
					@Override
					public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent e) {
						if(Config.config.getBoolean("minebay.general.enable-user-rooms") && (Config.config.getBoolean("minebay.general.allow-room-creation") || p.hasPermission("minebay.user-rooms.create.when-disallowed"))){
							if(MineBay.hasPermissionToCreateRoom(p)){
								p.openInventory(buyRoomGUI().getForPlayer(p));
							}else{
								p.sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.room-create.error.too-many-rooms")));
							}
						}else{
							p.sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.user-rooms-disabled")));
						}
						return true;
					}
				}));
		builder.addElement(50, new StaticGUIElement(Tools.createItem(Material.BANNER, 1, 10, Config.getMessage("minebay.gui.rooms.list-all")))
				.setAction(new GUIElementAction() {
					
					@Override
					public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent e) {
						p.openInventory(getAuctionRoomsGUI(null).getForPlayer(p));
						return true;
					}
				}));
		builder.addElement(51, new StaticGUIElement(Tools.createItem(Material.BANNER, 1, 14, Config.getMessage("minebay.gui.rooms.list-self")))
				.setAction(new GUIElementAction() {
					
					@Override
					public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent e) {
						p.openInventory(getAuctionRoomsGUI(p.getName()).getForPlayer(p));
						return true;
					}
				}));
		GUIElement gPane = new StaticGUIElement(Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0"));
		builder.addElement(45, gPane);
		builder.addElement(46, gPane);
		builder.addElement(47, gPane);
		builder.addElement(48, gPane);
		builder.setSupplier(new ItemSupplier<AuctionRoom>() {
			
			@Override
			public GUIElement toGUIElement(Player p, AuctionRoom room) {
				return new StaticGUIElement(room.getSelectItemStack(p))
						.setAction(new GUIElementAction() {
							
							@Override
							public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent e) {
								if(button.equals(ClickAction.LEFT_CLICK) || button.equals(ClickAction.SHIFT_LEFT_CLICK)){
									p.openInventory(room.getMineBayInv(0, p));
								}else if((button.equals(ClickAction.RIGHT_CLICK) || button.equals(ClickAction.SHIFT_RIGHT_CLICK))&& room.canEdit(p)){
									p.openInventory(room.getSettingsGUI().getForPlayer(p));
								}
								return true;
							}
						});
			}
			
			@Override
			public List<AuctionRoom> getItems() {
				if(owner == null) {
					return AuctionRooms.getAuctionRooms();
				}
				return AuctionRooms.getAuctionRoomsByOwner(owner);
			}
		});
		return builder;
	}

	public static GUIMultiPage<AuctionRoom> getAuctionRoomsGUI(String owner) {
		return getAuctionRoomsBuilder(owner).build();
	}
	
	public static GUIMultiPage<AuctionRoom> getAuctionRoomsSellGUI(String owner, BigDecimal price){
		GUIBuilderMultiPage<AuctionRoom> builderBase = getAuctionRoomsBuilder(owner);
		builderBase.setSupplier(new ItemSupplier<AuctionRoom>() {
			
			@Override
			public GUIElement toGUIElement(Player p, AuctionRoom room) {
				return new StaticGUIElement(room.getSelectItemStack(p))
						.setAction(new GUIElementAction() {
							
							@Override
							public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
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
								return true;
							}
						});
			}
			
			@Override
			public List<AuctionRoom> getItems() {
				if(owner == null) {
					return AuctionRooms.getAuctionRooms();
				}
				return AuctionRooms.getAuctionRoomsByOwner(owner);
			}
		});
		return builderBase.build();
	}
	
	public static GUI getConfirmGUI(ItemStack baseItem, GUIElementAction confirm){
		GUIBuilder builder = new GUIBuilder(Config.prefix, InventoryType.HOPPER);

		GUIElement gPane = new StaticGUIElement(Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0"));
		builder.addElement(1, gPane);
		builder.addElement(2, gPane);
		
		builder.addElement(0, new StaticGUIElement(baseItem));
		ItemStack confirmItem = ItemUtils.createItem(ItemUtils.createBanner(null, DyeColor.GREEN),
				Config.getMessage("minebay.gui.confirm.confirm.name"),
				Config.getMessageList("minebay.gui.confirm.confirm.lore"));
		builder.addElement(3, new StaticGUIElement(confirmItem).setAction(confirm));
		ItemStack cancelItem = ItemUtils.createItem(ItemUtils.createBanner(null, DyeColor.RED),
				Config.getMessage("minebay.gui.confirm.cancel.name"),
				Config.getMessageList("minebay.gui.confirm.cancel.lore"));
		builder.addElement(4, new StaticGUIElement(cancelItem).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction a, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent e) {
				p.closeInventory();
				return true;
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
			public boolean action(Player p, ClickAction e, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
				EconomyResponse re = Main.econ.withdrawPlayer(p, Config.config.getInt("minebay.user-rooms.room-price"));
				if(re.transactionSuccess()){
					CancelTask.cancelForPlayer(p);
					AuctionRoom r = AuctionRooms.createAuctionRoom(p, AuctionRooms.getNewRoomID(), false);
					MineBay.updateRoomSelection();
					p.openInventory(r.getSettingsGUI().getForPlayer(p));
					p.sendMessage(Config.replaceForAuctionRoom(Config.getMessage("minebay.info.room-created"), r));
				}else{
					p.sendMessage(Config.getMessage("minebay.info.room-create.error.general").replace("%error%", re.errorMessage));
					p.closeInventory();
				}
				return true;
			}
		});
	}
	
	public static GUI buySlotsGUI(AuctionRoom r, int amount) {
		ItemStack baseItem = Tools.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.slots-buy.name"),
				Config.getMessageList("minebay.gui.confirm.slots-buy.lore",
						"price", ""+(amount*Config.config.getInt("minebay.user-rooms.slot-price")),
						"amount", ""+amount,
						"room-id", ""+r.getRoomID()));
		return getConfirmGUI(baseItem, new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction e, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
				EconomyResponse re = Main.econ.withdrawPlayer(p, Config.config.getInt("minebay.user-rooms.slot-price")*amount);
				if(re.transactionSuccess()){
					r.setSlots(r.getSlots()+amount);
					r.saveAllSettings();
					r.updateSettings();
					MineBay.updateRoomSelection();
					if(Config.config.getBoolean("minebay.general.user-rooms-settings.slot-notify")){
						p.sendMessage(Config.getMessage("minebay.info.slot-buy.success", "slotamount", ""+amount, "price", ""+Config.config.getInt("minebay.user-rooms.slot-price")*amount));
					}
				}
				p.openInventory(r.getSettingsGUI().getForPlayer(p));
				return true;
			}
		});
	}
	
	public static GUI sellSlotsGUI(AuctionRoom r, int amount) {
		ItemStack baseItem = Tools.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.slots-sell.name"),
				Config.getMessageList("minebay.gui.confirm.slots-sell.lore",
						"price", ""+(amount*Config.config.getInt("minebay.user-rooms.slot-sell-price")),
						"amount", ""+amount,
						"room-id", ""+r.getRoomID()));
		return getConfirmGUI(baseItem, new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction e, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
				EconomyResponse re = Main.econ.depositPlayer(p, Config.config.getInt("minebay.user-rooms.slot-sell-price")*amount);
				if(re.transactionSuccess()){
					r.setSlots(r.getSlots()-amount);
					r.saveAllSettings();
					r.updateSettings();
					MineBay.updateRoomSelection();
					if(Config.config.getBoolean("minebay.general.user-rooms-settings.slot-notify")){
						p.sendMessage(Config.getMessage("minebay.info.slot-sell.success", "slotamount%", ""+amount, "price", ""+Config.config.getInt("minebay.user-rooms.slot-sell-price")*amount));
					}
				}
				p.openInventory(r.getSettingsGUI().getForPlayer(p));
				return true;
			}
		});
	}
	
	public static GUI sellRoomGUI(AuctionRoom r) {
		int sl = (r.getSlots() - Config.config.getInt("minebay.user-rooms.default-slot-number"))*Config.config.getInt("minebay.user-rooms.slot-sell-price");
		int pr = Config.config.getInt("minebay.user-rooms.room-sell-price");
		
		ItemStack baseItem = Tools.createItem(Material.NAME_TAG, 1, 0,
				Config.getMessage("minebay.gui.confirm.room-sell.name"),
				Config.getMessageList("minebay.gui.confirm.room-sell.lore",
						"price", ""+(sl+pr)));
		return getConfirmGUI(baseItem, new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction e, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
				int worth = r.getWorth();
				EconomyResponse re = Main.econ.depositPlayer(p, worth);
				if(re.transactionSuccess()){
					AuctionRooms.deleteAuctionRoom(r.getRoomID());
					for(Player pl : Bukkit.getOnlinePlayers()){
						Inventory oI = MineBay.getOpenInv(pl);
						if(oI == null) continue;
						GUI gui2 = GUIUtils.getGUI(oI);
						if(gui2 == null) continue;
						HashMap<String, Object> props = gui2.getHolder().getProperties();
						String t = (String) props.get("minebay_type");
						if(t==null) continue;
						if(t.equals("auction room")) {
							if(((int) props.get("minebay_auctionroom_id")) == r.getRoomID()) {
								pl.closeInventory();
							}
						}
					}
					MineBay.updateRoomSelection();
					p.closeInventory();
					p.sendMessage(Config.getMessage("minebay.info.sell-room.success").replace("%price%", ""+worth));
				}else{
					p.sendMessage(Config.getMessage("minebay.info.sell-room.error").replace("%error%", re.errorMessage));
				}
				return true;
			}
		});
	}
	
	public static GUI purchaseItemGUI(SellItem sellIt) {
		GUI base = getConfirmGUI(sellIt.getItem(), new GUIElementAction() {
			
			@SuppressWarnings("deprecation")
			@Override
			public boolean action(Player p, ClickAction e, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
				AuctionRoom r = sellIt.getRoom();
				EconomyResponse re = Main.econ.withdrawPlayer(p, sellIt.getPrice().doubleValue());
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
				EconomyResponse r2 = Main.econ.depositPlayer(seller, sellerAm);
				EconomyResponse r3 = null;
				if(owner!=null){
					r3 = Main.econ.depositPlayer(owner, ownerAm);
				}
				if(re.transactionSuccess() && r2.transactionSuccess()){
					if((owner!=null && r3!=null && r3.transactionSuccess()) || owner==null){
						p.sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.success"), sellIt, r));
						r.removeSellItem(sellIt.getID());
						r.updateMineBay();
						ItemUtils.addItemOrDrop(p, sellIt.getItem());
						p.closeInventory();
						if(seller.isOnline()){
							((Player)seller).sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.seller.success"), sellIt, r).replace("%buyer%", p.getName()).replace("%price2%", ""+sellerAm));
						}
						if(owner!=null && owner.isOnline() && r.getTaxshare() > 0){
							((Player) owner).sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.purchase.room-owner.success"), sellIt, r).replace("%buyer%", p.getName()).replace("%price2%", ""+ownerAm));
						}
					}
				}
				return true;
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
