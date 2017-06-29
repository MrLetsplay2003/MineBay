package me.mrletsplay.minebay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AuctionRooms {
	
	private static File configFile = new File("plugins/MineBay/AuctionRooms", "AuctionRooms.yml");
	public static FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	
	public static void save(){
		try{
			config.save(configFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static AuctionRoom createAuctionRoom(String player, int id){
		AuctionRoom nRoom = getNewAuctionRoom(player, id);
		nRoom.saveAllSettings();
		List<Integer> rIDs = getAuctionRoomIDs();
		if(!rIDs.contains(nRoom.getRoomID())){
			rIDs.add(nRoom.getRoomID());
		}
		config.set("auction-room-ids", rIDs);
		save();
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
		save();
	}
	
	public static AuctionRoom getNewAuctionRoom(String owner, int id){
		AuctionRoom r = new AuctionRoom(id);
		r.setDefaultSettings(owner);
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
			if(r.getOwner()!=null && r.getOwner().equals(owner)){
				aRooms.add(r);
			}
		}
		return aRooms;
	}
	
	public static List<Integer> getAuctionRoomIDs(){
		return config.getIntegerList("auction-room-ids");
	}
	
	public static AuctionRoom getAuctionRoomByID(int id){
		return new AuctionRoom(id);
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
