package me.mrletsplay.minebay;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
		
		inv.setItem(0, item.getConfirmItemStack());
		inv.setItem(1, gPane);
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
			ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
			ItemMeta gMeta = gPane.getItemMeta();
			gMeta.setDisplayName("§0");
			gPane.setItemMeta(gMeta);
			for(int i = 5*9+1; i < 6*9-4; i++){
				inv.setItem(i, gPane);
			}
			ItemStack gPane1 = Tools.arrowLeft();
			ItemMeta gMeta1 = gPane1.getItemMeta();
			gMeta1.setDisplayName("§7Previous page");
			gPane1.setItemMeta(gMeta1);
			ItemStack gPane2 = Tools.arrowRight();
			ItemMeta gMeta2 = gPane2.getItemMeta();
			gMeta2.setDisplayName("§7Next page");
			gPane2.setItemMeta(gMeta2);
			ItemStack gPane3 = new ItemStack(Material.STAINED_GLASS_PANE);
			ItemMeta gMeta3 = gPane3.getItemMeta();
			gMeta3.setDisplayName("§8Auction Rooms");
			List<String> l = new ArrayList<>();
			l.add("§8Page: §7"+page);
			l.add("§8Owner: §7"+owner);
			gMeta3.setLore(l);
			gPane3.setItemMeta(gMeta3);
			inv.setItem(45, gPane3);
			inv.setItem(50, Tools.createItem(Material.BANNER, 1, 10, "§7All Rooms"));
			inv.setItem(51, Tools.createItem(Material.BANNER, 1, 14, "§7Your Rooms"));
			inv.setItem(52, gPane1);
			inv.setItem(53, gPane2);
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
			for(int i = 5*9+1; i < 6*9-4; i++){
				inv.setItem(i, gPane);
			}
			ItemStack gPane1 = Tools.arrowLeft();
			ItemMeta gMeta1 = gPane1.getItemMeta();
			gMeta1.setDisplayName("§7Previous page");
			gPane1.setItemMeta(gMeta1);
			ItemStack gPane2 = Tools.arrowRight();
			ItemMeta gMeta2 = gPane2.getItemMeta();
			gMeta2.setDisplayName("§7Next page");
			gPane2.setItemMeta(gMeta2);
			ItemStack gPane3 = new ItemStack(Material.STAINED_GLASS_PANE);
			ItemMeta gMeta3 = gPane3.getItemMeta();
			gMeta3.setDisplayName("§8Sell Item");
			List<String> l = new ArrayList<>();
			l.add("§8Page: §7"+page);
			l.add("§8Owner: §7"+owner);
			l.add("§8Price: §7"+price);
			gMeta3.setLore(l);
			gPane3.setItemMeta(gMeta3);
			inv.setItem(45, gPane3);
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
				String mode = mbInv.getItem(45).getItemMeta().getDisplayName();
				if(mode.equals("§8Auction Rooms")){
					int page = Integer.parseInt(Config.onlyDigitsNoColor(mbInv.getItem(45).getItemMeta().getLore().get(0)));
					String search = mbInv.getItem(45).getItemMeta().getLore().get(1).replace("§8Owner: §7", "");
					MineBay.changeInv(mbInv, getRoomSelectionMenu(page, search, pl));
				}else if(mode.equals("§8Sell Item")){
					int page = Integer.parseInt(Config.onlyDigitsNoColor(mbInv.getItem(45).getItemMeta().getLore().get(0)));
					String search = mbInv.getItem(45).getItemMeta().getLore().get(1).replace("§8Owner: §7", "");
					int price = Integer.parseInt(Config.onlyDigitsNoColor(mbInv.getItem(45).getItemMeta().getLore().get(2)));
					MineBay.changeInv(mbInv, getSellRoomSelectionMenu(page, search, price));
				}
			}
		}
	}
	
	public static String getInvType(Inventory inv){
		if(inv.getName().equals(Config.simpleReplace(Config.Config.getString("minebay.prefix")))){
			try{
				if(inv.getSize() == 9*6){
					String mode = inv.getItem(45).getItemMeta().getDisplayName();
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
	
}
