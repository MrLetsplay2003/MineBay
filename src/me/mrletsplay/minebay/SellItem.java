package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import me.mrletsplay.mrcore.bukkitimpl.versioned.VersionedDyeColor;

public class SellItem{

	private String seller;
	private ItemStack item;
	private BigDecimal price;
	private int id;
	private AuctionRoom ar;
	
	public SellItem(ItemStack item, AuctionRoom sellRoom, String seller, BigDecimal price, int id) {
		this.seller = seller;
		this.item = item;
		this.price = price;
		this.id = id;
		this.ar = sellRoom;
	}
	
	public String getSeller() {
		return seller;
	}
	
	@SuppressWarnings("deprecation")
	public OfflinePlayer getSellerPlayer() {
		return Config.useUUIDs ? Bukkit.getOfflinePlayer(UUID.fromString(seller)) : Bukkit.getOfflinePlayer(seller);
	}
	
	public boolean isSeller(Player p) {
		if(Config.useUUIDs) {
			return p.getUniqueId().toString().equals(seller);
		}else {
			return p.getName().equals(seller);
		}
	}
	
	public String getSellerName() {
		if(Config.useUUIDs) {
			return Bukkit.getOfflinePlayer(UUID.fromString(seller)).getName();
		}else {
			return seller;
		}
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public BigDecimal getPrice() {
		return price;
	}
	
	public AuctionRoom getRoom() {
		return ar;
	}
	
	public int getID() {
		return id;
	}
	
	public ItemStack getSellItemStack(Player p){
		return ItemUtils.createItem(item, item.getItemMeta().getDisplayName(), Config.getMessageList("minebay.gui.room.sold-item.lore", 
					"price", price+" "+Main.econ.getCurrencyNamePlural(),
					"seller-name", getSellerName(),
					"item-id", ""+id,
					"retract-sale", (isSeller(p)?Config.getMessage("minebay.gui.room.sold-item.retract-sale"):""),
					"remove-sale", (getRoom().canEdit(p) && !isSeller(p)?Config.getMessage("minebay.gui.room.sold-item.remove-sale"):"")
				));
	}
	
	public ItemStack getConfirmItemStack(){
		return ItemUtils.createItem(ItemUtils.createBanner("", VersionedDyeColor.YELLOW), Config.getMessage("minebay.gui.confirm.buy-item.info.name"), Config.getMessageList("minebay.gui.confirm.buy-item.info.lore", 
					"price", price+" "+Main.econ.getCurrencyNamePlural(),
					"seller", getSellerName(),
					"item-id", ""+id,
					"room-id", ""+ar.getID()));
	}
	
}
