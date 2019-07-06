package me.mrletsplay.minebay.notifications;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.minebay.AuctionRoom;
import me.mrletsplay.minebay.AuctionRooms;
import me.mrletsplay.minebay.Config;
import me.mrletsplay.minebay.SellItem;
import me.mrletsplay.mrcore.bukkitimpl.config.BukkitConfigMappers;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;

public class CancelledOffer implements OfflineNotification {
	
	private AuctionRoom room;
	private ItemStack item;
	
	@JSONConstructor
	private CancelledOffer() {}
	
	public CancelledOffer(SellItem item) {
		this.room = item.getRoom();
		this.item = item.getItem();
	}
	
	public AuctionRoom getRoom() {
		return room;
	}
	
	public ItemStack getItem() {
		return item;
	}

	@Override
	public String getMessage() {
		return Config.getMessage("minebay.info.notification.offer-cancelled", "room", room.getName());
	}

	@Override
	public void onSent(Player p) {
		HashMap<Integer,ItemStack> excess = p.getInventory().addItem(item);
		for(Map.Entry<Integer, ItemStack> me : excess.entrySet()){
			p.getWorld().dropItem(p.getLocation(), me.getValue());
		}
	}
	
	@Override
	public void preSerialize(JSONObject object) {
		object.put("room", room.getID());
		object.put("item", BukkitConfigMappers.ITEM_MAPPER.mapObject(null, item));
	}
	
	@Override
	public void preDeserialize(JSONObject object) {
		room = AuctionRooms.getAuctionRoomByID(object.getInt("room"));
		item = BukkitConfigMappers.ITEM_MAPPER.constructObject(null, object.getJSONObject("item"));
	}

}
