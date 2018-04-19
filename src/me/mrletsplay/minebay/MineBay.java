package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.mrletsplay.mrcore.bukkitimpl.GUIUtils;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUI;

public class MineBay {
	
	public static AuctionRoom getMainAuctionRoom(){
		return AuctionRooms.getAuctionRoomByID(0);
	}
	
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
	
}
