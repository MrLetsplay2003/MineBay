package me.mrletsplay.minebay;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class SellItem{

	private String seller;
	private ItemStack item;
	private int price;
	private int id;
	private AuctionRoom ar;
	
	public SellItem(ItemStack item, AuctionRoom sellRoom, String seller, int price, int id) {
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
	
	public int getPrice() {
		return price;
	}
	
	public int getID() {
		return id;
	}
	
	public ItemStack getSellItemStack(Player p){
		ItemStack newItem = new ItemStack(item);
		ItemMeta im = newItem.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add("§8Price: §7"+price+" "+Main.econ.currencyNamePlural());
		lore.add("§8Seller: §7"+getSellerName());
		lore.add("§8Product ID: §7"+id);
		if(isSeller(p)){
			lore.add("§7Click to retract sale");
		}
		im.setLore(lore);
		newItem.setItemMeta(im);
		return newItem;
	}
	
	public ItemStack getConfirmItemStack(){
		ItemStack newItem = new ItemStack(Material.BANNER);
		BannerMeta im = (BannerMeta) newItem.getItemMeta();
		im.setBaseColor(DyeColor.YELLOW);
		im.setDisplayName("§eInfo");
		List<String> lore = new ArrayList<>();
		lore.add("§8Price: §7"+price+" "+Main.econ.currencyNamePlural());
		lore.add("§8Seller: §7"+getSellerName());
		lore.add("§8Product ID: §7"+id);
		lore.add("§8Auction Room: §7"+ar.getRoomID());
		im.setLore(lore);
		newItem.setItemMeta(im);
		return newItem;
	}
	
}
