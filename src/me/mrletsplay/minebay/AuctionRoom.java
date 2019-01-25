package me.mrletsplay.minebay;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.Files;

import me.mrletsplay.mrcore.bukkitimpl.config.BukkitCustomConfig;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.misc.StringUtils;

public class AuctionRoom {

	private String owner;
	private int taxshare;
	private int slots;
	private int roomID;
	private String name, description;
	private ItemStack icon;
	private boolean isDefaultRoom;
//	private GUI blockSelectGUI;
	
	private File roomFile;
	private BukkitCustomConfig roomConfig;
	
	@SuppressWarnings("deprecation")
	public AuctionRoom(int id) {
		roomFile = new File("plugins/MineBay/AuctionRooms", id+".yml");
		roomConfig = ConfigLoader.loadConfigFromFile(new BukkitCustomConfig(roomFile), roomFile, true);
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
		this.taxshare = Config.config.getInt("minebay.user-rooms.default-tax-percent");
		this.slots = Config.config.getInt("minebay.user-rooms.default-slot-number");
		this.icon = new ItemStack(Material.getMaterial(Config.config.getString("minebay.user-rooms.default-icon-material")));
		this.isDefaultRoom = isDefaultRoom;
		if(owner!=null){
			this.name = getOwnerName()+"'s Auction Room";
		}else{
			this.name = "Default Auction Room";
		}
		this.description = null;
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
		roomConfig.saveToFile();
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
	
	public int getID() {
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
		roomConfig.set("sold-items.item."+iID+".price", item.getPrice().toString());
		saveRoomConfig();
		updateMineBay();
	}
	
	public void removeSellItem(int id){
		List<Integer> ids = getItemIDs();
		if(ids.contains((Integer)id)){
			ids.remove((Integer)id);
			roomConfig.set("sold-items.ids", ids);
			roomConfig.unset("sold-items.item."+id);
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
			BigDecimal price = new BigDecimal(roomConfig.getString("sold-items.item."+id+".price"));
			return new SellItem(item, AuctionRooms.getAuctionRoomByID(roomID), seller, price, id);
		}else{
			return null;
		}
	}
	
	public Inventory getMineBayInv(int page, Player p) {
		return GUIs.getAuctionRoomGUI(p, roomID, page);
	}
	
	public void updateMineBay(){
//		try{
//			for(Player pl : Bukkit.getOnlinePlayers()){
//				Inventory oI = MineBay.getOpenInv(pl);
//				if(oI == null) continue;
//				GUI gui = GUIUtils.getGUI(oI);
//				if(gui == null) continue;
//				GUIHolder holder = (GUIHolder) oI.getHolder();
//				String t = (String) holder.getProperty("minebay_type");
//				if(t == null) continue;
//				if(t.equals("auction room")){
//					if(((int) holder.getProperty("minebay_auctionroom_id")) == roomID) {
//						try {
//							int page = GUIMultiPage.getPage(oI);
//							if(page!=-1){
//								MineBay.changeInv(oI, getMineBayInv(0, pl));
//							}
//						}catch(Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		}catch(ConcurrentModificationException e){
//			Bukkit.getScheduler().runTaskLater(Main.pl, new Runnable() {
//				
//				@Override
//				public void run() {
//					updateMineBay();
//				}
//			}, 1);
//		}
		GUIs.AUCTION_ROOM_GUI.refreshAllInstances(holder -> (int) holder.getProperty(Main.pl, "room_id") == roomID);
	}
	
	public void updateSettings(){
//		try{
//			for(Player pl : Bukkit.getOnlinePlayers()){
//				Inventory oI = MineBay.getOpenInv(pl);
//				if(oI == null) continue;
//				GUI gui = GUIUtils.getGUI(oI);
//				if(gui == null) continue;
//				GUIHolder holder = (GUIHolder) oI.getHolder();
//				String t = (String) holder.getProperty("minebay_type");
//				if(t == null) continue;
//				if(t.equals("settings "+roomID)){
//					MineBay.changeInv(oI, roomSettingsGUI.getForPlayer(pl));
//				}
//			}
//		}catch(ConcurrentModificationException e){
//			Bukkit.getScheduler().runTaskLater(Main.pl, new Runnable() {
//				
//				@Override
//				public void run() {
//					updateSettings();
//				}
//			}, 1);
//		}
		GUIs.AUCTION_ROOM_SETTINGS_GUI.refreshAllInstances(holder -> (int) holder.getProperty(Main.pl, "room_id") == roomID);
	}
	
	public ItemStack getSelectItemStack(Player p){
		if(icon==null) return null;
		ItemStack newItem = icon.clone();
		ItemMeta im = newItem.getItemMeta();
		im.setDisplayName(Config.getMessage("minebay.gui.rooms.room-item.name", "room-name", name, "room-id", ""+roomID));
		List<String> lore = Config.getMessageList("minebay.gui.rooms.room-item.lore",
				"owner", owner!=null?getOwnerName():Config.getMessage("minebay.gui.misc.none"),
				"slots-limit", (slots==-1?Config.getMessage("minebay.gui.rooms.room-item.slots-unlimited"):""+slots),
				"slots-occupied", ""+getOccupiedSlots(),
				"tax", ""+taxshare,
				"room-id", ""+roomID,
				"can-edit", canEdit(p)?Config.getMessage("minebay.gui.rooms.room-item.can-edit"):"");
		List<String> fLore = new ArrayList<>();
		for(String s2 : lore) {
			if(!s2.contains("%description%")) {
				fLore.add(s2);
				continue;
			}
			if(description!=null) {
				String fS = s2.replace("%description%", description);
				StringUtils.wrapString(fS, 50).stream()
						.map(s -> Config.getMessage("minebay.gui.rooms.room-item.description-linebreak-color")+s)
						.forEach(fLore::add);
			}else {
				fLore.add(s2.replace("%description%", Config.getMessage("minebay.gui.misc.none")));
			}
		}
		im.setLore(fLore);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
		newItem.setItemMeta(im);
		return newItem;
	}
	
//	private GUI buildBlockSelectionGUI() {
//		GUIBuilder builder = new GUIBuilder(Config.prefix, 6);
//		GUIElementAction bAction = new GUIElementAction() {
//			
//			@Override
//			public void onAction(GUIElementActionEvent e) {
//				setIcon(e.getItemClicked());
//				e.getPlayer().closeInventory();
//				saveAllSettings();
//				updateSettings();
//				updateMineBay();
//				MineBay.updateRoomSelection();
//				e.getPlayer().sendMessage(Config.getMessage("minebay.info.newicon-applied").replace("%type%", Config.getFriendlyTypeName(e.getItemClicked().getData())));
//				e.setCancelled(true);
//			}
//		};
//
//		GUIElement gPane = new StaticGUIElement(ItemUtils.createItem(VersionedMaterial.BLACK_STAINED_GLASS_PANE, 1, "§0"));
//		for(int i = 45; i < 6*9; i++) {
//			builder.addElement(i, gPane);
//		}
//		
//		builder.addElement(0, new StaticGUIElement(Tools.createItem(Material.GRASS, 1, 0, "§7Block | Grass")).setAction(bAction));
//		builder.addElement(1, new StaticGUIElement(Tools.createItem(Material.DIRT, 1, 0, "§7Block | Dirt")).setAction(bAction));
//		builder.addElement(2, new StaticGUIElement(Tools.createItem(Material.STONE, 1, 0, "§7Block | Stone")).setAction(bAction));
//		builder.addElement(3, new StaticGUIElement(Tools.createItem(Material.BEDROCK, 1, 0, "§7Block | Bedrock")).setAction(bAction));
//		builder.addElement(4, new StaticGUIElement(Tools.createItem(Material.SPONGE, 1, 0, "§7Block | Sponge")).setAction(bAction));
//		builder.addElement(5, new StaticGUIElement(Tools.createItem(Material.DIAMOND_BLOCK, 1, 0, "§7Block | Diamond Block")).setAction(bAction));
//		builder.addElement(6, new StaticGUIElement(Tools.createItem(Material.REDSTONE_BLOCK, 1, 0, "§7Block | Redstone Block")).setAction(bAction));
//		builder.addElement(7, new StaticGUIElement(Tools.createItem(Material.IRON_BLOCK, 1, 0, "§7Block | Iron Block")).setAction(bAction));
//		builder.addElement(8, new StaticGUIElement(Tools.createItem(Material.TNT, 1, 0, "§7Block | TNT")).setAction(bAction));
//		builder.addElement(9, new StaticGUIElement(Tools.createItem(Material.EMERALD_BLOCK, 1, 0, "§7Block | Emerald Block")).setAction(bAction));
//		builder.addElement(10, new StaticGUIElement(Tools.createItem(Material.SAND, 1, 0, "§7Block | Sand")).setAction(bAction));
//		builder.addElement(11, new StaticGUIElement(Tools.createItem(Material.COBBLESTONE, 1, 0, "§7Block | Cobblestone")).setAction(bAction));
//		builder.addElement(12, new StaticGUIElement(Tools.createItem(Material.OBSIDIAN, 1, 0, "§7Block | Obsidian")).setAction(bAction));
//		builder.addElement(13, new StaticGUIElement(Tools.createItem(Material.LAPIS_BLOCK, 1, 0, "§7Block | Lapis Lazuli Block")).setAction(bAction));
//		builder.addElement(14, new StaticGUIElement(Tools.createItem(Material.DIAMOND, 1, 0, "§7Item | Diamond")).setAction(bAction));
//		builder.addElement(15, new StaticGUIElement(Tools.createItem(Material.EMERALD, 1, 0, "§7Item | Emerald")).setAction(bAction));
//		builder.addElement(16, new StaticGUIElement(Tools.createItem(Material.GOLD_INGOT, 1, 0, "§7Item | Gold Ingot")).setAction(bAction));
//		builder.addElement(17, new StaticGUIElement(Tools.createItem(Material.IRON_INGOT, 1, 0, "§7Item | Iron Ingot")).setAction(bAction));
//		builder.addElement(18, new StaticGUIElement(Tools.createItem(Material.REDSTONE, 1, 0, "§7Item | Redstone")).setAction(bAction));
//		builder.addElement(19, new StaticGUIElement(Tools.createItem(Material.COAL, 1, 0, "§7Item | Coal")).setAction(bAction));
//		
//		builder.addElement(45, new StaticGUIElement(Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"))).setAction(new GUIElementAction() {
//			
//			@Override
//			public void onAction(GUIElementActionEvent e) {
//				e.getPlayer().openInventory(getSettingsGUI().getForPlayer(e.getPlayer()));
//				e.setCancelled(true);
//			}
//		}));
//		
//		builder.addElement(53, new StaticGUIElement(Tools.createItem(Tools.letterC(DyeColor.ORANGE), Config.getMessage("minebay.gui.room-settings.custom-icon.name"), Config.getMessageList("minebay.gui.room-settings.custom-icon.lore", "price", ""+Config.config.getInt("minebay.user-rooms.custom-icon-price")))).setAction(new GUIElementAction() {
//			
//			@Override
//			public void onAction(GUIElementActionEvent e) {
//				e.getPlayer().openInventory(getIconChangeGUI().getForPlayer(e.getPlayer()));
//				e.setCancelled(true);
//			}
//		}));
//
//		HashMap<String, Object> props = new HashMap<>();
//		props.put("minebay_type", "select block");
//		props.put("minebay_blockselect_id", roomID);
//		builder.setProperties(props);
//		return builder.build();
//	}
	
//	public GUI getBlockSelectGUI() {
//		return blockSelectGUI;
//	}
	
	public int getWorth(){
		if(isDefaultRoom){
			return 0;
		}else{
			int sl = (slots - Config.config.getInt("minebay.user-rooms.default-slot-number"))*Config.config.getInt("minebay.user-rooms.slot-sell-price");
			int pr = Config.config.getInt("minebay.user-rooms.room-sell-price");
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
