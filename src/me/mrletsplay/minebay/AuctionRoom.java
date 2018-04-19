package me.mrletsplay.minebay;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.Files;

import me.mrletsplay.mrcore.bukkitimpl.GUIUtils;
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
import me.mrletsplay.mrcore.misc.OtherTools;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import net.milkbowl.vault.economy.EconomyResponse;

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
	private GUI blockSelectGUI;
	private GUI customIconGUI;
	
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
		this.blockSelectGUI = buildBlockSelectionGUI();
		this.customIconGUI = buildCustomIconGUI();
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
		HashMap<String, Object> props = new HashMap<>();
		props.put("minebay_type", "auction room");
		props.put("minebay_auctionroom_id", roomID);
		builder.setProperties(props);
		return builder.build();
	}
	
	private GUI buildSettingsMenu() {
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
				p.openInventory(roomGUI.getForPlayer(p));
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
		
		AuctionRoom room = this;
		
		builder.addElement(32, new StaticGUIElement(Tools.createItem(Tools.arrowLeft(), Config.getMessage("minebay.gui.room-settings.slots-buy.name"), Config.getMessageList("minebay.gui.room-settings.slots-buy.lore"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				if(!isDefaultRoom()){
					if(button.equals(ClickAction.LEFT_CLICK)){
						if(getSlots() < Config.config.getInt("minebay.user-rooms.max-slots")){
							p.openInventory(GUIs.buySlotsGUI(room, 1).getForPlayer(p));
						}else{
							p.sendMessage(Config.getMessage("minebay.info.slot-buy.toomanyslots"));
						}
					}else if(button.equals(ClickAction.SHIFT_LEFT_CLICK)){
						if(getSlots()+5 <= Config.config.getInt("minebay.user-rooms.max-slots")){
							p.openInventory(GUIs.buySlotsGUI(room, 5).getForPlayer(p));
						}else if(getSlots() < Config.config.getInt("minebay.user-rooms.max-slots")){
							p.openInventory(GUIs.buySlotsGUI(room, Config.config.getInt("minebay.user-rooms.max-slots")-getSlots()).getForPlayer(p));
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
									p.openInventory(GUIs.sellSlotsGUI(room, 1).getForPlayer(p));
								}else{
									p.sendMessage(Config.getMessage("minebay.info.slot-sell.all-slots-occupied"));
								}
							}else{
								p.sendMessage(Config.getMessage("minebay.info.slot-sell.notenoughslots"));
							}
						}else if(button.equals(ClickAction.SHIFT_LEFT_CLICK)){
							if(getSlots()-5 >= Config.config.getInt("minebay.user-rooms.default-slot-number")){
								if(getOccupiedSlots() <= getSlots()-5){
									p.openInventory(GUIs.sellSlotsGUI(room, 5).getForPlayer(p));
								}else{
									p.sendMessage(Config.getMessage("minebay.info.slot-sell.all-slots-occupied"));
								}
							}else if(getSlots() > Config.config.getInt("minebay.user-rooms.default-slot-number")){
								int slotsToSell = getSlots()-Config.config.getInt("minebay.user-rooms.default-slot-number");
								if(getOccupiedSlots() <= getSlots()-slotsToSell){
									p.openInventory(GUIs.sellSlotsGUI(room, slotsToSell).getForPlayer(p));
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
				Inventory newInv = GUIs.getAuctionRoomsGUI("all").getForPlayer(p);
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
							p.openInventory(GUIs.sellRoomGUI(room).getForPlayer(p));
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
		

		HashMap<String, Object> props = new HashMap<>();
		props.put("minebay_type", "settings");
		props.put("minebay_settings_id", roomID);
		builder.setProperties(props);
		return builder.build();
	}
	
	private GUI buildCustomIconGUI() {
		GUIBuilder builder = new GUIBuilder(Config.prefix, 1);
		GUIElement gPane = new StaticGUIElement(Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0"));
		for(int i = 0; i < 9; i++) {
			builder.addElement(i, gPane);
		}
		
		builder.addElement(0, new StaticGUIElement(Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction a, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
				p.openInventory(getBlockSelectGUI().getForPlayer(p));
				return true;
			}
		}));
		
		builder.addElement(4, new StaticGUIElement(Tools.createItem(Tools.createBanner(null, DyeColor.WHITE), Config.getMessage("minebay.gui.room-settings.custom-icon.item-drop.name"), Config.getMessageList("minebay.gui.room-settings.custom-icon.item-drop.lore"))).setAction(new GUIElementAction() {
			
			@SuppressWarnings("deprecation")
			@Override
			public boolean action(Player p, ClickAction a, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				if(clickedWith != null && !clickedWith.getType().equals(Material.AIR)) {
					int price = isDefaultRoom()?0:Config.config.getInt("minebay.user-rooms.custom-icon-price");
					EconomyResponse re = Main.econ.withdrawPlayer(p, price);
					if(re.transactionSuccess()){
						setIcon(event.getCursor());
						saveAllSettings();
						updateSettings();
						MineBay.updateRoomSelection();
						if(!Config.config.getBoolean("minebay.general.user-rooms-settings.change-icon-remove-item")){
							Tools.addItem(p, event.getCursor());
						}
						event.setCursor(new ItemStack(Material.AIR));
						p.openInventory(getSettingsGUI().getForPlayer(p));
						p.sendMessage(Config.getMessage("minebay.info.buy-icon.success").replace("%type%", getIcon().getType().name().toLowerCase().replace("_", " ")).replace("%price%", ""+price));
					}else{
						p.sendMessage(Config.getMessage("minebay.info.buy-icon.error").replace("%error%", re.errorMessage));
					}
				}
				return true;
			}
		}));
		
		builder.setDragDropListener(new GUIDragDropListener() {
			
			@Override
			public boolean allowDragDrop(Player p, ItemStack it, Inventory inv, GUI gui, InventoryClickEvent event) {
				return true;
			}
		});
		
		return builder.build();
	}
	
	public GUI getIconChangeGUI(){
		return customIconGUI;
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
	
	public GUI getSettingsGUI() {
		return roomSettingsGUI;
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
				Inventory oI = MineBay.getOpenInv(pl);
				if(oI == null) continue;
				GUI gui = GUIUtils.getGUI(oI);
				if(gui == null) continue;
				HashMap<String, Object> props = gui.getHolder().getProperties();
				String t = (String) props.get("minebay_type");
				if(t == null) continue;
				if(t.equals("auction room "+roomID)){
					Inventory mbInv = pl.getOpenInventory().getTopInventory();
					int page = getMineBayPage(mbInv);
					if(page!=-1){
						pl.openInventory(getMineBayInv(page, pl));
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
				Inventory oI = MineBay.getOpenInv(pl);
				if(oI == null) continue;
				GUI gui = GUIUtils.getGUI(oI);
				if(gui == null) continue;
				HashMap<String, Object> props = gui.getHolder().getProperties();
				String t = (String) props.get("minebay_type");
				if(t == null) continue;
				if(t.equals("settings "+roomID)){
					pl.openInventory(roomSettingsGUI.getForPlayer(pl));
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
		im.setDisplayName(Config.getMessage("minebay.gui.rooms.room-item.name", "room-name", name, "room-id", ""+roomID));
		List<String> lore = new ArrayList<>();
		lore = Config.getMessageList("minebay.gui.rooms.room-item.lore",
				"owner", owner!=null?getOwnerName():Config.getMessage("minebay.gui.misc.none"),
				"slots-limit", (slots==-1?Config.getMessage("minebay.gui.rooms.room-item.slots-unlimited"):""+slots),
				"slots-occupied", ""+getOccupiedSlots(),
				"tax", ""+taxshare,
				"room-id", ""+roomID,
				"can-edit", canEdit(p)?Config.getMessage("minebay.gui.rooms.room-item.can-edit"):"");
		
		List<String> fLore = new ArrayList<>();
		if(description!=null) {
			for(String s2 : lore) {
				if(!s2.contains("%description%")) {
					fLore.add(s2);
					continue;
				}
				String fS = s2.replace("%description%", description);
				System.out.println(fS);
				OtherTools.advSplit(fS, 5).stream()
						.map(s -> Config.getMessage("minebay.gui.rooms.room-item.description-linebreak-color")+s)
						.forEach(s -> {
							System.out.println(s);
						});
			}
		}
		im.setLore(fLore);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
		newItem.setItemMeta(im);
		return newItem;
	}
	
	private GUI buildBlockSelectionGUI() {
		GUIBuilder builder = new GUIBuilder(Config.prefix, 6);
		GUIElementAction bAction = new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				setIcon(event.getCurrentItem());
				p.closeInventory();
				saveAllSettings();
				updateSettings();
				updateMineBay();
				MineBay.updateRoomSelection();
				p.sendMessage(Config.getMessage("minebay.info.newicon-applied").replace("%type%", event.getCurrentItem().getType().name().toLowerCase().replace("_", " ")));
				return true;
			}
		};

		GUIElement gPane = new StaticGUIElement(Tools.createItem(Material.STAINED_GLASS_PANE, 1, 0, "§0"));
		for(int i = 0; i < 6*9; i++) {
			builder.addElement(i, gPane);
		}
		
		builder.addElement(0, new StaticGUIElement(Tools.createItem(Material.GRASS, 1, 0, "§7Block | Grass")).setAction(bAction));
		builder.addElement(1, new StaticGUIElement(Tools.createItem(Material.DIRT, 1, 0, "§7Block | Dirt")).setAction(bAction));
		builder.addElement(2, new StaticGUIElement(Tools.createItem(Material.STONE, 1, 0, "§7Block | Stone")).setAction(bAction));
		builder.addElement(3, new StaticGUIElement(Tools.createItem(Material.BEDROCK, 1, 0, "§7Block | Bedrock")).setAction(bAction));
		builder.addElement(4, new StaticGUIElement(Tools.createItem(Material.SPONGE, 1, 0, "§7Block | Sponge")).setAction(bAction));
		builder.addElement(5, new StaticGUIElement(Tools.createItem(Material.DIAMOND_BLOCK, 1, 0, "§7Block | Diamond Block")).setAction(bAction));
		builder.addElement(6, new StaticGUIElement(Tools.createItem(Material.REDSTONE_BLOCK, 1, 0, "§7Block | Redstone Block")).setAction(bAction));
		builder.addElement(7, new StaticGUIElement(Tools.createItem(Material.IRON_BLOCK, 1, 0, "§7Block | Iron Block")).setAction(bAction));
		builder.addElement(8, new StaticGUIElement(Tools.createItem(Material.TNT, 1, 0, "§7Block | TNT")).setAction(bAction));
		builder.addElement(9, new StaticGUIElement(Tools.createItem(Material.EMERALD_BLOCK, 1, 0, "§7Block | Emerald Block")).setAction(bAction));
		builder.addElement(10, new StaticGUIElement(Tools.createItem(Material.SAND, 1, 0, "§7Block | Sand")).setAction(bAction));
		builder.addElement(11, new StaticGUIElement(Tools.createItem(Material.COBBLESTONE, 1, 0, "§7Block | Cobblestone")).setAction(bAction));
		builder.addElement(12, new StaticGUIElement(Tools.createItem(Material.OBSIDIAN, 1, 0, "§7Block | Obsidian")).setAction(bAction));
		builder.addElement(13, new StaticGUIElement(Tools.createItem(Material.LAPIS_BLOCK, 1, 0, "§7Block | Lapis Lazuli Block")).setAction(bAction));
		builder.addElement(14, new StaticGUIElement(Tools.createItem(Material.DIAMOND, 1, 0, "§7Item | Diamond")).setAction(bAction));
		builder.addElement(15, new StaticGUIElement(Tools.createItem(Material.EMERALD, 1, 0, "§7Item | Emerald")).setAction(bAction));
		builder.addElement(16, new StaticGUIElement(Tools.createItem(Material.GOLD_INGOT, 1, 0, "§7Item | Gold Ingot")).setAction(bAction));
		builder.addElement(17, new StaticGUIElement(Tools.createItem(Material.IRON_INGOT, 1, 0, "§7Item | Iron Ingot")).setAction(bAction));
		builder.addElement(18, new StaticGUIElement(Tools.createItem(Material.REDSTONE, 1, 0, "§7Item | Redstone")).setAction(bAction));
		builder.addElement(19, new StaticGUIElement(Tools.createItem(Material.COAL, 1, 0, "§7Item | Coal")).setAction(bAction));
		
		builder.addElement(45, new StaticGUIElement(Tools.createItem(Tools.arrowLeft(DyeColor.ORANGE), Config.getMessage("minebay.gui.misc.back"))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				p.openInventory(getSettingsGUI().getForPlayer(p));
				return true;
			}
		}));
		
		builder.addElement(53, new StaticGUIElement(Tools.createItem(Tools.letterC(DyeColor.ORANGE), Config.getMessage("minebay.gui.room-settings.custom-icon.name"), Config.getMessageList("minebay.gui.room-settings.custom-icon.lore", "price", ""+Config.config.getInt("minebay.user-rooms.custom-icon-price")))).setAction(new GUIElementAction() {
			
			@Override
			public boolean action(Player p, ClickAction button, ItemStack clickedWith, Inventory inv, GUI gui, InventoryClickEvent event) {
				p.openInventory(getIconChangeGUI().getForPlayer(p));
				return true;
			}
		}));

		HashMap<String, Object> props = new HashMap<>();
		props.put("minebay_type", "select block");
		props.put("minebay_blockselect_id", roomID);
		builder.setProperties(props);
		return builder.build();
	}
	
	public GUI getBlockSelectGUI() {
		return blockSelectGUI;
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
