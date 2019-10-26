package me.mrletsplay.minebay;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MineBay {
	
	public static AuctionRoom getMainAuctionRoom(){
		return AuctionRooms.getAuctionRoomByID(0);
	}
	
	public static void updateRoomSelection(){
		GUIs.AUCTION_ROOMS_GUI.refreshAllInstances();
	}
	
	public static Inventory getOpenInv(Player p) {
		if(p.getOpenInventory()!=null && p.getOpenInventory().getTopInventory()!=null){
			return p.getOpenInventory().getTopInventory();
		}else{
			return null;
		}
	}
	
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
		return rooms.size() < mRooms || mRooms == -1;
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
	
	public static boolean hasPermissionToCreateDefaultRoomSale(AuctionRoom rm, Player p){
		int mSales = Config.config.getInt("minebay.general.max-default-room-sales");
		for(String perm : Config.config.getStringList("room-perms")){
			if(p.hasPermission(perm)){
				int r = Config.config.getInt("room-perm."+perm+".max-default-room-sales");
				if(r > mSales) mSales = r;
			}
		}
		List<SellItem> its = rm.getSoldItemsBySeller(p);
		return its.size() < mSales;
	}
	
}
