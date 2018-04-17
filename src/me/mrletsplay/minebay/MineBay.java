package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.mrletsplay.mrcore.bukkitimpl.GUIUtils;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUI;

public class MineBay {
	
	public static AuctionRoom getMainAuctionRoom(){
		return AuctionRooms.getAuctionRoomByID(0);
	}
	
	public static void showPurchaseConfirmDialog(Player p, SellItem item){
		String name = Config.prefix+" "+Config.getMessage("minebay.gui.purchase-confirm.items.name");
		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, name);
		ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta gMeta = gPane.getItemMeta();
		gMeta.setDisplayName("§0");
		gPane.setItemMeta(gMeta);
		
		inv.setItem(0, item.getItem());
		inv.setItem(1, item.getConfirmItemStack());
		inv.setItem(2, gPane);
		inv.setItem(3, Tools.createItem(Material.BANNER, 1, 10, Config.getMessage("minebay.gui.purchase-confirm.items.confirm")));
		inv.setItem(4, Tools.createItem(Material.BANNER, 1, 1, Config.getMessage("minebay.gui.purchase-confirm.items.cancel")));
		
		p.openInventory(inv);
	}
	
//	public static void changeInv(Inventory oldInv, Inventory newInv){
//		int i = 0;
//		for(ItemStack it : newInv.getContents()){
//			oldInv.setItem(i, it);
//			i++;
//		}
//	}
	
//	public static Inventory getRoomSelectionMenu(int page, String owner, Player p){
//		List<AuctionRoom> aRooms;
//		if(owner.equals("all")){
//			aRooms = AuctionRooms.getAuctionRooms();
//		}else{
//			aRooms = AuctionRooms.getAuctionRoomsByOwner(owner);
//		}
//		int pages = aRooms.size()/9/5;
//		if(pages >= page && page >= 0){
//			Inventory inv = Bukkit.createInventory(null, 6*9, Config.prefix);
//			int start = page*5*9;
//			int end = (aRooms.size()<=start+5*9)?aRooms.size():start+5*9;
//			for(int i = start; i < end; i++){
//				AuctionRoom it = aRooms.get(i);
//				ItemStack i2 = it.getSelectItemStack(p);
//				if(i2==null) {
//					Main.pl.getLogger().warning("Room "+it.getRoomID()+" seems to be misconfigured!");
//					boolean b = it.backupConfig(new File("plugins/MineBay/AuctionRooms", it.getRoomID()+"-old.yml"));
//					if(it.getOwner()!=null) {
//						it.setDefaultSettings(it.getOwner(), it.isDefaultRoom());
//						Main.pl.getLogger().warning("Its old config has been renamed to "+it.getRoomID()+"-old.yml and the default settings have been reapplied to prevent further errors");
//					}else {
//						AuctionRooms.deleteAuctionRoom(it.getRoomID());
//						Main.pl.getLogger().warning("Failed to fix errors! The room has been deleted to prevent further errors "+(b?"(Old config has been renamed to "+it.getRoomID()+"-old.yml)":"(Failed to backup old config file)"));
//					}
//					inv.setItem(i-start, Tools.createItem(Material.BARRIER, 1, 0, "§c?", "§cFailed to load room"));
//				}else {
//					inv.setItem(i-start, it.getSelectItemStack(p));
//				}
//			}
//			ItemStack gPane = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0");
//			for(int i = 5*9; i < 6*9-4; i++){
//				inv.setItem(i, gPane);
//			}
//			ItemStack createRoom = Tools.createItem(Material.STAINED_CLAY, 1, 5, Config.getMessage("minebay.gui.rooms.create-room"));
//			ItemStack aLeft = Tools.createItem(Tools.arrowLeft(), Config.getMessage("minebay.gui.misc.previous-page"));
//			ItemStack aRight = Tools.createItem(Tools.arrowRight(), Config.getMessage("minebay.gui.misc.next-page"));
//			ItemStack gPane2 = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§8Auction Rooms", "§8Page: §7"+page, "§8Owner: §7"+owner);
//			inv.setItem(46, gPane2);
//			inv.setItem(49, createRoom);
//			inv.setItem(50, Tools.createItem(Material.BANNER, 1, 10, Config.getMessage("minebay.gui.rooms.list-all")));
//			inv.setItem(51, Tools.createItem(Material.BANNER, 1, 14, Config.getMessage("minebay.gui.rooms.list-self")));
//			inv.setItem(52, aLeft);
//			inv.setItem(53, aRight);
//			return inv;
//		}else{
//			return null;
//		}
//	}
//	
//	public static Inventory getSellRoomSelectionMenu(int page, String owner, BigDecimal price){
//		List<AuctionRoom> aRooms;
//		if(owner.equals("all")){
//			aRooms = AuctionRooms.getAuctionRooms();
//		}else{
//			aRooms = AuctionRooms.getAuctionRoomsByOwner(owner);
//		}
//		int pages = aRooms.size()/9/5;
//		if(pages >= page && page >= 0){
//			Inventory inv = Bukkit.createInventory(null, 6*9, Config.prefix);
//			int start = page*5*9;
//			int end = (aRooms.size()<=start+5*9)?aRooms.size():start+5*9;
//			for(int i = start; i < end; i++){
//				AuctionRoom it = aRooms.get(i);
//				inv.setItem(i-start, it.getSelectItemStack(null));
//			}
//			ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
//			ItemMeta gMeta = gPane.getItemMeta();
//			gMeta.setDisplayName("§0");
//			gPane.setItemMeta(gMeta);
//			for(int i = 5*9; i < 6*9-4; i++){
//				inv.setItem(i, gPane);
//			}
//			ItemStack aLeft = Tools.createItem(Tools.arrowLeft(), Config.getMessage("minebay.gui.misc.previous-page"));
//			ItemStack aRight = Tools.createItem(Tools.arrowRight(), Config.getMessage("minebay.gui.misc.next-page"));
//			ItemStack gPane3 = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§8Sell Item", "§8Page: §7"+page, "§8Owner: §7"+owner, "§8Price: §7"+price);
//			inv.setItem(46, gPane3);
//			inv.setItem(50, Tools.createItem(Material.BANNER, 1, 10, Config.getMessage("minebay.gui.rooms.list-all")));
//			inv.setItem(51, Tools.createItem(Material.BANNER, 1, 14, Config.getMessage("minebay.gui.rooms.list-self")));
//			inv.setItem(52, aLeft);
//			inv.setItem(53, aRight);
//			return inv;
//		}else{
//			return null;
//		}
//	}
	
	public static void updateRoomSelection(){
		for(Player pl : Bukkit.getOnlinePlayers()){
			Inventory oI = getOpenInv(pl);
			if(oI == null) continue;
			GUI gui = GUIUtils.getGUI(oI);
			if(gui == null) continue;
			HashMap<String, Object> props = gui.getHolder().getProperties();
			String t = (String) props.get("minebay_type");
			if(t == null) continue;
			if(t.equals("auction rooms")){
				pl.openInventory(GUIs.getAuctionRoomsGUI((String) props.get("minebay_search")).getForPlayer(pl, (int) props.get("minebay_page")));
			}else if(t.equals("sell item")){
				pl.openInventory(GUIs.getAuctionRoomsSellGUI((String) props.get("minebay_search"), (BigDecimal) props.get("price")).getForPlayer(pl, (int) props.get("minebay_page")));
			}
		}
	}
	
	public static Inventory getOpenInv(Player p) {
		if(p.getOpenInventory()!=null && p.getOpenInventory().getTopInventory()!=null){
			return p.getOpenInventory().getTopInventory();
		}else{
			return null;
		}
	}
	
//	public static String getInvType(Inventory inv){
//		if(inv.getHolder() instanceof GUIHolder) {
//			GUIHolder holder = (GUIHolder) inv.getHolder();
//			String mode = (String) holder.getProperties().get("minebay_type");
//			return mode;
//		}
//		return "other";
//	}
	
//	public static String getInvType(Player p){
//		if(p.getOpenInventory()!=null && p.getOpenInventory().getTopInventory()!=null){
//			return getInvType(p.getOpenInventory().getTopInventory());
//		}else{
//			return "none";
//		}
//	}
	
	public static boolean hasPermissionToCreateRoom(Player p){
		int mRooms = Config.config.getInt("minebay.user-rooms.max-rooms");
		if(p.hasPermission("minebay.user-rooms.create.unlimited")){
			mRooms = -1;
		}else{
			for(String perm : Config.config.getStringList("room-perms")){
				if(p.hasPermission(perm)){
					int r = Config.config.getInt("room-perm."+perm+".max-rooms");
					if(r > mRooms){
						mRooms = r;
					}
				}
			}
		}
		List<AuctionRoom> rooms = AuctionRooms.getAuctionRoomsByOwner(p.getName());
		if(rooms.size() < mRooms || mRooms == -1){
			return true;
		}
		return false;
	}
	
	public static boolean hasPermissionForColoredNames(Player p){
		if(p.hasPermission("minebay.user-rooms.use-colored-names")){
			return true;
		}else{
			for(String perm : Config.config.getStringList("room-perms")){
				if(p.hasPermission(perm)){
					if(Config.config.getBoolean("room-perm."+perm+".allow-colored-names")){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean hasPermissionForColoredDescriptions(Player p){
		if(p.hasPermission("minebay.user-rooms.use-colored-descriptions")){
			return true;
		}else{
			for(String perm : Config.config.getStringList("room-perms")){
				if(p.hasPermission(perm)){
					if(Config.config.getBoolean("room-perm."+perm+".allow-colored-descriptions")){
						return true;
					}
				}
			}
		}
		return false;
	}
	
//	public static GUI getConfirmGUI(ItemStack baseItem, String type){
//		GUIBuilder builder = new GUIBuilder(Config.prefix, 1);
//		
//		builder.addElement(0, new StaticGUIElement(baseItem));
//		builder.addElement(slot, e)
//		
//		return builder.build();
//		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Config.prefix+" §7Confirm");
//		
//		ItemStack gPane = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0");
//		ItemStack confirm = Tools.createBanner("§aConfirm", DyeColor.GREEN);
//		ItemStack cancel = Tools.createBanner("§cCancel", DyeColor.RED);
//		
//		inv.setItem(0, baseItem);
//		inv.setItem(1, gPane);
//		inv.setItem(2, gPane);
//		inv.setItem(3, confirm);
//		inv.setItem(4, cancel);
//		
//		return inv;
//	}
	
}
