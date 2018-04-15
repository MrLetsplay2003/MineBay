package me.mrletsplay.minebay;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.Files;

import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ClickAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUI;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilder;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIBuilderMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIDragDropListener;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElement;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIElementAction;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.ItemSupplier;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.StaticGUIElement;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;

public class AuctionRoom {

	private String owner;
	private int taxshare;
	private int slots;
	private int roomID;
	private String name, description;
	private ItemStack icon;
	private boolean isDefaultRoom;
	private GUIMultiPage<SellItem> roomGUI;
	private GUI roomSettingsGUI;
	
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
		this.roomGUI = buildRoomGUI();
		this.roomSettingsGUI = buildSettingsMenu();
		if(s) saveAllSettings();
	}
	
	private GUIMultiPage<SellItem> buildRoomGUI(){
		GUIBuilderMultiPage<SellItem> builder = new GUIBuilderMultiPage<>(Config.prefix, 6);
		builder.addPageSlotsInRange(0, 44);
		builder.addPreviousPageItem(52, ItemUtils.createItem(ItemUtils.arrowLeft(DyeColor.WHITE), Config.getMessage("minebay.gui.misc.previous-page")));
		builder.addNextPageItem(53, ItemUtils.createItem(ItemUtils.arrowRight(DyeColor.WHITE), Config.getMessage("minebay.gui.misc.next-page")));
		builder.addElement(45, new StaticGUIElement(ItemUtils.createItem(ItemUtils.arrowLeft(DyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back")))
				.setAction(new GUIElementAction() {
					
					@Override
					public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent e) {
						p.openInventory(GUIs.getAuctionRoomsGUI(null).getForPlayer(p));
						return true;
					}
				}));
		GUIElement gPane = new StaticGUIElement(Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0"));
		builder.addElement(46, gPane);
		builder.addElement(47, gPane);
		builder.addElement(48, gPane);
		builder.addElement(49, gPane);
		builder.addElement(50, gPane);
		builder.addElement(51, gPane);
		builder.setSupplier(new ItemSupplier<SellItem>() {
			
			@Override
			public GUIElement toGUIElement(Player p, SellItem item) {
				return new StaticGUIElement(item.getSellItemStack(p))
						.setAction(new GUIElementAction() {
							
							@Override
							public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent e) {
								AuctionRoom room = item.getRoom();
								if(item.getSeller()!=null && !item.isSeller(p)){
									p.closeInventory();
									MineBay.showPurchaseConfirmDialog(p, item);
								}else{
									HashMap<Integer,ItemStack> excess = p.getInventory().addItem(item.getItem());
									for(Map.Entry<Integer, ItemStack> me : excess.entrySet()){
										p.getWorld().dropItem(p.getLocation(), me.getValue());
									}
									room.removeSellItem(item.getID());
									p.closeInventory();
									p.sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.retract-sale.success")));
								}
								return true;
							}
						});
			}
			
			@Override
			public List<SellItem> getItems() {
				return getSoldItems();
			}
		});
		builder.setDragDropListener(new GUIDragDropListener() {
			
			@Override
			public boolean allowDragDrop(Player p, ItemStack item, Inventory inv, GUI gui, InventoryClickEvent e) {
				return Config.config.getBoolean("minebay.general.allow-drag-and-drop");
			}
		});
		builder.setActionListener(new GUIAction() {
			
			@SuppressWarnings("deprecation")
			@Override
			public boolean action(Player p, ClickAction a, ItemStack it, GUIElement el, Inventory inv, GUI gui, InventoryClickEvent e) {
				if(it != null && !it.getType().equals(Material.AIR) && el == null) {
					Events.sellItem.put(p, new Object[]{roomID, it});
					int maxTime = Config.config.getInt("minebay.general.max-type-time-seconds");
					if(maxTime>0){
						Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask(p), maxTime * 20);
					}
					e.setCursor(new ItemStack(Material.AIR));
					p.sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.sell.type-in-price")));
					p.closeInventory();
				}
				return true;
			}
		});
		return builder.build();
	}
	
	public GUI buildSettingsMenu() {
		GUIBuilder builder = new GUIBuilder(Config.prefix, 6);
		GUIElement gPane = new StaticGUIElement(Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0"));
		for(int i = 0; i < 9*6; i++) {
			builder.addElement(i, gPane);
		}
		builder.addElement(10, new GUIElement() {

			@Override
			public ItemStack getItem(Player p) {
				List<String> l1 = Config.getMessageList("minebay.gui.room-settings.name-desc.name-lore", "name", name, "description", description!=null?description:Config.getMessage("minebay.gui.misc.none"));
				String lbC = Config.getMessage("minebay.gui.room-settings.name-desc.name-lore-linebreak-color");
				List<String> l1f = new ArrayList<>();
				for(String s : l1) {
					for(String s2 : WordUtils.wrap(s, 50).split(System.getProperty("line.separator"))) {
						l1f.add(lbC+s2);
					}
				}
				return Tools.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.name-desc.name"), l1f.toArray(new String[l1f.size()]));
			}
			
		});
		
		builder.addElement(14, new StaticGUIElement(Tools.createItem(Material.STAINED_CLAY, 1, 4, Config.getMessage("minebay.gui.room-settings.name-desc.change-name"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				Events.changeName.put(p, roomID);
				int maxTime = Config.config.getInt("minebay.general.max-type-time-seconds");
				if(maxTime>0){
					Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask(p), maxTime*20);
				}
				p.closeInventory();
				p.sendMessage(Config.simpleReplace(Config.getMessage("minebay.info.newname")));
				return true;
			}
		}));
		
		builder.addElement(15, new StaticGUIElement(Tools.createItem(Material.STAINED_CLAY, 1, 4, Config.getMessage("minebay.gui.room-settings.name-desc.change-description"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				Events.changeDescription.put(p, roomID);
				int maxTime = Config.config.getInt("minebay.general.max-type-time-seconds");
				if(maxTime>0){
					Bukkit.getScheduler().runTaskLater(Main.pl, new CancelTask(p), maxTime*20);
				}
				p.closeInventory();
				p.sendMessage(Config.getMessage("minebay.info.newdescription"));
				return true;
			}
		}));
		
		builder.addElement(19, new GUIElement() {

			@Override
			public ItemStack getItem(Player p) {
				return Tools.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.block.name"), Config.getMessageList("minebay.gui.room-settings.block.lore", "type", icon.getType().toString().toLowerCase().replace("_", " ")));
			}
			
		});
		
		builder.addElement(23, new StaticGUIElement(Tools.createItem(Material.STAINED_CLAY, 1, 4, Config.getMessage("minebay.gui.room-settings.block-change.name"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				p.openInventory(getBlockSelectionInv());
				saveAllSettings();
				updateSettings();
				MineBay.updateRoomSelection();
				return true;
			}
		}));
		
		builder.addElement(28, new GUIElement() {

			@Override
			public ItemStack getItem(Player p) {
				return Tools.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.slots.name"), Config.getMessageList("minebay.gui.room-settings.slots.lore", "slots", ""+slots));
			}
			
		});
		
		builder.addElement(32, new StaticGUIElement(Tools.createItem(Tools.arrowLeft(), Config.getMessage("minebay.gui.room-settings.slots-buy.name"), Config.getMessageList("minebay.gui.room-settings.slots-buy.lore"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				if(!isDefaultRoom()){
					if(button.equals(ClickAction.LEFT_CLICK)){
						if(getSlots() < Config.config.getInt("minebay.user-rooms.max-slots")){
							p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Buy Slot/s", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.slot-price"), "§8Room ID: §7"+getRoomID(), "§8Count: §71")));
						}else{
							p.sendMessage(Config.getMessage("minebay.info.slot-buy.toomanyslots"));
						}
					}else if(button.equals(ClickAction.SHIFT_LEFT_CLICK)){
						if(getSlots()+5 <= Config.config.getInt("minebay.user-rooms.max-slots")){
							p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Buy Slot/s", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.slot-price"), "§8Room ID: §7"+getRoomID(), "§8Count: §75")));
						}else if(getSlots() < Config.config.getInt("minebay.user-rooms.max-slots")){
							p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Buy Slot/s", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.slot-price"), "§8Room ID: §7"+getRoomID(), "§8Count: §7"+(Config.config.getInt("minebay.user-rooms.max-slots")-getSlots()))));
						}else{
							p.sendMessage(Config.getMessage("minebay.info.slot-buy.toomanyslots"));
						}
					}
				}else{
					p.sendMessage(Config.getMessage("minebay.info.slot-buy.is-default"));
				}
				return true;
			}
		}));
		
		builder.addElement(33, new StaticGUIElement(Tools.createItem(Tools.arrowRight(), Config.getMessage("minebay.gui.room-settings.slots-sell.name"), Config.getMessageList("minebay.gui.room-settings.slots-sell.lore"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				if(!isDefaultRoom()){
					if(Config.config.getBoolean("minebay.general.allow-slot-selling")){
						if(button.equals(ClickAction.LEFT_CLICK)){
							if(getSlots() > Config.config.getInt("minebay.user-rooms.default-slot-number")){
								if(getOccupiedSlots() <= getSlots()-1){
									p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Sell Slot/s", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.slot-sell-price"), "§8Room ID: §7"+getRoomID(), "§8Count: §71")));
								}else{
									p.sendMessage(Config.getMessage("minebay.info.slot-sell.all-slots-occupied"));
								}
							}else{
								p.sendMessage(Config.getMessage("minebay.info.slot-sell.notenoughslots"));
							}
						}else if(button.equals(ClickAction.SHIFT_LEFT_CLICK)){
							if(getSlots()-5 >= Config.config.getInt("minebay.user-rooms.default-slot-number")){
								if(getOccupiedSlots() <= getSlots()-5){
									p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Sell Slot/s", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.slot-sell-price"), "§8Room ID: §7"+getRoomID(), "§8Count: §75")));
								}else{
									p.sendMessage(Config.getMessage("minebay.info.slot-sell.all-slots-occupied"));
								}
							}else if(getSlots() > Config.config.getInt("minebay.user-rooms.default-slot-number")){
								int slotsToSell = getSlots()-Config.config.getInt("minebay.user-rooms.default-slot-number");
								if(getOccupiedSlots() <= getSlots()-slotsToSell){
									p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Sell Slot/s", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.slot-sell-price"), "§8Room ID: §7"+getRoomID(), "§8Count: §7"+slotsToSell)));
								}
							}else{
								p.sendMessage(Config.getMessage("minebay.info.slot-sell.notenoughslots"));
							}
						}
					}else{
						p.sendMessage(Config.getMessage("minebay.info.slot-sell.not-allowed"));
					}
				}else{
					p.sendMessage(Config.getMessage("minebay.info.slot-sell.is-default"));
				}
				return true;
			}
		}));
		
		
		builder.addElement(37, new GUIElement() {

			@Override
			public ItemStack getItem(Player p) {
				return Tools.createItem(Material.NAME_TAG, 1, 0, Config.getMessage("minebay.gui.room-settings.tax.name"), Config.getMessageList("minebay.gui.room-settings.tax.lore", "tax", ""+taxshare));
			}
			
		});
		
		builder.addElement(41, new StaticGUIElement(Tools.createItem(Tools.arrowLeft(), Config.getMessage("minebay.gui.room-settings.tax-increase.name"), Config.getMessageList("minebay.gui.room-settings.tax-increase.lore"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				if(button.equals(ClickAction.LEFT_CLICK)){
					if(getTaxshare()<Config.config.getInt("minebay.user-rooms.max-tax-percent")){
						setTaxshare(getTaxshare()+1);
						saveAllSettings();
						updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							p.sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+getTaxshare()));
						}
					}else{
						p.sendMessage(Config.getMessage("minebay.info.tax.toohigh"));
					}
				}else if(button.equals(ClickAction.SHIFT_LEFT_CLICK)){
					if(getTaxshare()+10<=Config.config.getInt("minebay.user-rooms.max-tax-percent")){
						setTaxshare(getTaxshare()+10);
						saveAllSettings();
						updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							p.sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+getTaxshare()));
						}
					}else if(getTaxshare()<Config.config.getInt("minebay.user-rooms.max-tax-percent")){
						setTaxshare(Config.config.getInt("minebay.user-rooms.max-tax-percent"));
						saveAllSettings();
						updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							p.sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+getTaxshare()));
						}
					}else{
						p.sendMessage(Config.getMessage("minebay.info.tax.toohigh"));
					}
				}
				return true;
			}
		}));
		
		builder.addElement(42, new StaticGUIElement(Tools.createItem(Tools.arrowRight(), Config.getMessage("minebay.gui.room-settings.tax-decrease.name"), Config.getMessageList("minebay.gui.room-settings.tax-decrease.lore"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				if(button.equals(ClickAction.LEFT_CLICK)){
					if(getTaxshare()>0){
						setTaxshare(getTaxshare()-1);
						saveAllSettings();
						updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							p.sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+getTaxshare()));
						}
					}else{
						p.sendMessage(Config.getMessage("minebay.info.tax.toolow"));
					}
				}else if(button.equals(ClickAction.SHIFT_LEFT_CLICK)){
					if(getTaxshare()>9){
						setTaxshare(getTaxshare()-10);
						saveAllSettings();
						updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							p.sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+getTaxshare()));
						}
					}else if(getTaxshare()>0){
						setTaxshare(0);
						saveAllSettings();
						updateSettings();
						MineBay.updateRoomSelection();
						if(Config.config.getBoolean("minebay.general.user-rooms-settings.tax-notify")){
							p.sendMessage(Config.getMessage("minebay.info.tax.success").replace("%newtax%", ""+getTaxshare()));
						}
					}else{
						p.sendMessage(Config.getMessage("minebay.info.tax.toolow"));
					}
				}
				return true;
			}
		}));
		
		builder.addElement(45, new StaticGUIElement(Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				Inventory newInv = MineBay.getRoomSelectionMenu(0, "all", p);
				p.openInventory(newInv);
				return true;
			}
		}));
		
		builder.addElement(53, new StaticGUIElement(Tools.createItem(Material.STAINED_CLAY, 1, 14, Config.getMessage("minebay.gui.room-settings.delete"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				if(Config.config.getBoolean("minebay.general.allow-room-selling")){
					if(getRoomID() != 0){
						if(getSoldItems().isEmpty()){
							int sl = (getSlots() - Config.config.getInt("minebay.user-rooms.default-slot-number"))*Config.config.getInt("minebay.user-rooms.slot-sell-price");
							int pr = Config.config.getInt("minebay.user-rooms.room-sell-price");
							p.openInventory(MineBay.getConfirmGUI(Tools.createItem(Material.NAME_TAG, 1, 0, "§8Delete Room", "§8Price: §7"+(sl+pr), "§8Room ID: §7"+getRoomID())));
						}else{
							p.sendMessage(Config.getMessage("minebay.info.sell-room.not-empty"));
						}
					}else{
						p.sendMessage(Config.getMessage("minebay.info.sell-room.is-default"));
						p.closeInventory();
					}
				}else{
					p.sendMessage(Config.getMessage("minebay.info.sell-room.not-allowed"));
				}
				return true;
			}
		}));
		
		
		return builder.build();
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
			BigDecimal price = new BigDecimal(roomConfig.getString("sold-items.item."+id+".price"));
			return new SellItem(item, AuctionRooms.getAuctionRoomByID(roomID), seller, price, id);
		}else{
			return null;
		}
	}
	
	public Inventory getMineBayInv(int page, Player p) {
		return roomGUI.getForPlayer(p, page);
	}
	
//	public Inventory getMineBayInv(int page, Player p){
//		List<SellItem> sItems = getSoldItems();
//		int pages = sItems.size()/9/5;
//		if(pages >= page && page >= 0){
//			Inventory inv = Bukkit.createInventory(null, 6*9, Config.prefix);
//			int start = page*5*9;
//			int end = (sItems.size()<=start+5*9)?sItems.size():start+5*9;
//			for(int i = start; i < end; i++){
//				SellItem it = sItems.get(i);
//				inv.setItem(i-start, it.getSellItemStack(p));
//			}
//			ItemStack gPane = new ItemStack(Material.STAINED_GLASS_PANE);
//			ItemMeta gMeta = gPane.getItemMeta();
//			gMeta.setDisplayName("§0");
//			gPane.setItemMeta(gMeta);
//			for(int i = 5*9+1; i < 6*9-2; i++){
//				inv.setItem(i, gPane);
//			}
//			ItemStack gPane1 = Tools.arrowLeft();
//			ItemMeta gMeta1 = gPane1.getItemMeta();
//			gMeta1.setDisplayName(Config.getMessage("minebay.gui.misc.previous-page"));
//			gPane1.setItemMeta(gMeta1);
//			ItemStack gPane2 = Tools.arrowRight();
//			ItemMeta gMeta2 = gPane2.getItemMeta();
//			gMeta2.setDisplayName(Config.getMessage("minebay.gui.misc.next-page"));
//			gPane2.setItemMeta(gMeta2);
//			ItemStack gPane3 = new ItemStack(Material.STAINED_GLASS_PANE);
//			ItemMeta gMeta3 = gPane3.getItemMeta();
//			gMeta3.setDisplayName("§8Auction Room");
//			List<String> l = new ArrayList<>();
//			l.add("§7Page: "+page);
//			l.add("§7Room ID: "+roomID);
//			gMeta3.setLore(l);
//			gPane3.setItemMeta(gMeta3);
//			ItemStack back = Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"));
//			inv.setItem(45, back);
//			inv.setItem(46, gPane3);
//			inv.setItem(52, gPane1);
//			inv.setItem(53, gPane2);
//			return inv;
//		}else{
//			return null;
//		}
//	}
	
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
					MineBay.changeInv(mbInv, roomSettingsGUI.getForPlayer(pl));
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
		Inventory inv = Bukkit.createInventory(null, 6*9, Config.prefix);
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
		
		inv.setItem(6*9-1, Tools.createItem(Tools.letterC(DyeColor.ORANGE), "§6Custom block/item", "§8Price: §7"+Config.config.getInt("minebay.user-rooms.custom-icon-price")));
		
		return inv;
	}
	
	public Inventory getIconChangeMenu(){
		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Config.prefix+" §8Custom icon");
		
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
