package me.mrletsplay.minebay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.Files;

public class AuctionRoom {

	private String owner;
	private int taxshare;
	private int slots;
	private int roomID;
	private String name, description;
	private ItemStack icon;
	private boolean isDefaultRoom;
	
	private File roomFile;
	private FileConfiguration roomConfig;
	
	@SuppressWarnings("deprecation")
	public AuctionRoom(int id) {
		roomFile = new File("plugins/MineBay/AuctionRooms", id+".yml");
		roomConfig = YamlConfiguration.loadConfiguration(roomFile);
		this.owner = roomConfig.getString("owner");
		boolean s = false;
		if(this.owner!=null) {
			if(Tools.isUUID(this.owner)) {
				if(!Config.use_uuids) {
					Main.pl.getLogger().info("Converting room "+id+"'s owner uuid to name...");
					this.owner = Bukkit.getPlayer(UUID.fromString(owner)).getName();
					s = true;
				}
			}else if(Config.use_uuids) {
				Main.pl.getLogger().info("Converting room "+id+"'s owner name to uuid...");
				owner = Bukkit.getOfflinePlayer(owner).getUniqueId().toString();
				s = true;
			}
		}
		this.taxshare = roomConfig.getInt("tax-share");
		this.slots = roomConfig.getInt("slots");
		this.name = roomConfig.getString("name");
		this.description = roomConfig.getString("description");
		this.icon = roomConfig.getItemStack("icon");
		this.isDefaultRoom = roomConfig.getBoolean("default-room");
		this.roomID = id;
		if(s) saveAllSettings();
	}
	
	public void setDefaultSettings(String owner, boolean isDefaultRoom){
		this.owner = owner;
		this.taxshare = Config.Config.getInt("minebay.user-rooms.default-tax-percent");
		this.slots = Config.Config.getInt("minebay.user-rooms.default-slot-number");
		this.icon = new ItemStack(Material.getMaterial(Config.Config.getString("minebay.user-rooms.default-icon-material")));
		this.isDefaultRoom = isDefaultRoom;
		if(owner!=null){
			this.name = getOwnerName()+"'s Auction Room";
		}else{
			this.name = "Default Auction Room";
		}
		this.description=null;
		saveAllSettings();
	}
	
	public void saveAllSettings(){
		roomConfig.set("owner", owner);
		roomConfig.set("tax-share", taxshare);
		roomConfig.set("slots", slots);
		roomConfig.set("name", name);
		roomConfig.set("description", description);
		roomConfig.set("icon", icon);
		roomConfig.set("default-room", isDefaultRoom);
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
	
	public boolean isOwner(Player p) {
		if(Config.use_uuids) {
			return p.getUniqueId().toString().equals(owner);
		}else {
			return p.getName().equals(owner);
		}
	}
	
	public boolean canEdit(Player p) {
		return (p!=null && isOwner(p)) || (p!=null && isDefaultRoom && p.hasPermission("minebay.default-rooms.allow-edit")) || (p!=null && !isDefaultRoom && p.hasPermission("minebay.user-rooms.allow-edit"));
	}
	
	public String getOwnerName() {
		if(owner==null) return null;
		if(!Config.use_uuids) {
			return owner;
		}else {
			return Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName();
		}
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
	
	public String getName() {
		return name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setSlots(int slots) {
		this.slots = slots;
	}
	
	public void setTaxshare(int taxshare) {
		this.taxshare = taxshare;
	}
	
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}
	
	public ItemStack getIcon() {
		return icon;
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
	
	public List<SellItem> getSoldItemsBySeller(Player seller){
		List<SellItem> it = new ArrayList<>();
		for(SellItem i : getSoldItems()){
			if(i.isSeller(seller)){
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
			return new SellItem(item, AuctionRooms.getAuctionRoomByID(roomID), seller, price, id);
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
			ItemStack back = Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), "§6Back");
			inv.setItem(45, back);
			inv.setItem(46, gPane3);
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
		
		List<String> l1 = new ArrayList<>();
		l1.add("§8Currently: §7"+name);
		l1.add("");
		l1.add("§7Description");
		if(description!=null) {
			for(String s : WordUtils.wrap("§8Currently: §7"+description, 50).split(System.getProperty("line.separator"))) {
				l1.add("§7"+s);
			}
		}else {
			l1.add("§8Currently: §7none");
		}
		
		inv.setItem(10, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Name", l1.toArray(new String[l1.size()])));
		inv.setItem(14, Tools.createItem(Material.STAINED_CLAY, 1, 4, "§7Change Name"));
		inv.setItem(15, Tools.createItem(Material.STAINED_CLAY, 1, 4, "§7Change Description"));
		
		inv.setItem(19, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Block", "§8Currently: §7"+icon.getType().toString().toLowerCase().replace("_", " ")));
		inv.setItem(23, Tools.createItem(Material.STAINED_CLAY, 1, 4, "§7Change Block"));
		
		inv.setItem(28, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Slots", "§8Currently: §7"+slots));
		inv.setItem(32, Tools.createItem(Tools.arrowLeft(), "§7Buy slot/s", "§8Left click to buy 1 slot", "§8Shift-left click to buy 5 slots"));
		inv.setItem(33, Tools.createItem(Tools.arrowRight(), "§7Sell slot/s", "§8Left click to sell 1 slot", "§8Shift-left click to sell 5 slots"));

		inv.setItem(37, Tools.createItem(Material.NAME_TAG, 1, 0, "§7Tax", "§8Currently: §7"+taxshare+"%"));
		inv.setItem(41, Tools.createItem(Tools.arrowLeft(), "§7Increase Tax", "§8Left click to increase tax by 1%", "§8Shift left-click to increase tax by 10%"));
		inv.setItem(42, Tools.createItem(Tools.arrowRight(), "§7Decrease Tax", "§8Left click to decrease tax by 1%", "§8Shift left-click to decrease tax by 10%"));
		
		ItemStack back = Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), "§6Back");
		inv.setItem(45, back);
		inv.setItem(46, gPane3);
		
		inv.setItem(53, Tools.createItem(Material.STAINED_CLAY, 1, 14, "§cDelete Room"));
		return inv;
	}
	
	public static int getMineBayPage(Inventory inv){
		try{
			int page = Integer.parseInt(Config.onlyDigitsNoColor(inv.getItem(46).getItemMeta().getLore().get(0)));
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
		if(icon==null) return null;
		ItemStack newItem = icon.clone();
		ItemMeta im = newItem.getItemMeta();
		im.setDisplayName("§7"+name);
		List<String> lore = new ArrayList<>();
		if(owner!=null){
			lore.add("§8Owner: §7"+getOwnerName());
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
		if(description!=null) {
			for(String s : WordUtils.wrap("§8Description: §7"+description, 50).split(System.lineSeparator())) {
				lore.add("§7"+s);
			}
		}
		if(canEdit(p)){
			lore.add("§7Right-click for settings");
		}
		im.setLore(lore);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
		newItem.setItemMeta(im);
		return newItem;
	}
	
	public Inventory getBlockSelectionInv(){
		Inventory inv = Bukkit.createInventory(null, 6*9, Config.simpleReplace(me.mrletsplay.minebay.Config.Config.getString("minebay.prefix")));
		ItemStack gPane3 = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta gMeta3 = gPane3.getItemMeta();
		gMeta3.setDisplayName("§8Change Block");
		List<String> l = new ArrayList<>();
		l.add("§8Room ID: §7"+roomID);
		gMeta3.setLore(l);
		gPane3.setItemMeta(gMeta3);
		
		ItemStack it = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0");
		for(int i = 5*9; i < inv.getSize(); i++){
			inv.setItem(i, it);
		}
		
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
		inv.setItem(14, Tools.createItem(Material.DIAMOND, 1, 0, "§7Item | Diamond"));
		inv.setItem(15, Tools.createItem(Material.EMERALD, 1, 0, "§7Item | Emerald"));
		inv.setItem(16, Tools.createItem(Material.GOLD_INGOT, 1, 0, "§7Item | Gold Ingot"));
		inv.setItem(17, Tools.createItem(Material.IRON_INGOT, 1, 0, "§7Item | Iron Ingot"));
		inv.setItem(18, Tools.createItem(Material.REDSTONE, 1, 0, "§7Item | Redstone"));
		inv.setItem(19, Tools.createItem(Material.COAL, 1, 0, "§7Item | Coal"));
		
		ItemStack back = Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), "§6Back");
		inv.setItem(45, back);
		inv.setItem(46, gPane3);
		
		inv.setItem(6*9-1, Tools.createItem(Tools.letterC(DyeColor.ORANGE), "§6Custom block/item", "§8Price: §7"+Config.Config.getInt("minebay.user-rooms.custom-icon-price")));
		
		return inv;
	}
	
	public Inventory getIconChangeMenu(){
		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Config.simpleReplace(me.mrletsplay.minebay.Config.Config.getString("minebay.prefix")+" §8Custom icon"));
		
		ItemStack it = Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0");
		for(int i = 0; i < inv.getSize(); i++){
			inv.setItem(i, it);
		}
		
		ItemStack back = Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), "§6Back");
		inv.setItem(0, back);
		inv.setItem(1, Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§8Custom icon", "§8Room ID: §7"+roomID));
		inv.setItem(2, Tools.createItem(Material.STAINED_GLASS_PANE, 1, 7, "§8Drop item here"));
		
		return inv;
	}
	
	public int getWorth(){
		if(isDefaultRoom){
			return 0;
		}else{
			int sl = (slots - Config.Config.getInt("minebay.user-rooms.default-slot-number"))*Config.Config.getInt("minebay.user-rooms.slot-sell-price");
			int pr = Config.Config.getInt("minebay.user-rooms.room-sell-price");
			return sl+pr;
		}
	}
	
	public boolean isDefaultRoom() {
		return isDefaultRoom;
	}
	
	public boolean backupConfig(File to) {
		try {
			if(!roomFile.exists()) return false;
			if(!to.exists()) {
				to.getParentFile().mkdirs();
				to.createNewFile();
			}
			Files.copy(roomFile, to);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
