package me.mrletsplay.minebay;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ClickAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUI;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilderMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElement;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElementAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ItemSupplier;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.StaticGUIElement;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;

public class GUIs {

	public static GUIMultiPage<AuctionRoom> getAuctionRoomsGUI(String owner) {
		GUIBuilderMultiPage<AuctionRoom> builder = new GUIBuilderMultiPage<>("Auction rooms", 6);
		builder.addPreviousPageItem(52, ItemUtils.arrowLeft(DyeColor.WHITE));
		builder.addNextPageItem(53, ItemUtils.arrowRight(DyeColor.WHITE));
		builder.addPageSlotsInRange(0, 36);
		builder.addElement(49, new StaticGUIElement(Tools.createItem(Material.STAINED_CLAY, 1, 5, Config.getMessage("minebay.gui.rooms.create-room")))
				.setAction(new GUIElementAction() {
					
					@Override
					public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui) {
						p.openInventory(Main.createRoomGUI.getForPlayer(p));
						return true;
					}
				}));
		builder.addElement(50, new StaticGUIElement(Tools.createItem(Material.BANNER, 1, 10, Config.getMessage("minebay.gui.rooms.list-all")))
				.setAction(new GUIElementAction() {
					
					@Override
					public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui) {
						p.openInventory(getAuctionRoomsGUI(null).getForPlayer(p));
						return true;
					}
				}));
		builder.addElement(51, new StaticGUIElement(Tools.createItem(Material.BANNER, 1, 14, Config.getMessage("minebay.gui.rooms.list-self")))
				.setAction(new GUIElementAction() {
					
					@Override
					public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui) {
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
							public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui) {
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
	
}
