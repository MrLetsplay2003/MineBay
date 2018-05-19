package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	
	public boolean isSeller(Player p) {
		if(Config.use_uuids) {
			return p.getUniqueId().toString().equals(seller);
		}else {
			return p.getName().equals(seller);
		}
	}
	
	public String getSellerName() {
		if(Config.use_uuids) {
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
		return Tools.createItem(item, item.getItemMeta().getDisplayName(), Config.getMessageList("minebay.gui.room.sold-item.lore", 
					"price", price+" "+Main.econ.getCurrencyNamePlural(),
					"seller-name", getSellerName(),
					"item-id", ""+id,
					"retract-sale", (isSeller(p)?Config.getMessage("minebay.gui.room.sold-item.retract-sale"):"")
				));
	}
	
	public ItemStack getConfirmItemStack(){
		return Tools.createItem(Tools.createBanner("", DyeColor.YELLOW), Config.getMessage("minebay.gui.confirm.buy-item.info.name"), Config.getMessageList("minebay.gui.confirm.buy-item.info.lore", 
					"price", price+" "+Main.econ.getCurrencyNamePlural(),
					"seller", getSellerName(),
					"item-id", ""+id,
					"room-id", ""+ar.getRoomID()));
	}
	
}
