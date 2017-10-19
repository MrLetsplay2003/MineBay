package me.mrletsplay.minebay;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MineBay {
	
	public static AuctionRoom getMainAuctionRoom(){
		return AuctionRooms.getAuctionRoomByID(0);
	}
	
	public static void showPurchaseConfirmDialog(Player p, SellItem item){
		String name = Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §8Confirm";
		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, name);
		ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta gMeta = gPane.getItemMeta();
		gMeta.setDisplayName("§0");
		gPane.setItemMeta(gMeta);
		
		inv.setItem(0, item.getItem());
		inv.setItem(1, item.getConfirmItemStack());
		inv.setItem(2, gPane);
		inv.setItem(3, Tools.createItem(Material.BANNER, 1, 10, "§aConfirm"));
		inv.setItem(4, Tools.createItem(Material.BANNER, 1, 1, "§cCancel"));
		
		p.openInventory(inv);
	}
	
	public static void changeInv(Inventory oldInv, Inventory newInv){
		int i = 0;
		for(ItemStack it : newInv.getContents()){
			oldInv.setItem(i, it);
			i++;
		}
	}
	
	public static Inventory getRoomSelectionMenu(int page, String owner, Player p){
		List<AuctionRoom> aRooms;
		if(owner.equals("all")){
			aRooms = AuctionRooms.getAuctionRooms();
		}else{
			aRooms = AuctionRooms.getAuctionRoomsByOwner(owner);
		}
		int pages = aRooms.size()/9/5;
		if(pages >= page && page >= 0){
			Inventory inv = Bukkit.createInventory(null, 6*9, Config.simpleReplace(Config.Config.getString("minebay.prefix")));
			int start = page*5*9;
			int end = (aRooms.size()<=start+5*9)?aRooms.size():start+5*9;
			for(int i = start; i < end; i++){
				AuctionRoom it = aRooms.get(i);
				inv.setItem(i-start, it.getSelectItemStack(p));
			}
			ItemStack gPane = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0");
			for(int i = 5*9; i < 6*9-4; i++){
				inv.setItem(i, gPane);
			}
			ItemStack createRoom = Tools.createItem(Material.STAINED_CLAY, 1, 5, "§aCreate new room");
			ItemStack aLeft = Tools.createItem(Tools.arrowLeft(), "§7Previous page");
			ItemStack aRight = Tools.createItem(Tools.arrowRight(), "§7Next page");
			ItemStack gPane2 = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§8Auction Rooms", "§8Page: §7"+page, "§8Owner: §7"+owner);
			inv.setItem(46, gPane2);
			inv.setItem(49, createRoom);
			inv.setItem(50, Tools.createItem(Material.BANNER, 1, 10, "§7All Rooms"));
			inv.setItem(51, Tools.createItem(Material.BANNER, 1, 14, "§7Your Rooms"));
			inv.setItem(52, aLeft);
			inv.setItem(53, aRight);
			return inv;
		}else{
			return null;
		}
	}
	
	public static Inventory getSellRoomSelectionMenu(int page, String owner, int price){
		List<AuctionRoom> aRooms;
		if(owner.equals("all")){
			aRooms = AuctionRooms.getAuctionRooms();
		}else{
			aRooms = AuctionRooms.getAuctionRoomsByOwner(owner);
		}
		int pages = aRooms.size()/9/5;
		if(pages >= page && page >= 0){
			Inventory inv = Bukkit.createInventory(null, 6*9, Config.simpleReplace(Config.Config.getString("minebay.prefix")));
			int start = page*5*9;
			int end = (aRooms.size()<=start+5*9)?aRooms.size():start+5*9;
			for(int i = start; i < end; i++){
				AuctionRoom it = aRooms.get(i);
				inv.setItem(i-start, it.getSelectItemStack(null));
			}
			ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
			ItemMeta gMeta = gPane.getItemMeta();
			gMeta.setDisplayName("§0");
			gPane.setItemMeta(gMeta);
			for(int i = 5*9; i < 6*9-4; i++){
				inv.setItem(i, gPane);
			}
			ItemStack gPane1 = Tools.createItem(Tools.arrowLeft(), "§7Previous page");
			ItemStack gPane2 = Tools.createItem(Tools.arrowRight(), "§7Next page");
			ItemStack gPane3 = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§8Sell Item", "§8Page: §7"+page, "§8Owner: §7"+owner, "§8Price: §7"+price);
			inv.setItem(46, gPane3);
			inv.setItem(50, Tools.createItem(Material.BANNER, 1, 10, "§7All Rooms"));
			inv.setItem(51, Tools.createItem(Material.BANNER, 1, 14, "§7Your Rooms"));
			inv.setItem(52, gPane1);
			inv.setItem(53, gPane2);
			return inv;
		}else{
			return null;
		}
	}
	
	public static void updateRoomSelection(){
		for(Player pl : Bukkit.getOnlinePlayers()){
			String t = getInvType(pl);
			if(t.equals("auction rooms") || t.equals("sell item")){
				Inventory mbInv = pl.getOpenInventory().getTopInventory();
				String mode = mbInv.getItem(46).getItemMeta().getDisplayName();
				if(mode.equals("§8Auction Rooms")){
					int page = Integer.parseInt(Config.onlyDigitsNoColor(mbInv.getItem(46).getItemMeta().getLore().get(0)));
					String search = mbInv.getItem(46).getItemMeta().getLore().get(1).replace("§8Owner: §7", "");
					MineBay.changeInv(mbInv, getRoomSelectionMenu(page, search, pl));
				}else if(mode.equals("§8Sell Item")){
					int page = Integer.parseInt(Config.onlyDigitsNoColor(mbInv.getItem(46).getItemMeta().getLore().get(0)));
					String search = mbInv.getItem(46).getItemMeta().getLore().get(1).replace("§8Owner: §7", "");
					int price = Integer.parseInt(Config.onlyDigitsNoColor(mbInv.getItem(46).getItemMeta().getLore().get(2)));
					MineBay.changeInv(mbInv, getSellRoomSelectionMenu(page, search, price));
				}
			}
		}
	}
	
	public static String getInvType(Inventory inv){
		if(inv.getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix")))){
			try{
				if(inv.getSize() == 9*6){
					String mode = inv.getItem(46).getItemMeta().getDisplayName();
					if(mode.equals("§8Auction Room")){
						return "auction room";
					}else if(mode.equals("§8Auction Rooms")){
						return "auction rooms";
					}else if(mode.equals("§8Settings")){
						return "settings";
					}else if(mode.equals("§8Sell Item")){
						return "sell item";
					}else{
						return "other";
					}
				}else if(inv.getSize() == 3*9){
					String mode = inv.getItem(18).getItemMeta().getDisplayName();
					if(mode.equals("§8Change Block")){
						return "change block";
					}else{
						return "other";
					}
				}else{
					return "other";
				}
			}catch(Exception e){
				e.printStackTrace();
				return "other";
			}
		}else if(inv.getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §8Confirm")){
			return "purchase confirm";
		}else{
			return "other";
		}
	}
	
	public static String getInvType(Player p){
		if(p.getOpenInventory()!=null && p.getOpenInventory().getTopInventory()!=null){
			return getInvType(p.getOpenInventory().getTopInventory());
		}else{
			return "none";
		}
	}
	
	public static boolean hasPermissionToCreateRoom(Player p){
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
			return true;
		}
		return false;
	}
	
	public static boolean hasPermissionForColoredNames(Player p){
		if(p.hasPermission("minebay.user-rooms.use-colored-names")){
			return true;
		}else{
			for(String perm : Config.Config.getStringList("room-perms")){
				if(p.hasPermission(perm)){
					if(Config.Config.getBoolean("room-perm."+perm+".allow-colored-names")){
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
			for(String perm : Config.Config.getStringList("room-perms")){
				if(p.hasPermission(perm)){
					if(Config.Config.getBoolean("room-perm."+perm+".allow-colored-descriptions")){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static Inventory getConfirmGUI(ItemStack baseItem){
		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Config.simpleReplace(Config.Config.getString("minebay.prefix"))+" §7Confirm");
		
		ItemStack gPane = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0");
		ItemStack confirm = Tools.createBanner("§aConfirm", DyeColor.GREEN);
		ItemStack cancel = Tools.createBanner("§cCancel", DyeColor.RED);
		
		inv.setItem(0, baseItem);
		inv.setItem(1, gPane);
		inv.setItem(2, gPane);
		inv.setItem(3, confirm);
		inv.setItem(4, cancel);
		
		return inv;
	}
	
}
