package me.mrletsplay.minebay;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

	public static GUIMultiPage<AuctionRoom> getAuctionRoomsGUI(String owner) {
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
								ItemStack baseItem = ItemUtils.createItem(Material.GRASS, 1, 0,
										Config.getMessage("minebay.gui.confirm.room-create.name"),
										Config.getMessageList("minebay.gui.confirm.room-create.lore", "price", ""+Config.config.getInt("minebay.user-rooms.room-price")));
								p.openInventory(getConfirmGUI(baseItem, new GUIElementAction() {
									
									@Override
									public boolean action(Player p, ClickAction e, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
										EconomyResponse re = Main.econ.withdrawPlayer(p, Config.config.getInt("minebay.user-rooms.room-price"));
										if(re.transactionSuccess()){
											CancelTask.cancelForPlayer(p);
											AuctionRoom r = AuctionRooms.createAuctionRoom(p, AuctionRooms.getNewRoomID(), false);
											MineBay.updateRoomSelection();
											p.openInventory(r.getSettingsMenu());
											p.sendMessage(Config.replaceForAuctionRoom(Config.getMessage("minebay.info.room-created"), r));
										}else{
											p.sendMessage(Config.getMessage("minebay.info.room-create.error.general").replace("%error%", re.errorMessage));
											p.closeInventory();
										}
										return true;
									}
								}).getForPlayer(p));
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
									p.openInventory(room.getSettingsMenu());
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
		return builder.build();
	}
	
	public static GUI getConfirmGUI(ItemStack baseItem, GUIElementAction confirm){
		GUIBuilder builder = new GUIBuilder(Config.prefix, 1);

		GUIElement gPane = new StaticGUIElement(Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0"));
		builder.addElement(1, gPane);
		builder.addElement(2, gPane);
		
		builder.addElement(0, new StaticGUIElement(baseItem));
		builder.addElement(3, new StaticGUIElement(ItemUtils.createBanner("§aConfirm", DyeColor.GREEN)).setAction(confirm));
		builder.addElement(4, new StaticGUIElement(ItemUtils.createBanner("§cCancel", DyeColor.RED)).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction a, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent e) {
				p.closeInventory();
				return true;
			}
		}));
		
		return builder.build();
	}
	
}
