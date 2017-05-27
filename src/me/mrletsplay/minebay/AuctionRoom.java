package me.mrletsplay.minebay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AuctionRoom {

	private String owner;
	private int taxshare;
	private int slots;
	private int roomID;
	private String name;
	private Material iconMaterial;
	
	private File roomFile;
	private FileConfiguration roomConfig;
	
	public AuctionRoom(int id) {
		roomFile = new File("plugins/MineBay/AuctionRooms", id+".yml");
		roomConfig = YamlConfiguration.loadConfiguration(roomFile);
		String owner = roomConfig.getString("owner");
		int taxshare = roomConfig.getInt("tax-share");
		int slots = roomConfig.getInt("slots");
		String name = roomConfig.getString("name");
		Material m = Material.getMaterial(roomConfig.getString("icon-material"));
		this.owner = owner;
		this.roomID = id;
		this.taxshare = taxshare;
		this.slots = slots;
		this.name = name;
		this.iconMaterial = m;
	}
	
	public void setDefaultSettings(String owner){
		this.owner = owner;
		this.taxshare = Config.Config.getInt("minebay.user-rooms.default-tax-percent");
		this.slots = Config.Config.getInt("minebay.user-rooms.default-slot-number");
		this.iconMaterial = Material.getMaterial(Config.Config.getString("minebay.user-rooms.default-icon-material"));
		if(owner!=null){
			this.name = owner+"'s Auction Room";
		}else{
			this.name = "Default Auction Room";
		}
		saveAllSettings();
	}
	
	public void saveAllSettings(){
		roomConfig.set("owner", owner);
		roomConfig.set("tax-share", taxshare);
		roomConfig.set("slots", slots);
		roomConfig.set("name", name);
		roomConfig.set("icon-material", iconMaterial.name());
		saveRoomConfig();
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void saveRoomConfig(){
		try {
			roomConfig.save(roomFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getOwner() {
		return owner;
	}
	
	public int getTaxshare() {
		return taxshare;
	}
	
	public int getSlots() {
		return slots;
	}
	
	public int getRoomID() {
		return roomID;
	}
	
	public Material getIconMaterial() {
		return iconMaterial;
	}
	
	public String getName() {
		return name;
	}
	
	public void setSlots(int slots) {
		this.slots = slots;
	}
	
	public void setTaxshare(int taxshare) {
		this.taxshare = taxshare;
	}
	
	public void setIconMaterial(Material iconMaterial) {
		this.iconMaterial = iconMaterial;
	}
	
	public void addSellItem(SellItem item){
		List<Integer> ids = getItemIDs();
		int iID = getNewItemID();
		ids.add(iID);
		roomConfig.set("sold-items.ids", ids);
		roomConfig.set("sold-items.item."+iID+".item", item.getItem());
		roomConfig.set("sold-items.item."+iID+".seller", item.getSeller());
		roomConfig.set("sold-items.item."+iID+".price", item.getPrice());
		saveRoomConfig();
		updateMineBay();
	}
	
	public void removeSellItem(int id){
		List<Integer> ids = getItemIDs();
		if(ids.contains((Integer)id)){
			ids.remove((Integer)id);
			roomConfig.set("sold-items.ids", ids);
			roomConfig.set("sold-items.item."+id, null);
			saveRoomConfig();
		}
		updateMineBay();
	}
	
	public List<SellItem> getSoldItems(){
		List<Integer> ids = getItemIDs();
		List<SellItem> items = new ArrayList<>();
		for(int id : ids){
			items.add(getItemByID(id));
		}
		return items;
	}
	
	public int getNewItemID(){
		int id = 0;
		List<Integer> ids = getItemIDs();
		while(ids.contains(id)){
			id++;
		}
		return id;
	}
	
	public List<Integer> getItemIDs(){
		return roomConfig.getIntegerList("sold-items.ids");
	}
	
	public int getOccupiedSlots(){
		List<String> sellers = new ArrayList<>();
		for(SellItem i : getSoldItems()){
			if(!sellers.contains(i.getSeller())){
				sellers.add(i.getSeller());
			}
		}
		return sellers.size();
	}
	
	public List<SellItem> getSoldItemsBySeller(String seller){
		List<SellItem> it = new ArrayList<>();
		for(SellItem i : getSoldItems()){
			if(i.getSeller().equals(seller)){
				it.add(i);
			}
		}
		return it;
	}
	
	public SellItem getItemByID(int id){
		if(getItemIDs().contains(id)){
			ItemStack item = roomConfig.getItemStack("sold-items.item."+id+".item");
			String seller = roomConfig.getString("sold-items.item."+id+".seller");
			int price = roomConfig.getInt("sold-items.item."+id+".price");
			return new SellItem(item, AuctionRooms.getAuctionRoomByID(roomID), seller,price, id);
		}else{
			return null;
		}
	}
	
	public Inventory getMineBayInv(int page, Player p){
		List<SellItem> sItems = getSoldItems();
		int pages = sItems.size()/9/5;
		if(pages >= page && page >= 0){
			Inventory inv = Bukkit.createInventory(null, 6*9, Config.simpleReplace(me.mrletsplay.minebay.Config.Config.getString("minebay.prefix")));
			int start = page*5*9;
			int end = (sItems.size()<=start+5*9)?sItems.size():start+5*9;
			for(int i = start; i < end; i++){
				SellItem it = sItems.get(i);
				inv.setItem(i-start, it.getSellItemStack(p));
			}
			ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
			ItemMeta gMeta = gPane.getItemMeta();
			gMeta.setDisplayName("§0");
			gPane.setItemMeta(gMeta);
			for(int i = 5*9+1; i < 6*9-2; i++){
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
			gMeta3.setDisplayName("§8Auction Room");
			List<String> l = new ArrayList<>();
			l.add("§7Page: "+page);
			l.add("§7Room ID: "+roomID);
			gMeta3.setLore(l);
			gPane3.setItemMeta(gMeta3);
			inv.setItem(45, gPane3);
			inv.setItem(52, gPane1);
			inv.setItem(53, gPane2);
			return inv;
		}else{
			return null;
		}
	}
	
	public Inventory getSettingsMenu(){
		Inventory inv = Bukkit.createInventory(null, 6*9, Config.simpleReplace(me.mrletsplay.minebay.Config.Config.getString("minebay.prefix")));
		ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta gMeta = gPane.getItemMeta();
		gMeta.setDisplayName("§0");
		gPane.setItemMeta(gMeta);
		for(int i = 0; i < inv.getSize(); i++){
			inv.setItem(i, gPane);
		}
		ItemStack gPane3 = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta gMeta3 = gPane3.getItemMeta();
		gMeta3.setDisplayName("§8Settings");
		List<String> l = new ArrayList<>();
		l.add("§8Room ID: §7"+roomID);
		gMeta3.setLore(l);
		gPane3.setItemMeta(gMeta3);
		
		inv.setItem(10, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Name", "§8Current: §7"+name));
		inv.setItem(14, Tools.createItem(Material.STAINED_CLAY, 1, 4, "§7Change Name"));
		
		inv.setItem(19, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Block", "§8Current: §7"+iconMaterial.toString().toLowerCase().replace("_", " ")));
		inv.setItem(23, Tools.createItem(Material.STAINED_CLAY, 1, 4, "§7Change Block"));
		
		inv.setItem(28, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Slots", "§8Current: §7"+slots));
		inv.setItem(32, Tools.createItem(Tools.arrowLeft(), "§7Buy 1 slot"));
		inv.setItem(33, Tools.createItem(Tools.arrowRight(), "§7Sell 1 slot"));

		inv.setItem(37, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Tax", "§8Current: §7"+taxshare+"%"));
		inv.setItem(41, Tools.createItem(Tools.arrowLeft(), "§7Increase Tax"));
		inv.setItem(42, Tools.createItem(Tools.arrowRight(), "§7Decrease Tax"));
		
		inv.setItem(45, gPane3);
		
		inv.setItem(53, Tools.createItem(Material.STAINED_CLAY, 1, 14, "§cDelete Room"));
		return inv;
	}
	
	public static int getMineBayPage(Inventory inv){
		try{
			int page = Integer.parseInt(Config.onlyDigitsNoColor(inv.getItem(45).getItemMeta().getLore().get(0)));
			return page;
		}catch(Exception e){
			return -1;
		}
	}
	
	public void updateMineBay(){
		try{
			for(Player pl : Bukkit.getOnlinePlayers()){
				String t = MineBay.getInvType(pl);
				if(t.equals("auction room")){
					Inventory mbInv = pl.getOpenInventory().getTopInventory();
					int page = getMineBayPage(mbInv);
					if(page!=-1){
						MineBay.changeInv(mbInv, getMineBayInv(page, pl));
					}
				}
			}
		}catch(ConcurrentModificationException e){
			Bukkit.getScheduler().runTaskLater(Main.pl, new Runnable() {
				
				@Override
				public void run() {
					updateMineBay();
				}
			}, 1);
		}
	}
	
	public void updateSettings(){
		try{
			for(Player pl : Bukkit.getOnlinePlayers()){
				String t = MineBay.getInvType(pl);
				if(t.equals("settings")){
					Inventory mbInv = pl.getOpenInventory().getTopInventory();
					MineBay.changeInv(mbInv, getSettingsMenu());
				}
			}
		}catch(ConcurrentModificationException e){
			Bukkit.getScheduler().runTaskLater(Main.pl, new Runnable() {
				
				@Override
				public void run() {
					updateSettings();
				}
			}, 1);
		}
	}
	
	public ItemStack getSelectItemStack(Player p){
		ItemStack newItem = new ItemStack(iconMaterial);
		ItemMeta im = newItem.getItemMeta();
		im.setDisplayName("§7"+name);
		List<String> lore = new ArrayList<>();
		if(owner!=null){
			lore.add("§8Owner: §7"+owner);
		}else{
			lore.add("§8Owner: §7None");
		}
		if(slots==-1){
			lore.add("§8Slots: §7"+getOccupiedSlots()+"/unlimited");
		}else{
			lore.add("§8Slots: §7"+getOccupiedSlots()+"/"+slots);
		}
		lore.add("§8Tax: §7"+taxshare+"%");
		lore.add("§8ID: §7"+roomID);
		if(p!=null && p.getName().equals(owner)){
			lore.add("§7Right-click for settings");
		}
		im.setLore(lore);
		newItem.setItemMeta(im);
		return newItem;
	}
	
	public Inventory getBlockSelectionInv(){
		Inventory inv = Bukkit.createInventory(null, 3*9, Config.simpleReplace(me.mrletsplay.minebay.Config.Config.getString("minebay.prefix")));
		ItemStack gPane3 = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta gMeta3 = gPane3.getItemMeta();
		gMeta3.setDisplayName("§8Change Block");
		List<String> l = new ArrayList<>();
		l.add("§8Room ID: §7"+roomID);
		gMeta3.setLore(l);
		gPane3.setItemMeta(gMeta3);
		
		inv.setItem(0, Tools.createItem(Material.GRASS, 1, 0, "§7Block | Grass"));
		inv.setItem(1, Tools.createItem(Material.DIRT, 1, 0, "§7Block | Dirt"));
		inv.setItem(2, Tools.createItem(Material.STONE, 1, 0, "§7Block | Stone"));
		inv.setItem(3, Tools.createItem(Material.BEDROCK, 1, 0, "§7Block | Bedrock"));
		inv.setItem(4, Tools.createItem(Material.SPONGE, 1, 0, "§7Block | Sponge"));
		inv.setItem(5, Tools.createItem(Material.DIAMOND_BLOCK, 1, 0, "§7Block | Diamond Block"));
		inv.setItem(6, Tools.createItem(Material.REDSTONE_BLOCK, 1, 0, "§7Block | Redstone Block"));
		inv.setItem(7, Tools.createItem(Material.IRON_BLOCK, 1, 0, "§7Block | Iron Block"));
		inv.setItem(8, Tools.createItem(Material.TNT, 1, 0, "§7Block | TNT"));
		inv.setItem(9, Tools.createItem(Material.EMERALD_BLOCK, 1, 0, "§7Block | Emerald Block"));
		inv.setItem(10, Tools.createItem(Material.SAND, 1, 0, "§7Block | Sand"));
		inv.setItem(11, Tools.createItem(Material.COBBLESTONE, 1, 0, "§7Block | Cobblestone"));
		inv.setItem(12, Tools.createItem(Material.OBSIDIAN, 1, 0, "§7Block | Obsidian"));
		inv.setItem(13, Tools.createItem(Material.LAPIS_BLOCK, 1, 0, "§7Block | Lapis Lazuli Block"));
		
		inv.setItem(18, gPane3);
		
		ItemStack it = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0");
		for(int i = 19; i < inv.getSize(); i++){
			inv.setItem(i, it);
		}
		
		return inv;
	}
	
}
