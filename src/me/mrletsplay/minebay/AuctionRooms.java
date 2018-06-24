package me.mrletsplay.minebay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import me.mrletsplay.mrcore.bukkitimpl.BukkitCustomConfig;

public class AuctionRooms {
	
	private static File configFile = new File("plugins/MineBay/AuctionRooms", "AuctionRooms.yml");
	public static BukkitCustomConfig config = (BukkitCustomConfig) new BukkitCustomConfig(configFile).loadConfigSafely();;
	
	public static AuctionRoom createAuctionRoom(Player player, int id, boolean isDefaultRoom){
		AuctionRoom nRoom = getNewAuctionRoom(player, id, isDefaultRoom);
		nRoom.saveAllSettings();
		List<Integer> rIDs = getAuctionRoomIDs();
		if(!rIDs.contains(nRoom.getRoomID())){
			rIDs.add(nRoom.getRoomID());
		}
		config.set("auction-room-ids", rIDs);
		config.saveConfigSafely();
		return nRoom;
	}
	
	public static void deleteAuctionRoom(int id){
		List<Integer> rIDs = getAuctionRoomIDs();
		rIDs.remove((Integer)id);
		config.set("auction-room-ids", rIDs);
		File roomFile = new File("plugins/MineBay/AuctionRooms", id+".yml");
		if(roomFile.exists()){
			roomFile.delete();
		}
		config.saveConfigSafely();
	}
	
	public static AuctionRoom getNewAuctionRoom(Player owner, int id, boolean isDefaultRoom){
		AuctionRoom r = new AuctionRoom(id, true);
		r.setDefaultSettings(owner!=null?(Config.use_uuids?owner.getUniqueId().toString():owner.getName()):null, isDefaultRoom);
		return r;
	}
	
	public static List<AuctionRoom> getAuctionRooms(){
		List<Integer> aRoomIDs = getAuctionRoomIDs();
		List<AuctionRoom> aRooms = new ArrayList<>();
		for(int id : aRoomIDs){
			aRooms.add(getAuctionRoomByID(id));
		}
		return aRooms;
	}
	
	public static List<AuctionRoom> getAuctionRoomsByOwner(String owner){
		List<Integer> aRoomIDs = getAuctionRoomIDs();
		List<AuctionRoom> aRooms = new ArrayList<>();
		for(int id : aRoomIDs){
			AuctionRoom r = getAuctionRoomByID(id);
			if(r.getOwnerName()!=null && r.getOwnerName().equals(owner)){
				aRooms.add(r);
			}
		}
		return aRooms;
	}
	
	public static List<Integer> getAuctionRoomIDs(){
		return config.getIntegerList("auction-room-ids");
	}
	
	public static AuctionRoom getAuctionRoomByID(int id){
		return new AuctionRoom(id, false);
	}
	
	public static int getNewRoomID(){
		int id = 0;
		List<Integer> ids = getAuctionRoomIDs();
		while(ids.contains(id)){
			id++;
		}
		return id;
	}
	
}
